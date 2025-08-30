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
        
        // Task newTask = generateRandomTask(player);
        // taskData.setCurrentTask(newTask);
        
        // 保存任务数据
        savePlayerTaskData(taskData);
        
        // 通知玩家
        // player.sendMessage("你获得了一个新任务: " + newTask.getDescription());
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