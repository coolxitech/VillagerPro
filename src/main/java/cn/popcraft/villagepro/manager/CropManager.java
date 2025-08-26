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
        // 在实际实现中，这里应该从数据库加载作物存储数据
        // 简化处理，暂时为空
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
        // 在实际实现中，这里应该将作物存储数据保存到数据库
        plugin.getLogger().info("已保存 " + cropStorages.size() + " 个作物存储数据");
    }
}
