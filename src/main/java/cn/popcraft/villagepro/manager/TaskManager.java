package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.PlayerTaskData;
import cn.popcraft.villagepro.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {
    private final VillagePro plugin;
    private final Map<UUID, PlayerTaskData> taskCache = new ConcurrentHashMap<>();

    public TaskManager(VillagePro plugin) {
        this.plugin = plugin;
        startPeriodicSaveTask();
    }
    
    /**
     * 在插件禁用时关闭数据库连接
     */
    public void shutdown() {
    }
    
    /**
     * 为玩家分配新任务
     * @param player 玩家
     * @return 新任务
     */
    public Task assignNewTask(Player player) {
        // 检查Quests插件是否可用
        if (plugin.getQuestsIntegrationManager().isQuestsAvailable()) {
            // 使用Quests的随机任务功能
            if (plugin.getQuestsIntegrationManager().assignRandomQuest(player)) {
                player.sendMessage(ChatColor.GREEN + "已通过Quests插件分配随机任务!");
                return null; // Quests任务不需要在我们的系统中跟踪
            } else {
                player.sendMessage(ChatColor.RED + "无法通过Quests插件分配任务!");
            }
        }
        
        // 如果Quests不可用或分配失败，使用内置任务系统
        Task.TaskType randomType;
        do {
            randomType = getRandomTaskType();
        } while (!isValidTaskType(randomType));
        
        // 生成任务参数
        int targetAmount;
        double rewardMoney;
        int rewardExp;
        
        // 根据任务类型设置参数
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
        
        // 创建新任务
        Task newTask = new Task(randomType, targetAmount, rewardMoney, rewardExp);
        
        // 获取或创建玩家任务数据
        PlayerTaskData playerTaskData = getOrCreatePlayerTaskData(player.getUniqueId());
        
        // 将任务设置为玩家的当前任务（这会自动添加到活跃任务列表中）
        playerTaskData.setCurrentTask(newTask);
        
        // 保存玩家任务数据
        savePlayerTaskData(playerTaskData);
        
        // 发送任务生成消息
        Map<String, String> replacements = new HashMap<>();
        replacements.put("description", newTask.getDescription());
        player.sendMessage(plugin.getMessageManager().getMessage("task.generated", replacements));
        
        // 返回新创建的任务
        return newTask;
    }
    
    /**
     * 验证任务类型是否有效
     */
    private boolean isValidTaskType(Task.TaskType type) {
        // 检查任务类型是否在配置文件启用的任务类型列表中
        List<String> enabledTaskTypes = plugin.getConfig().getStringList("tasks.types");
        
        // 如果配置中没有任务类型列表，则默认启用所有任务类型
        if (enabledTaskTypes.isEmpty()) {
            return true;
        }
        
        // 检查当前任务类型是否在启用列表中
        return enabledTaskTypes.contains(type.name());
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
        // 注释掉对不存在的数据库的引用
        // database.saveWithId(taskData, taskData.getPlayerUuid().toString(), "player_tasks");
        
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
    
    /**
     * 生成随机任务类型
     */
    private Task.TaskType getRandomTaskType() {
        Task.TaskType[] taskTypes = Task.TaskType.values();
        return taskTypes[plugin.getRandom().nextInt(taskTypes.length)];
    }
    
    /**
     * 启动定期保存任务数据的任务
     */
    private void startPeriodicSaveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            saveAll();
        }, 6000L, 6000L); // 每5分钟保存一次
    }
    
    /**
     * 加载所有任务数据
     */
    public void loadAll() {
        // 从存储中加载所有任务数据
        // 移除对不存在的数据库的引用
        // database.findAll(PlayerTaskData.class).forEach(taskData -> 
        //     taskCache.put(taskData.getPlayerUuid(), taskData));
    }

    /**
     * 保存所有任务数据
     */
    public void saveAll() {
        // 保存所有任务数据到存储
        // 移除对不存在的数据库的引用
        // taskCache.values().forEach(taskData -> 
        //     database.saveWithId(taskData, taskData.getPlayerUuid().toString(), "player_tasks"));
    }

    /**
     * 保存玩家任务数据
     */
    public void savePlayerTaskData(PlayerTaskData taskData) {
        // 移除对不存在的数据库的引用
        // database.saveWithId(taskData, playerId.toString(), "player_tasks");
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
     * 更新任务进度
     * 如果使用Quests系统，则同时更新Quests任务进度
     */
    public void updateTaskProgress(UUID playerId, UUID taskId, int amount) {
        // 首先更新内置任务系统
        PlayerTaskData taskData = taskCache.get(playerId);
        if (taskData == null) return;
        
        Task task = taskData.getTaskById(taskId);
        if (task == null) return;
        
        task.setProgress(task.getProgress() + amount);
        // 注释掉对不存在的数据库的引用
        // database.saveWithId(taskData, playerId.toString(), "player_tasks");
        
        // 如果启用了Quests系统，也更新Quests任务进度
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && plugin.getQuestsIntegrationManager().isQuestsAvailable()) {
            plugin.getQuestsIntegrationManager().updateQuestProgress(player, task.getType().name(), amount);
        }
    }
    
    /**
     * 完成任务
     * 如果使用Quests系统，则同时完成Quests任务
     */
    public void completeTask(UUID playerId, UUID taskId) {
        // 完成内置任务系统中的任务
        PlayerTaskData taskData = taskCache.get(playerId);
        if (taskData == null) return;
        
        Task task = taskData.getTaskById(taskId);
        if (task == null) return;
        
        // 从活跃任务列表中移除
        taskData.getActiveTasks().removeIf(t -> t.getId().equals(taskId));
        
        // 如果启用了Quests系统，也完成Quests任务
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && plugin.getQuestsIntegrationManager().isQuestsAvailable()) {
            plugin.getQuestsIntegrationManager().completeQuest(player, task.getType().name());
        }
    }
    
    /**
     * 创建进度条
     * @param percentage 进度百分比
     * @return 进度条字符串
     */
    public String createProgressBar(double percentage) {
        int filledBars = (int) (percentage / 10);
        StringBuilder bar = new StringBuilder("[");
        
        for (int i = 0; i < 10; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        
        bar.append("] ");
        bar.append(String.format("%.1f", percentage)).append("%");
        
        return bar.toString();
    }
    
    /**
     * 增加玩家的任务积分
     * @param playerUUID 玩家UUID
     * @param points 要增加的积分数量
     */
    public void addTaskPoints(UUID playerUUID, int points) {
        // 获取或创建玩家数据
        PlayerTaskData data = getPlayerTaskData(playerUUID);
        if (data == null) {
            data = new PlayerTaskData(playerUUID);
        }

        // 增加积分
        data.addTaskPoints(points);

        // 保存数据
        savePlayerTaskData(data);

        // 如果玩家在线，可以发送消息
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§a获得 §b" + points + " §a任务积分！当前积分: §e" + data.getTaskPoints());
        }
    }
    
    /**
     * 获取可用任务
     */
    public List<Task> getAvailableTasks(UUID playerId) {
        PlayerTaskData playerData = taskCache.get(playerId);
        if (playerData == null) {
            return new ArrayList<>();
        }
        return playerData.getActiveTasks();
    }
}