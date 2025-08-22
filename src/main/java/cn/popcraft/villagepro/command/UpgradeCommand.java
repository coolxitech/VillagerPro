package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.gui.UpgradeGUI;
import cn.popcraft.villagepro.model.Village;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UpgradeCommand implements CommandExecutor {
    private final VillagePro plugin;

    public UpgradeCommand(VillagePro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        // 打开升级GUI
        UpgradeGUI gui = new UpgradeGUI(plugin, player, plugin.getVillageManager().getOrCreateVillage(player));
        gui.openMainMenu(player);

        return true;
    }
}