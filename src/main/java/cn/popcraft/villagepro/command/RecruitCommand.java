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
    private final FollowManager followManager;

    public RecruitCommand(VillagePro plugin) {
        this.plugin = plugin;
        this.followManager = plugin.getFollowManager(); // 直接缓存，避免每次 get 调用
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.player-only"));
            return true;
        }

        // 权限检查（任意一个满足即通过）
        if (!player.hasPermission("villagepro.recruit") && !player.hasPermission("villagepro.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }
        
        // 如果有参数且参数为 help，则显示帮助信息
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }

        // 查找最近的未招募村民
        Villager target = plugin.getVillageManager().findNearestUnrecruitedVillager(player, 5);
        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
            return true;
        }

        // 真正的业务交给 VillageManager 完成（包括消息发送）
        boolean success = plugin.getVillageManager().recruitVillager(player, target);
        if (success) {
            // 成功后询问是否跟随
            followManager.requestFollow(player, target);
        }
        // 若失败，错误信息已在 recruitVillager 中发送
        return true;
    }
    
    private void showHelp(Player player) {
        java.util.List<String> helpMessages = plugin.getMessageManager().getMessageList("commands.recruit.help");
        for (String message : helpMessages) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
