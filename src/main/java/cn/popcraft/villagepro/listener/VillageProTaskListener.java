package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.model.Task;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.CraftItemEvent;

import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class VillageProTaskListener implements Listener {
    private final VillagePro plugin;
    private final TaskManager taskManager;
    
    public VillageProTaskListener(VillagePro plugin, TaskManager taskManager) {
        this.plugin = plugin;
        this.taskManager = taskManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            switch (task.getType()) {
                case MINE_IRON:
                    if (blockType == Material.IRON_ORE) {
                        taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                        checkAndCompleteTask(player, task);
                    }
                    break;
                case MINE_DIAMOND:
                    if (blockType == Material.DIAMOND_ORE) {
                        taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                        checkAndCompleteTask(player, task);
                    }
                    break;
                case MINE_STONE:
                    if (blockType == Material.STONE) {
                        taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                        checkAndCompleteTask(player, task);
                    }
                    break;
                case COLLECT_WOOD:
                    if (blockType.toString().endsWith("_LOG")) {
                        taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                        checkAndCompleteTask(player, task);
                    }
                    break;
                case COLLECT_WHEAT:
                    // 收集小麦任务将在其他事件中处理
                    break;
                case REACH_LEVEL:
                    // 达到等级任务将在PlayerLevelChangeEvent中处理
                    break;
                case TRADE_WITH_VILLAGER:
                    // 与村民交易任务将在其他事件中处理
                    break;
                case BAKE_BREAD:
                    // 烘焙面包任务将在其他事件中处理
                    break;
                case BREW_POTION:
                    // 酿造药水任务将在其他事件中处理
                    break;
                case ENCHANT_ITEM:
                    // 附魔物品任务将在其他事件中处理
                    break;
                case KILL_ENDERMAN:
                    // 击杀末影人任务将在EntityDeathEvent中处理
                    break;
                case KILL_SKELETON:
                    // 击杀骷髅任务将在EntityDeathEvent中处理
                    break;
                case HARVEST_CROP:
                    // 收获作物任务将在其他事件中处理
                    break;
                case SHEAR_SHEEP:
                    // 剪羊毛任务将在其他事件中处理
                    break;
                case KILL_ZOMBIE:
                    // 击杀僵尸任务将在EntityDeathEvent中处理
                    break;
                case TAME_ANIMAL:
                    // 驯服动物任务将在其他事件中处理
                    break;
                case KILL_CREEPER:
                    // 击杀苦力怕任务将在EntityDeathEvent中处理
                    break;
                case COLLECT_FLOWER:
                    // 收集花朵任务将在其他事件中处理
                    break;
                case MILK_COW:
                    // 挤牛奶任务将在其他事件中处理
                    break;
                case EXPLORE_BIOME:
                    // 探索生物群系任务将在其他事件中处理
                    break;
                case FISH_ITEM:
                    // 钓鱼任务将在其他事件中处理
                    break;
                case DELIVER_POTION:
                    // 递送药水任务将在其他事件中处理
                    break;
                case CRAFT_ITEM:
                    // 制作物品任务将在CraftItemEvent中处理
                    break;
                case KILL_SPIDER:
                    // 击杀蜘蛛任务将在EntityDeathEvent中处理
                    break;
                case BREED_ANIMAL:
                    // 繁殖动物任务将在其他事件中处理
                    break;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            EntityType entityType = event.getEntityType();
            
            List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
            
            for (Task task : activeTasks) {
                switch (task.getType()) {
                    case COLLECT_WOOD:
                        // 收集木材任务将在BlockBreakEvent中处理
                        break;
                    case KILL_ZOMBIE:
                        if (entityType == EntityType.ZOMBIE) {
                            taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                            checkAndCompleteTask(player, task);
                        }
                        break;
                    case KILL_SKELETON:
                        if (entityType == EntityType.SKELETON) {
                            taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                            checkAndCompleteTask(player, task);
                        }
                        break;
                    case KILL_CREEPER:
                        if (entityType == EntityType.CREEPER) {
                            taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                            checkAndCompleteTask(player, task);
                        }
                        break;
                    case KILL_SPIDER:
                        if (entityType == EntityType.SPIDER) {
                            taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                            checkAndCompleteTask(player, task);
                        }
                        break;
                    case KILL_ENDERMAN:
                        if (entityType == EntityType.ENDERMAN) {
                            taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                            checkAndCompleteTask(player, task);
                        }
                        break;
                    case COLLECT_WHEAT:
                        // 收集小麦任务将在其他事件中处理
                        break;
                    case REACH_LEVEL:
                        // 达到等级任务将在PlayerLevelChangeEvent中处理
                        break;
                    case TRADE_WITH_VILLAGER:
                        // 与村民交易任务将在其他事件中处理
                        break;
                    case BAKE_BREAD:
                        // 烘焙面包任务将在其他事件中处理
                        break;
                    case BREW_POTION:
                        // 酿造药水任务将在其他事件中处理
                        break;
                    case ENCHANT_ITEM:
                        // 附魔物品任务将在其他事件中处理
                        break;
                    case HARVEST_CROP:
                        // 收获作物任务将在其他事件中处理
                        break;
                    case SHEAR_SHEEP:
                        // 剪羊毛任务将在其他事件中处理
                        break;
                    case TAME_ANIMAL:
                        // 驯服动物任务将在其他事件中处理
                        break;
                    case COLLECT_FLOWER:
                        // 收集花朵任务将在其他事件中处理
                        break;
                    case MILK_COW:
                        // 挤牛奶任务将在其他事件中处理
                        break;
                    case EXPLORE_BIOME:
                        // 探索生物群系任务将在其他事件中处理
                        break;
                    case FISH_ITEM:
                        // 钓鱼任务将在其他事件中处理
                        break;
                    case DELIVER_POTION:
                        // 递送药水任务将在其他事件中处理
                        break;
                    case CRAFT_ITEM:
                        // 制作物品任务将在CraftItemEvent中处理
                        break;
                    case BREED_ANIMAL:
                        // 繁殖动物任务将在其他事件中处理
                        break;
                    case MINE_DIAMOND:
                        // 挖钻石任务将在BlockBreakEvent中处理
                        break;
                    case MINE_IRON:
                        // 挖铁矿石任务将在BlockBreakEvent中处理
                        break;
                    case MINE_STONE:
                        // 挖掘石头任务将在BlockBreakEvent中处理
                        break;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack result = event.getRecipe().getResult();
            int amount = result.getAmount();
            
            List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
            
            for (Task task : activeTasks) {
                if (task.getType() == Task.TaskType.CRAFT_ITEM) {
                    taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), amount);
                    checkAndCompleteTask(player, task);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            
            List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
            
            for (Task task : activeTasks) {
                if (task.getType() == Task.TaskType.FISH_ITEM) {
                    taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                    checkAndCompleteTask(player, task);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.TRADE_WITH_VILLAGER) {
                if (event.getRightClicked().getType() == EntityType.VILLAGER) {
                    taskManager.updateTaskProgress(player.getUniqueId(), task.getId(), 1);
                    checkAndCompleteTask(player, task);
                }
            }
        }
    }
    
    /**
     * 处理玩家消费物品事件 - 用于消费类任务
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material consumedType = event.getItem().getType();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            // 修复类型比较错误，从字符串比较改为枚举比较
            if (task.getType() == Task.TaskType.DELIVER_POTION) {
                if (task.getTargetItem().equalsIgnoreCase(consumedType.name())) {
                    taskManager.updateTaskProgress(player.getUniqueId(), task.getTaskId(), 1);
                    
                    if (task.getProgress() >= task.getTargetAmount()) {
                        completeTask(player, task);
                    } else {
                        showTaskProgress(player, task);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        int newLevel = event.getNewLevel();
        
        List<Task> activeTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        for (Task task : activeTasks) {
            if (task.getType() == Task.TaskType.REACH_LEVEL) {
                // 更新进度为玩家当前等级
                task.setProgress(newLevel);
                checkAndCompleteTask(player, task);
            }
        }
    }
    
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
        // 发送完成消息
        Map<String, String> replacements = new HashMap<>();
        replacements.put("task", task.getDescription());
        replacements.put("reward", task.getRewardDescription());
        
        player.sendMessage("任务完成!");
        
        // 移除已完成的任务
        taskManager.completeTask(player.getUniqueId(), task.getId());
    }
}