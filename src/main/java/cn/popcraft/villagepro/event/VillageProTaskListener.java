package cn.popcraft.villagepro.event;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.model.Task;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.HashSet;

public class VillageProTaskListener implements Listener {
    private final TaskManager taskManager;
    
    // 定义不同类型任务所需的材料或实体
    private static final Set<Material> WOOD_TYPES = new HashSet<>();
    private static final Set<Material> STONE_TYPES = new HashSet<>();
    private static final Set<Material> ORE_TYPES = new HashSet<>();
    private static final Set<EntityType> ANIMAL_TYPES = new HashSet<>();
    
    static {
        // 木材类型
        WOOD_TYPES.add(Material.OAK_LOG);
        WOOD_TYPES.add(Material.SPRUCE_LOG);
        WOOD_TYPES.add(Material.BIRCH_LOG);
        WOOD_TYPES.add(Material.JUNGLE_LOG);
        WOOD_TYPES.add(Material.ACACIA_LOG);
        WOOD_TYPES.add(Material.DARK_OAK_LOG);
        WOOD_TYPES.add(Material.MANGROVE_LOG);
        WOOD_TYPES.add(Material.CHERRY_LOG);
        
        // 石头类型
        STONE_TYPES.add(Material.STONE);
        STONE_TYPES.add(Material.COBBLESTONE);
        STONE_TYPES.add(Material.ANDESITE);
        STONE_TYPES.add(Material.DIORITE);
        STONE_TYPES.add(Material.GRANITE);
        
        // 矿石类型
        ORE_TYPES.add(Material.COAL_ORE);
        ORE_TYPES.add(Material.IRON_ORE);
        ORE_TYPES.add(Material.GOLD_ORE);
        ORE_TYPES.add(Material.DIAMOND_ORE);
        ORE_TYPES.add(Material.EMERALD_ORE);
        
        // 动物类型
        ANIMAL_TYPES.add(EntityType.COW);
        ANIMAL_TYPES.add(EntityType.PIG);
        ANIMAL_TYPES.add(EntityType.SHEEP);
        ANIMAL_TYPES.add(EntityType.CHICKEN);
        ANIMAL_TYPES.add(EntityType.HORSE);
        ANIMAL_TYPES.add(EntityType.DONKEY);
        ANIMAL_TYPES.add(EntityType.MULE);
    }
    
    public VillageProTaskListener(VillagePro plugin, TaskManager taskManager) {
        this.taskManager = taskManager;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        // 检查是否有相关的任务
        Task currentTask = taskManager.getCurrentTask(player);
        if (currentTask == null) return;
        
        Task.TaskType taskType = currentTask.getType();
        int amount = 1;
        
        // 根据任务类型处理不同的方块破坏事件
        switch (taskType) {
            case COLLECT_WOOD:
                if (WOOD_TYPES.contains(blockType)) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), amount);
                }
                break;
                
            case MINE_STONE:
                if (STONE_TYPES.contains(blockType)) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), amount);
                }
                break;
                
            case MINE_IRON:
                if (blockType == Material.IRON_ORE) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), amount);
                }
                break;
                
            case MINE_DIAMOND:
                if (blockType == Material.DIAMOND_ORE) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), amount);
                }
                break;
                
            default:
                // 其他任务类型不处理此事件
                break;
        }
    }
    
    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player) {
            Player player = (Player) event.getBreeder();
            
            Task currentTask = taskManager.getCurrentTask(player);
            if (currentTask == null) return;
            
            Task.TaskType taskType = currentTask.getType();
            if (taskType == Task.TaskType.BREED_ANIMAL) {
                // 检查是否是允许的动物类型
                if (ANIMAL_TYPES.contains(event.getEntityType())) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
                    taskManager.checkTaskCompletion(player);
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (event.getOwner() instanceof Player) {
            Player player = (Player) event.getOwner();
            
            Task currentTask = taskManager.getCurrentTask(player);
            if (currentTask == null) return;
            
            Task.TaskType taskType = currentTask.getType();
            if (taskType == Task.TaskType.TAME_ANIMAL) {
                taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
                taskManager.checkTaskCompletion(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        
        Task currentTask = taskManager.getCurrentTask(player);
        if (currentTask == null) return;
        
        Task.TaskType taskType = currentTask.getType();
        if (taskType == Task.TaskType.FISH_ITEM && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
            taskManager.checkTaskCompletion(player);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;
        
        Task currentTask = taskManager.getCurrentTask(player);
        if (currentTask == null) return;
        
        Task.TaskType taskType = currentTask.getType();
        EntityType entityType = event.getEntityType();
        
        // 根据任务类型处理不同的实体死亡事件
        switch (taskType) {
            case KILL_ZOMBIE:
                if (entityType == EntityType.ZOMBIE) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
                    taskManager.checkTaskCompletion(player);
                }
                break;
                
            case KILL_SKELETON:
                if (entityType == EntityType.SKELETON) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
                    taskManager.checkTaskCompletion(player);
                }
                break;
                
            case KILL_SPIDER:
                if (entityType == EntityType.SPIDER) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
                    taskManager.checkTaskCompletion(player);
                }
                break;
                
            case KILL_CREEPER:
                if (entityType == EntityType.CREEPER) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
                    taskManager.checkTaskCompletion(player);
                }
                break;
                
            case KILL_ENDERMAN:
                if (entityType == EntityType.ENDERMAN) {
                    taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
                    taskManager.checkTaskCompletion(player);
                }
                break;
                
            default:
                // 其他任务类型不处理此事件
                break;
        }
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            Task currentTask = taskManager.getCurrentTask(player);
            if (currentTask == null) return;
            
            Task.TaskType taskType = currentTask.getType();
            if (taskType == Task.TaskType.CRAFT_ITEM) {
                ItemStack result = event.getRecipe().getResult();
                taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), result.getAmount());
                taskManager.checkTaskCompletion(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        Task currentTask = taskManager.getCurrentTask(player);
        if (currentTask == null) return;
        
        Task.TaskType taskType = currentTask.getType();
        if (taskType == Task.TaskType.MILK_COW && event.getRightClicked().getType() == EntityType.COW) {
            // 注意：实际的挤奶事件需要通过其他方式检测，这里仅作示例
            taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
            taskManager.checkTaskCompletion(player);
        }
    }
    
    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        
        Task currentTask = taskManager.getCurrentTask(player);
        if (currentTask == null) return;
        
        Task.TaskType taskType = currentTask.getType();
        if (taskType == Task.TaskType.SHEAR_SHEEP && event.getEntity().getType() == EntityType.SHEEP) {
            taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), 1);
            taskManager.checkTaskCompletion(player);
        }
    }
    
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        
        Task currentTask = taskManager.getCurrentTask(player);
        if (currentTask == null) return;
        
        Task.TaskType taskType = currentTask.getType();
        if (taskType == Task.TaskType.REACH_LEVEL) {
            int newLevel = event.getNewLevel();
            int oldLevel = event.getOldLevel();
            int levelGained = newLevel - oldLevel;
            
            if (levelGained > 0) {
                taskManager.updateTaskProgress(player.getUniqueId(), currentTask.getTaskId(), levelGained);
                taskManager.checkTaskCompletion(player);
            }
        }
    }
}