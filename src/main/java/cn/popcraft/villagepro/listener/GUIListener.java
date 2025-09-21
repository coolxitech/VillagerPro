package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.gui.ProductionGUI;
import cn.popcraft.villagepro.gui.UpgradeGUI;
import cn.popcraft.villagepro.model.UpgradeType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * GUI 事件监听器
 * 适配优化版 ProductionGUI（使用 InventoryHolder + 统一事件处理）
 */
public class GUIListener implements Listener {
    private final VillagePro plugin;

    public GUIListener(VillagePro plugin) {
        this.plugin = plugin;
    }

    /**
     * 处理库存点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // 1. 如果是 ProductionGUI，交由 ProductionGUI 处理
        if (inventory.getHolder() instanceof ProductionGUI) {
            ProductionGUI.onInventoryClick(event); // 静态方法统一处理
            return;
        }

        // 2. 如果是 UpgradeGUI，交由 UpgradeGUI 处理
        if (inventory.getHolder() instanceof UpgradeGUI) {
            UpgradeGUI.onInventoryClick(event); // 静态方法统一处理
            return;
        }
    }


    /**
     * 常量定义
     */
    private static class SLOTS {
        // 控制槽位
        private static final int CLOSE = 22;
        private static final int CONFIRM = 20;
        private static final int CANCEL = 24;
    }
}