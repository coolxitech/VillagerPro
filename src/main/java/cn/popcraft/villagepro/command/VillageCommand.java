package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.manager.VillageManager;

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
                        // 使用第二个参数作为目标等级
                        int targetLevel = Integer.parseInt(args[2]);
                        if (targetLevel > 0 && targetLevel <= 5) {
                            upgradeToLevel(player, type, targetLevel);
                        } else {
                            player.sendMessage(messageManager.getMessage("help.invalid-usage"));
                        }
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

        player.sendMessage(messageManager.getMessage("village.info.header"));
        for (UpgradeType type : UpgradeType.values()) {
            int level = village.getUpgradeLevel(type);
            Map<String, String> replacements = new HashMap<>();
            replacements.put("type", messageManager.getMessage("upgrade-types." + type.name()));
            replacements.put("level", String.valueOf(level));
            player.sendMessage(messageManager.getMessage("village.info.upgrade", replacements));
        }
    }
    
    /**
     * 升级村庄到指定等级
     * @param player 玩家
     * @param type 升级类型
     * @param targetLevel 目标等级
     */
    private void upgradeToLevel(Player player, UpgradeType type, int targetLevel) {
        // 获取或创建村庄
        Village village = plugin.getVillageManager().getOrCreateVillage(player);
        
        // 获取当前等级
        int currentLevel = village.getUpgradeLevel(type);
        
        // 检查目标等级是否有效
        if (targetLevel <= currentLevel) {
            player.sendMessage(plugin.getMessageManager().getMessage("upgrade.invalid-level"));
            return;
        }
        
        boolean success = true;
        
        // 逐级升级到目标等级
        for (int level = currentLevel + 1; level <= targetLevel; level++) {
            // 检查是否已达到最高等级
            if (level > 5) {
                player.sendMessage(plugin.getMessageManager().getMessage("upgrade.max-level-reached"));
                break;
            }
            
            cn.popcraft.villagepro.model.Upgrade upgrade = plugin.getConfigManager().getUpgrade(type, level);
            if (upgrade == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("upgrade.failed"));
                success = false;
                break;
            }
            
            // 检查资源是否足够
            VillageManager.ResourceCheckResult checkResult = plugin.getVillageManager().checkUpgradeResources(player, upgrade);
            if (!checkResult.success) {
                player.sendMessage(plugin.getMessageManager().getMessage("upgrade.failed"));
                success = false;
                break;
            }
            
            // 消耗资源
            plugin.getVillageManager().consumeUpgradeResources(player, checkResult);
            
            // 更新升级等级
            village.setUpgradeLevel(type, level);
            
            // 发送升级消息
            Map<String, String> replacements = new HashMap<>();
            replacements.put("type", type.name());  // 直接使用枚举名称作为占位符值
            replacements.put("level", String.valueOf(level));
            player.sendMessage(plugin.getMessageManager().getMessage("upgrade.success", replacements));
        }
        
        if (success) {
            // 保存村庄数据
            plugin.getVillageManager().saveVillage(village);
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
        List<String> helpMessages = messageManager.getMessageList("commands.village.help");
        for (String message : helpMessages) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}