package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.FollowManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

public class RecruitCommand implements CommandExecutor {
    private final VillagePro plugin;

    public RecruitCommand(VillagePro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        // 查找最近的未招募村民
        Villager villager = plugin.getVillageManager().findNearestUnrecruitedVillager(player, 5);
        if (villager == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
            return true;
        }

        // 尝试招募村民
        if (plugin.getVillageManager().recruitVillager(player, villager)) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.success"));
            
            // 询问是否跟随
            plugin.getFollowManager().requestFollow(player, villager);
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
        }

        return true;
    }
}