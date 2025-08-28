package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillageUpgrade;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.storage.SQLiteStorage;
import cn.popcraft.villagepro.util.VillagerUtils;
import cn.popcraft.villagepro.util.VillagerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillageManager {
    private final VillagePro plugin;
    private final SQLiteStorage villageStorage;
    private final Map<UUID, Village> villageCache = new HashMap<>();

    public VillageManager(VillagePro plugin) {
        this.plugin = plugin;
        this.villageStorage = plugin.getDatabase();
        this.villageStorage.registerTable(Village.class);
        
        // 加载所有村庄数据
        loadAll();
        
        // 定时保存数据（每10分钟一次）
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::saveAll, 20 * 60 * 10, 20 * 60 * 10);
    }

    public VillageUpgrade getVillageUpgrade(UUID playerUuid) {
        Village village = villageCache.get(playerUuid);
        if (village != null) {
            return village.getUpgrade();
        }
        return null;
    }

    /**
     * 加载所有村庄数据
     */
    public void loadAll() {
        villageCache.clear();
        villageStorage.findAll(Village.class).forEach(village -> {
            if (village != null && village.getOwnerUuid() != null) {
                villageCache.put(village.getOwnerUuid(), village);
            }
        });
        plugin.getLogger().info("已加载 " + villageCache.size() + " 个村庄数据");
    }

    /**
     * 保存所有村庄数据
     */
    public void saveAll() {
        villageCache.values().forEach(village -> villageStorage.save(village));
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
            villageStorage.save(village);
        }
        
        return village;
    }

    /**
     * 保存村庄数据
     */
    public void saveVillage(Village village) {
        if (village != null) {
            villageStorage.save(village);
        }
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
                
                // 检查村民是否有效且未被招募
                if (villager.isValid() && !VillagerUtils.isRecruited(villager)) {
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
        // 检查村民是否有效
        if (villager == null || !villager.isValid()) {
            player.sendMessage(plugin.getMessageManager().getMessage("villager.not-found"));
            return false;
        }
        
        // 检查村民是否已被招募
        if (VillagerUtils.isRecruited(villager)) {
            player.sendMessage(plugin.getMessageManager().getMessage("villager.already-recruited"));
            return false;
        }
        
        // 获取玩家的村庄数据
        Village village = getOrCreateVillage(player);
        
        // 检查是否达到最大村民数量
        int maxVillagers = plugin.getConfigManager().getMaxVillagers();
        if (village.getVillagerIds().size() >= maxVillagers) {
            player.sendMessage(plugin.getMessageManager().getMessage("villager.max-villagers-reached"));
            return false;
        }
        
        // 检查玩家是否有足够的资源
        if (!hasEnoughResources(player)) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
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
        Map<String, String> replacements = new HashMap<>();
        replacements.put("player", player.getName());
        villager.setCustomName(plugin.getMessageManager().getMessage("villager.name", replacements));
        villager.setCustomNameVisible(true);
        
        // 初始化村民技能
        plugin.getVillagerSkillManager().initializeVillagerSkills(villager);
        
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
                plugin.getLogger().warning(plugin.getMessageManager().getMessage("config.invalid-upgrade-type", Map.of("type", entry.getKey())));
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
                plugin.getLogger().warning(plugin.getMessageManager().getMessage("config.invalid-upgrade-type", Map.of("type", entry.getKey())));
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
            player.sendMessage(plugin.getMessageManager().getMessage("upgrade.max-level-reached"));
            return false;
        }
        
        // 获取下一级升级配置
        int nextLevel = currentLevel + 1;
        cn.popcraft.villagepro.model.Upgrade upgrade = plugin.getConfigManager().getUpgrade(type, nextLevel);
        
        if (upgrade == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("upgrade.failed"));
            return false;
        }
        
        // 检查玩家是否有足够的资源
        if (!hasEnoughUpgradeResources(player, upgrade)) {
            player.sendMessage(plugin.getMessageManager().getMessage("upgrade.failed"));
            return false;
        }
        
        // 扣除资源
        consumeUpgradeResources(player, upgrade);
        
        // 更新升级等级
        village.getUpgradeLevels().put(type, nextLevel);
        saveVillage(village);
        
        // 如果是特定的升级类型，更新相关村民的技能
        if (type == UpgradeType.HEALTH || type == UpgradeType.SPEED || type == UpgradeType.PROTECTION) {
            // 应用技能效果到所有已招募的村民
            for (UUID villagerId : village.getVillagerIds()) {
                VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerId);
                if (villagerEntity != null) {
                    plugin.getVillagerSkillManager().applyVillagerSkills(villagerEntity.getVillager());
                }
            }
        }
        
        // 发送成功消息
        Map<String, String> replacements = new HashMap<>();
        replacements.put("type", plugin.getMessageManager().getMessage("upgrade-types." + type.name()));
        replacements.put("level", String.valueOf(nextLevel));
        player.sendMessage(plugin.getMessageManager().getMessage("upgrade.success", replacements));
        
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
                plugin.getLogger().warning(plugin.getMessageManager().getMessage("config.invalid-upgrade-type", Map.of("type", entry.getKey())));
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
                plugin.getLogger().warning(plugin.getMessageManager().getMessage("config.invalid-upgrade-type", Map.of("type", entry.getKey())));
            }
        }
    }
}