package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.storage.VillageStorage;
import cn.popcraft.villagepro.util.VillagerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.EnumMap;
import java.util.logging.Level;

public class VillageManager {
    private final VillagePro plugin;
    private final Map<UUID, Village> villageCache = new ConcurrentHashMap<>();
    private final VillageStorage villageStorage;
    private final int maxVillagers; // 缓存配置值，避免每次查询

    public VillageManager(VillagePro plugin) {
        this.plugin = plugin;
        this.villageStorage = new VillageStorage(plugin, plugin.getGson());
        this.maxVillagers = plugin.getConfigManager().getMaxVillagers(); // 读取一次
    }

    /**
     * 关闭村庄管理器，保存所有数据并关闭数据库连接
     */
    public void close() {
        saveAll();
        if (villageStorage != null) {
            villageStorage.close();
        }
    }

    // ResourceCheckResult 内部类定义
    public static class ResourceCheckResult {
        public boolean success = true;
        public String message = "";
        public double money = 0;
        public int diamonds = 0;
        public int points = 0;
        public Map<Material, Integer> items = new HashMap<>();
    }
    
    /**
     * 加载所有村庄数据
     */
    public void loadAll() {
        villageCache.clear();
        try {
            villageStorage.findAll().forEach(village -> {
                if (village != null && village.getOwnerUuid() != null) {
                    villageCache.put(village.getOwnerUuid(), village);
                }
            });
            plugin.getLogger().info("已加载 " + villageCache.size() + " 个村庄数据");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load villages", e);
        }
    }

    /**
     * 保存所有村庄数据
     */
    public void saveAll() {
        // 检查插件是否启用
        if (!plugin.isEnabled()) {
            plugin.getLogger().warning("插件已禁用，跳过保存村庄数据");
            return;
        }
        
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                for (Village village : villageCache.values()) {
                    villageStorage.save(village);
                }
                plugin.getLogger().info("已保存 " + villageCache.size() + " 个村庄数据");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save villages", e);
            }
        });
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    villageStorage.save(v);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save village for player: " + v.getOwnerUuid(), e);
                }
            });
            return v;
        });
    }

    /**
     * 保存村庄数据
     */
    public void saveVillage(Village village) {
        if (village != null) {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskAsynchronously(plugin, () -> {
                try {
                    villageStorage.save(village);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save village", e);
                }
            });
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
        // 3️⃣ 最大数量检查（延迟获取配置值）
        if (village.getVillagerIds().size() >= getMaxVillagers()) {
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
        // 8️⃣ 技能初始化
        plugin.getVillagerSkillManager().initializeVillagerSkills(villager);
        // 9️⃣ 实体包装注册
        VillagerEntity ve = new VillagerEntity(villager, player.getUniqueId());
        plugin.getVillagerEntities().put(vid, ve);
        // 🔟 成功消息
        player.sendMessage(plugin.getMessageManager().getMessage("recruit.success"));
        return true;
    }
    
    /**
     * 检查玩家是否有足够的资源进行招募
     * @param player 玩家
     * @return 是否有足够的资源
     */
    private ResourceCheckResult checkRecruitResources(Player player) {
        ResourceCheckResult r = new ResourceCheckResult();
        // 金钱
        double costMoney = plugin.getConfigManager().getRecruitCostMoney();
        if (costMoney > 0 && plugin.getEconomyManager().isAvailable() && !plugin.getEconomyManager().has(player, costMoney)) {
            r.success = false;
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", plugin.getEconomyManager().format(costMoney));
            r.message = plugin.getMessageManager().getMessage("recruit.not-enough-money", replacements);
            return r;
        }
        r.money = costMoney;
        // 钻石
        int costDiamonds = plugin.getConfigManager().getRecruitCostDiamonds();
        if (costDiamonds > 0 && !player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), costDiamonds)) {
            r.success = false;
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.valueOf(costDiamonds));
            r.message = plugin.getMessageManager().getMessage("recruit.not-enough-diamonds", replacements);
            return r;
        }
        r.diamonds = costDiamonds;
        
        // 点券
        int costPoints = plugin.getConfigManager().getRecruitCostPoints();
        if (costPoints > 0 && plugin.getEconomyManager().isPlayerPointsAvailable() && !plugin.getEconomyManager().hasPoints(player, costPoints)) {
            r.success = false;
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.valueOf(costPoints));
            r.message = plugin.getMessageManager().getMessage("recruit.not-enough-points", replacements);
            return r;
        }
        r.points = costPoints;
        // 物品
        Map<String, Integer> cfgItems = plugin.getConfigManager().getRecruitCostItems();
        for (Map.Entry<String, Integer> e : cfgItems.entrySet()) {
            int need = e.getValue();
            if (!player.getInventory().containsAtLeast(new ItemStack(Material.valueOf(e.getKey().startsWith("itemsadder:") ? "STONE" : e.getKey())), need)) {
                r.success = false;
                Map<String, String> replacements = new HashMap<>();
                replacements.put("item", cn.popcraft.villagepro.util.ItemNameUtil.getItemDisplayName(e.getKey()));
                replacements.put("amount", String.valueOf(need));
                r.message = plugin.getMessageManager().getMessage("recruit.not-enough-items", replacements);
                return r;
            }
            try {
                Material mat = Material.valueOf(e.getKey());
                r.items.put(mat, need);
            } catch (IllegalArgumentException ex) {
                // 对于ItemsAdder物品或其他自定义物品，我们只检查数量而不实际验证
                r.items.put(Material.STONE, need); // 使用STONE作为占位符
            }
        }
        r.success = true;
        return r;
    }

    private boolean consumeRecruitResources(Player player, ResourceCheckResult rc) {
        // 金钱
        if (rc.money > 0 && plugin.getEconomyManager().isAvailable()) {
            if (!plugin.getEconomyManager().withdraw(player, rc.money)) {
                plugin.getLogger().warning("[Recruit] 扣除金钱失败: " + player.getName() + " 金额: " + rc.money);
                return false;
            }
        }
        // 钻石
        if (rc.diamonds > 0) {
            if (!player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), rc.diamonds)) {
                plugin.getLogger().warning("[Recruit] 钻石不足: " + player.getName() + " 需要: " + rc.diamonds);
                return false;
            }
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, rc.diamonds));
        }
        // 点券
        if (rc.points > 0 && plugin.getEconomyManager().isPlayerPointsAvailable()) {
            if (!plugin.getEconomyManager().hasPoints(player, rc.points)) {
                plugin.getLogger().warning("[Recruit] 点券不足: " + player.getName() + " 需要: " + rc.points);
                return false;
            }
            if (!plugin.getEconomyManager().withdrawPoints(player, rc.points)) {
                plugin.getLogger().warning("[Recruit] 扣除点券失败: " + player.getName() + " 数量: " + rc.points);
                return false;
            }
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

    /**
     * 获取玩家的村民升级等级
     * @param playerUuid 玩家UUID
     * @param type 升级类型
     * @return 升级等级
     */
    public int getUpgradeLevel(UUID playerUuid, UpgradeType type) {
        Village village = villageCache.get(playerUuid);
        if (village != null) {
            return village.getUpgradeLevel(type);
        }
        return 0;
    }

    /**
     * 升级村庄
     * @param player 玩家
     * @param type 升级类型
     * @return 是否成功升级
     */
    public boolean upgradeVillage(Player player, UpgradeType type) {

        return true;
    }

    /**
     * 获取升级类型的名称
     * @param type 升级类型
     * @return 类型名称
     */
    private String getUpgradeTypeName(UpgradeType type) {
        if (type == null) {
            return "unknown";
        }
        return plugin.getMessageManager().getMessage("upgrade-types." + type.name());
    }
    

    /**
     * 检查玩家是否有足够的资源进行升级
     * @param player 玩家
     * @param upgrade 升级配置
     * @return 资源检查结果
     */
    public ResourceCheckResult checkUpgradeResources(Player player, cn.popcraft.villagepro.model.Upgrade upgrade) {
        ResourceCheckResult r = new ResourceCheckResult();
        
        // 金钱
        double costMoney = upgrade.getCostMoney();
        if (costMoney > 0 && plugin.getEconomyManager().isAvailable() && !plugin.getEconomyManager().has(player, costMoney)) {
            r.success = false;
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", plugin.getEconomyManager().format(costMoney));
            r.message = plugin.getMessageManager().getMessage("upgrade.not-enough-money", replacements);
            return r;
        }
        r.money = costMoney;
        
        // 钻石
        double costDiamonds = upgrade.getCostDiamonds();
        if (costDiamonds > 0 && !player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), (int) costDiamonds)) {
            r.success = false;
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.valueOf((int) costDiamonds));
            r.message = plugin.getMessageManager().getMessage("upgrade.not-enough-diamonds", replacements);
            return r;
        }
        r.diamonds = (int) costDiamonds;
        
        // 点券
        int costPoints = upgrade.getCostPoints();
        if (costPoints > 0 && plugin.getEconomyManager().isPlayerPointsAvailable() && !plugin.getEconomyManager().hasPoints(player, costPoints)) {
            r.success = false;
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.valueOf(costPoints));
            r.message = plugin.getMessageManager().getMessage("upgrade.not-enough-points", replacements);
            return r;
        }
        r.points = costPoints;
        
        // 物品
        Map<String, Integer> costItems = upgrade.getCostItems();
        for (Map.Entry<String, Integer> entry : costItems.entrySet()) {
            String itemKey = entry.getKey();
            int amount = entry.getValue();

            // 检查物品是否足够（对于ItemsAdder物品仅做基本检查）
            boolean hasItem = false;
            if (itemKey.startsWith("itemsadder:")) {
                // 对于ItemsAdder物品，我们只做基本检查
                // 在实际应用中，你可能需要集成ItemsAdder API来精确检查
                hasItem = true; // 假设玩家有这个物品
            } else {
                try {
                    Material material = Material.valueOf(itemKey);
                    if (player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
                        r.items.put(material, amount);
                        hasItem = true;
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning(plugin.getMessageManager().getMessage("config.invalid-upgrade-type", Map.of("type", itemKey)));
                }
            }

            if (!hasItem) {
                r.success = false;
                Map<String, String> replacements = new HashMap<>();
                replacements.put("item", cn.popcraft.villagepro.util.ItemNameUtil.getItemDisplayName(itemKey));
                replacements.put("amount", String.valueOf(amount));
                r.message = plugin.getMessageManager().getMessage("upgrade.not-enough-items", replacements);
                return r;
            }
        }
        
        r.success = true;
        return r;
    }

    /**
     * 消耗升级所需的资源
     * @param player 玩家
     * @param rc 资源检查结果
     */
    public void consumeUpgradeResources(Player player, ResourceCheckResult rc) {
        // 扣除金钱（如果经济系统可用）
        if (rc.money > 0 && plugin.getEconomyManager().isAvailable()) {
            plugin.getEconomyManager().withdraw(player, rc.money);
        }

        // 扣除钻石
        if (rc.diamonds > 0) {
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, rc.diamonds));
        }
        
        // 扣除点券
        if (rc.points > 0 && plugin.getEconomyManager().isPlayerPointsAvailable()) {
            plugin.getEconomyManager().withdrawPoints(player, rc.points);
            plugin.getLogger().info("从玩家 " + player.getName() + " 扣除了 " + rc.points + " 点券");
        }

        // 扣除物品
        for (Map.Entry<Material, Integer> e : rc.items.entrySet()) {
            Material mat = e.getKey();
            int need = e.getValue();
            int left = need;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() == mat) {
                    int rm = Math.min(left, stack.getAmount());
                    stack.setAmount(stack.getAmount() - rm);
                    left -= rm;
                    if (left == 0) break;
                }
            }
        }
        // 注意：ItemsAdder物品的扣除需要通过ItemsAdder API完成，这里只处理原版物品
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

    /**
     * 获取最大村民数量配置值
     * @return 最大村民数量
     */
    public int getMaxVillagers() {
        // 实时获取配置值，确保获取最新配置
        if (plugin.getConfigManager() != null) {
            return plugin.getConfigManager().getMaxVillagers();
        }
        return maxVillagers; // 返回默认值
    }

    /**
     * 检查玩家是否达到最大村民数量限制
     * @param player 玩家
     * @return 是否达到限制
     */
    public boolean isVillagerLimitReached(Player player) {
        Village village = getVillage(player.getUniqueId());
        if (village == null) {
            return false;
        }
        return village.getVillagerIds().size() >= getMaxVillagers();
    }
}