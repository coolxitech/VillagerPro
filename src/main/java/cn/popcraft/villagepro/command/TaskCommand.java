package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaskCommand implements CommandExecutor {
    private final VillagePro plugin;

    public TaskCommand(VillagePro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("该命令只能由玩家执行。");
            return true;
        }

        Player player = (Player) sender;
        
        // 检查权限
        if (!player.hasPermission("villagepro.task") && !player.hasPermission("villagepro.*")) {
            player.sendMessage("你没有权限使用此命令。");
            return true;
        }
        
        // 如果有参数且参数为 help，则显示帮助信息
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }

        // 打开任务GUI
        plugin.getTaskGUI().openTaskGUI(player);
        return true;
    }
    
    private void showHelp(Player player) {
        java.util.List<String> helpMessages = plugin.getMessageManager().getMessageList("commands.task.help");
        for (String message : helpMessages) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}