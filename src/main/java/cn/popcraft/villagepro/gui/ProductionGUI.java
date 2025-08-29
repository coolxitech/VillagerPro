package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.VillagerProfession;
import cn.popcraft.villagepro.model.CropStorage;
import cn.popcraft.villagepro.manager.CropManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 村民产出GUI，用于获取村民生产的物品
 */
public class ProductionGUI {
    private final VillagePro plugin;
    private final MessageManager messageManager;
    private final Random random = new Random();
    
    public ProductionGUI(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }
    
    /**
     * 打开村民产出主菜单
     * @param player 玩家
     */
    public void openMainMenu(Player player) {
        // 检查消息管理器是否已正确初始化
        if (messageManager == null) {
            player.sendMessage(ChatColor.RED + "插件消息系统未正确初始化，请联系服务器管理员");
            return;
        }
        
        Map<String, String> replacements = new HashMap<>();
        String title = messageManager.getMessage("gui.production-title", replacements);
        Inventory inventory = Bukkit.createInventory(null, 27, title);
        
        // 添加不同类型村民的产出按钮
        addVillagerProductionButton(inventory, 10, VillagerProfession.FARMER, Material.WHEAT, "农民");
        addVillagerProductionButton(inventory, 11, VillagerProfession.FISHERMAN, Material.FISHING_ROD, "渔夫");
        addVillagerProductionButton(inventory, 12, VillagerProfession.SHEPHERD, Material.WHITE_WOOL, "牧羊人");
        addVillagerProductionButton(inventory, 13, VillagerProfession.FLETCHER, Material.ARROW, "制箭师");
        addVillagerProductionButton(inventory, 14, VillagerProfession.LIBRARIAN, Material.BOOK, "图书管理员");
        addVillagerProductionButton(inventory, 15, VillagerProfession.CLERIC, Material.POTION, "祭司");
        addVillagerProductionButton(inventory, 16, VillagerProfession.CARTOGRAPHER, Material.MAP, "制图师");
        
        // 添加关闭按钮
        addCloseButton(inventory, 26);
        
        player.openInventory(inventory);
    }
    
    /**
     * 添加村民产出按钮
     */
    private void addVillagerProductionButton(Inventory inventory, int slot, VillagerProfession profession, Material material, String displayName) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        
        // 设置按钮名称
        meta.setDisplayName("§6" + displayName);
        
        // 设置按钮描述
        List<String> lore = new ArrayList<>();
        lore.add("§7点击获取该类型村民的产出");
        lore.add("");
        lore.add(messageManager.getMessage("gui.click-to-interact", new HashMap<>()));
        meta.setLore(lore);
        
        button.setItemMeta(meta);
        inventory.setItem(slot, button);
    }
    
    /**
     * 打开特定村民职业的产出菜单
     * @param player 玩家
     * @param profession 村民职业
     */
    public void openProfessionProductionMenu(Player player, VillagerProfession profession) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("profession", profession.getDisplayName());
        String title = messageManager.getMessage("gui.profession-production-title", replacements);
        Inventory inventory = Bukkit.createInventory(null, 54, title); // 使用更大的GUI
        
        // 根据职业类型添加产出物品
        switch (profession) {
            case FARMER:
                // 农民产出
                addProductionItem(inventory, 10, Material.WHEAT, "小麦", player);
                addProductionItem(inventory, 11, Material.CARROT, "胡萝卜", player);
                addProductionItem(inventory, 12, Material.POTATO, "马铃薯", player);
                addProductionItem(inventory, 13, Material.BEETROOT, "甜菜根", player);
                addProductionItem(inventory, 14, Material.PUMPKIN, "南瓜", player);
                addProductionItem(inventory, 15, Material.MELON_SLICE, "西瓜片", player);
                addProductionItem(inventory, 16, Material.APPLE, "苹果", player);
                break;
            case FISHERMAN:
                // 渔夫产出
                addProductionItem(inventory, 10, Material.COD, "鳕鱼", player);
                addProductionItem(inventory, 11, Material.SALMON, "鲑鱼", player);
                addProductionItem(inventory, 12, Material.TROPICAL_FISH, "热带鱼", player);
                addProductionItem(inventory, 13, Material.PUFFERFISH, "河豚", player);
                addProductionItem(inventory, 14, Material.STRING, "线", player);
                addProductionItem(inventory, 15, Material.STICK, "木棍", player);
                break;
            case SHEPHERD:
                // 牧羊人产出
                addProductionItem(inventory, 10, Material.WHITE_WOOL, "羊毛", player);
                addProductionItem(inventory, 11, Material.MUTTON, "羊肉", player);
                addProductionItem(inventory, 12, Material.LEATHER, "皮革", player);
                addProductionItem(inventory, 13, Material.BONE, "骨头", player);
                addProductionItem(inventory, 14, Material.ROTTEN_FLESH, "腐肉", player);
                break;
            case FLETCHER:
                // 制箭师产出
                addProductionItem(inventory, 10, Material.ARROW, "箭", player);
                addProductionItem(inventory, 11, Material.FEATHER, "羽毛", player);
                addProductionItem(inventory, 12, Material.FLINT, "燧石", player);
                addProductionItem(inventory, 13, Material.STICK, "木棍", player);
                addProductionItem(inventory, 14, Material.STRING, "线", player);
                break;
            case LIBRARIAN:
                // 图书管理员产出
                addProductionItem(inventory, 10, Material.BOOK, "书", player);
                addProductionItem(inventory, 11, Material.ENCHANTED_BOOK, "附魔书", player);
                addProductionItem(inventory, 12, Material.PAPER, "纸", player);
                addProductionItem(inventory, 13, Material.BOOKSHELF, "书架", player);
                addProductionItem(inventory, 14, Material.GLASS, "玻璃", player);
                break;
            case CLERIC:
                // 祭司产出
                addProductionItem(inventory, 10, Material.POTION, "药水", player);
                addProductionItem(inventory, 11, Material.EXPERIENCE_BOTTLE, "经验瓶", player);
                addProductionItem(inventory, 12, Material.REDSTONE, "红石", player);
                addProductionItem(inventory, 13, Material.LAPIS_LAZULI, "青金石", player);
                addProductionItem(inventory, 14, Material.GLOWSTONE_DUST, "荧石粉", player);
                break;
            case CARTOGRAPHER:
                // 制图师产出
                addProductionItem(inventory, 10, Material.MAP, "地图", player);
                addProductionItem(inventory, 11, Material.COMPASS, "指南针", player);
                addProductionItem(inventory, 12, Material.PAPER, "纸", player);
                addProductionItem(inventory, 13, Material.GLASS_PANE, "玻璃板", player);
                break;
            case WEAPONSMITH:
                // 武器匠产出
                addProductionItem(inventory, 10, Material.IRON_SWORD, "铁剑", player);
                addProductionItem(inventory, 11, Material.DIAMOND_SWORD, "钻石剑", player);
                addProductionItem(inventory, 12, Material.BOW, "弓", player);
                addProductionItem(inventory, 13, Material.ARROW, "箭", player);
                addProductionItem(inventory, 14, Material.IRON_INGOT, "铁锭", player);
                break;
            case ARMORER:
                // 盔甲匠产出
                addProductionItem(inventory, 10, Material.IRON_HELMET, "铁头盔", player);
                addProductionItem(inventory, 11, Material.IRON_CHESTPLATE, "铁胸甲", player);
                addProductionItem(inventory, 12, Material.IRON_LEGGINGS, "铁护腿", player);
                addProductionItem(inventory, 13, Material.IRON_BOOTS, "铁靴子", player);
                addProductionItem(inventory, 14, Material.IRON_INGOT, "铁锭", player);
                break;
            case TOOLSMITH:
                // 工具匠产出
                addProductionItem(inventory, 10, Material.IRON_PICKAXE, "铁镐", player);
                addProductionItem(inventory, 11, Material.IRON_AXE, "铁斧", player);
                addProductionItem(inventory, 12, Material.IRON_SHOVEL, "铁锹", player);
                addProductionItem(inventory, 13, Material.IRON_HOE, "铁锄", player);
                addProductionItem(inventory, 14, Material.IRON_INGOT, "铁锭", player);
                break;
            case BUTCHER:
                // 皮匠产出
                addProductionItem(inventory, 10, Material.COOKED_BEEF, "牛排", player);
                addProductionItem(inventory, 11, Material.COOKED_PORKCHOP, "烤猪排", player);
                addProductionItem(inventory, 12, Material.COOKED_CHICKEN, "熟鸡肉", player);
                addProductionItem(inventory, 13, Material.COOKED_MUTTON, "熟羊肉", player);
                addProductionItem(inventory, 14, Material.LEATHER, "皮革", player);
                break;
            default:
                // 默认产出
                addProductionItem(inventory, 13, Material.BREAD, "面包", player);
                break;
        }
        
        // 添加返回按钮
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(messageManager.getMessage("gui.back", new HashMap<>()));
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton); // 放在底部中央
        
        player.openInventory(inventory);
    }
    
    /**
     * 添加产出物品
     */
    private void addProductionItem(Inventory inventory, int slot, Material material, String displayName, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7点击获取产出");
        // 显示玩家当前存储的该物品数量
        int storedAmount = plugin.getCropManager().getCropStorage(player.getUniqueId()).getCropAmount(material.name());
        lore.add("§7当前存储: §e" + storedAmount + " §7个");
        lore.add("§7左键: 获取随机数量");
        lore.add("§7右键: 获取1个");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
    
    /**
     * 处理产出获取
     * @param player 玩家
     * @param material 产出物品类型
     * @param amount 获取数量
     * @param isRightClick 是否右键点击
     */
    public void handleProductionWithdraw(Player player, Material material, int amount, boolean isRightClick) {
        String cropType = material.name();
        int storedAmount = plugin.getCropManager().getCropStorage(player.getUniqueId()).getCropAmount(cropType);
        
        // 如果是右键点击，只获取1个
        if (isRightClick) {
            amount = 1;
        }
        
        // 如果是左键点击，随机获取1-5个
        if (amount <= 0) {
            amount = random.nextInt(5) + 1;
        }
        
        if (storedAmount >= amount) {
            // 从存储中移除物品
            if (plugin.getCropManager().removeCrop(player.getUniqueId(), cropType, amount)) {
                // 给玩家物品
                ItemStack item = new ItemStack(material, amount);
                player.getInventory().addItem(item);
                
                // 发送成功消息
                Map<String, String> replacements = new HashMap<>();
                replacements.put("amount", String.valueOf(amount));
                replacements.put("crop", material.name().toLowerCase());
                player.sendMessage(messageManager.getMessage("crop.withdraw-success", replacements));
            } else {
                // 发送失败消息
                Map<String, String> replacements = new HashMap<>();
                replacements.put("crop", material.name().toLowerCase());
                player.sendMessage(messageManager.getMessage("crop.withdraw-failed", replacements));
            }
        } else {
            // 发送失败消息
            Map<String, String> replacements = new HashMap<>();
            replacements.put("crop", material.name().toLowerCase());
            player.sendMessage(messageManager.getMessage("crop.withdraw-failed", replacements));
        }
    }
    
    /**
     * 添加关闭按钮
     */
    private void addCloseButton(Inventory inventory, int slot) {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(messageManager.getMessage("gui.close", new HashMap<>()));
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(slot, closeButton);
    }
}