package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.CropStorage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CropCommand implements CommandExecutor, TabCompleter {
    private final VillagePro plugin;
    
    public CropCommand(VillagePro plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.player-only"));
            return true;
        }
        
        if (!player.hasPermission("villagepro.crop") && !player.hasPermission("villagepro.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            showCropList(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                showCropList(player);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "用法: /crop info <作物类型>");
                    return true;
                }
                showCropInfo(player, args[1]);
                break;
            case "harvest":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "用法: /crop harvest <作物类型> [数量]");
                    return true;
                }
                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                        if (amount <= 0) {
                            player.sendMessage(ChatColor.RED + "数量必须大于0！");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "无效的数量: " + args[2]);
                        return true;
                    }
                }
                harvestCrop(player, args[1], amount);
                break;
            case "store":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "用法: /crop store <作物类型> <数量>");
                    return true;
                }
                try {
                    int storeAmount = Integer.parseInt(args[2]);
                    if (storeAmount <= 0) {
                        player.sendMessage(ChatColor.RED + "数量必须大于0！");
                        return true;
                    }
                    storeCrop(player, args[1], storeAmount);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "无效的数量: " + args[2]);
                }
                break;
            case "balance":
                showCropList(player);
                break;
            case "withdraw":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "用法: /crop withdraw <作物类型> [数量]");
                    return true;
                }
                int withdrawAmount = 1;
                if (args.length >= 3) {
                    try {
                        withdrawAmount = Integer.parseInt(args[2]);
                        if (withdrawAmount <= 0) {
                            player.sendMessage(ChatColor.RED + "数量必须大于0！");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "无效的数量: " + args[2]);
                        return true;
                    }
                }
                harvestCrop(player, args[1], withdrawAmount); // withdraw与harvest功能相同
                break;
            case "deposit":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "用法: /crop deposit <作物类型> <数量>");
                    return true;
                }
                try {
                    int depositAmount = Integer.parseInt(args[2]);
                    if (depositAmount <= 0) {
                        player.sendMessage(ChatColor.RED + "数量必须大于0！");
                        return true;
                    }
                    storeCrop(player, args[1], depositAmount); // deposit与store功能相同
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "无效的数量: " + args[2]);
                }
                break;
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showCropList(Player player) {
        CropStorage storage = getPlayerCropStorage(player);
        Map<String, Integer> crops = storage.getCrops();
        
        if (crops.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "你还没有任何作物存储。");
            return;
        }
        
        player.sendMessage(ChatColor.GREEN + "=== 你的作物存储 ===");
        int totalCrops = 0;
        
        for (Map.Entry<String, Integer> entry : crops.entrySet()) {
            String displayName = getCropDisplayName(entry.getKey());
            int amount = entry.getValue();
            totalCrops += amount;
            
            player.sendMessage(ChatColor.WHITE + displayName + ": " + 
                             ChatColor.YELLOW + amount + ChatColor.GRAY + " 个");
        }
        
        player.sendMessage(ChatColor.GREEN + "总计: " + ChatColor.YELLOW + totalCrops + ChatColor.GREEN + " 个作物");
    }
    
    private void showCropInfo(Player player, String cropType) {
        if (!isValidCropType(cropType)) {
            player.sendMessage(ChatColor.RED + "无效的作物类型: " + cropType);
            return;
        }
        
        CropStorage storage = getPlayerCropStorage(player);
        int amount = storage.getCropAmount(cropType);
        String displayName = getCropDisplayName(cropType);
        
        player.sendMessage(ChatColor.GREEN + "=== 作物信息 ===");
        player.sendMessage(ChatColor.WHITE + "作物类型: " + ChatColor.YELLOW + displayName);
        player.sendMessage(ChatColor.WHITE + "存储数量: " + ChatColor.YELLOW + amount + ChatColor.GRAY + " 个");
    }
    
    private void harvestCrop(Player player, String cropType, int amount) {
        if (!isValidCropType(cropType)) {
            player.sendMessage(ChatColor.RED + "无效的作物类型: " + cropType);
            return;
        }
        
        String displayName = getCropDisplayName(cropType);
        
        if (plugin.getCropManager().addCrop(player.getUniqueId(), cropType, amount)) {
            player.sendMessage(ChatColor.GREEN + "成功收获 " + ChatColor.YELLOW + amount + 
                             ChatColor.GREEN + " 个 " + displayName + "!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } else {
            player.sendMessage(ChatColor.RED + "收获失败！");
        }
    }
    
    private void storeCrop(Player player, String cropType, int amount) {
        if (!isValidCropType(cropType)) {
            player.sendMessage(ChatColor.RED + "无效的作物类型: " + cropType);
            return;
        }
        
        String displayName = getCropDisplayName(cropType);
        
        if (plugin.getCropManager().addCrop(player.getUniqueId(), cropType, amount)) {
            player.sendMessage(ChatColor.GREEN + "成功存储 " + ChatColor.YELLOW + amount + 
                             ChatColor.GREEN + " 个 " + displayName + "!");
        } else {
            player.sendMessage(ChatColor.RED + "存储失败！");
        }
    }
    
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "=== 作物命令帮助 ===");
        player.sendMessage(ChatColor.WHITE + "/crop list - 显示作物列表");
        player.sendMessage(ChatColor.WHITE + "/crop info <作物类型> - 显示作物信息");
        player.sendMessage(ChatColor.WHITE + "/crop harvest <作物类型> [数量] - 收获作物");
        player.sendMessage(ChatColor.WHITE + "/crop store <作物类型> <数量> - 存储作物");
        player.sendMessage(ChatColor.WHITE + "/crop balance - 查看作物余额");
        player.sendMessage(ChatColor.WHITE + "/crop withdraw <作物类型> [数量] - 取出作物");
        player.sendMessage(ChatColor.WHITE + "/crop deposit <作物类型> <数量> - 存入作物");
        player.sendMessage(ChatColor.WHITE + "/crop help - 显示此帮助信息");
    }
    
    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("list", "info", "harvest", "store", "balance", "withdraw", "deposit", "help");
            String finalArg = args[0].toLowerCase();
            return subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(finalArg))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("info") || subCommand.equals("harvest") || subCommand.equals("store") 
                || subCommand.equals("withdraw") || subCommand.equals("deposit")) {
                List<String> cropTypes = getCropTypes();
                String finalArg = args[1].toLowerCase();
                return cropTypes.stream()
                        .filter(crop -> crop.toLowerCase().startsWith(finalArg))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("harvest") || subCommand.equals("store") 
                || subCommand.equals("withdraw") || subCommand.equals("deposit")) {
                return Arrays.asList("1", "8", "16", "32", "64");
            }
        }
        
        return completions;
    }
    
    private List<String> getCropTypes() {
        return plugin.getCropManager().getAvailableCropTypes();
    }
    
    private CropStorage getPlayerCropStorage(Player player) {
        return plugin.getCropManager().getPlayerCropStorage(player.getUniqueId());
    }
    
    private boolean isValidCropType(String cropType) {
        return getCropTypes().contains(cropType.toLowerCase());
    }
    
    private String getCropDisplayName(String cropType) {
        return plugin.getCropManager().getCropDisplayName(cropType);
    }
}
