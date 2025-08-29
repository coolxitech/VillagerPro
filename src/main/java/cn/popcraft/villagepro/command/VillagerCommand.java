package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.VillagerProfession;
import cn.popcraft.villagepro.model.ProfessionSkill;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.model.FollowMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class VillagerCommand implements CommandExecutor {
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("该命令只能由玩家执行。");
            return true;
        }

        if (args.length == 0) {
            // 显示帮助信息
            player.sendMessage(messageManager.getMessage("commands.villager.usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "production":
                plugin.getProductionGUI().openMainMenu(player);
                break;
            case "upgrade":
                if (args.length < 2) {
                    player.sendMessage(messageManager.getMessage("commands.villager.usage"));
                    return true;
                }
                try {
                    UpgradeType type = UpgradeType.valueOf(args[1].toUpperCase());
                    plugin.getVillageManager().upgradeVillage(player, type);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(messageManager.getMessage("upgrade.failed"));
                }
                break;
            case "follow":
                plugin.getFollowManager().toggleFollowMode(player);
                break;
            case "info":
                // TODO: 显示村民信息
                player.sendMessage("此功能正在开发中...");
                break;
            case "list":
                // TODO: 列出所有村民
                player.sendMessage("此功能正在开发中...");
                break;
            case "remove":
                // TODO: 移除村民
                player.sendMessage("此功能正在开发中...");
                break;
            default:
                player.sendMessage(messageManager.getMessage("commands.villager.usage"));
                break;
        }

        return true;
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
                "command", "/villager help [页码]"
            )));
        } else if (page == 2) {
            // 第二页：职业和技能信息
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6=== 村民职业与技能 ==="));
            for (VillagerProfession profession : VillagerProfession.values()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + profession.getDisplayName() + ":"));
                for (ProfessionSkill skill : profession.getSkills()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "  &7- &f" + skill.getDisplayName() + " (&e" + skill.name() + "&f) - " + skill.getDescription()));
                }
            }
            
            // 发送分页提示
            player.sendMessage(messageManager.getMessage("help.page-info", Map.of(
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
            plugin.getProductionGUI().openMainMenu(player);
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
            // 查找玩家最近的村民
            Villager nearestVillager = plugin.getVillageManager().findNearestUnrecruitedVillager(player, 5.0);
            if (nearestVillager == null) {
                player.sendMessage(messageManager.getMessage("villager.not-found"));
                return;
            }
            
            // 检查是否是玩家自己的村民
            if (!plugin.getVillagerEntities().containsKey(nearestVillager.getUniqueId())) {
                player.sendMessage(messageManager.getMessage("villager.not-found"));
                return;
            }
            
            // 根据参数设置跟随模式
            FollowMode followMode = FollowMode.FOLLOW; // 默认模式
            if (args.length > 1) {
                try {
                    followMode = FollowMode.valueOf(args[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(messageManager.getMessage("help.invalid-usage"));
                    return;
                }
            }
            
            // 设置村民的跟随模式
            VillagerEntity villagerEntity = plugin.getVillagerEntities().get(nearestVillager.getUniqueId());
            if (villagerEntity != null) {
                villagerEntity.setFollowMode(followMode);
                
                // 发送对应的消息
                String messageKey = "follow." + followMode.name().toLowerCase();
                player.sendMessage(messageManager.getMessage(messageKey));
            }
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
            // 查找玩家最近的村民
            Villager nearestVillager = plugin.getVillageManager().findNearestUnrecruitedVillager(player, 5.0);
            if (nearestVillager == null) {
                player.sendMessage(messageManager.getMessage("villager.not-found"));
                return;
            }
            
            // 检查是否是玩家自己的村民
            if (!plugin.getVillagerEntities().containsKey(nearestVillager.getUniqueId())) {
                player.sendMessage(messageManager.getMessage("villager.not-found"));
                return;
            }
            
            // 显示村民信息
            VillagerEntity villagerEntity = plugin.getVillagerEntities().get(nearestVillager.getUniqueId());
            if (villagerEntity != null) {
                player.sendMessage(messageManager.getMessage("villager.name", Map.of("player", player.getName())));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7职业: &e" + villagerEntity.getProfession().getDisplayName()));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7跟随模式: &e" + villagerEntity.getFollowMode().name()));
            }
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
            // 获取玩家的村庄
            Village village = plugin.getVillageManager().getVillage(player.getUniqueId());
            if (village == null) {
                player.sendMessage(messageManager.getMessage("village.not-found"));
                return;
            }
            
            // 列出所有村民
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6=== 你的村民列表 ==="));
            for (UUID villagerId : village.getVillagerIds()) {
                VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerId);
                if (villagerEntity != null) {
                    Villager villager = villagerEntity.getBukkitEntity();
                    if (villager != null && villager.isValid()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            "&e" + villager.getCustomName() + " &7- 职业: " + villagerEntity.getProfession().getDisplayName()));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            "&c[ID: " + villagerId.toString().substring(0, 8) + "] 村民已离线或不存在"));
                    }
                }
            }
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
                
                // 获取玩家的村庄
                Village village = plugin.getVillageManager().getVillage(player.getUniqueId());
                if (village == null || !village.getVillagerIds().contains(villagerId)) {
                    player.sendMessage(messageManager.getMessage("villager.not-found"));
                    return;
                }
                
                // 移除村民
                plugin.getVillageManager().removeVillager(player, villagerId);
            } catch (IllegalArgumentException e) {
                player.sendMessage(messageManager.getMessage("villager.not-found"));
            }
        }
    }
}