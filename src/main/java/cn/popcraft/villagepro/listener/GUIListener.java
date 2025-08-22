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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

/**
 * GUI事件监听器
 */
public class GUIListener implements Listener {
    private final VillagePro plugin;
    
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
        
        // 检查是否是升级GUI
        if (inventoryTitle.equals(plugin.getMessageManager().getMessage("gui.upgrade-title"))) {
            event.setCancelled(true);
            handleUpgradeGUI(player, clickedItem, event.getRawSlot());
        }
        // 检查是否是产出GUI
        else if (inventoryTitle.contains(plugin.getMessageManager().getMessage("gui.production-title"))) {
            event.setCancelled(true);
            handleProductionGUI(player, clickedItem, event.getRawSlot());
        }
        // 检查是否是职业产出GUI
        else if (inventoryTitle.contains(plugin.getMessageManager().getMessage("gui.profession-production-title", java.util.Map.of("profession", "")))) {
            event.setCancelled(true);
            handleProfessionProductionGUI(player, clickedItem, event.getRawSlot());
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
    
    private void handleProfessionProductionGUI(Player player, ItemStack clickedItem, int slot) {
        // 这里处理具体的职业产出获取逻辑
        // 由于这是一个简化实现，我们只是关闭GUI并发送一条消息
        switch (slot) {
            case 22: // 返回按钮
                new ProductionGUI(plugin).openMainMenu(player);
                break;
            default:
                // 处理具体的产出获取
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    // 这里应该实现实际的产出获取逻辑
                    // 为简化起见，我们只发送一条消息
                    player.sendMessage("§a你尝试获取 " + clickedItem.getType().name() + " 产出");
                    player.closeInventory();
                }
                break;
        }
    }
}