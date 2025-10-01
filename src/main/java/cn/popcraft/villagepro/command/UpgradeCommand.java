package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.gui.UpgradeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 升级命令执行器
 */
public class UpgradeCommand implements CommandExecutor {
    private final VillagePro plugin;

    public UpgradeCommand(VillagePro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("此命令只能由玩家执行");
            return true;
        }
        
        // 如果有参数且参数为 help，则显示帮助信息
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }

        // 打开升级GUI
        UpgradeGUI gui = new UpgradeGUI(plugin);
        gui.openMainMenu(player);
        return true;
    }
    
    private void showHelp(Player player) {
        java.util.List<String> helpMessages = plugin.getMessageManager().getMessageList("commands.upgrade.help");
        for (String message : helpMessages) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}