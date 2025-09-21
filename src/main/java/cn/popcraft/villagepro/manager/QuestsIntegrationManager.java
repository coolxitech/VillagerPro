package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Quests插件集成管理器 (适配 PikaMug/Quests)
 * 用于在检测到Quests插件时，使用Quests的任务系统替代内置任务系统
 */
public class QuestsIntegrationManager {
    private final VillagePro plugin;
    private Plugin questsPlugin; // 直接保存 Plugin 实例
    private boolean questsAvailable = false;

    public QuestsIntegrationManager(VillagePro plugin) {
        this.plugin = plugin;
        initializeQuestsIntegration();
    }

    /**
     * 初始化Quests集成
     */
    private void initializeQuestsIntegration() {
        this.questsPlugin = Bukkit.getPluginManager().getPlugin("Quests");
        if (this.questsPlugin == null || !this.questsPlugin.isEnabled()) {
            this.plugin.getLogger().info("未检测到Quests插件，将使用内置任务系统");
            return;
        }
        this.questsAvailable = true;
        this.plugin.getLogger().info("成功集成Quests插件，将使用Quests任务系统");
    }

    /**
     * 检查Quests插件是否可用
     */
    public boolean isQuestsAvailable() {
        return questsAvailable && questsPlugin != null && questsPlugin.isEnabled();
    }

    // ================== 核心：获取 QPlayer 实例 ==================

    private Object getQPlayer(Player player) {
        if (!isQuestsAvailable() || player == null) {
            return null;
        }

        try {
            Class<?> questsClass = questsPlugin.getClass();
            
            // 尝试不同的方法名获取玩家实例
            Method getPlayerMethod = null;
            Exception lastException = null;
            
            // 尝试 getPlayer 方法
            try {
                getPlayerMethod = questsClass.getMethod("getPlayer", org.bukkit.entity.Player.class);
            } catch (NoSuchMethodException e) {
                lastException = e;
                
                // 尝试 getQuester 方法（旧版本）
                try {
                    getPlayerMethod = questsClass.getMethod("getQuester", org.bukkit.entity.Player.class);
                } catch (NoSuchMethodException ex) {
                    lastException = ex;
                    
                    // 尝试 get 方法（可能的新版本）
                    try {
                        getPlayerMethod = questsClass.getMethod("get", org.bukkit.entity.Player.class);
                    } catch (NoSuchMethodException exc) {
                        lastException = exc;
                        
                        // 尝试 getPlayerByUUID 方法
                        try {
                            getPlayerMethod = questsClass.getMethod("getPlayerByUUID", java.util.UUID.class);
                            // 如果找到这个方法，我们需要传递UUID而不是Player
                            Object result = getPlayerMethod.invoke(questsPlugin, player.getUniqueId());
                            return result;
                        } catch (NoSuchMethodException exce) {
                            lastException = exce;
                        }
                    }
                }
            }
            
            if (getPlayerMethod == null) {
                plugin.getLogger().log(Level.WARNING, "无法找到获取Quests玩家实例的方法", lastException);
                return null;
            }
            
            // 如果我们没有提前返回（getPlayerByUUID情况），则正常调用
            if (!getPlayerMethod.getName().equals("getPlayerByUUID")) {
                return getPlayerMethod.invoke(questsPlugin, player);
            }
            // getPlayerByUUID的情况已经在上面处理了
            return null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法获取玩家的QPlayer实例: " + e.getMessage(), e);
            return null;
        }
    }

    // ================== 功能：触发自定义目标完成 ==================

    /**
     * 触发玩家完成一个自定义任务目标
     * @param player 玩家
     * @param questId 任务ID (在 quests.yml 中定义的 key)
     * @param objectiveName 自定义目标名称 (在 quests.yml 中 custom_objective 下的 key)
     * @param amount 完成数量
     * @return 是否成功触发
     */
    public boolean completeCustomObjective(Player player, String questId, String objectiveName, int amount) {
        if (!isQuestsAvailable()) return false;

        Object qPlayer = getQPlayer(player);
        if (qPlayer == null) return false;

        try {
            // 1. 通过 QPlayer 获取 QuestProgress 对象
            Class<?> qPlayerClass = qPlayer.getClass();
            
            // 尝试不同的方法名获取任务进度
            Method getQuestProgressMethod = null;
            Exception lastException = null;
            
            try {
                // 首先尝试 getQuestProgress 方法
                getQuestProgressMethod = qPlayerClass.getMethod("getQuestProgress", String.class);
            } catch (NoSuchMethodException e) {
                lastException = e;
                try {
                    // 尝试 getQuestData 方法（旧版本）
                    getQuestProgressMethod = qPlayerClass.getMethod("getQuestData", String.class);
                } catch (NoSuchMethodException ex) {
                    lastException = ex;
                }
            }
            
            if (getQuestProgressMethod == null) {
                plugin.getLogger().log(Level.WARNING, "无法找到获取任务进度的方法", lastException);
                return false;
            }
            
            Object questProgress = getQuestProgressMethod.invoke(qPlayer, questId);

            if (questProgress == null) {
                plugin.getLogger().log(Level.WARNING, "玩家 " + player.getName() + " 未领取任务: " + questId);
                return false;
            }

            // 2. 调用 progressObjective 方法
            Class<?> questProgressClass = questProgress.getClass();
            
            // 尝试不同的方法名更新进度
            Method progressObjectiveMethod = null;
            lastException = null;
            
            try {
                // 首先尝试 progressObjective 方法
                progressObjectiveMethod = questProgressClass.getMethod("progressObjective", String.class, int.class);
            } catch (NoSuchMethodException e) {
                lastException = e;
                try {
                    // 尝试 addObjective 方法（旧版本）
                    progressObjectiveMethod = questProgressClass.getMethod("addObjective", String.class, int.class);
                } catch (NoSuchMethodException ex) {
                    lastException = ex;
                }
            }
            
            if (progressObjectiveMethod == null) {
                plugin.getLogger().log(Level.WARNING, "无法找到更新任务进度的方法", lastException);
                return false;
            }
            
            progressObjectiveMethod.invoke(questProgress, objectiveName, amount);

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法完成自定义目标 (" + questId + ":" + objectiveName + "): " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 简化版：完成一次自定义目标 (amount = 1)
     */
    public boolean completeCustomObjective(Player player, String questId, String objectiveName) {
        return completeCustomObjective(player, questId, objectiveName, 1);
    }

    // ================== 让玩家领取一个随机任务 ==================

    /**
     * 让玩家领取一个随机任务
     * @param player 玩家
     * @return 是否成功
     */
    public boolean assignRandomQuest(Player player) {
        if (!isQuestsAvailable()) return false;

        Object qPlayer = getQPlayer(player);
        if (qPlayer == null) return false;

        try {
            Class<?> qPlayerClass = qPlayer.getClass();
            
            // 尝试不同的方法名分配随机任务
            Method assignRandomQuestMethod = null;
            Exception lastException = null;
            
            try {
                // 首先尝试 assignRandomQuest 方法
                assignRandomQuestMethod = qPlayerClass.getMethod("assignRandomQuest");
            } catch (NoSuchMethodException e) {
                lastException = e;
                try {
                    // 尝试 giveRandomQuest 方法（旧版本）
                    assignRandomQuestMethod = qPlayerClass.getMethod("giveRandomQuest");
                } catch (NoSuchMethodException ex) {
                    lastException = ex;
                }
            }
            
            if (assignRandomQuestMethod == null) {
                plugin.getLogger().log(Level.WARNING, "无法找到分配随机任务的方法", lastException);
                return false;
            }
            
            assignRandomQuestMethod.invoke(qPlayer);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法分配随机任务: " + e.getMessage(), e);
            return false;
        }
    }

    // ================== 适配你原有接口的方法 ==================

    /**
     * 创建一个Quests任务（实际是触发预定义任务的进度）
     * 注意：你需要在 quests.yml 中预先配置好任务
     * @param player 玩家
     * @param taskType 任务类型（如 MINE_STONE）
     * @param targetAmount 目标数量
     * @param rewardExp 奖励经验（由Quests配置决定）
     * @param rewardMoney 奖励金钱（由Quests配置决定）
     * @return 是否成功
     */
    public boolean createQuest(Player player, String taskType, int targetAmount, int rewardExp, double rewardMoney) {
        if (!isQuestsAvailable()) return false;

        // 这里你需要定义一个映射，将 taskType 映射到 quests.yml 中的任务ID
        String questId = mapTaskTypeToQuestId(taskType);
        if (questId == null) {
            return false;
        }

        // 直接推进进度
        String objectiveName = mapTaskTypeToObjective(taskType);
        return completeCustomObjective(player, questId, objectiveName, targetAmount);
    }

    /**
     * 更新任务进度
     */
    public boolean updateQuestProgress(Player player, String taskType, int progress) {
        if (!isQuestsAvailable()) return false;

        String questId = mapTaskTypeToQuestId(taskType);
        String objectiveName = mapTaskTypeToObjective(taskType);
        if (questId == null || objectiveName == null) {
            return false;
        }

        return completeCustomObjective(player, questId, objectiveName, progress);
    }

    /**
     * 完成任务（由Quests自动判断）
     */
    public boolean completeQuest(Player player, String taskType) {
        // 无需主动调用，Quests 会在目标达成时自动完成
        plugin.getLogger().info("玩家 " + player.getName() + " 的任务 '" + taskType + "' 可能已完成");
        return true;
    }

    // ================== 工具方法：映射 ==================

    /**
     * 将任务类型映射到 quests.yml 中的任务ID
     * 你需要根据你的 quests.yml 配置来修改这个方法
     */
    private String mapTaskTypeToQuestId(String taskType) {
        switch (taskType) {
            case "MINE_STONE":
            case "MINE_IRON":
            case "MINE_DIAMOND":
                return "mining_quest"; // 假设你在 quests.yml 中定义了一个 mining_quest
            case "KILL_ZOMBIE":
            case "KILL_SKELETON":
            case "KILL_CREEPER":
                return "killing_quest";
            case "COLLECT_WHEAT":
            case "HARVEST_CROP":
                return "farming_quest";
            case "FISH_ITEM":
                return "fishing_quest";
            case "ENCHANT_ITEM":
                return "enchanting_quest";
            default:
                return null;
        }
    }

    /**
     * 将任务类型映射到 quests.yml 中的 custom_objective 名称
     */
    private String mapTaskTypeToObjective(String taskType) {
        switch (taskType) {
            case "MINE_STONE": return "mine_stone";
            case "MINE_IRON": return "mine_iron";
            case "MINE_DIAMOND": return "mine_diamond";
            case "KILL_ZOMBIE": return "kill_zombie";
            case "KILL_SKELETON": return "kill_skeleton";
            case "KILL_CREEPER": return "kill_creeper";
            case "COLLECT_WHEAT": return "collect_wheat";
            case "HARVEST_CROP": return "harvest_crop";
            case "FISH_ITEM": return "fish_item";
            case "ENCHANT_ITEM": return "enchant_item";
            default: return null;
        }
    }
}