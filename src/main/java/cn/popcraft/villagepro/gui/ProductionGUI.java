package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.VillagerProfession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * 村民产出GUI，用于获取村民生产的物品
 */
public class ProductionGUI {
    private final VillagePro plugin;
    private final MessageManager messageManager;
    
    public ProductionGUI(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }
    
    /**
     * 打开村民产出主菜单
     * @param player 玩家
     */
    public void openMainMenu(Player player) {
        String title = messageManager.getMessage("gui.production-title", new HashMap<>());
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
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(messageManager.getMessage("gui.close", new HashMap<>()));
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(22, closeButton);
        
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
        replacements.put("profession", profession.name());
        String title = messageManager.getMessage("gui.profession-production-title", replacements);
        Inventory inventory = Bukkit.createInventory(null, 27, title);
        
        // 根据职业类型添加产出物品
        switch (profession) {
            case FARMER:
                // 农民产出
                addProductionItem(inventory, 10, Material.WHEAT, "小麦", player);
                addProductionItem(inventory, 11, Material.CARROT, "胡萝卜", player);
                addProductionItem(inventory, 12, Material.POTATO, "马铃薯", player);
                addProductionItem(inventory, 13, Material.BEETROOT, "甜菜根", player);
                break;
            case FISHERMAN:
                // 渔夫产出
                addProductionItem(inventory, 10, Material.COD, "鳕鱼", player);
                addProductionItem(inventory, 11, Material.SALMON, "鲑鱼", player);
                addProductionItem(inventory, 12, Material.TROPICAL_FISH, "热带鱼", player);
                break;
            case SHEPHERD:
                // 牧羊人产出
                addProductionItem(inventory, 10, Material.WHITE_WOOL, "羊毛", player);
                addProductionItem(inventory, 11, Material.MUTTON, "羊肉", player);
                break;
            case FLETCHER:
                // 制箭师产出
                addProductionItem(inventory, 10, Material.ARROW, "箭", player);
                addProductionItem(inventory, 11, Material.FEATHER, "羽毛", player);
                addProductionItem(inventory, 12, Material.FLINT, "燧石", player);
                break;
            case LIBRARIAN:
                // 图书管理员产出
                addProductionItem(inventory, 10, Material.BOOK, "书", player);
                addProductionItem(inventory, 11, Material.ENCHANTED_BOOK, "附魔书", player);
                break;
            case CLERIC:
                // 祭司产出
                addProductionItem(inventory, 10, Material.POTION, "药水", player);
                addProductionItem(inventory, 11, Material.EXPERIENCE_BOTTLE, "经验瓶", player);
                break;
            case CARTOGRAPHER:
                // 制图师产出
                addProductionItem(inventory, 10, Material.MAP, "地图", player);
                addProductionItem(inventory, 11, Material.COMPASS, "指南针", player);
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
        inventory.setItem(22, backButton);
        
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
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
}