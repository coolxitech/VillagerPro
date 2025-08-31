// src/main/java/cn/popcraft/villagepro/manager/CropManager.java
package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.CropStorage;
import cn.popcraft.villagepro.model.Village;
import org.bukkit.Material;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;

public class CropManager {
    private final VillagePro plugin;
    private final Map<UUID, CropStorage> cropStorages = new HashMap<>();
    
    public CropManager(VillagePro plugin) {
        this.plugin = plugin;
        // 初始化作物存储
        loadCropStorages();
    }
    
    private void loadCropStorages() {
        // 从VillageStorage加载所有村庄数据，然后提取作物存储信息
        try {
            // 获取所有村庄数据
            Collection<Village> villages = plugin.getDatabase().findAll();
            
            // 从每个村庄提取作物存储数据
            for (Village village : villages) {
                UUID playerId = village.getOwnerUuid();
                if (playerId != null) {
                    // 创建作物存储对象
                    Map<String, Integer> crops = village.getCropStorage();
                    if (crops != null && !crops.isEmpty()) {
                        CropStorage storage = new CropStorage(playerId, crops);
                        cropStorages.put(playerId, storage);
                    }
                }
            }
            
            plugin.getLogger().info("已加载 " + cropStorages.size() + " 个作物存储数据");
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
                // 获取玩家的村庄数据
                cn.popcraft.villagepro.model.Village village = plugin.getVillageManager().getVillage(playerId);
                if (village != null) {
                    // 更新村庄中的作物存储数据
                    village.setCropStorage(storage.getCrops());
                    // 保存村庄数据（包含作物存储）
                    try {
                        plugin.getDatabase().save(village);
                        savedCount++;
                    } catch (java.sql.SQLException e) {
                        plugin.getLogger().severe("保存作物数据失败: 玩家=" + playerId + ", 错误=" + e.getMessage());
                    }
                }
            }
        }
        plugin.getLogger().info("已保存 " + savedCount + " 个作物存储数据");
    }
    
    /**
     * 加载所有作物数据
     */
    public void loadAll() {
        // 从VillageStorage加载所有村庄数据，然后提取作物存储信息
        try {
            // 获取所有村庄数据
            Collection<cn.popcraft.villagepro.model.Village> villages = plugin.getDatabase().findAll();
            
            // 从每个村庄提取作物存储数据
            for (cn.popcraft.villagepro.model.Village village : villages) {
                UUID playerId = village.getOwnerUuid();
                if (playerId != null) {
                    // 创建作物存储对象
                    Map<String, Integer> crops = village.getCropStorage();
                    if (crops != null && !crops.isEmpty()) {
                        CropStorage storage = new CropStorage(playerId, crops);
                        cropStorages.put(playerId, storage);
                    }
                }
            }
            
            plugin.getLogger().info("已加载 " + cropStorages.size() + " 个作物存储数据");
        } catch (Exception e) {
            plugin.getLogger().severe("加载作物存储数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
