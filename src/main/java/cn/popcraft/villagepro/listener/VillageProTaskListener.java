package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.model.Task;
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
import org.bukkit.event.player.PlayerExpChangeEvent;
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
import java.util.UUID;

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
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack result = event.getRecipe().getResult();
            Material craftedType = result.getType();
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
            if (task.getType().equals("CONSUME")) {
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
    
    /**
     * 创建进度条
     */
    private String createProgressBar(double percentage) {
        int barLength = 20;
        int filledLength = (int) (barLength * percentage / 100);
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);
        
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append("█");
            } else if (i == filledLength && percentage % (100.0 / barLength) > 0) {
                bar.append("▌");
            } else {
                bar.append(ChatColor.GRAY).append("█");
            }
        }
        
        return bar.toString();
    }
    
    private void completeTask(Player player, Task task) {
        // 发送完成消息
        Map<String, String> replacements = new HashMap<>();
        replacements.put("task", task.getDescription());
        replacements.put("reward", task.getRewardDescription());
        player.sendMessage(plugin.getMessageManager().getMessage("task.completed", replacements));
        
        // 发放奖励
        taskManager.rewardPlayer(player);
        
        // 从任务列表中移除
        taskManager.completeTask(player.getUniqueId(), task.getId());
    }

    private void showTaskProgress(Player player, Task task) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("task", task.getDescription());
        replacements.put("progress", String.valueOf(task.getProgress()));
        replacements.put("target", String.valueOf(task.getTargetAmount()));
        replacements.put("percentage", String.format("%.1f", (double) task.getProgress() / task.getTargetAmount() * 100));
        player.sendMessage(plugin.getMessageManager().getMessage("task.progress", replacements));
    }
}
