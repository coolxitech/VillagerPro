package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import me.pikamug.quests.events.quest.QuestCompleteEvent;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestsEventListener implements Listener {

    private final VillagePro plugin;

    public QuestsEventListener(VillagePro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent event) {
        Quester quester = event.getQuester();
        Quest quest = event.getQuest();
        
        String playerName = quester.getPlayerName();
        String questId = quest.getId();
        String questName = quest.getName();

        // 计算并发放积分
        int points = plugin.calculatePointsForQuest(questName);
        if (points > 0) {
            // 发放积分
            plugin.getTaskManager().addTaskPoints(quester.getUUID(), points);
            
            // 发送消息
            quester.sendMessage("§a恭喜完成任务 §e" + questName + " §a获得 §b" + points + " §a任务积分！");
            plugin.getLogger().info("玩家 " + playerName + " 完成任务 " + questName + "，获得 " + points + " 积分");
        }
    }
}