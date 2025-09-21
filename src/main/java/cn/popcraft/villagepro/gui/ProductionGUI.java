package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.model.VillagerProfession;
import cn.popcraft.villagepro.util.ItemNameUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class ProductionGUI implements InventoryHolder {
    private final VillagePro plugin;
    private final MessageManager messageManager;
    private final Random random = new Random();
    private final Set<UUID> clickCooldown = new HashSet<>();

    public ProductionGUI(VillagePro plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        ItemNameUtil.init(plugin);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    // ==================== 主菜单 ====================

    public void openMainMenu(Player player) {
        String title = messageManager.getMessage("gui.production-title", new HashMap<>());
        Inventory inv = Bukkit.createInventory(this, 27, title);
        addProfessionButton(inv, 10, VillagerProfession.FARMER, Material.WHEAT, "农民");
        addProfessionButton(inv, 11, VillagerProfession.FISHERMAN, Material.FISHING_ROD, "渔夫");
        addProfessionButton(inv, 12, VillagerProfession.SHEPHERD, Material.WHITE_WOOL, "牧羊人");
        addProfessionButton(inv, 13, VillagerProfession.FLETCHER, Material.ARROW, "制箭师");
        addProfessionButton(inv, 14, VillagerProfession.LIBRARIAN, Material.BOOK, "图书管理员");
        addProfessionButton(inv, 15, VillagerProfession.CLERIC, Material.POTION, "祭司");
        addProfessionButton(inv, 16, VillagerProfession.CARTOGRAPHER, Material.MAP, "制图师");
        addCloseButton(inv, 26);
        player.openInventory(inv);
    }

    // ==================== 职业菜单 ====================

    public void openProfessionMenu(Player player, VillagerProfession profession) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("profession", profession.getDisplayName());
        String title = messageManager.getMessage("gui.profession-production-title", replacements);
        Inventory inv = Bukkit.createInventory(this, 54, title);

        List<Material> items = getItemsFor(profession);
        int slot = 10;
        for (Material mat : items) {
            if (slot % 9 == 8) slot += 2;
            addProductionItem(inv, slot, mat, player);
            slot++;
        }

        addBackButton(inv, 49);
        player.openInventory(inv);
    }

    // ==================== 物资领取逻辑 ====================

    public void withdrawItem(Player player, Material material, boolean isRightClick) {
        if (clickCooldown.contains(player.getUniqueId())) return;

        String cropName = material.name();
        int stored = plugin.getCropManager().getCropStorage(player.getUniqueId()).getCropAmount(cropName);
        int amount = isRightClick ? 1 : random.nextInt(5) + 1;

        if (stored >= amount && plugin.getCropManager().removeCrop(player.getUniqueId(), cropName, amount)) {
            player.getInventory().addItem(new ItemStack(material, amount));
            Map<String, String> reps = Map.of(
                "amount", String.valueOf(amount),
                "crop", ItemNameUtil.getItemDisplayName(cropName)
            );
            player.sendMessage(messageManager.getMessage("crop.withdraw-success", reps));
            playSuccessSound(player);
        } else {
            player.sendMessage(messageManager.getMessage("crop.withdraw-failed", Map.of("crop", cropName)));
            playFailSound(player);
        }

        clickCooldown.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> clickCooldown.remove(player.getUniqueId()), 10L);
        player.closeInventory();
    }

    // ==================== 工具方法 ====================

    private void addProfessionButton(Inventory inv, int slot, VillagerProfession prof, Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6" + name);
        meta.setLore(List.of("§7点击查看产出", "", "§e▶ 点击进入"));
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void addProductionItem(Inventory inv, int slot, Material material, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String displayName = ItemNameUtil.getItemDisplayName(material.name());
        meta.setDisplayName("§e" + displayName);
        int stored = plugin.getCropManager().getCropStorage(player.getUniqueId()).getCropAmount(material.name());
        meta.setLore(List.of(
            "§7点击领取",
            "§7当前存储: §e" + stored + " §7个",
            "§7左键: 随机 1-5 个",
            "§7右键: 获取 1 个"
        ));
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void addBackButton(Inventory inv, int slot) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6返回");
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void addCloseButton(Inventory inv, int slot) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c关闭");
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
    }

    private void playFailSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
    }

    private void playOpenSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
    }

    // ==================== 事件处理 ====================

    public static void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ProductionGUI gui)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        String title = event.getView().getTitle();

        if (title.equals(gui.messageManager.getMessage("gui.production-title", new HashMap<>()))) {
            handleMainClick(gui, player, item);
        } else if (title.contains("产出")) {
            handleProductionClick(gui, player, item, event.getClick() == ClickType.RIGHT);
        }
    }

    private static void handleMainClick(ProductionGUI gui, Player player, ItemStack item) {
        VillagerProfession prof = getProfessionFromItem(item);
        if (prof != null) {
            gui.openProfessionMenu(player, prof);
        } else if (item.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }

    private static void handleProductionClick(ProductionGUI gui, Player player, ItemStack item, boolean isRight) {
        if (item.getType() == Material.ARROW) {
            gui.openMainMenu(player);
        } else if (item.getType().isItem()) {
            gui.withdrawItem(player, item.getType(), isRight);
        }
    }

    private static VillagerProfession getProfessionFromItem(ItemStack item) {
        return switch (item.getType()) {
            case WHEAT -> VillagerProfession.FARMER;
            case FISHING_ROD -> VillagerProfession.FISHERMAN;
            case WHITE_WOOL -> VillagerProfession.SHEPHERD;
            case ARROW -> VillagerProfession.FLETCHER;
            case BOOK -> VillagerProfession.LIBRARIAN;
            case POTION -> VillagerProfession.CLERIC;
            case MAP -> VillagerProfession.CARTOGRAPHER;
            default -> null;
        };
    }

    private static List<Material> getItemsFor(VillagerProfession prof) {
        return switch (prof) {
            case FARMER -> List.of(Material.WHEAT, Material.CARROT, Material.POTATO, Material.BEETROOT);
            case FISHERMAN -> List.of(Material.COD, Material.SALMON, Material.TROPICAL_FISH);
            case SHEPHERD -> List.of(Material.WHITE_WOOL, Material.MUTTON, Material.LEATHER);
            case FLETCHER -> List.of(Material.ARROW, Material.FEATHER, Material.FLINT);
            case LIBRARIAN -> List.of(Material.BOOK, Material.ENCHANTED_BOOK, Material.PAPER);
            case CLERIC -> List.of(Material.POTION, Material.EXPERIENCE_BOTTLE, Material.REDSTONE);
            case CARTOGRAPHER -> List.of(Material.MAP, Material.COMPASS, Material.GLASS_PANE);
            default -> List.of(Material.BREAD);
        };
    }
}