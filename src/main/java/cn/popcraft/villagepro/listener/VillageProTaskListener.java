package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.model.Task;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class VillageProTaskListener implements Listener {
    private final VillagePro plugin;
    private final TaskManager taskManager;
    private final Random random = new Random();

    public VillageProTaskListener(VillagePro plugin, TaskManager taskManager) {
        this.plugin = plugin;
        this.taskManager = taskManager;
    }
    
    /**
     * 处理方块破坏事件 - 用于挖掘类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            boolean shouldUpdate = false;
            
            switch (task.getType()) {
                case MINE_IRON:
                    if (material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE) {
                        shouldUpdate = true;
                    }
                    break;
                case MINE_DIAMOND:
                    if (material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE) {
                        shouldUpdate = true;
                    }
                    break;
                case MINE_STONE:
                    if (isStoneType(material)) {
                        shouldUpdate = true;
                    }
                    break;
            }
            
            if (shouldUpdate) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理实体死亡事件 - 用于击杀类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (!(killer instanceof Player)) return;
        
        Player player = (Player) killer;
        EntityType entityType = event.getEntity().getType();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            boolean shouldUpdate = false;
            
            switch (task.getType()) {
                case KILL_ZOMBIE:
                    if (entityType == EntityType.ZOMBIE) {
                        shouldUpdate = true;
                    }
                    break;
                case KILL_SKELETON:
                    if (entityType == EntityType.SKELETON) {
                        shouldUpdate = true;
                    }
                    break;
                case KILL_CREEPER:
                    if (entityType == EntityType.CREEPER) {
                        shouldUpdate = true;
                    }
                    break;
                case KILL_SPIDER:
                    if (entityType == EntityType.SPIDER || entityType == EntityType.CAVE_SPIDER) {
                        shouldUpdate = true;
                    }
                    break;
                case KILL_ENDERMAN:
                    if (entityType == EntityType.ENDERMAN) {
                        shouldUpdate = true;
                    }
                    break;
            }
            
            if (shouldUpdate) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家收获作物事件 - 用于收获类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHarvestBlock(org.bukkit.event.player.PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        Material material = event.getHarvestedBlock().getType();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.HARVEST_CROP && isCropType(material)) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家钓鱼事件 - 用于钓鱼类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item)) return;
        
        Player player = event.getPlayer();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.FISH_ITEM) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理附魔事件 - 用于附魔类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.ENCHANT_ITEM) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理动物繁殖事件 - 用于繁殖类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player) {
            Player player = (Player) event.getBreeder();
            
            List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
            
            for (Task task : activeTasks) {
                if (task.getType() == Task.TaskType.BREED_ANIMAL) {
                    taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                    checkAndCompleteTask(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家剪羊毛事件 - 用于剪羊毛类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.SHEAR_SHEEP) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                checkAndCompleteTask(player, task);
            }
        }
    }
    
    /**
     * 处理实体驯服事件 - 用于驯服类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (event.getOwner() instanceof Player) {
            Player player = (Player) event.getOwner();
            
            List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
            
            for (Task task : activeTasks) {
                if (task.getType() == Task.TaskType.TAME_ANIMAL) {
                    taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                    checkAndCompleteTask(player, task);
                }
            }
        }
    }
    
    /**
     * 处理酿造事件 - 用于酿造类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrew(org.bukkit.event.inventory.BrewEvent event) {
        // 检查附近是否有玩家
        Collection<Entity> nearbyEntities = event.getBlock().getWorld()
            .getNearbyEntities(event.getBlock().getLocation(), 3, 3, 3);
        
        Player player = null;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                player = (Player) entity;
                break;
            }
        }
        
        if (player != null) {
            List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
            
            for (Task task : activeTasks) {
                if (task.getType() == Task.TaskType.BREW_POTION) {
                    taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                    checkAndCompleteTask(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家拾取物品事件 - 用于收集类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        Material itemType = event.getItem().getItemStack().getType();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            boolean shouldUpdate = false;
            
            switch (task.getType()) {
                case COLLECT_WHEAT:
                    if (itemType == Material.WHEAT) {
                        shouldUpdate = true;
                    }
                    break;
                case COLLECT_WOOD:
                    if (isWoodType(itemType)) {
                        shouldUpdate = true;
                    }
                    break;
                case COLLECT_FLOWER:
                    if (isFlowerType(itemType)) {
                        shouldUpdate = true;
                    }
                    break;
            }
            
            if (shouldUpdate) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), event.getItem().getItemStack().getAmount());
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家制作物品事件 - 用于制造类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Material craftedItemType = event.getRecipe().getResult().getType();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.CRAFT_ITEM) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), event.getRecipe().getResult().getAmount());
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家与村民交易事件 - 用于交易类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTradeWithVillager(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager)) return;
        
        Player player = event.getPlayer();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.TRADE_WITH_VILLAGER) {
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家探索生物群系事件 - 用于探索类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // 为了性能考虑，我们只在特定条件下检查
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.EXPLORE_BIOME) {
                // 这里简化处理，实际应用中可能需要更复杂的生物群系检测逻辑
                taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                
                if (task.getProgress() >= task.getTargetAmount()) {
                    completeTask(player, task);
                } else {
                    showTaskProgress(player, task);
                }
            }
        }
    }
    
    
    private void checkAndCompleteTask(Player player, Task task) {
        if (task.getProgress() >= task.getTargetAmount()) {
            // 任务完成
            completeTask(player, task);
        } else {
            // 显示进度
            showTaskProgress(player, task);
        }
    }
    
    private void showTaskProgress(Player player, Task task) {
        // 检查是否启用了Quests系统，如果启用则不显示内置进度
        if (plugin.getQuestsIntegrationManager().isQuestsAvailable()) {
            // Quests系统会处理进度显示，我们不需要重复显示
            return;
        }
        
        // 显示任务进度信息
        double progressPercentage = (double) task.getProgress() / task.getTargetAmount() * 100;
        String message = String.format(
            "任务进度: %s [%d/%d] %.1f%%", 
            task.getType().name(),
            task.getProgress(), 
            task.getTargetAmount(), 
            progressPercentage
        );
        player.sendMessage(message);
    }
    
    private void completeTask(Player player, Task task) {
        // 检查是否启用了Quests系统
        if (plugin.getQuestsIntegrationManager().isQuestsAvailable()) {
            // 由Quests系统处理任务完成和奖励发放
            // 我们只需要在内部标记任务完成
            taskManager.completeTask(player.getUniqueId(), task.getId());
            return;
        }
        
        // 使用内置系统处理任务完成
        // 标记任务为完成
        taskManager.completeTask(player.getUniqueId(), task.getId());
        
        // 通知玩家任务完成
        player.sendMessage("任务完成!");
        
        // 给予任务奖励
        giveTaskReward(player, task);
    }
    
    /**
     * 检查是否是石头类型
     */
    private boolean isStoneType(Material material) {
        return material.name().endsWith("STONE") || 
               material == Material.COBBLESTONE || 
               material == Material.COBBLED_DEEPSLATE ||
               material == Material.ANDESITE ||
               material == Material.DIORITE ||
               material == Material.GRANITE;
    }
    
    /**
     * 检查是否是作物类型
     */
    private boolean isCropType(Material material) {
        return material == Material.WHEAT || 
               material == Material.CARROTS || 
               material == Material.POTATOES ||
               material == Material.BEETROOTS ||
               material == Material.MELON ||
               material == Material.PUMPKIN;
    }
    
    /**
     * 检查是否是木头类型
     */
    private boolean isWoodType(Material material) {
        return material.name().endsWith("LOG") || 
               material.name().endsWith("WOOD") ||
               material == Material.OAK_LOG ||
               material == Material.SPRUCE_LOG ||
               material == Material.BIRCH_LOG ||
               material == Material.JUNGLE_LOG ||
               material == Material.ACACIA_LOG ||
               material == Material.DARK_OAK_LOG;
    }
    
    /**
     * 检查是否是花卉类型
     */
    private boolean isFlowerType(Material material) {
        return material.name().endsWith("FLOWER") ||
               material == Material.DANDELION ||
               material == Material.POPPY ||
               material == Material.BLUE_ORCHID ||
               material == Material.ALLIUM ||
               material == Material.AZURE_BLUET ||
               material == Material.RED_TULIP ||
               material == Material.ORANGE_TULIP ||
               material == Material.WHITE_TULIP ||
               material == Material.PINK_TULIP ||
               material == Material.OXEYE_DAISY ||
               material == Material.CORNFLOWER ||
               material == Material.LILY_OF_THE_VALLEY ||
               material == Material.WITHER_ROSE ||
               material == Material.SUNFLOWER ||
               material == Material.LILAC ||
               material == Material.ROSE_BUSH ||
               material == Material.PEONY;
    }
    
    /**
     * 给予任务奖励
     */
    private void giveTaskReward(Player player, Task task) {
        // 如果启用了Quests系统，由Quests处理奖励
        if (plugin.getQuestsIntegrationManager().isQuestsAvailable()) {
            return;
        }
        
        // 使用内置奖励系统
        // 发放经验奖励
        if (task.getRewardExp() > 0) {
            player.giveExp(task.getRewardExp());
        }
        
        // 发放金钱奖励
        if (task.getRewardMoney() > 0 && plugin.getEconomyManager().isAvailable()) {
            plugin.getEconomyManager().deposit(player, task.getRewardMoney());
        }
        
        // 发放物品奖励
        if (task.getItemRewards() != null && !task.getItemRewards().isEmpty()) {
            for (ItemStack item : task.getItemRewards()) {
                player.getInventory().addItem(item);
            }
        }
        
        // 触发任务完成消息
        Map<String, String> replacements = new HashMap<>();
        replacements.put("task", task.getDescription());
        replacements.put("reward", task.getRewardDescription());
        
        player.sendMessage("任务完成!");
    }
    
    /**
     * 检查是否有新任务可用
     */
    private void checkForNewTasks(Player player) {
        // 可以在这里添加逻辑来检查是否应该自动分配新任务
    }
}