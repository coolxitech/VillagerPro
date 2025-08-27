package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.VillagerProfession;
import cn.popcraft.villagepro.model.ProfessionSkill;
import cn.popcraft.villagepro.model.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VillagerCommand implements CommandExecutor, TabCompleter {
    private final VillagePro plugin;
    private final MessageManager messageManager;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public VillagerCommand(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        
        // 初始化子命令
        subCommands.add(new ProductionSubCommand(plugin));
        subCommands.add(new UpgradeSubCommand(plugin));
        subCommands.add(new FollowSubCommand(plugin));
        subCommands.add(new InfoSubCommand(plugin));
        subCommands.add(new ListSubCommand(plugin));
        subCommands.add(new RemoveSubCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("help.player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // 无参数或无效参数，显示帮助信息
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player, args);
            return true;
        }
        
        // 查找并执行子命令
        for (SubCommand subCommand : subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName())) {
                if (player.hasPermission("villagerpro." + subCommand.getName()) || player.hasPermission("villagerpro.admin")) {
                    subCommand.execute(player, args);
                    return true;
                } else {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
            }
        }
        
        // 未知命令
        player.sendMessage(messageManager.getMessage("help.invalid-usage"));
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 补全子命令
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand.getName());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("upgrade")) {
            // 补全升级类型
            for (UpgradeType type : UpgradeType.values()) {
                if (type.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(type.name().toLowerCase());
                }
            }
        }
        
        return completions;
    }
    
    /**
     * 发送村民相关帮助信息
     * @param player 玩家
     * @param args 命令参数
     */
    private void sendHelpMessage(Player player, String[] args) {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(messageManager.getMessage("help.invalid-usage"));
                return;
            }
        }
        
        // 发送帮助标题
        player.sendMessage(messageManager.getMessage("help.title"));
        player.sendMessage(messageManager.getMessage("help.page", Map.of("current", String.valueOf(page), "total", "2")));
        
        // 分页显示帮助信息
        if (page == 1) {
            // 第一页：基本命令
            for (SubCommand subCommand : subCommands) {
                player.sendMessage(messageManager.getMessage("help.command-format", Map.of(
                    "command", "villager " + subCommand.getName(),
                    "args", subCommand.getUsage(),
                    "description", subCommand.getDescription()
                )));
            }
            
            // 发送分页提示
            player.sendMessage(messageManager.getMessage("help.page-info", Map.of(
                "current", "1",
                "total", "2",
                "command", "/villager help [页码]"
            )));
        } else if (page == 2) {
            // 第二页：职业和技能信息
            player.sendMessage(ChatColor.GOLD + "=== 村民职业与技能 ===");
            for (VillagerProfession profession : VillagerProfession.values()) {
                player.sendMessage(ChatColor.YELLOW + profession.getDisplayName() + ":");
                for (ProfessionSkill skill : profession.getSkills()) {
                    player.sendMessage("  " + ChatColor.GRAY + "- " + ChatColor.WHITE + skill.getDisplayName() + " (" + ChatColor.YELLOW + skill.name() + ChatColor.WHITE + ") - " + skill.getDescription());
                }
            }
            
            // 发送分页提示
            player.sendMessage(messageManager.getMessage("help.page-info", Map.of(
                "current", "2",
                "total", "2",
                "command", "/villager help [页码]"
            )));
        } else {
            player.sendMessage(messageManager.getMessage("help.page-not-found", Map.of("page", String.valueOf(page), "total", "2")));
            return;
        }
    }
    
    // 子命令接口
    private interface SubCommand {
        String getName();
        String getUsage();
        String getDescription();
        void execute(Player player, String[] args);
    }
    
    // 产出子命令
    private class ProductionSubCommand implements SubCommand {
        private final VillagePro plugin;
        
        public ProductionSubCommand(VillagePro plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public String getName() {
            return "production";
        }
        
        @Override
        public String getUsage() {
            return "";
        }
        
        @Override
        public String getDescription() {
            return "打开村民产出界面";
        }
        
        @Override
        public void execute(Player player, String[] args) {
            // TODO: 实现产出GUI功能
            player.sendMessage("产出功能尚未实现");
        }
    }
    
    // 升级子命令
    private class UpgradeSubCommand implements SubCommand {
        private final VillagePro plugin;
        
        public UpgradeSubCommand(VillagePro plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public String getName() {
            return "upgrade";
        }
        
        @Override
        public String getUsage() {
            return "<type>";
        }
        
        @Override
        public String getDescription() {
            return "升级村民能力";
        }
        
        @Override
        public void execute(Player player, String[] args) {
            if (args.length < 2) {
                player.sendMessage(messageManager.getMessage("help.command-format", Map.of(
                    "command", "villager upgrade",
                    "args", "<type>",
                    "description", "升级村民能力"
                )));
                return;
            }
            
            try {
                UpgradeType type = UpgradeType.valueOf(args[1].toUpperCase());
                plugin.getVillageManager().upgradeVillage(player, type);
            } catch (IllegalArgumentException e) {
                player.sendMessage(messageManager.getMessage("upgrade.failed"));
            }
        }
    }
    
    // 跟随子命令
    private class FollowSubCommand implements SubCommand {
        private final VillagePro plugin;
        
        public FollowSubCommand(VillagePro plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public String getName() {
            return "follow";
        }
        
        @Override
        public String getUsage() {
            return "[mode]";
        }
        
        @Override
        public String getDescription() {
            return "切换村民跟随模式";
        }
        
        @Override
        public void execute(Player player, String[] args) {
            // TODO: 实现跟随功能
            player.sendMessage("跟随功能尚未实现");
        }
    }
    
    // 信息子命令
    private class InfoSubCommand implements SubCommand {
        private final VillagePro plugin;
        
        public InfoSubCommand(VillagePro plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public String getName() {
            return "info";
        }
        
        @Override
        public String getUsage() {
            return "";
        }
        
        @Override
        public String getDescription() {
            return "显示村民详细信息";
        }
        
        @Override
        public void execute(Player player, String[] args) {
            // TODO: 实现村民信息功能
            player.sendMessage("村民信息功能尚未实现");
        }
    }
    
    // 列表子命令
    private class ListSubCommand implements SubCommand {
        private final VillagePro plugin;
        
        public ListSubCommand(VillagePro plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public String getName() {
            return "list";
        }
        
        @Override
        public String getUsage() {
            return "";
        }
        
        @Override
        public String getDescription() {
            return "列出所有村民";
        }
        
        @Override
        public void execute(Player player, String[] args) {
            // TODO: 实现村民列表功能
            player.sendMessage("村民列表功能尚未实现");
        }
    }
    
    // 移除子命令
    private class RemoveSubCommand implements SubCommand {
        private final VillagePro plugin;
        
        public RemoveSubCommand(VillagePro plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public String getName() {
            return "remove";
        }
        
        @Override
        public String getUsage() {
            return "<id>";
        }
        
        @Override
        public String getDescription() {
            return "移除村民";
        }
        
        @Override
        public void execute(Player player, String[] args) {
            if (args.length < 2) {
                player.sendMessage(messageManager.getMessage("help.command-format", Map.of(
                    "command", "villager remove",
                    "args", "<id>",
                    "description", "移除指定村民"
                )));
                return;
            }
            
            try {
                UUID villagerId = UUID.fromString(args[1]);
                // TODO: 实现移除村民功能
                player.sendMessage("移除村民功能尚未实现");
            } catch (IllegalArgumentException e) {
                player.sendMessage(messageManager.getMessage("villager.not-found"));
            }
        }
    }
}