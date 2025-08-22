package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.PlayerTaskData;
import java.util.List;
import java.util.ArrayList;
import cn.popcraft.villagepro.model.Task;
import cn.popcraft.villagepro.storage.SQLiteStorage;
import org.bukkit.entity.Player;
import org.bukkit.Material;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskManager {
    private final VillagePro plugin;
    private final SQLiteStorage database;
    private final Map<UUID, PlayerTaskData> taskCache = new HashMap<>();

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
        database.findAll(PlayerTaskData.class).forEach(taskData -> 
            taskCache.put(taskData.getPlayerUuid(), taskData)
        );
        plugin.getLogger().info("已加载 " + taskCache.size() + " 个玩家任务数据");
    }

    /**
     * 保存所有任务数据
     */
    public void saveAll() {
        taskCache.values().forEach(database::save);
        plugin.getLogger().info("已保存 " + taskCache.size() + " 个玩家任务数据");
    }

    /**
     * 为玩家生成随机任务
     */
    public Task generateRandomTask(Player player) {
        // 随机生成任务逻辑
        Task task = new Task();
        task.setPlayerUuid(player.getUniqueId());
        Task.TaskType[] taskTypes = Task.TaskType.values();
        Task.TaskType randomType = taskTypes[plugin.getRandom().nextInt(taskTypes.length)];
        task.setType(randomType);
        
        // 根据任务类型设置目标数量
        switch (randomType) {
            case MINE_IRON:
            case MINE_DIAMOND:
            case MINE_STONE:
                task.setTargetAmount(plugin.getRandom().nextInt(10) + 5); // 5-15 个目标
                break;
            case BAKE_BREAD:
            case CRAFT_ITEM:
                task.setTargetAmount(plugin.getRandom().nextInt(5) + 3); // 3-8 个目标
                break;
            case KILL_SKELETON:
            case KILL_CREEPER:
            case KILL_ZOMBIE:
            case KILL_SPIDER:
            case KILL_ENDERMAN:
                task.setTargetAmount(plugin.getRandom().nextInt(8) + 3); // 3-10 个目标
                break;
            case REACH_LEVEL:
                task.setTargetAmount(plugin.getRandom().nextInt(10) + 5); // 5-15 级
                break;
            case COLLECT_WHEAT:
            case COLLECT_WOOD:
            case COLLECT_FLOWER:
            case HARVEST_CROP:
                task.setTargetAmount(plugin.getRandom().nextInt(20) + 10); // 10-30 个目标
                break;
            case FISH_ITEM:
                task.setTargetAmount(plugin.getRandom().nextInt(5) + 5); // 5-10 个目标
                break;
            case BREED_ANIMAL:
            case SHEAR_SHEEP:
                task.setTargetAmount(plugin.getRandom().nextInt(5) + 2); // 2-7 个目标
                break;
            case TRADE_WITH_VILLAGER:
                task.setTargetAmount(plugin.getRandom().nextInt(3) + 2); // 2-5 个目标
                break;
            case ENCHANT_ITEM:
            case BREW_POTION:
                task.setTargetAmount(plugin.getRandom().nextInt(3) + 1); // 1-4 个目标
                break;
            case TAME_ANIMAL:
                task.setTargetAmount(plugin.getRandom().nextInt(2) + 1); // 1-3 个目标
                break;
            case MILK_COW:
                task.setTargetAmount(plugin.getRandom().nextInt(3) + 1); // 1-4 个目标
                break;
            case EXPLORE_BIOME:
                task.setTargetAmount(1); // 探索1个生物群系
                break;
            case DELIVER_POTION:
                task.setTargetAmount(plugin.getRandom().nextInt(3) + 1); // 1-4 个目标
                break;
            default:
                task.setTargetAmount(plugin.getRandom().nextInt(5) + 1); // 默认 1-5 个目标
        }
        
        // 设置奖励
        task.setRewardExp(plugin.getRandom().nextInt(100) + 50); // 50-150 经验
        task.setRewardMoney(plugin.getRandom().nextInt(200) + 100); // 100-300 金币
        
        // 设置任务描述
        switch (randomType) {
            case COLLECT_WHEAT:
                task.setDescription("收集 " + task.getTargetAmount() + " 个小麦");
                task.setTargetItem(Material.WHEAT.name());
                break;
            case KILL_ZOMBIE:
                task.setDescription("击杀 " + task.getTargetAmount() + " 只僵尸");
                break;
            case DELIVER_POTION:
                task.setDescription("交付 " + task.getTargetAmount() + " 瓶药水");
                task.setTargetItem(Material.POTION.name());
                break;
            case MINE_IRON:
                task.setDescription("挖 " + task.getTargetAmount() + " 个铁矿石");
                task.setTargetItem(Material.IRON_ORE.name());
                break;
            case MINE_DIAMOND:
                task.setDescription("挖 " + task.getTargetAmount() + " 个钻石矿石");
                task.setTargetItem(Material.DIAMOND_ORE.name());
                break;
            case BAKE_BREAD:
                task.setDescription("烤 " + task.getTargetAmount() + " 个面包");
                task.setTargetItem(Material.BREAD.name());
                break;
            case KILL_SKELETON:
                task.setDescription("击杀 " + task.getTargetAmount() + " 只骷髅");
                break;
            case KILL_CREEPER:
                task.setDescription("击杀 " + task.getTargetAmount() + " 只苦力怕");
                break;
            case REACH_LEVEL:
                task.setDescription("达到 " + task.getTargetAmount() + " 级经验等级");
                break;
            case FISH_ITEM:
                task.setDescription("钓鱼 " + task.getTargetAmount() + " 次");
                break;
            case CRAFT_ITEM:
                task.setDescription("制作 " + task.getTargetAmount() + " 个物品");
                break;
            case ENCHANT_ITEM:
                task.setDescription("附魔 " + task.getTargetAmount() + " 个物品");
                break;
            case BREED_ANIMAL:
                task.setDescription("繁殖 " + task.getTargetAmount() + " 次动物");
                break;
            case HARVEST_CROP:
                task.setDescription("收获 " + task.getTargetAmount() + " 次作物");
                break;
            case EXPLORE_BIOME:
                task.setDescription("探索一个新的生物群系");
                break;
            case TRADE_WITH_VILLAGER:
                task.setDescription("与村民完成 " + task.getTargetAmount() + " 次交易");
                break;
            case COLLECT_WOOD:
                task.setDescription("收集 " + task.getTargetAmount() + " 个原木");
                task.setTargetItem(Material.OAK_LOG.name());
                break;
            case MINE_STONE:
                task.setDescription("挖 " + task.getTargetAmount() + " 个石头");
                task.setTargetItem(Material.STONE.name());
                break;
            case KILL_SPIDER:
                task.setDescription("击杀 " + task.getTargetAmount() + " 只蜘蛛");
                break;
            case KILL_ENDERMAN:
                task.setDescription("击杀 " + task.getTargetAmount() + " 只末影人");
                break;
            case COLLECT_FLOWER:
                task.setDescription("收集 " + task.getTargetAmount() + " 朵花");
                break;
            case SHEAR_SHEEP:
                task.setDescription("剪 " + task.getTargetAmount() + " 只羊的毛");
                break;
            case MILK_COW:
                task.setDescription("挤 " + task.getTargetAmount() + " 次牛奶");
                break;
            case TAME_ANIMAL:
                task.setDescription("驯服 " + task.getTargetAmount() + " 只动物");
                break;
            case BREW_POTION:
                task.setDescription("酿造 " + task.getTargetAmount() + " 瓶药水");
                break;
            default:
                task.setDescription("完成 " + task.getTargetAmount() + " 个任务目标");
        }
        
        // 保存任务数据
        PlayerTaskData taskData = taskCache.computeIfAbsent(player.getUniqueId(), k -> new PlayerTaskData());
        taskData.setCurrentTask(task);
        database.save(taskData);
        
        return task;
    }

    /**
     * 检查任务是否完成
     */
    public boolean checkTaskCompletion(Player player) {
        PlayerTaskData taskData = taskCache.get(player.getUniqueId());
        if (taskData == null || taskData.getCurrentTask() == null) {
            return false;
        }
        
        Task task = taskData.getCurrentTask();
        return task.getProgress() >= task.getTargetAmount();
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
        
        // 发放经验或金币奖励
        player.giveExp(taskData.getCurrentTask().getRewardExp());
        plugin.getEconomy().depositPlayer(player, taskData.getCurrentTask().getRewardMoney());
        
        // 清除当前任务
        taskData.setCurrentTask(null);
        database.save(taskData);
    }
}