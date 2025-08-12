package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.util.VillagerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simplelite.SimpleDatabase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillageManager {
    private final VillagePro plugin;
    private final SimpleDatabase database;
    private final Map<UUID, Village> villageCache = new HashMap<>();

    public VillageManager(VillagePro plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        
        // 注册数据模型
        database.registerTable(Village.class);
        
        // 加载所有村庄数据
        loadAll();
    }

    /**
     * 加载所有村庄数据
     */
    public void loadAll() {
        villageCache.clear();
        database.findAll(Village.class).forEach(village -> 
            villageCache.put(village.getOwnerUuid(), village)
        );
        plugin.getLogger().info("已加载 " + villageCache.size() + " 个村庄数据");
    }

    /**
     * 保存所有村庄数据
     */
    public void saveAll() {
        villageCache.values().forEach(database::save);
        plugin.getLogger().info("已保存 " + villageCache.size() + " 个村庄数据");
    }

    /**
     * 获取玩家的村庄数据
     */
    @Nullable
    public Village getVillage(UUID playerUuid) {
        return villageCache.get(playerUuid);
    }

    /**
     * 获取玩家的村庄数据，如果不存在则创建
     */
    @NotNull
    public Village getOrCreateVillage(Player player) {
        UUID playerUuid = player.getUniqueId();
        Village village = villageCache.get(playerUuid);
        
        if (village == null) {
            village = new Village();
            village.setOwnerUuid(playerUuid);
            
            // 初始化升级等级
            Map<UpgradeType, Integer> upgradeLevels = new HashMap<>();
            for (UpgradeType type : UpgradeType.values()) {
                upgradeLevels.put(type, 0);
            }
            village.setUpgradeLevels(upgradeLevels);
            
            villageCache.put(playerUuid, village);
            database.save(village);
        }
        
        return village;
    }

    /**
     * 保存村庄数据
     */
    public void saveVillage(Village village) {
        database.save(village);
    }
    
    /**
     * 查找最近的未招募村民
     * @param player 玩家
     * @param radius 搜索半径
     * @return 最近的未招募村民，如果没有则返回null
     */
    @Nullable
    public Villager findNearestUnrecruitedVillager(Player player, double radius) {
        Collection<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        Villager nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                
                // 检查是否已被招募
                if (!VillagerUtils.isRecruited(villager)) {
                    double distance = player.getLocation().distance(villager.getLocation());
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = villager;
                    }
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * 招募村民
     * @param player 玩家
     * @param villager 村民
     * @return 是否成功招募
     */
    public boolean recruitVillager(Player player, Villager villager) {
        // 检查村民是否已被招募
        if (VillagerUtils.isRecruited(villager)) {
            return false;
        }
        
        // 获取玩家的村庄数据
        Village village = getOrCreateVillage(player);
        
        // 检查是否达到最大村民数量
        int maxVillagers = plugin.getConfigManager().getMaxVillagers();
        if (village.getVillagerIds().size() >= maxVillagers) {
            return false;
        }
        
        // 检查玩家是否有足够的资源
        if (!hasEnoughResources(player)) {
            return false;
        }
        
        // 扣除资源
        consumeRecruitResources(player);
        
        // 更新村庄数据
        UUID villagerUuid = villager.getUniqueId();
        village.getVillagerIds().add(villagerUuid);
        saveVillage(village);
        
        // 设置村民的所有者
        VillagerUtils.setOwner(villager, player.getUniqueId());
        
        // 设置村民的名称
        villager.setCustomName("§a" + player.getName() + "的村民");
        villager.setCustomNameVisible(true);
        
        // 创建村民实体包装
        VillagerEntity villagerEntity = new VillagerEntity(villager, player.getUniqueId());
        plugin.getVillagerEntities().put(villagerUuid, villagerEntity);
        
        return true;
    }
    
    /**
     * 检查玩家是否有足够的资源进行招募
     * @param player 玩家
     * @return 是否有足够的资源
     */
    private boolean hasEnoughResources(Player player) {
        // 检查金钱（这里假设使用经济插件，实际实现可能不同）
        double costMoney = plugin.getConfigManager().getRecruitCostMoney();
        // 这里应该检查玩家的金钱，但由于没有经济插件，暂时跳过
        
        // 检查物品
        Map<String, Integer> costItems = plugin.getConfigManager().getRecruitCostItems();
        for (Map.Entry<String, Integer> entry : costItems.entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey());
                int amount = entry.getValue();
                
                if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的物品类型: " + entry.getKey());
            }
        }
        
        return true;
    }
    
    /**
     * 消耗招募所需的资源
     * @param player 玩家
     */
    private void consumeRecruitResources(Player player) {
        // 扣除金钱（这里假设使用经济插件，实际实现可能不同）
        double costMoney = plugin.getConfigManager().getRecruitCostMoney();
        // 这里应该扣除玩家的金钱，但由于没有经济插件，暂时跳过
        
        // 扣除物品
        Map<String, Integer> costItems = plugin.getConfigManager().getRecruitCostItems();
        for (Map.Entry<String, Integer> entry : costItems.entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey());
                int amount = entry.getValue();
                
                player.getInventory().removeItem(new ItemStack(material, amount));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的物品类型: " + entry.getKey());
            }
        }
    }
    
    /**
     * 获取玩家的村民升级等级
     * @param playerUuid 玩家UUID
     * @param type 升级类型
     * @return 升级等级
     */
    public int getUpgradeLevel(UUID playerUuid, UpgradeType type) {
        Village village = getVillage(playerUuid);
        if (village == null) {
            return 0;
        }
        
        Integer level = village.getUpgradeLevels().get(type);
        return level != null ? level : 0;
    }
    
    /**
     * 升级玩家的村民
     * @param player 玩家
     * @param type 升级类型
     * @return 是否成功升级
     */
    public boolean upgradeVillage(Player player, UpgradeType type) {
        Village village = getOrCreateVillage(player);
        int currentLevel = getUpgradeLevel(player.getUniqueId(), type);
        
        // 检查是否已达到最高等级
        if (currentLevel >= 5) {
            return false;
        }
        
        // 获取下一级升级配置
        int nextLevel = currentLevel + 1;
        cn.popcraft.villagepro.model.Upgrade upgrade = plugin.getConfigManager().getUpgrade(type, nextLevel);
        
        if (upgrade == null) {
            return false;
        }
        
        // 检查玩家是否有足够的资源
        if (!hasEnoughUpgradeResources(player, upgrade)) {
            return false;
        }
        
        // 扣除资源
        consumeUpgradeResources(player, upgrade);
        
        // 更新升级等级
        village.getUpgradeLevels().put(type, nextLevel);
        saveVillage(village);
        
        return true;
    }
    
    /**
     * 检查玩家是否有足够的资源进行升级
     * @param player 玩家
     * @param upgrade 升级配置
     * @return 是否有足够的资源
     */
    private boolean hasEnoughUpgradeResources(Player player, cn.popcraft.villagepro.model.Upgrade upgrade) {
        // 检查金钱（这里假设使用经济插件，实际实现可能不同）
        double costMoney = upgrade.getCostMoney();
        // 这里应该检查玩家的金钱，但由于没有经济插件，暂时跳过
        
        // 检查钻石
        int costDiamonds = upgrade.getCostDiamonds();
        if (costDiamonds > 0 && !player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), costDiamonds)) {
            return false;
        }
        
        // 检查其他物品
        Map<String, Integer> costItems = upgrade.getCostItems();
        for (Map.Entry<String, Integer> entry : costItems.entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey());
                int amount = entry.getValue();
                
                if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的物品类型: " + entry.getKey());
            }
        }
        
        return true;
    }
    
    /**
     * 消耗升级所需的资源
     * @param player 玩家
     * @param upgrade 升级配置
     */
    private void consumeUpgradeResources(Player player, cn.popcraft.villagepro.model.Upgrade upgrade) {
        // 扣除金钱（这里假设使用经济插件，实际实现可能不同）
        double costMoney = upgrade.getCostMoney();
        // 这里应该扣除玩家的金钱，但由于没有经济插件，暂时跳过
        
        // 扣除钻石
        int costDiamonds = upgrade.getCostDiamonds();
        if (costDiamonds > 0) {
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, costDiamonds));
        }
        
        // 扣除其他物品
        Map<String, Integer> costItems = upgrade.getCostItems();
        for (Map.Entry<String, Integer> entry : costItems.entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey());
                int amount = entry.getValue();
                
                player.getInventory().removeItem(new ItemStack(material, amount));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的物品类型: " + entry.getKey());
            }
        }
    }
}