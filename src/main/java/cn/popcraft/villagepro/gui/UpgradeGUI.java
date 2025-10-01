package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.Upgrade;
import cn.popcraft.villagepro.model.UpgradeType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import cn.popcraft.villagepro.util.ItemNameUtil;

import java.util.*;

/**
 * 村民升级GUI
 * 优化版本：防止取走按钮、修复点击无反应、代码更健壮
 */
public class UpgradeGUI implements InventoryHolder {
    private final VillagePro plugin;
    private final MessageManager messageManager;

    public UpgradeGUI(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    /**
     * 打开升级主菜单
     */
    public void openMainMenu(Player player) {
        String title = messageManager.getMessage("gui.upgrade-title");
        Inventory inventory = Bukkit.createInventory(this, 27, title);

        addUpgradeButton(inventory, 10, UpgradeType.TRADE, Material.EMERALD, "交易");
        addUpgradeButton(inventory, 11, UpgradeType.HEALTH, Material.GOLDEN_APPLE, "生命");
        addUpgradeButton(inventory, 12, UpgradeType.SPEED, Material.FEATHER, "速度");
        addUpgradeButton(inventory, 13, UpgradeType.PROTECTION, Material.IRON_CHESTPLATE, "保护");
        addUpgradeButton(inventory, 14, UpgradeType.RESTOCK_SPEED, Material.CLOCK, "补货速度");
        addUpgradeButton(inventory, 15, UpgradeType.CROP_GROWTH, Material.WHEAT, "作物生长");
        addUpgradeButton(inventory, 16, UpgradeType.TRADE_AMOUNT, Material.CHEST, "交易数量");

        addCloseButton(inventory, 22);

        player.openInventory(inventory);
        playOpenSound(player);
    }

    /**
     * 添加升级类型按钮
     */
    private void addUpgradeButton(Inventory inv, int slot, UpgradeType type, Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + displayName);
            List<String> lore = new ArrayList<>();
            lore.add("§7" + messageManager.getMessage("upgrade." + type.name().toLowerCase().replace('_', '-') + ".description"));
            lore.add("");
            lore.add(messageManager.getMessage("gui.click-to-upgrade"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    /**
     * 打开确认升级菜单
     */
    public void openConfirmMenu(Player player, UpgradeType type) {
        setPlayerUpgradeType(player, type);

        int currentLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), type);
        if (currentLevel >= 5) {
            player.sendMessage(messageManager.getMessage("upgrade.max-level-reached"));
            return;
        }

        int nextLevel = currentLevel + 1;
        Upgrade upgrade = plugin.getConfigManager().getUpgrade(type, nextLevel);
        if (upgrade == null) {
            String errorMessage = messageManager.getMessage("upgrade.failed") + " 类型: " + type.name() + ", 等级: " + nextLevel;
            player.sendMessage(errorMessage);
            plugin.getLogger().warning(errorMessage);
            return;
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("type", messageManager.getMessage("upgrade-types." + type.name()));
        String title = messageManager.getMessage("gui.confirm-title", replacements);
        Inventory inventory = Bukkit.createInventory(this, 27, title);

        // 升级信息
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(messageManager.getMessage("gui.upgrade-info"));
            infoMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_item"), PersistentDataType.BYTE, (byte) 1);
            List<String> lore = new ArrayList<>();

            replacements.clear();
            replacements.put("level", String.valueOf(currentLevel));
            lore.add(messageManager.getMessage("gui.current-level", replacements));

            replacements.clear();
            replacements.put("level", String.valueOf(nextLevel));
            lore.add(messageManager.getMessage("gui.target-level", replacements));

            lore.add("");
            lore.add(messageManager.getMessage("gui.cost"));

            if (upgrade.getCostMoney() > 0) {
                replacements.clear();
                replacements.put("amount", String.valueOf(upgrade.getCostMoney()));
                lore.add(messageManager.getMessage("gui.cost-money", replacements));
            }
            if (upgrade.getCostDiamonds() > 0) {
                replacements.clear();
                replacements.put("amount", String.valueOf(upgrade.getCostDiamonds()));
                lore.add(messageManager.getMessage("gui.cost-diamond", replacements));
            }
            for (Map.Entry<String, Integer> entry : upgrade.getCostItems().entrySet()) {
                try {
                    Material material = Material.valueOf(entry.getKey());
                    int amount = entry.getValue();
                    replacements.clear();
                    replacements.put("amount", String.valueOf(amount));
                    replacements.put("item", ItemNameUtil.getItemDisplayName(entry.getKey()));
                    lore.add(messageManager.getMessage("gui.cost-item", replacements));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的物品类型: " + entry.getKey());
                }
            }

            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(13, infoItem);

        // 确认按钮
        ItemStack confirmButton = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(messageManager.getMessage("gui.confirm"));
            confirmMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_item"), PersistentDataType.BYTE, (byte) 1);
            confirmButton.setItemMeta(confirmMeta);
        }
        inventory.setItem(11, confirmButton);

        // 取消按钮
        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(messageManager.getMessage("gui.cancel"));
            cancelMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_item"), PersistentDataType.BYTE, (byte) 1);
            cancelButton.setItemMeta(cancelMeta);
        }
        inventory.setItem(15, cancelButton);

        player.openInventory(inventory);
        playOpenSound(player);
    }

    // ==================== 静态方法：管理玩家升级状态 ====================

    private static final Map<UUID, UpgradeType> playerUpgradeType = new HashMap<>();

    public static void setPlayerUpgradeType(Player player, UpgradeType type) {
        playerUpgradeType.put(player.getUniqueId(), type);
    }

    public static UpgradeType getPlayerUpgradeType(Player player) {
        return playerUpgradeType.get(player.getUniqueId());
    }

    public static void clearPlayerUpgradeType(Player player) {
        playerUpgradeType.remove(player.getUniqueId());
    }

    // ==================== 工具方法 ====================

    private void addCloseButton(Inventory inventory, int slot) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messageManager.getMessage("gui.close"));
            item.setItemMeta(meta);
        }
        inventory.setItem(slot, item);
    }

    private void playOpenSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
    }

    private void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
    }

    private void playFailSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
    }

    // ==================== GUI 事件处理 ====================

    /**
     * 处理 UpgradeGUI 的点击事件
     */
    public static void onInventoryClick(InventoryClickEvent event) {
        // 1. 检查是否是本GUI
        if (!(event.getInventory().getHolder() instanceof UpgradeGUI gui)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();

        // 2. 完全阻止所有操作（最安全）
        event.setCancelled(true);
        event.setResult(Event.Result.DENY); // 阻止任何结果

        // 3. 检查物品是否为空或空气
        if (current == null || current.getType() == Material.AIR) {
            return;
        }


        // 5. 获取标题判断菜单类型
        String title = event.getView().getTitle();

        if (title.equals(gui.messageManager.getMessage("gui.upgrade-title"))) {
            // 主菜单
            handleMainMenuClick(player, current, gui.plugin);
        } else if (title.equals(gui.messageManager.getMessage("gui.confirm-title"))) {
            // 确认菜单
            handleConfirmMenuClick(player, current, gui.plugin, event);
        }
    }

    private static void handleMainMenuClick(Player player, ItemStack item, VillagePro plugin) {
        Material type = item.getType();
        UpgradeType upgradeType = switch (type) {
            case EMERALD -> UpgradeType.TRADE;
            case GOLDEN_APPLE -> UpgradeType.HEALTH;
            case FEATHER -> UpgradeType.SPEED;
            case IRON_CHESTPLATE -> UpgradeType.PROTECTION;
            case CLOCK -> UpgradeType.RESTOCK_SPEED;
            case WHEAT -> UpgradeType.CROP_GROWTH;
            case CHEST -> UpgradeType.TRADE_AMOUNT;
            default -> null;
        };

        if (upgradeType != null) {
            new UpgradeGUI(plugin).openConfirmMenu(player, upgradeType);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
        }
    }

    private static void handleConfirmMenuClick(Player player, ItemStack item, VillagePro plugin, InventoryClickEvent event) {
        // 阻止任何物品被移动或取出
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        
        MessageManager messageManager = plugin.getMessageManager();
        
        if (item.getType() == Material.LIME_WOOL && item.hasItemMeta() &&
            item.getItemMeta().getDisplayName().equals(messageManager.getMessage("gui.confirm"))) {
            UpgradeType type = getPlayerUpgradeType(player);
            if (type != null) {
                if (plugin.getVillageManager().upgradeVillage(player, type)) {
                    player.sendMessage(messageManager.getMessage("upgrade.success"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                } else {
                    player.sendMessage(messageManager.getMessage("upgrade.failed"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
                }
                clearPlayerUpgradeType(player);
                player.closeInventory();
            }
        } else if (item.getType() == Material.RED_WOOL && item.hasItemMeta() &&
                   item.getItemMeta().getDisplayName().equals(messageManager.getMessage("gui.cancel"))) {
            clearPlayerUpgradeType(player);
            new UpgradeGUI(plugin).openMainMenu(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } else if (item.getType() == Material.PAPER && item.hasItemMeta() &&
                   item.getItemMeta().getDisplayName().equals(messageManager.getMessage("gui.upgrade-info"))) {
            // 升级信息按钮，不执行任何操作
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
        }
    }

    /**
     * 防止拖拽（虽然 onInventoryClick 已阻止，但双重保险）
     */
    public static void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof UpgradeGUI) {
            event.setCancelled(true);
        }
    }
}