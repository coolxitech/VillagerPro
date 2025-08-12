package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.UpgradeType;
import cn.popcraft.villagepro.model.Village;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeGUI implements Listener {
    private final VillagePro plugin;
    private final Map<String, UpgradeType> inventoryMap = new HashMap<>();

    public UpgradeGUI(VillagePro plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 打开升级菜单
     * @param player 玩家
     */
    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§6村民升级");
        
        // 获取玩家的村庄数据
        Village village = plugin.getVillageManager().getOrCreateVillage(player);
        
        // 添加各种升级选项
        addUpgradeItem(inventory, 10, UpgradeType.TRADE, "§e交易升级", Material.EMERALD, 
                "提升村民交易效率", village.getUpgradeLevels().get(UpgradeType.TRADE));
        
        addUpgradeItem(inventory, 12, UpgradeType.HEALTH, "§c生命升级", Material.GOLDEN_APPLE, 
                "提升村民生命值", village.getUpgradeLevels().get(UpgradeType.HEALTH));
        
        addUpgradeItem(inventory, 14, UpgradeType.SPEED, "§b速度升级", Material.SUGAR, 
                "提升村民移动速度", village.getUpgradeLevels().get(UpgradeType.SPEED));
        
        addUpgradeItem(inventory, 16, UpgradeType.PROTECTION, "§7保护升级", Material.IRON_CHESTPLATE, 
                "提升村民抗性", village.getUpgradeLevels().get(UpgradeType.PROTECTION));
        
        // 添加关闭按钮
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§c关闭");
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(22, closeButton);
        
        // 记录该玩家打开的库存
        String inventoryId = player.getUniqueId().toString();
        inventoryMap.put(inventoryId, null); // 主菜单没有特定的升级类型
        
        player.openInventory(inventory);
    }
    
    /**
     * 打开特定升级类型的确认菜单
     * @param player 玩家
     * @param type 升级类型
     */
    public void openConfirmMenu(Player player, UpgradeType type) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§6确认升级: " + type.name());
        
        // 获取玩家当前的升级等级
        int currentLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), type);
        
        // 如果已经达到最高等级，显示提示
        if (currentLevel >= 5) {
            ItemStack maxItem = new ItemStack(Material.BARRIER);
            ItemMeta maxMeta = maxItem.getItemMeta();
            maxMeta.setDisplayName("§c已达到最高等级");
            maxItem.setItemMeta(maxMeta);
            inventory.setItem(13, maxItem);
        } else {
            // 获取下一级升级配置
            int nextLevel = currentLevel + 1;
            cn.popcraft.villagepro.model.Upgrade upgrade = plugin.getConfigManager().getUpgrade(type, nextLevel);
            
            if (upgrade != null) {
                // 创建升级信息物品
                ItemStack infoItem = new ItemStack(Material.PAPER);
                ItemMeta infoMeta = infoItem.getItemMeta();
                infoMeta.setDisplayName("§6升级信息");
                
                List<String> lore = new ArrayList<>();
                lore.add("§7当前等级: §e" + currentLevel);
                lore.add("§7目标等级: §e" + nextLevel);
                lore.add("§7费用:");
                lore.add("§7 - 金币: §e" + upgrade.getCostMoney());
                lore.add("§7 - 钻石: §e" + upgrade.getCostDiamonds());
                
                // 添加其他物品消耗
                for (Map.Entry<String, Integer> entry : upgrade.getCostItems().entrySet()) {
                    lore.add("§7 - " + entry.getKey() + ": §e" + entry.getValue());
                }
                
                infoMeta.setLore(lore);
                infoItem.setItemMeta(infoMeta);
                inventory.setItem(13, infoItem);
                
                // 添加确认按钮
                ItemStack confirmButton = new ItemStack(Material.LIME_WOOL);
                ItemMeta confirmMeta = confirmButton.getItemMeta();
                confirmMeta.setDisplayName("§a确认升级");
                confirmButton.setItemMeta(confirmMeta);
                inventory.setItem(11, confirmButton);
            }
        }
        
        // 添加取消按钮
        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§c取消");
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(15, cancelButton);
        
        // 记录该玩家打开的库存
        String inventoryId = player.getUniqueId().toString();
        inventoryMap.put(inventoryId, type);
        
        player.openInventory(inventory);
    }
    
    /**
     * 添加升级物品到库存
     */
    private void addUpgradeItem(Inventory inventory, int slot, UpgradeType type, String name, Material material, String description, Integer level) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + description);
        lore.add("§7当前等级: §e" + (level != null ? level : 0) + "/5");
        lore.add("");
        lore.add("§e点击升级");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        inventory.setItem(slot, item);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        String inventoryTitle = event.getView().getTitle();
        if (!inventoryTitle.startsWith("§6村民升级") && !inventoryTitle.startsWith("§6确认升级")) {
            return;
        }
        
        event.setCancelled(true);
        
        String inventoryId = player.getUniqueId().toString();
        UpgradeType currentType = inventoryMap.get(inventoryId);
        
        if (inventoryTitle.startsWith("§6村民升级")) {
            // 主菜单点击
            switch (event.getSlot()) {
                case 10:
                    openConfirmMenu(player, UpgradeType.TRADE);
                    break;
                case 12:
                    openConfirmMenu(player, UpgradeType.HEALTH);
                    break;
                case 14:
                    openConfirmMenu(player, UpgradeType.SPEED);
                    break;
                case 16:
                    openConfirmMenu(player, UpgradeType.PROTECTION);
                    break;
                case 22:
                    player.closeInventory();
                    break;
            }
        } else if (inventoryTitle.startsWith("§6确认升级") && currentType != null) {
            // 确认菜单点击
            switch (event.getSlot()) {
                case 11:
                    // 确认升级
                    if (plugin.getVillageManager().upgradeVillage(player, currentType)) {
                        player.sendMessage("§a[VillagePro] 升级成功!");
                        player.closeInventory();
                    } else {
                        player.sendMessage("§c[VillagePro] 升级失败，请确保你有足够的资源!");
                    }
                    break;
                case 15:
                    // 返回主菜单
                    openMenu(player);
                    break;
            }
        }
    }
}