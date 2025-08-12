package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VillageCommand implements CommandExecutor {
    private final VillagePro plugin;

    public VillageCommand(VillagePro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以使用此命令!");
            return true;
        }

        // 将在后续实现命令处理逻辑
        player.sendMessage("§a村庄系统命令 - 功能开发中");
        return true;
    }
}