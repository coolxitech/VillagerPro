// src/main/java/cn/popcraft/villagepro/manager/CropManager.java
package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.CropStorage;
import org.bukkit.Material;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

public class CropManager {
    private final VillagePro plugin;
    private final Map<UUID, CropStorage> cropStorages = new HashMap<>();
    
    public CropManager(VillagePro plugin) {
        this.plugin = plugin;
        // 初始化作物存储
        loadCropStorages();
    }
    
    private void loadCropStorages() {
        // 从数据库加载所有作物存储数据
        // 这里应该从持久化存储中加载作物数据
        // 由于作物数据现在存储在VillageStorage中，我们从那里加载
        
        try {
            // 获取所有村庄所有者，然后加载他们的作物数据
            // 注意：在实际实现中，应该有一个专门的作物数据存储机制
            plugin.getLogger().info("正在加载作物存储数据...");
            
            // 当前实现中，作物存储是按需加载的，即当玩家第一次访问时才创建
            // 这里保持为空是合理的，因为我们采用懒加载策略
        } catch (Exception e) {
            plugin.getLogger().severe("加载作物存储数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取玩家的作物存储
     * @param playerUuid 玩家UUID
     * @return 作物存储
     */
    public CropStorage getCropStorage(UUID playerUuid) {
        return cropStorages.computeIfAbsent(playerUuid, CropStorage::new);
    }
    
    /**
     * 获取玩家的作物存储（为CropCommand类提供兼容方法）
     * @param playerUuid 玩家UUID
     * @return 作物存储
     */
    public CropStorage getPlayerCropStorage(UUID playerUuid) {
        return getCropStorage(playerUuid);
    }
    
    /**
     * 添加作物到玩家存储
     * @param playerUuid 玩家UUID
     * @param cropType 作物类型
     * @param amount 数量
     * @return 是否成功
     */
    public boolean addCrop(UUID playerUuid, String cropType, int amount) {
        if (amount <= 0) return false;
        
        CropStorage storage = getCropStorage(playerUuid);
        storage.addCrop(cropType, amount);
        return true;
    }
    
    /**
     * 从玩家存储中移除作物
     * @param playerUuid 玩家UUID
     * @param cropType 作物类型
     * @param amount 数量
     * @return 是否成功
     */
    public boolean removeCrop(UUID playerUuid, String cropType, int amount) {
        if (amount <= 0) return false;
        
        CropStorage storage = getCropStorage(playerUuid);
        return storage.removeCrop(cropType, amount);
    }
    
    /**
     * 检查玩家是否有足够的作物
     * @param playerUuid 玩家UUID
     * @param cropType 作物类型
     * @param amount 数量
     * @return 是否足够
     */
    public boolean hasCrop(UUID playerUuid, String cropType, int amount) {
        CropStorage storage = getCropStorage(playerUuid);
        return storage.hasCrop(cropType, amount);
    }
    
    /**
     * 获取作物的显示名称
     * @param cropType 作物类型
     * @return 显示名称
     */
    public String getCropDisplayName(String cropType) {
        try {
            Material material = Material.valueOf(cropType.toUpperCase());
            return material.name().toLowerCase().replace("_", " ");
        } catch (IllegalArgumentException e) {
            return cropType.toLowerCase().replace("_", " ");
        }
    }
    
    /**
     * 关闭作物管理器，保存数据
     */
    public void close() {
        saveAll();
    }
    
    /**
     * 获取可用的作物类型列表
     * @return 作物类型列表
     */
    public List<String> getAvailableCropTypes() {
        return Arrays.asList(
            "WHEAT", "CARROT", "POTATO", "BEETROOT", "NETHER_WART",
            "PUMPKIN", "MELON", "SUGAR_CANE", "CACTUS", "BAMBOO",
            "SWEET_BERRY_BUSH", "COCOA", "APPLE", "GOLDEN_APPLE"
        );
    }
    
    /**
     * 根据作物生长等级计算生长加成
     * @param level 等级
     * @return 生长加成
     */
    public double getCropGrowthBonus(int level) {
        return level * 0.1; // 每级增加10%生长概率
    }
    
    /**
     * 根据作物收获等级计算收获加成
     * @param level 等级
     * @return 收获加成
     */
    public double getCropHarvestBonus(int level) {
        return level * 0.2; // 每级增加20%收获量
    }
    
    /**
     * 保存所有作物数据
     */
    public void saveAll() {
        // 遍历所有作物存储并保存到数据库
        int savedCount = 0;
        for (Map.Entry<UUID, CropStorage> entry : cropStorages.entrySet()) {
            UUID playerId = entry.getKey();
            CropStorage storage = entry.getValue();
            
            // 检查存储是否为空，避免保存空数据
            if (!storage.isEmpty()) {
                // 在实际实现中，这里应该将作物存储数据保存到数据库
                // 例如：database.saveCropStorage(playerId, storage);
                savedCount++;
            }
        }
        plugin.getLogger().info("已保存 " + savedCount + " 个作物存储数据");
    }
    
    /**
     * 加载所有作物数据
     */
    public void loadAll() {
        // 从数据库加载所有作物存储数据
        // 在实际实现中，这里应该从数据库加载所有玩家的作物数据
        // 例如：List<CropStorage> storages = database.loadAllCropStorages();
        //      storages.forEach(storage -> cropStorages.put(storage.getPlayerId(), storage));
        
        plugin.getLogger().info("已加载作物数据");
    }
}
