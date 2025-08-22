package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.FollowMode;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.model.VillagerProfession;
import cn.popcraft.villagepro.util.VillagerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class VillagerListener implements Listener {
    private final VillagePro plugin;
    private final Random random = new Random();

    public VillagerListener(VillagePro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // 只处理主手交互
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        // 只处理村民实体
        if (!(event.getRightClicked() instanceof Villager villager)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // 检查是否是玩家自己的村民
        if (VillagerUtils.isOwnedBy(villager, player)) {
            // 如果玩家潜行，切换跟随模式
            if (player.isSneaking()) {
                UUID villagerUuid = villager.getUniqueId();
                VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
                
                if (villagerEntity != null) {
                    FollowMode currentMode = villagerEntity.getFollowMode();
                    FollowMode newMode;
                    
                    // 循环切换模式：NONE -> FOLLOW -> STAY -> NONE
                    switch (currentMode) {
                        case NONE:
                            newMode = FollowMode.FOLLOW;
                            player.sendMessage("§a[VillagePro] 村民将跟随你");
                            break;
                        case FOLLOW:
                            newMode = FollowMode.STAY;
                            player.sendMessage("§a[VillagePro] 村民将停留在此处");
                            break;
                        case STAY:
                            newMode = FollowMode.NONE;
                            player.sendMessage("§a[VillagePro] 村民将自由活动");
                            break;
                        default:
                            newMode = FollowMode.NONE;
                            break;
                    }
                    
                    plugin.getFollowManager().setFollowMode(villager, newMode);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // 只处理村民实体
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        
        // 检查是否是被招募的村民
        UUID villagerUuid = villager.getUniqueId();
        if (plugin.getVillagerEntities().containsKey(villagerUuid)) {
            // 移除村民技能效果
            plugin.getVillagerSkillManager().removeVillagerSkills(villager);
            
            // 从缓存中移除
            VillagerEntity villagerEntity = plugin.getVillagerEntities().remove(villagerUuid);
            
            // 从村庄数据中移除
            UUID ownerUuid = villagerEntity.getOwnerUuid();
            plugin.getVillageManager().getVillage(ownerUuid).getVillagerIds().remove(villagerUuid);
            plugin.getVillageManager().saveVillage(plugin.getVillageManager().getVillage(ownerUuid));
            
            // 通知玩家
            Player owner = plugin.getServer().getPlayer(ownerUuid);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage("§c[VillagePro] 你的一名村民死亡了!");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // 只处理村民实体
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        
        // 检查是否是被招募的村民
        UUID villagerUuid = villager.getUniqueId();
        if (plugin.getVillagerEntities().containsKey(villagerUuid)) {
            VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
            UUID ownerUuid = villagerEntity.getOwnerUuid();
            
            // 获取村民保护等级
            int protectionLevel = plugin.getVillageManager().getUpgradeLevel(ownerUuid, UpgradeType.PROTECTION);
            
            // 根据保护等级减少伤害
            if (protectionLevel > 0) {
                double reduction = 0.1 * protectionLevel; // 每级减少10%伤害
                double newDamage = event.getDamage() * (1 - reduction);
                event.setDamage(newDamage);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim().toLowerCase();
        
        // 检查是否是跟随响应
        if (message.equals("y") || message.equals("n")) {
            if (plugin.getFollowManager().handleFollowResponse(player, message)) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * 处理村民补货事件
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerReplenishTrade(VillagerReplenishTradeEvent event) {
        org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager) event.getEntity();
        
        // 检查是否是被招募的村民
        UUID villagerUuid = villager.getUniqueId();
        if (!plugin.getVillagerEntities().containsKey(villagerUuid)) {
            return;
        }
        
        VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
        UUID ownerUuid = villagerEntity.getOwnerUuid();
        
        // 获取村民补货速度升级等级
        int restockSpeedLevel = plugin.getVillageManager().getUpgradeLevel(ownerUuid, UpgradeType.RESTOCK_SPEED);
        if (restockSpeedLevel <= 0) {
            return;
        }
        
        // 根据升级等级增加补货次数
        org.bukkit.inventory.MerchantRecipe recipe = event.getRecipe();
        int maxUses = recipe.getMaxUses();
        int bonusUses = restockSpeedLevel * 2; // 每级增加2次补货次数
        
        org.bukkit.inventory.MerchantRecipe newRecipe = new org.bukkit.inventory.MerchantRecipe(
                recipe.getResult(),
                recipe.getUses(),
                maxUses + bonusUses,
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier()
        );
        
        // 复制原始配方的成分
        newRecipe.setIngredients(recipe.getIngredients());
        
        // 替换原始配方
        event.setRecipe(newRecipe);
    }
    
    /**
     * 处理村民获取交易事件
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager) event.getEntity();
        
        // 检查是否是被招募的村民
        UUID villagerUuid = villager.getUniqueId();
        if (!plugin.getVillagerEntities().containsKey(villagerUuid)) {
            return;
        }
        
        VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
        UUID ownerUuid = villagerEntity.getOwnerUuid();
        
        // 获取村民交易数量升级等级
        int tradeAmountLevel = plugin.getVillageManager().getUpgradeLevel(ownerUuid, UpgradeType.TRADE_AMOUNT);
        if (tradeAmountLevel <= 0) {
            return;
        }
        
        // 根据升级等级增加交易数量
        org.bukkit.inventory.MerchantRecipe recipe = event.getRecipe();
        int maxUses = recipe.getMaxUses();
        int bonusUses = tradeAmountLevel * 3; // 每级增加3次交易次数
        
        org.bukkit.inventory.MerchantRecipe newRecipe = new org.bukkit.inventory.MerchantRecipe(
                recipe.getResult(),
                recipe.getUses(),
                maxUses + bonusUses,
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier()
        );
        
        // 复制原始配方的成分
        newRecipe.setIngredients(recipe.getIngredients());
        
        // 替换原始配方
        event.setRecipe(newRecipe);
    }
    
    /**
     * 处理村民职业变化事件
     */
    @EventHandler
    public void onVillagerCareerChange(org.bukkit.event.entity.VillagerCareerChangeEvent event) {
        org.bukkit.entity.Villager villager = event.getEntity();
        UUID villagerUuid = villager.getUniqueId();
        
        // 检查是否是被招募的村民
        if (plugin.getVillagerEntities().containsKey(villagerUuid)) {
            VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
            // 更新村民的职业
            villagerEntity.setProfession(cn.popcraft.villagepro.model.VillagerProfession.fromBukkit(event.getProfession()));
            // 重新应用技能
            plugin.getVillagerSkillManager().applyVillagerSkills(villager);
        }
    }
    
    /**
     * 定期检查农民村民附近是否有成熟的作物可以收获
     */
    public void startFarmerHarvestTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 遍历所有被招募的村民
                for (VillagerEntity villagerEntity : plugin.getVillagerEntities().values()) {
                    org.bukkit.entity.Villager villager = villagerEntity.getVillager();
                    
                    // 检查是否是农民职业且处于跟随或停留模式
                    if (villagerEntity.getProfession() == VillagerProfession.FARMER && 
                        (villagerEntity.getFollowMode() == FollowMode.FOLLOW || 
                         villagerEntity.getFollowMode() == FollowMode.STAY)) {
                        
                        // 查找附近的成熟作物
                        List<Block> matureCrops = findNearbyMatureCrops(villager.getLocation(), 5);
                        
                        // 如果有成熟作物，随机收获一些
                        if (!matureCrops.isEmpty()) {
                            int harvestCount = Math.min(matureCrops.size(), 3 + random.nextInt(3)); // 每次收获3-5个作物
                            
                            for (int i = 0; i < harvestCount; i++) {
                                if (!matureCrops.isEmpty()) {
                                    Block crop = matureCrops.remove(random.nextInt(matureCrops.size()));
                                    harvestCrop(crop, villagerEntity.getOwnerUuid());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L * 30, 20L * 60); // 每30秒检查一次，持续执行
    }
    
    /**
     * 查找附近的成熟作物
     */
    private List<Block> findNearbyMatureCrops(Location location, int radius) {
        List<Block> matureCrops = new ArrayList<>();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = location.getBlock().getRelative(x, y, z);
                    if (isMatureCrop(block)) {
                        matureCrops.add(block);
                    }
                }
            }
        }
        
        return matureCrops;
    }
    
    /**
     * 检查是否是成熟的作物
     */
    private boolean isMatureCrop(Block block) {
        Material type = block.getType();
        if (type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES || 
            type == Material.BEETROOTS || type == Material.NETHER_WART) {
            
            if (block.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) block.getBlockData();
                return ageable.getAge() == ageable.getMaximumAge();
            }
        }
        return false;
    }
    
    /**
     * 收获作物
     */
    private void harvestCrop(Block crop, UUID ownerUuid) {
        Material cropType = crop.getType();
        Material dropType = getDropType(cropType);
        
        if (dropType != null) {
            // 获取玩家的作物产量加成
            int cropYieldLevel = plugin.getVillageManager().getUpgradeLevel(ownerUuid, UpgradeType.CROP_GROWTH);
            double yieldMultiplier = 1.0 + (cropYieldLevel * 0.2); // 每级增加20%产量
            
            // 计算产量
            int baseAmount = 1;
            if (cropType == Material.WHEAT) baseAmount = 1;
            else if (cropType == Material.CARROTS) baseAmount = 1;
            else if (cropType == Material.POTATOES) baseAmount = 1;
            else if (cropType == Material.BEETROOTS) baseAmount = 1;
            else if (cropType == Material.NETHER_WART) baseAmount = 1;
            
            int amount = (int) Math.floor(baseAmount * yieldMultiplier);
            
            // 添加到玩家的作物存储
            plugin.getCropManager().addCrop(ownerUuid, dropType.name(), amount);
            
            // 重置作物为种子状态
            if (crop.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) crop.getBlockData();
                ageable.setAge(0);
                crop.setBlockData(ageable);
            }
        }
    }
    
    /**
     * 获取作物的掉落物类型
     */
    private Material getDropType(Material cropType) {
        switch (cropType) {
            case WHEAT:
                return Material.WHEAT;
            case CARROTS:
                return Material.CARROT;
            case POTATOES:
                return Material.POTATO;
            case BEETROOTS:
                return Material.BEETROOT;
            case NETHER_WART:
                return Material.NETHER_WART;
            default:
                return null;
        }
    }
}