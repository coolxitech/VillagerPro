package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.gui.UpgradeGUI;
import cn.popcraft.villagepro.gui.ProductionGUI;
import cn.popcraft.villagepro.model.UpgradeType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 村民命令处理器
 */
public class VillagerCommand implements CommandExecutor, TabCompleter {
    private final VillagePro plugin;
    
    public VillagerCommand(VillagePro plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + "此命令只能由玩家执行!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // 显示帮助信息
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "recruit":
                // 招募村民
                recruitVillager(player);
                break;
            case "upgrade":
                // 升级村民
                upgradeVillager(player);
                break;
            case "production":
            case "产出":
                // 村民产出
                openProductionGUI(player);
                break;
            case "task":
                // 村民任务
                handleTask(player);
                break;
            default:
                showHelp(player);
                break;
        }
        return true;
    }

    private void recruitVillager(Player player) {
        // 查找最近的未招募村民
        Villager villager = plugin.getVillageManager().findNearestUnrecruitedVillager(player, 5);
        if (villager == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
            return;
        }

        // 尝试招募村民
        if (plugin.getVillageManager().recruitVillager(player, villager)) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.success"));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
        }
    }

    private void upgradeVillager(Player player) {
        // 打开升级GUI
        UpgradeGUI gui = new UpgradeGUI(plugin);
        gui.openMainMenu(player);
    }
    
    private void openProductionGUI(Player player) {
        // 打开产出GUI
        ProductionGUI gui = new ProductionGUI(plugin);
        gui.openMainMenu(player);
    }

    private void handleTask(Player player) {
        // 处理村民任务
        player.sendMessage(plugin.getMessageManager().getMessage("task.pending"));
    }

    private void showHelp(Player player) {
        // 显示帮助信息
        player.sendMessage("§a===== 村民命令帮助 =====");
        player.sendMessage("§a/villager recruit - 招募村民");
        player.sendMessage("§a/villager upgrade - 升级村民");
        player.sendMessage("§a/villager production - 获取村民产出");
        player.sendMessage("§a/villager task - 村民任务");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("recruit", "upgrade", "production", "task");
        }
        return new ArrayList<>();
    }
}