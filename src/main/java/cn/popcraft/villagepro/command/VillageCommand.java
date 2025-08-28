package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VillageCommand implements CommandExecutor, TabCompleter {
    private final VillagePro plugin;
    private final MessageManager messageManager;
    
    public VillageCommand(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("help.player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // 检查基本权限
        if (!player.hasPermission("villagepro.village") && !player.hasPermission("villagepro.admin")) {
            player.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        // 无参数或无效参数，显示帮助信息
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }
        
        // 处理子命令
        if (args[0].equalsIgnoreCase("create")) {
            if (player.hasPermission("villagepro.village.create") || player.hasPermission("villagepro.admin")) {
                plugin.getVillageManager().getOrCreateVillage(player);
                player.sendMessage(messageManager.getMessage("village.created"));
            } else {
                player.sendMessage(messageManager.getMessage("no-permission"));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("upgrade")) {
            if (player.hasPermission("villagepro.village.upgrade") || player.hasPermission("villagepro.admin")) {
                if (args.length > 2) {
                    try {
                        UpgradeType type = UpgradeType.valueOf(args[1].toUpperCase());
                        // 这里应该使用第二个参数作为等级，但目前插件设计是逐级升级
                        plugin.getVillageManager().upgradeVillage(player, type);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(messageManager.getMessage("help.invalid-usage"));
                    }
                } else if (args.length > 1) {
                    try {
                        UpgradeType type = UpgradeType.valueOf(args[1].toUpperCase());
                        plugin.getVillageManager().upgradeVillage(player, type);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(messageManager.getMessage("help.invalid-usage"));
                    }
                } else {
                    player.sendMessage(messageManager.getMessage("help.invalid-usage"));
                }
            } else {
                player.sendMessage(messageManager.getMessage("no-permission"));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("info")) {
            if (player.hasPermission("villagepro.village.info") || player.hasPermission("villagepro.admin")) {
                showVillageInfo(player);
            } else {
                player.sendMessage(messageManager.getMessage("no-permission"));
            }
            return true;
        }
        
        // 未知命令
        player.sendMessage(messageManager.getMessage("help.invalid-usage"));
        return true;
    }
    
    /**
     * 显示村庄信息
     * @param player 玩家
     */
    private void showVillageInfo(Player player) {
        Village village = plugin.getVillageManager().getVillage(player.getUniqueId());
        
        if (village == null) {
            player.sendMessage(messageManager.getMessage("village.not-found"));
            return;
        }
        
        // 发送村庄信息标题
        player.sendMessage("§a===== 村庄信息 =====");
        
        // 显示村民数量
        int villagerCount = village.getVillagerIds().size();
        player.sendMessage("§e村民数量: §f" + villagerCount);
        
        // 显示升级信息
        player.sendMessage("§e村庄升级:");
        for (Map.Entry<UpgradeType, Integer> entry : village.getUpgradeLevels().entrySet()) {
            UpgradeType type = entry.getKey();
            int level = entry.getValue();
            String typeName = messageManager.getMessage("upgrade-types." + type.name(), new HashMap<>());
            player.sendMessage("  §7- §f" + typeName + ": §e" + level);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 补全子命令
            String subCommand = args[0].toLowerCase();
            if ("create".startsWith(subCommand)) {
                completions.add("create");
            }
            if ("upgrade".startsWith(subCommand)) {
                completions.add("upgrade");
            }
            if ("info".startsWith(subCommand)) {
                completions.add("info");
            }
            if ("help".startsWith(subCommand)) {
                completions.add("help");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("upgrade")) {
            // 补全升级类型
            String upgradeType = args[1].toLowerCase();
            for (UpgradeType type : UpgradeType.values()) {
                if (type.name().toLowerCase().startsWith(upgradeType)) {
                    completions.add(type.name().toLowerCase());
                }
            }
        }
        
        return completions;
    }
    
    /**
     * 发送村庄命令帮助信息
     * @param player 玩家
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage(messageManager.getMessage("help.title"));
        player.sendMessage(messageManager.getMessage("help.page", Map.of("current", "1", "total", "2")));
        
        // 发送村庄相关命令
        player.sendMessage(messageManager.getMessage("help.command-format", Map.of(
            "command", "village create",
            "args", "",
            "description", "创建一个新的村庄")));
        player.sendMessage(messageManager.getMessage("help.command-format", Map.of(
            "command", "village upgrade",
            "args", "<类型>",
            "description", "升级村庄指定类型")));
        player.sendMessage(messageManager.getMessage("help.command-format", Map.of(
            "command", "village info",
            "args", "",
            "description", "显示村庄详细信息")));
        
        // 发送分页信息
        player.sendMessage(messageManager.getMessage("help.page", Map.of("current", "1", "total", "2")));
    }
}