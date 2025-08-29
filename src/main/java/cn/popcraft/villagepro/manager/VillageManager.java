package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillageUpgrade;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.storage.SQLiteStorage;
import cn.popcraft.villagepro.util.VillagerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.EnumMap;
import java.util.Collections;
import java.util.logging.Level;

public class VillageManager {
    private final VillagePro plugin;
    private final Map<UUID, Village> villageCache = new ConcurrentHashMap<>();
    private final SQLiteStorage villageStorage;
    private final int maxVillagers; // 缓存配置值，避免每次查询

    public VillageManager(VillagePro plugin) {
        this.plugin = plugin;
        this.villageStorage = new SQLiteStorage(plugin, plugin.getGson());
        this.maxVillagers = plugin.getConfigManager().getMaxVillagers(); // 读取一次
    }

    // ResourceCheckResult 内部类定义
    private static class ResourceCheckResult {
        boolean success = true;
        String message = "";
        double money = 0;
        Map<Material, Integer> items = new HashMap<>();
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
     * 获取或创建玩家的村庄数据
     */
    public Village getOrCreateVillage(Player player) {
        UUID uuid = player.getUniqueId();
        return villageCache.computeIfAbsent(uuid, id -> {
            Village v = new Village();
            v.setOwnerUuid(id);
            v.setUpgradeLevels(new EnumMap<>(UpgradeType.class));
            for (UpgradeType type : UpgradeType.values()) {
                v.getUpgradeLevels().put(type, 0);
            }
            v.setVillagerIds(new ArrayList<>());
            // 异步保存，防止阻塞主线程
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> villageStorage.save(v));
            return v;
        });
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
     * 保存所有村庄数据
     */
    public void saveAllVillages() {
        for (Village village : villageCache.values()) {
            saveVillage(village);
        }
    }
    
    /**
     * 查找最近的未招募村民
     * @param player 玩家
     * @param radius 搜索半径
     * @return 最近的未招募村民，如果没有则返回null
     */
    public Villager findNearestUnrecruitedVillager(Player player, double radius) {
        Collection<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        Villager nearest = null;
        double minDistSq = Double.MAX_VALUE;
        for (Entity e : nearby) {
            if (e instanceof Villager villager && villager.isValid() && !VillagerUtils.isRecruited(villager)) {
                double distSq = player.getLocation().distanceSquared(villager.getLocation());
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = villager;
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
        // 1️⃣ 基础校验
        if (villager == null || !villager.isValid()) {
            player.sendMessage(plugin.getMessageManager().getMessage("villager.not-found"));
            return false;
        }
        if (VillagerUtils.isRecruited(villager)) {
            player.sendMessage(plugin.getMessageManager().getMessage("villager.already-recruited"));
            return false;
        }
        // 2️⃣ 获取或创建村庄（线程安全）
        Village village = getOrCreateVillage(player);
        // 3️⃣ 最大数量检查
        if (village.getVillagerIds().size() >= maxVillagers) {
            player.sendMessage(plugin.getMessageManager().getMessage("villager.max-villagers-reached"));
            return false;
        }
        // 4️⃣ 资源检查（返回详细信息）
        ResourceCheckResult rc = checkRecruitResources(player);
        if (!rc.success) {
            player.sendMessage(rc.message);
            return false;
        }
        // 5️⃣ 扣除资源（已确保足够）
        if (!consumeRecruitResources(player, rc)) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
            return false;
        }
        // 6️⃣ 更新数据（同步块确保集合安全）
        UUID vid = villager.getUniqueId();
        synchronized (village) {
            village.getVillagerIds().add(vid);
            saveVillage(village); // 异步持久化
        }
        // 7️⃣ 实体属性设置
        VillagerUtils.setOwner(villager, player.getUniqueId());
        villager.setCustomName(plugin.getMessageManager().getMessage(
                "villager.name", Map.of("player", player.getName())));
        villager.setCustomNameVisible(true);
        plugin.getVillagerSkillManager().initializeVillagerSkills(villager);
        // 8️⃣ 内存包装
        VillagerEntity entity = new VillagerEntity(villager, player.getUniqueId());
        plugin.getVillagerEntities().put(vid, entity);
        // 9️⃣ 成功提示（在这里统一发送，RecruitCommand 不再重复）
        player.sendMessage(plugin.getMessageManager().getMessage("recruit.success"));
        return true;
    }
    
    /**
     * 检查玩家是否有足够的资源进行招募
     * @param player 玩家
     * @return 是否有足够的资源
     */
    private boolean hasEnoughResources(Player player) {
        // 检查金钱（如果经济系统可用）
        double costMoney = plugin.getConfigManager().getRecruitCostMoney();
        if (costMoney > 0 && plugin.getEconomy() != null) {
            if (!plugin.getEconomy().has(player, costMoney)) {
                return false;
            }
        }
        
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
        // 扣除金钱（如果经济系统可用）
        double costMoney = plugin.getConfigManager().getRecruitCostMoney();
        if (costMoney > 0 && plugin.getEconomy() != null) {
            plugin.getEconomy().withdrawPlayer(player, costMoney);
        }
        
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
     * 升级玩家的村民（升级到下一级）
     * @param player 玩家
     * @param type 升级类型
     * @return 是否成功升级
     */
    public boolean upgradeVillage(Player player, cn.popcraft.villagepro.model.UpgradeType type) {
        int currentLevel = getUpgradeLevel(player.getUniqueId(), type);
        return upgradeVillage(player, type, currentLevel + 1);
    }
    
    /**
     * 升级玩家的村民
     * @param player 玩家
     * @param type 升级类型
     * @param level 目标等级
     * @return 是否成功升级
     */
    public boolean upgradeVillage(Player player, cn.popcraft.villagepro.model.UpgradeType type, int level) {
        Village village = getOrCreateVillage(player);
        int currentLevel = getUpgradeLevel(player.getUniqueId(), type);
        
        // 检查目标等级是否有效
        if (level <= currentLevel) {
            player.sendMessage(plugin.getMessageManager().getMessage("upgrade.failed"));
            return false;
        }
        
        // 检查是否超过最高等级
        if (level > 5) {
            player.sendMessage(plugin.getMessageManager().getMessage("upgrade.max-level-reached"));
            return false;
        }
        
        // 获取升级配置
        cn.popcraft.villagepro.model.Upgrade upgrade = plugin.getConfigManager().getUpgrade(type, level);
        
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
        
        // 更新村庄数据
        village.getUpgradeLevels().put(type, level);
        saveVillage(village);
        
        // 发送成功消息
        Map<String, String> replacements = new HashMap<>();
        replacements.put("type", type.name());
        replacements.put("level", String.valueOf(level));
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
        // 检查金钱（如果经济系统可用）
        double costMoney = upgrade.getCostMoney();
        if (costMoney > 0 && plugin.getEconomy() != null) {
            if (!plugin.getEconomy().has(player, costMoney)) {
                return false;
            }
        }
        
        // 检查钻石
        int costDiamonds = upgrade.getCostDiamonds();
        if (costDiamonds > 0) {
            if (!player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), costDiamonds)) {
                return false;
            }
        }
        
        // 检查物品
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
        // 扣除金钱（如果经济系统可用）
        double costMoney = upgrade.getCostMoney();
        if (costMoney > 0 && plugin.getEconomy() != null) {
            plugin.getEconomy().withdrawPlayer(player, costMoney);
        }
        
        // 扣除钻石
        int costDiamonds = upgrade.getCostDiamonds();
        if (costDiamonds > 0) {
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, costDiamonds));
        }
        
        // 扣除物品
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
    
    /**
     * 移除村民
     * @param player 玩家
     * @param villagerId 村民ID
     * @return 是否成功移除
     */
    public boolean removeVillager(Player player, UUID villagerId) {
        Village village = getVillage(player.getUniqueId());
        if (village == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("village.not-found"));
            return false;
        }
        if (!village.getVillagerIds().remove(villagerId)) {
            player.sendMessage(plugin.getMessageManager().getMessage("villager.not-found"));
            return false;
        }
        // 保存
        saveVillage(village);
        // 移除内存缓存
        plugin.getVillagerEntities().remove(villagerId);
        // 实体清理
        Entity entity = Bukkit.getEntity(villagerId);
        if (entity instanceof Villager villager) {
            VillagerUtils.setOwner(villager, null);
            villager.setCustomName(null);
            villager.remove(); // 完全删除实体
        }
        player.sendMessage(plugin.getMessageManager().getMessage("villager.removed"));
        return true;
    }
    // -------------- 资源检查/扣除封装 -----------------
    private ResourceCheckResult checkRecruitResources(Player player) {
        ResourceCheckResult r = new ResourceCheckResult();
        // 金钱
        double costMoney = plugin.getConfigManager().getRecruitCostMoney();
        if (costMoney > 0 && plugin.getEconomy() != null && !plugin.getEconomy().has(player, costMoney)) {
            r.success = false;
            r.message = plugin.getMessageManager().getMessage("recruit.failed.not-enough-money");
            return r;
        }
        r.money = costMoney;
        // 物品
        Map<String, Integer> cfgItems = plugin.getConfigManager().getRecruitCostItems();
        for (Map.Entry<String, Integer> e : cfgItems.entrySet()) {
            Material mat;
            try { mat = Material.valueOf(e.getKey()); }
            catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("[Recruit] Invalid material: " + e.getKey());
                continue; // 跳过错误条目
            }
            int need = e.getValue();
            if (!player.getInventory().containsAtLeast(new ItemStack(mat), need)) {
                r.success = false;
                r.message = plugin.getMessageManager().getMessage(
                        "recruit.failed.not-enough-items",
                        Map.of("item", mat.name(), "amount", String.valueOf(need)));
                return r;
            }
            r.items.put(mat, need);
        }
        r.success = true;
        return r;
    }
    private boolean consumeRecruitResources(Player player, ResourceCheckResult rc) {
        // 金钱
        if (rc.money > 0 && plugin.getEconomy() != null) {
            plugin.getEconomy().withdrawPlayer(player, rc.money);
        }
        // 物品（手动遍历确保完整扣除）
        for (Map.Entry<Material, Integer> e : rc.items.entrySet()) {
            Material mat = e.getKey();
            int need = e.getValue();
            int left = need;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack == null) continue;
                if (stack.getType() == mat) {
                    int rm = Math.min(left, stack.getAmount());
                    stack.setAmount(stack.getAmount() - rm);
                    left -= rm;
                    if (left == 0) break;
                }
            }
            if (left > 0) {
                // 理论不应到达这里
                plugin.getLogger().warning("[Recruit] 扣除物品失败: " + mat + " 缺少 " + left);
                return false;
            }
        }
        return true;
    }
}