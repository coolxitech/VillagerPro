package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.gui.ProductionGUI;
import cn.popcraft.villagepro.gui.UpgradeGUI;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.VillagerProfession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Random;

public class GUIListener implements Listener {
    private final VillagePro plugin;
    private final Random random = new Random();
    
    public GUIListener(VillagePro plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String inventoryTitle = event.getView().getTitle();
        
        // 检查是否是自定义GUI
        boolean isCustomGUI = inventoryTitle.equals(plugin.getMessageManager().getMessage("gui.upgrade-title")) ||
                             inventoryTitle.equals(plugin.getMessageManager().getMessage("gui.production-title", java.util.Map.of())) ||
                             inventoryTitle.contains(plugin.getMessageManager().getMessage("gui.profession-production-title", java.util.Map.of("profession", "")));
        
        // 如果是自定义GUI，完全阻止所有操作
        if (isCustomGUI) {
            // 完全阻止任何物品移动操作，包括Shift+点击、双击、拖拽等
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            
            // 处理具体点击事件
            if (inventoryTitle.equals(plugin.getMessageManager().getMessage("gui.upgrade-title"))) {
                handleUpgradeGUI(player, clickedItem, event.getRawSlot());
            }
            // 检查是否是产出GUI
            else if (inventoryTitle.equals(plugin.getMessageManager().getMessage("gui.production-title", java.util.Map.of()))) {
                handleProductionGUI(player, clickedItem, event.getRawSlot());
            }
            // 检查是否是职业产出GUI
            else if (inventoryTitle.contains(plugin.getMessageManager().getMessage("gui.profession-production-title", java.util.Map.of("profession", "")))) {
                handleProfessionProductionGUI(player, clickedItem, event.getRawSlot(), event.isRightClick());
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();
        
        // 阻止在自定义GUI中拖拽物品
        if (inventoryTitle.equals(plugin.getMessageManager().getMessage("gui.upgrade-title")) ||
            inventoryTitle.equals(plugin.getMessageManager().getMessage("gui.production-title", java.util.Map.of())) ||
            inventoryTitle.contains(plugin.getMessageManager().getMessage("gui.profession-production-title", java.util.Map.of("profession", "")))) {
            
            // 完全阻止拖拽操作
            event.setCancelled(true);
        }
    }
    
    private void handleUpgradeGUI(Player player, ItemStack clickedItem, int slot) {
        switch (slot) {
            case 10: // TRADE
                new UpgradeGUI(plugin).openConfirmMenu(player, UpgradeType.TRADE);
                break;
            case 11: // HEALTH
                new UpgradeGUI(plugin).openConfirmMenu(player, UpgradeType.HEALTH);
                break;
            case 12: // SPEED
                new UpgradeGUI(plugin).openConfirmMenu(player, UpgradeType.SPEED);
                break;
            case 13: // PROTECTION
                new UpgradeGUI(plugin).openConfirmMenu(player, UpgradeType.PROTECTION);
                break;
            case 14: // RESTOCK_SPEED
                new UpgradeGUI(plugin).openConfirmMenu(player, UpgradeType.RESTOCK_SPEED);
                break;
            case 15: // CROP_YIELD (原CROP_GROWTH)
                new UpgradeGUI(plugin).openConfirmMenu(player, UpgradeType.CROP_GROWTH);
                break;
            case 16: // TRADE_AMOUNT
                new UpgradeGUI(plugin).openConfirmMenu(player, UpgradeType.TRADE_AMOUNT);
                break;
            case 22: // CLOSE
                player.closeInventory();
                break;
            default:
                // 确认升级菜单的处理
                if (slot == 11) { // 确认按钮
                    UpgradeType type = UpgradeGUI.getPlayerUpgradeType(player);
                    if (type != null) {
                        plugin.getVillageManager().upgradeVillage(player, type);
                        UpgradeGUI.clearPlayerUpgradeType(player);
                        player.closeInventory();
                    }
                } else if (slot == 15) { // 取消按钮
                    UpgradeGUI.clearPlayerUpgradeType(player);
                    new UpgradeGUI(plugin).openMainMenu(player);
                }
                break;
        }
    }
    
    private void handleProductionGUI(Player player, ItemStack clickedItem, int slot) {
        switch (slot) {
            case 10: // 农民
                new ProductionGUI(plugin).openProfessionProductionMenu(player, VillagerProfession.FARMER);
                break;
            case 11: // 渔夫
                new ProductionGUI(plugin).openProfessionProductionMenu(player, VillagerProfession.FISHERMAN);
                break;
            case 12: // 牧羊人
                new ProductionGUI(plugin).openProfessionProductionMenu(player, VillagerProfession.SHEPHERD);
                break;
            case 13: // 制箭师
                new ProductionGUI(plugin).openProfessionProductionMenu(player, VillagerProfession.FLETCHER);
                break;
            case 14: // 图书管理员
                new ProductionGUI(plugin).openProfessionProductionMenu(player, VillagerProfession.LIBRARIAN);
                break;
            case 15: // 祭司
                new ProductionGUI(plugin).openProfessionProductionMenu(player, VillagerProfession.CLERIC);
                break;
            case 16: // 制图师
                new ProductionGUI(plugin).openProfessionProductionMenu(player, VillagerProfession.CARTOGRAPHER);
                break;
            case 22: // 关闭
                player.closeInventory();
                break;
        }
    }
    
    private void handleProfessionProductionGUI(Player player, ItemStack clickedItem, int slot, boolean isRightClick) {
        // 这里处理具体的职业产出获取逻辑
        switch (slot) {
            case 10: // 第一个产出物品
                handleProductionItem(player, clickedItem, isRightClick);
                break;
            case 11: // 第二个产出物品
                handleProductionItem(player, clickedItem, isRightClick);
                break;
            case 12: // 第三个产出物品
                handleProductionItem(player, clickedItem, isRightClick);
                break;
            case 13: // 第四个产出物品
                handleProductionItem(player, clickedItem, isRightClick);
                break;
            case 14: // 第五个产出物品
                handleProductionItem(player, clickedItem, isRightClick);
                break;
            case 15: // 第六个产出物品
                handleProductionItem(player, clickedItem, isRightClick);
                break;
            case 16: // 第七个产出物品
                handleProductionItem(player, clickedItem, isRightClick);
                break;
            case 49: // 返回按钮 (在54格GUI中位于底部中央)
                new ProductionGUI(plugin).openMainMenu(player);
                break;
            default:
                handleProductionItem(player, clickedItem, isRightClick);
                break;
        }
    }
    
    private void handleProductionItem(Player player, ItemStack clickedItem, boolean isRightClick) {
        if (clickedItem != null && clickedItem.getType() != Material.AIR && clickedItem.getType() != Material.ARROW) {
            // 使用ProductionGUI处理产出获取
            new ProductionGUI(plugin).handleProductionWithdraw(player, clickedItem.getType(), 0, isRightClick);
            // 不关闭GUI，让玩家可以继续获取其他物品
        }
    }
}