package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.manager.VillageManager;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VillageCommand implements CommandExecutor, TabCompleter {
    private final VillagePro plugin;
    private final MessageManager messageManager;
    private final VillageManager villageManager;
    
    public VillageCommand(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.villageManager = plugin.getVillageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("该命令只能由玩家执行!");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player, 1);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (!player.hasPermission("villagepro.village.create")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                
                // 创建村庄
                Village village = villageManager.getOrCreateVillage(player);
                
                // 发送成功消息
                player.sendMessage(messageManager.getMessage("village.created"));
                return true;
                
            case "info":
                if (!player.hasPermission("villagepro.village.info")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                
                // 显示村庄信息
                showVillageInfo(player);
                return true;
                
            case "upgrade":
                if (!player.hasPermission("villagepro.village.upgrade")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                
                // 升级村庄
                if (args.length < 2) {
                    player.sendMessage(messageManager.getMessage("help.command-format", Map.of("command", "upgrade", "args", "<level>", "description", "升级村庄")));
                    return true;
                }
                
                try {
                    int level = Integer.parseInt(args[1]);
                    Village existingVillage = villageManager.getVillage(player.getUniqueId());
                    if (existingVillage == null) {
                        player.sendMessage(messageManager.getMessage("village.not-found"));
                        return true;
                    }
                    
                    // 执行升级逻辑
                    // TODO: 实际升级逻辑
                    player.sendMessage(messageManager.getMessage("village.upgraded", Map.of("level", String.valueOf(level))));
                } catch (NumberFormatException e) {
                    player.sendMessage(messageManager.getMessage("errors.invalid-upgrade-level"));
                }
                
                return true;
                
            case "help":
                sendHelpMessage(player, 1);
                return true;
                
            default:
                player.sendMessage(messageManager.getMessage("errors.invalid-subcommand", Map.of("command", args[0])));
                sendHelpMessage(player, 1);
                return true;
        }
    }
    
    private void showVillageInfo(Player player) {
        Village village = villageManager.getVillage(player.getUniqueId());
        if (village == null) {
            player.sendMessage(messageManager.getMessage("village.not-found"));
            return;
        }
        
        player.sendMessage("-------------------------");
        player.sendMessage("村庄信息:");
        player.sendMessage("村民数量: " + village.getVillagerIds().size());
        player.sendMessage("村庄升级:");
        
        for (Map.Entry<UpgradeType, Integer> entry : village.getUpgradeLevels().entrySet()) {
            UpgradeType upgradeType = entry.getKey();
            int level = entry.getValue();
            String upgradeName = upgradeType.name();
            
            // 尝试从消息配置中获取升级类型名称
            String localizedUpgradeName = messageManager.getMessage("upgrade-types." + upgradeName);
            // 如果消息配置中没有定义，则使用枚举名称
            if (localizedUpgradeName.startsWith("&c未找到消息:")) {
                localizedUpgradeName = upgradeName;
            }
            
            player.sendMessage("  " + localizedUpgradeName + ": " + level);
        }
        
        player.sendMessage("-------------------------");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String lowerCaseArg = args[0].toLowerCase();
            
            completions.add("create");
            completions.add("info");
            completions.add("upgrade");
            completions.add("help");
            
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerCaseArg))
                .toList();
        } else if (args.length == 2 && args[0].equalsIgnoreCase("upgrade")) {
            // 为升级命令提供等级补全
            List<String> levelCompletions = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {  // 假设最大等级为10
                levelCompletions.add(String.valueOf(i));
            }
            return levelCompletions;
        }
        
        return Collections.emptyList();
    }
    
    private void sendHelpMessage(Player player, int page) {
        MessageManager messageManager = plugin.getMessageManager();
        if (messageManager == null) {
            player.sendMessage(org.bukkit.ChatColor.RED + "无法显示帮助信息: 消息管理器未初始化");
            return;
        }
        
        player.sendMessage(messageManager.getMessage("help.title"));
        player.sendMessage(messageManager.getMessage("help.page", Map.of("current", "1", "total", "1")));
        player.sendMessage(messageManager.getMessage("help.command-format", Map.of("command", "create", "args", "", "description", "创建一个新的村庄")));
        player.sendMessage(messageManager.getMessage("help.command-format", Map.of("command", "info", "args", "", "description", "查看你的村庄信息")));
        player.sendMessage(messageManager.getMessage("help.command-format", Map.of("command", "help", "args", "[页码]", "description", "显示帮助信息")));
    }
    
    // 添加新的sendHelpMessage方法以支持无页码参数的调用
    private void sendHelpMessage(Player player) {
        sendHelpMessage(player, 1);
    }
}