package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Upgrade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 村民升级GUI
 */
public class UpgradeGUI {
    private final VillagePro plugin;
    private final MessageManager messageManager;
    
    // 存储玩家打开的GUI类型
    private static final Map<UUID, UpgradeType> playerUpgradeType = new HashMap<>();
    
    public UpgradeGUI(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }
    
    public UpgradeGUI(VillagePro plugin, Player player) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }
    
    public UpgradeGUI(VillagePro plugin, Player player, cn.popcraft.villagepro.model.Village village) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }
    
    /**
     * 打开升级主菜单
     * @param player 玩家
     */
    public void openMainMenu(Player player) {
        String title = messageManager.getMessage("gui.upgrade-title");
        Inventory inventory = Bukkit.createInventory(null, 27, title);
        
        // 添加升级类型按钮
        addUpgradeTypeButton(inventory, 10, UpgradeType.TRADE, Material.EMERALD);
        addUpgradeTypeButton(inventory, 11, UpgradeType.HEALTH, Material.GOLDEN_APPLE);
        addUpgradeTypeButton(inventory, 12, UpgradeType.SPEED, Material.FEATHER);
        addUpgradeTypeButton(inventory, 13, UpgradeType.PROTECTION, Material.IRON_CHESTPLATE);
        addUpgradeTypeButton(inventory, 14, UpgradeType.RESTOCK_SPEED, Material.CLOCK);
        addUpgradeTypeButton(inventory, 15, UpgradeType.CROP_GROWTH, Material.WHEAT);
        addUpgradeTypeButton(inventory, 16, UpgradeType.TRADE_AMOUNT, Material.CHEST);
        
        // 添加关闭按钮
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(messageManager.getMessage("gui.close"));
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(22, closeButton);
        
        player.openInventory(inventory);
    }
    
    /**
     * 添加升级类型按钮
     */
    private void addUpgradeTypeButton(Inventory inventory, int slot, UpgradeType type, Material material) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        
        // 设置按钮名称
        String typeName = messageManager.getMessage("upgrade-types." + type.name());
        meta.setDisplayName("§6" + typeName);
        
        // 设置按钮描述
        List<String> lore = new ArrayList<>();
        lore.add("§7" + messageManager.getMessage("upgrade." + type.name().toLowerCase().replace('_', '-') + ".description"));
        lore.add("");
        lore.add(messageManager.getMessage("gui.click-to-upgrade"));
        meta.setLore(lore);
        
        button.setItemMeta(meta);
        inventory.setItem(slot, button);
    }
    
    /**
     * 打开确认升级菜单
     * @param player 玩家
     * @param type 升级类型
     */
    public void openConfirmMenu(Player player, UpgradeType type) {
        // 保存玩家正在操作的升级类型
        playerUpgradeType.put(player.getUniqueId(), type);
        
        // 获取当前等级
        int currentLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), type);
        
        // 检查是否已达到最高等级
        if (currentLevel >= 5) {
            player.sendMessage(messageManager.getMessage("upgrade.max-level-reached"));
            return;
        }
        
        // 获取下一级升级配置
        int nextLevel = currentLevel + 1;
        Upgrade upgrade = plugin.getConfigManager().getUpgrade(type, nextLevel);
        
        if (upgrade == null) {
            player.sendMessage(messageManager.getMessage("upgrade.failed"));
            return;
        }
        
        // 创建确认菜单
        Map<String, String> replacements = new HashMap<>();
        replacements.put("type", messageManager.getMessage("upgrade-types." + type.name()));
        String title = messageManager.getMessage("gui.confirm-title", replacements);
        Inventory inventory = Bukkit.createInventory(null, 27, title);
        
        // 添加升级信息
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(messageManager.getMessage("gui.upgrade-info"));
        
        // 设置升级信息描述
        List<String> lore = new ArrayList<>();
        
        // 当前等级
        replacements.clear();
        replacements.put("level", String.valueOf(currentLevel));
        lore.add(messageManager.getMessage("gui.current-level", replacements));
        
        // 目标等级
        replacements.clear();
        replacements.put("level", String.valueOf(nextLevel));
        lore.add(messageManager.getMessage("gui.target-level", replacements));
        
        // 升级费用
        lore.add("");
        lore.add(messageManager.getMessage("gui.cost"));
        
        // 金币费用
        if (upgrade.getCostMoney() > 0) {
            replacements.clear();
            replacements.put("amount", String.valueOf(upgrade.getCostMoney()));
            lore.add(messageManager.getMessage("gui.cost-money", replacements));
        }
        
        // 钻石费用
        if (upgrade.getCostDiamonds() > 0) {
            replacements.clear();
            replacements.put("amount", String.valueOf(upgrade.getCostDiamonds()));
            lore.add(messageManager.getMessage("gui.cost-diamond", replacements));
        }
        
        // 物品费用
        for (Map.Entry<String, Integer> entry : upgrade.getCostItems().entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey());
                int amount = entry.getValue();
                
                replacements.clear();
                replacements.put("amount", String.valueOf(amount));
                replacements.put("item", material.name());
                lore.add(messageManager.getMessage("gui.cost-item", replacements));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的物品类型: " + entry.getKey());
            }
        }
        
        infoMeta.setLore(lore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(13, infoItem);
        
        // 添加确认按钮
        ItemStack confirmButton = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(messageManager.getMessage("gui.confirm"));
        confirmButton.setItemMeta(confirmMeta);
        inventory.setItem(11, confirmButton);
        
        // 添加取消按钮
        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(messageManager.getMessage("gui.cancel"));
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(15, cancelButton);
        
        player.openInventory(inventory);
    }
    
    /**
     * 获取玩家当前操作的升级类型
     * @param player 玩家
     * @return 升级类型
     */
    public static UpgradeType getPlayerUpgradeType(Player player) {
        return playerUpgradeType.get(player.getUniqueId());
    }
    
    /**
     * 清除玩家的升级类型记录
     * @param player 玩家
     */
    public static void clearPlayerUpgradeType(Player player) {
        playerUpgradeType.remove(player.getUniqueId());
    }
}