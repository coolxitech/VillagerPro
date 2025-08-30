package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.PlayerTaskData;
import cn.popcraft.villagepro.model.Task;
import cn.popcraft.villagepro.storage.VillageStorage;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;
import java.util.Collection;

public class TaskManager {
    private final VillagePro plugin;
    private final VillageStorage database;
    private final Map<UUID, PlayerTaskData> taskCache = new HashMap<>();
    private final Random random = new Random();

    public List<Task> getAvailableTasks(UUID playerId) {
        PlayerTaskData playerData = taskCache.get(playerId);
        if (playerData == null) {
            return new ArrayList<>();
        }
        return playerData.getActiveTasks();
    }

    public void updateTaskProgress(UUID playerId, UUID taskId, int amount) {
        PlayerTaskData playerData = taskCache.get(playerId);
        if (playerData == null) return;
        
        Task task = playerData.getTaskById(taskId);
        if (task != null) {
            int newProgress = task.getProgress() + amount;
            task.setProgress(newProgress);
            database.updateTaskProgress(playerId, taskId, newProgress);
        }
    }

    public void completeTask(UUID playerId, UUID taskId) {
        PlayerTaskData playerData = taskCache.get(playerId);
        if (playerData == null) return;
        
        Task task = playerData.getTaskById(taskId);
        if (task != null) {
            playerData.removeTask(taskId);
            database.completeTask(playerId, taskId);
        }
    }

    public TaskManager(VillagePro plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        
        // 注册数据模型
        database.registerTable(PlayerTaskData.class);
        
        // 加载所有任务数据
        loadAll();
    }

    /**
     * 加载所有任务数据
     */
    public void loadAll() {
        taskCache.clear();
        // 修复数据库调用，添加表名参数
        database.findAll(PlayerTaskData.class, "player_tasks").forEach(taskData -> 
            taskCache.put(taskData.getPlayerUuid(), taskData)
        );
        plugin.getLogger().info("已加载 " + taskCache.size() + " 个玩家任务数据");
    }

    /**
     * 保存所有任务数据
     */
    public void saveAll() {
        // 修复数据库调用，使用带参数的save方法
        taskCache.values().forEach(taskData -> 
            database.saveWithId(taskData, taskData.getPlayerUuid().toString(), "player_tasks")
        );
        plugin.getLogger().info("已保存 " + taskCache.size() + " 个玩家任务数据");
    }

    /**
     * 保存玩家任务数据
     */
    public void savePlayerTaskData(PlayerTaskData taskData) {
        // 修复数据库调用，使用带参数的save方法
        database.saveWithId(taskData, taskData.getPlayerUuid().toString(), "player_tasks");
    }
    
    /**
     * 获取玩家任务数据
     */
    public PlayerTaskData getPlayerTaskData(UUID playerId) {
        return taskCache.get(playerId);
    }
    
    /**
     * 创建或获取玩家任务数据
     */
    public PlayerTaskData getOrCreatePlayerTaskData(UUID playerId) {
        return taskCache.computeIfAbsent(playerId, id -> {
            PlayerTaskData data = new PlayerTaskData();
            data.setPlayerUuid(id);
            return data;
        });
    }
    
    /**
     * 为玩家分配新任务
     */
    public void assignNewTask(Player player) {
        PlayerTaskData taskData = getOrCreatePlayerTaskData(player.getUniqueId());
        
        // 限制每个玩家最多3个活跃任务
        if (taskData.getActiveTasks().size() >= 3) {
            player.sendMessage("你已经有太多任务了!");
            return;
        }
        
        Task newTask = generateRandomTask(player);
        taskData.getActiveTasks().add(newTask);
        
        // 保存任务数据
        savePlayerTaskData(taskData);
        
        // 通知玩家
        player.sendMessage("你获得了一个新任务: " + newTask.getDescription());
    }
    
    /**
     * 生成随机任务
     */
    public Task generateRandomTask(Player player) {
        // 获取所有可能的任务类型
        Task.TaskType[] taskTypes = Task.TaskType.values();
        Task.TaskType randomType;
        
        // 确保选择一个有效的任务类型
        do {
            randomType = taskTypes[plugin.getRandom().nextInt(taskTypes.length)];
        } while (!isValidTaskType(randomType));
        
        // 根据任务类型设置目标数量和奖励
        int targetAmount = 0;
        double rewardMoney = 0;
        int rewardExp = 0;
        
        switch (randomType) {
            case COLLECT_WOOD:
                targetAmount = 64 + plugin.getRandom().nextInt(64); // 64-127
                rewardMoney = 100 + plugin.getRandom().nextInt(100); // 100-200
                rewardExp = 50 + plugin.getRandom().nextInt(50); // 50-100
                break;
                
            case MINE_STONE:
                targetAmount = 128 + plugin.getRandom().nextInt(128); // 128-255
                rewardMoney = 150 + plugin.getRandom().nextInt(100); // 150-250
                rewardExp = 75 + plugin.getRandom().nextInt(50); // 75-125
                break;
                
            case MINE_IRON:
                targetAmount = 32 + plugin.getRandom().nextInt(32); // 32-63
                rewardMoney = 200 + plugin.getRandom().nextInt(150); // 200-350
                rewardExp = 100 + plugin.getRandom().nextInt(50); // 100-150
                break;
                
            case MINE_DIAMOND:
                targetAmount = 8 + plugin.getRandom().nextInt(8); // 8-15
                rewardMoney = 500 + plugin.getRandom().nextInt(500); // 500-1000
                rewardExp = 200 + plugin.getRandom().nextInt(100); // 200-300
                break;
                
            case KILL_ZOMBIE:
                targetAmount = 30 + plugin.getRandom().nextInt(20); // 30-50
                rewardMoney = 150 + plugin.getRandom().nextInt(100); // 150-250
                rewardExp = 75 + plugin.getRandom().nextInt(50); // 75-125
                break;
                
            case KILL_SKELETON:
                targetAmount = 20 + plugin.getRandom().nextInt(20); // 20-40
                rewardMoney = 200 + plugin.getRandom().nextInt(150); // 200-350
                rewardExp = 100 + plugin.getRandom().nextInt(50); // 100-150
                break;
                
            case KILL_SPIDER:
                targetAmount = 25 + plugin.getRandom().nextInt(15); // 25-40
                rewardMoney = 175 + plugin.getRandom().nextInt(125); // 175-300
                rewardExp = 85 + plugin.getRandom().nextInt(45); // 85-130
                break;
                
            case KILL_CREEPER:
                targetAmount = 10 + plugin.getRandom().nextInt(10); // 10-20
                rewardMoney = 300 + plugin.getRandom().nextInt(200); // 300-500
                rewardExp = 150 + plugin.getRandom().nextInt(100); // 150-250
                break;
                
            case KILL_ENDERMAN:
                targetAmount = 5 + plugin.getRandom().nextInt(5); // 5-10
                rewardMoney = 400 + plugin.getRandom().nextInt(300); // 400-700
                rewardExp = 200 + plugin.getRandom().nextInt(100); // 200-300
                break;
                
            case TRADE_WITH_VILLAGER:
                targetAmount = 10 + plugin.getRandom().nextInt(10); // 10-20
                rewardMoney = 250 + plugin.getRandom().nextInt(150); // 250-400
                rewardExp = 125 + plugin.getRandom().nextInt(75); // 125-200
                break;
                
            case MILK_COW:
                targetAmount = 5 + plugin.getRandom().nextInt(5); // 5-10
                rewardMoney = 100 + plugin.getRandom().nextInt(100); // 100-200
                rewardExp = 50 + plugin.getRandom().nextInt(50); // 50-100
                break;
                
            case CRAFT_ITEM:
                targetAmount = 20 + plugin.getRandom().nextInt(20); // 20-40
                rewardMoney = 150 + plugin.getRandom().nextInt(100); // 150-250
                rewardExp = 75 + plugin.getRandom().nextInt(50); // 75-125
                break;
                
            case BREW_POTION:
                targetAmount = 10 + plugin.getRandom().nextInt(10); // 10-20
                rewardMoney = 200 + plugin.getRandom().nextInt(150); // 200-350
                rewardExp = 100 + plugin.getRandom().nextInt(50); // 100-150
                break;
                
            case COLLECT_WHEAT:
                targetAmount = 64 + plugin.getRandom().nextInt(64); // 64-127
                rewardMoney = 100 + plugin.getRandom().nextInt(100); // 100-200
                rewardExp = 50 + plugin.getRandom().nextInt(50); // 50-100
                break;
                
            case TAME_ANIMAL:
                targetAmount = 3 + plugin.getRandom().nextInt(3); // 3-6
                rewardMoney = 300 + plugin.getRandom().nextInt(200); // 300-500
                rewardExp = 150 + plugin.getRandom().nextInt(100); // 150-250
                break;
                
            case HARVEST_CROP:
                targetAmount = 128 + plugin.getRandom().nextInt(128); // 128-255
                rewardMoney = 125 + plugin.getRandom().nextInt(125); // 125-250
                rewardExp = 60 + plugin.getRandom().nextInt(60); // 60-120
                break;
                
            case DELIVER_POTION:
                targetAmount = 5 + plugin.getRandom().nextInt(5); // 5-10
                rewardMoney = 250 + plugin.getRandom().nextInt(150); // 250-400
                rewardExp = 125 + plugin.getRandom().nextInt(75); // 125-200
                break;
                
            case BAKE_BREAD:
                targetAmount = 32 + plugin.getRandom().nextInt(32); // 32-63
                rewardMoney = 125 + plugin.getRandom().nextInt(125); // 125-250
                rewardExp = 60 + plugin.getRandom().nextInt(60); // 60-120
                break;
                
            case BREED_ANIMAL:
                targetAmount = 10 + plugin.getRandom().nextInt(10); // 10-20
                rewardMoney = 200 + plugin.getRandom().nextInt(150); // 200-350
                rewardExp = 100 + plugin.getRandom().nextInt(50); // 100-150
                break;
                
            case EXPLORE_BIOME:
                targetAmount = 3 + plugin.getRandom().nextInt(3); // 3-6
                rewardMoney = 300 + plugin.getRandom().nextInt(200); // 300-500
                rewardExp = 150 + plugin.getRandom().nextInt(100); // 150-250
                break;
                
            case FISH_ITEM:
                targetAmount = 20 + plugin.getRandom().nextInt(20); // 20-40
                rewardMoney = 150 + plugin.getRandom().nextInt(100); // 150-250
                rewardExp = 75 + plugin.getRandom().nextInt(50); // 75-125
                break;
                
            case REACH_LEVEL:
                targetAmount = 5 + plugin.getRandom().nextInt(5); // 5-10
                rewardMoney = 200 + plugin.getRandom().nextInt(200); // 200-400
                rewardExp = 100 + plugin.getRandom().nextInt(100); // 100-200
                break;
                
            case SHEAR_SHEEP:
                targetAmount = 10 + plugin.getRandom().nextInt(10); // 10-20
                rewardMoney = 150 + plugin.getRandom().nextInt(100); // 150-250
                rewardExp = 75 + plugin.getRandom().nextInt(50); // 75-125
                break;
                
            case COLLECT_FLOWER:
                targetAmount = 32 + plugin.getRandom().nextInt(32); // 32-63
                rewardMoney = 100 + plugin.getRandom().nextInt(100); // 100-200
                rewardExp = 50 + plugin.getRandom().nextInt(50); // 50-100
                break;
                
            case ENCHANT_ITEM:
                targetAmount = 5 + plugin.getRandom().nextInt(5); // 5-10
                rewardMoney = 250 + plugin.getRandom().nextInt(150); // 250-400
                rewardExp = 125 + plugin.getRandom().nextInt(75); // 125-200
                break;
                
            default:
                // 默认值
                targetAmount = 10;
                rewardMoney = 100;
                rewardExp = 50;
                break;
        }
        
        // 创建并返回新任务
        return new Task(randomType, targetAmount, rewardMoney, rewardExp);
    }
    
    /**
     * 验证任务类型是否有效
     */
    private boolean isValidTaskType(Task.TaskType type) {
        // 这里可以添加具体的验证逻辑
        // 比如检查任务类型是否被禁用等
        return true;
    }
    
    /**
     * 检查任务是否完成
     */
    public boolean isTaskCompleted(UUID playerId, UUID taskId) {
        PlayerTaskData playerData = taskCache.get(playerId);
        if (playerData == null) return false;
        
        Task task = playerData.getTaskById(taskId);
        return task != null && task.getProgress() >= task.getTargetAmount();
    }
    
    /**
     * 检查任务是否完成并发放奖励
     */
    public void checkTaskCompletion(Player player) {
        Task currentTask = getCurrentTask(player);
        if (currentTask == null) return;
        
        if (currentTask.getProgress() >= currentTask.getTargetAmount()) {
            // 任务完成，发放奖励
            rewardPlayer(player);
            
            // 发送完成消息
            player.sendMessage(plugin.getMessageManager().getMessage("task.completed", 
                Map.of("task", currentTask.getType().name())));
        }
    }
    
    /**
     * 发放任务奖励
     */
    public void giveTaskReward(Player player, Task task) {
        // 根据任务类型发放奖励
        switch (task.getType()) {
            case MINE_IRON:
                // 发放铁锭奖励
                player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 5));
                player.sendMessage("任务完成! 获得5个铁锭奖励。");
                break;
            case MINE_DIAMOND:
                // 发放钻石奖励
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                player.sendMessage("任务完成! 获得1个钻石奖励。");
                break;
            case MINE_STONE:
                // 发放经验奖励
                player.giveExp(100);
                player.sendMessage("任务完成! 获得100点经验奖励。");
                break;
            // 可以添加更多任务类型和奖励
        }
        
        // 从任务列表中移除已完成的任务
        completeTask(player.getUniqueId(), task.getId());
    }
    
    /**
     * 获取玩家当前任务
     */
    public Task getCurrentTask(Player player) {
        PlayerTaskData taskData = taskCache.get(player.getUniqueId());
        return taskData != null ? taskData.getCurrentTask() : null;
    }
    
    /**
     * 获取玩家所有活跃任务
     */
    public List<Task> getPlayerActiveTasks(UUID playerUuid) {
        PlayerTaskData taskData = taskCache.get(playerUuid);
        return taskData != null ? taskData.getActiveTasks() : new ArrayList<>();
    }
    
    /**
     * 发放任务奖励
     */
    public void rewardPlayer(Player player) {
        PlayerTaskData taskData = taskCache.get(player.getUniqueId());
        if (taskData == null || taskData.getCurrentTask() == null) {
            return;
        }
        
        Task task = taskData.getCurrentTask();
        
        // 发放经验或金币奖励
        player.giveExp(task.getRewardExp());
        if (plugin.getEconomyManager().isAvailable()) {
            plugin.getEconomyManager().deposit(player, task.getRewardMoney());
        }
        
        // 清除当前任务
        taskData.setCurrentTask(null);
        database.saveWithId(taskData, taskData.getPlayerUuid().toString(), "player_tasks");
        
        // 发送奖励消息
        Map<String, String> replacements = new HashMap<>();
        replacements.put("exp", String.valueOf(task.getRewardExp()));
        replacements.put("money", String.valueOf(task.getRewardMoney()));
        player.sendMessage(plugin.getMessageManager().getMessage("task.reward", replacements));
    }
    
    /**
     * 获取任务进度百分比
     * @param task 任务
     * @return 进度百分比
     */
    public double getTaskProgressPercentage(Task task) {
        if (task.getTargetAmount() <= 0) {
            return 0.0;
        }
        return (double) task.getProgress() / task.getTargetAmount() * 100;
    }
}