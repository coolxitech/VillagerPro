package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Quests插件集成管理器 (适配 PikaMug/Quests)
 * 用于在检测到Quests插件时，使用Quests的任务系统替代内置任务系统
 * 
 * 注意：实际的Quests事件监听已移至QuestsEventListener类中处理
 */
public class QuestsIntegrationManager {
    private final VillagePro plugin;
    private Plugin questsPlugin;
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

    /**
     * 以下方法已废弃，因为实际的Quests集成已移至QuestsEventListener类中
     * 保留这些方法是为了保持向后兼容性
     */
    public boolean completeCustomObjective(Player player, String questId, String objectiveName, int amount) {
        // 不再需要实现，因为使用了事件监听器方式
        return false;
    }

    public boolean completeCustomObjective(Player player, String questId, String objectiveName) {
        // 不再需要实现，因为使用了事件监听器方式
        return false;
    }

    public boolean assignRandomQuest(Player player) {
        // 不再需要实现，因为使用了事件监听器方式
        return false;
    }

    public boolean createQuest(Player player, String taskType, int targetAmount, int rewardExp, double rewardMoney) {
        // 不再需要实现，因为使用了事件监听器方式
        return false;
    }

    public boolean updateQuestProgress(Player player, String taskType, int progress) {
        // 不再需要实现，因为使用了事件监听器方式
        return false;
    }

    public boolean completeQuest(Player player, String taskType) {
        // 不再需要实现，因为使用了事件监听器方式
        return false;
    }
}