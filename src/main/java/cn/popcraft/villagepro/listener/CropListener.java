package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.util.VillagerUtils;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.stream.Collectors;

import java.util.*;

/**
 * 作物监听器
 */
public class CropListener implements Listener {
    private final VillagePro plugin;
    private final Random random = new Random();
    
    // 可收集的作物方块
    private static final Set<Material> CROP_BLOCKS = new HashSet<>(Arrays.asList(
        Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
        Material.MELON, Material.PUMPKIN, Material.SUGAR_CANE, Material.CACTUS,
        Material.BAMBOO, Material.SWEET_BERRY_BUSH, Material.COCOA
    ));
    
    // 作物映射到物品
    private static final Map<Material, String> CROP_TO_ITEM = new HashMap<>();
    
    static {
        CROP_TO_ITEM.put(Material.WHEAT, "WHEAT");
        CROP_TO_ITEM.put(Material.CARROTS, "CARROT");
        CROP_TO_ITEM.put(Material.POTATOES, "POTATO");
        CROP_TO_ITEM.put(Material.BEETROOTS, "BEETROOT");
        CROP_TO_ITEM.put(Material.MELON, "MELON_SLICE");
        CROP_TO_ITEM.put(Material.PUMPKIN, "PUMPKIN");
        CROP_TO_ITEM.put(Material.SUGAR_CANE, "SUGAR_CANE");
        CROP_TO_ITEM.put(Material.CACTUS, "CACTUS");
        CROP_TO_ITEM.put(Material.BAMBOO, "BAMBOO");
        CROP_TO_ITEM.put(Material.SWEET_BERRY_BUSH, "SWEET_BERRIES");
        CROP_TO_ITEM.put(Material.COCOA, "COCOA_BEANS");
    }
    
    public CropListener(VillagePro plugin) {
        this.plugin = plugin;
        startCropCollectionTask();
    }
    
    /**
     * 处理作物生长事件
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCropGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        
        // 检查是否是作物方块
        if (!CROP_BLOCKS.contains(block.getType())) {
            return;
        }
        
        // 查找附近的村民
        Villager nearestVillager = findNearestRecruitedVillager(block.getLocation(), 16);
        if (nearestVillager == null) {
            return;
        }
        
        // 获取村民的所有者
        UUID ownerUuid = VillagerUtils.getOwner(nearestVillager);
        if (ownerUuid == null) {
            return;
        }
        
        // 获取作物生长速度升级等级
        int cropGrowthLevel = plugin.getVillageManager().getUpgradeLevel(ownerUuid, UpgradeType.CROP_GROWTH);
        if (cropGrowthLevel <= 0) {
            return;
        }
        
        // 根据升级等级增加生长速度
        if (block.getBlockData() instanceof Ageable ageable) {
            int currentAge = ageable.getAge();
            int maxAge = ageable.getMaximumAge();
            
            // 有一定概率额外增加生长阶段
            double growthBonus = plugin.getCropManager().getCropGrowthBonus(cropGrowthLevel);
            if (random.nextDouble() < growthBonus && currentAge < maxAge) {
                int newAge = Math.min(currentAge + 1, maxAge);
                ageable.setAge(newAge);
                block.setBlockData(ageable);
            }
        }
    }
    
    /**
     * 启动作物收集任务
     */
    private void startCropCollectionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                collectCrops();
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // 每分钟执行一次
    }
    
    /**
     * 收集作物
     */
    private void collectCrops() {
        // 遍历所有村民实体
        for (VillagerEntity villagerEntity : plugin.getVillagerEntities().values()) {
            Villager villager = villagerEntity.getVillager();
            if (villager == null || !villager.isValid()) {
                continue;
            }
            
            // 获取村民所有者
            UUID ownerUuid = villagerEntity.getOwnerUuid();
            if (ownerUuid == null) {
                continue;
            }
            
            // 获取村庄数据
            Village village = plugin.getVillageManager().getVillage(ownerUuid);
            if (village == null) {
                continue;
            }
            
            // 检查村民是否在农田附近
            List<Block> nearbyCrops = findNearbyCrops(villager.getLocation(), 8);
            if (nearbyCrops.isEmpty()) {
                continue;
            }
            
            // 获取作物生长速度升级等级
            int cropGrowthLevel = village.getUpgradeLevels().getOrDefault(UpgradeType.CROP_GROWTH, 0);
            if (cropGrowthLevel <= 0) {
                continue;
            }
            
            // 收集成熟的作物
            for (Block crop : nearbyCrops) {
                if (crop.getBlockData() instanceof Ageable ageable) {
                    if (ageable.getAge() == ageable.getMaximumAge()) {
                        // 收获作物
                        harvestCrop(crop, ownerUuid, cropGrowthLevel);
                        
                        // 重置作物生长阶段
                        ageable.setAge(0);
                        crop.setBlockData(ageable);
                    }
                } else if (crop.getType() == Material.MELON || crop.getType() == Material.PUMPKIN) {
                    // 收获西瓜和南瓜
                    harvestCrop(crop, ownerUuid, cropGrowthLevel);
                    
                    // 移除方块
                    crop.setType(Material.AIR);
                } else if (crop.getType() == Material.SUGAR_CANE || crop.getType() == Material.CACTUS || crop.getType() == Material.BAMBOO) {
                    // 检查上方是否有同类型方块
                    Block above = crop.getRelative(0, 1, 0);
                    if (above.getType() == crop.getType()) {
                        harvestCrop(above, ownerUuid, cropGrowthLevel);
                        above.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    /**
     * 收获作物
     * @param crop 作物方块
     * @param ownerUuid 所有者UUID
     * @param cropGrowthLevel 作物生长速度升级等级
     */
    private void harvestCrop(Block crop, UUID ownerUuid, int cropGrowthLevel) {
        String itemType = CROP_TO_ITEM.get(crop.getType());
        if (itemType == null) {
            return;
        }
        
        // 计算收获量
        int baseAmount = 1;
        if (crop.getType() == Material.WHEAT) baseAmount = 1;
        else if (crop.getType() == Material.CARROTS) baseAmount = 2;
        else if (crop.getType() == Material.POTATOES) baseAmount = 2;
        else if (crop.getType() == Material.BEETROOTS) baseAmount = 1;
        else if (crop.getType() == Material.MELON) baseAmount = 3;
        else if (crop.getType() == Material.PUMPKIN) baseAmount = 1;
        else if (crop.getType() == Material.SUGAR_CANE) baseAmount = 1;
        else if (crop.getType() == Material.CACTUS) baseAmount = 1;
        else if (crop.getType() == Material.BAMBOO) baseAmount = 1;
        else if (crop.getType() == Material.SWEET_BERRY_BUSH) baseAmount = 2;
        else if (crop.getType() == Material.COCOA) baseAmount = 2;
        
        // 根据升级等级增加收获量
        double harvestBonus = plugin.getCropManager().getCropHarvestBonus(cropGrowthLevel);
        int bonusAmount = (int) Math.floor(baseAmount * harvestBonus);
        int totalAmount = baseAmount + bonusAmount;
        
        // 添加到玩家的作物存储
        plugin.getCropManager().addCrop(ownerUuid, itemType, totalAmount);
        
        // 通知玩家
        org.bukkit.entity.Player player = plugin.getServer().getPlayer(ownerUuid);
        if (player != null && player.isOnline()) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.valueOf(totalAmount));
            replacements.put("crop", itemType);
            player.sendMessage(plugin.getMessageManager().getMessage("crop.collected", replacements));
        }
    }
    
    /**
     * 查找附近的作物
     * @param location 位置
     * @param radius 半径
     * @return 作物列表
     */
    private List<Block> findNearbyCrops(Location location, int radius) {
        List<Block> crops = new ArrayList<>();
        int radiusSquared = radius * radius;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radiusSquared) {
                        Block block = location.getBlock().getRelative(x, y, z);
                        if (CROP_BLOCKS.contains(block.getType())) {
                            crops.add(block);
                        }
                    }
                }
            }
        }
        
        return crops;
    }
    
    /**
     * 查找附近的已招募村民
     * @param location 位置
     * @param radius 半径
     * @return 最近的已招募村民
     */
    private Villager findNearestRecruitedVillager(Location location, int radius) {
        Collection<Villager> nearbyVillagers = location.getWorld().getEntitiesByClass(Villager.class).stream()
                .filter(villager -> villager.getLocation().distance(location) <= radius)
                .collect(Collectors.toList());
        Villager nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Villager villager : nearbyVillagers) {
            if (VillagerUtils.isRecruited(villager)) {
                double distance = villager.getLocation().distanceSquared(location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = villager;
                }
            }
        }
        
        return nearest;
    }
}