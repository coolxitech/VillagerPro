package cn.popcraft.villagepro.event;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.model.Task;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VillageProTaskListener implements Listener {
    private final VillagePro plugin;
    private final TaskManager taskManager;

    public VillageProTaskListener(VillagePro plugin, TaskManager taskManager) {
        this.plugin = plugin;
        this.taskManager = taskManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        Task task = taskManager.getCurrentTask(player);
        if (task == null) {
            return;
        }

        ItemStack item = event.getItem();
        switch (task.getType()) {
            case COLLECT_WHEAT:
                if (item != null && item.getType() == Material.WHEAT) {
                    task.setProgress(task.getProgress() + 1);
                }
                break;
            case BAKE_BREAD:
                if (item != null && item.getType() == Material.BREAD) {
                    task.setProgress(task.getProgress() + 1);
                }
                break;
            default:
                return;
        }

        // if (taskManager.checkTaskCompletion(player)) {
        //     taskManager.rewardPlayer(player);
        // }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            // 检查是否完成击杀任务
            // taskManager.updateTaskProgress(killer.getUniqueId(), Task.TaskType.KILL_ZOMBIE, 1);
            
            // 检查任务是否完成
            // if (taskManager.checkTaskCompletion(killer)) {
            //     taskManager.rewardPlayer(killer);
            // }
        }
    }
}