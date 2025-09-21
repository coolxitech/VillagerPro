package cn.popcraft.villagepro.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 物品名称工具类（自动本地化版本）
 * 支持：
 * 1. 原版物品：优先使用游戏内本地化名称（如“钻石”）
 * 2. ItemsAdder 物品：解析自定义名称
 * 3. 回退机制：缓存 + 手动映射 + 格式化
 */
public class ItemNameUtil {
    private static Plugin plugin;
    private static ItemFactory itemFactory;
    private static final Map<String, String> NAME_CACHE = new HashMap<>();
    private static final Map<String, String> MANUAL_NAME_MAP = new HashMap<>();

    static {
        // 保留你原有的手动映射（作为回退）
        MANUAL_NAME_MAP.put("EMERALD", "绿宝石");
        MANUAL_NAME_MAP.put("GOLDEN_APPLE", "金苹果");
        MANUAL_NAME_MAP.put("IRON_INGOT", "铁锭");
        MANUAL_NAME_MAP.put("DIAMOND", "钻石");
        MANUAL_NAME_MAP.put("GOLD_INGOT", "金锭");
        MANUAL_NAME_MAP.put("SUGAR", "糖");
        MANUAL_NAME_MAP.put("RABBIT_FOOT", "兔子脚");
        MANUAL_NAME_MAP.put("OBSIDIAN", "黑曜石");
        MANUAL_NAME_MAP.put("CLOCK", "时钟");
        MANUAL_NAME_MAP.put("BONE_MEAL", "骨粉");
        MANUAL_NAME_MAP.put("WHEAT_SEEDS", "小麦种子");
        MANUAL_NAME_MAP.put("WHEAT", "小麦");
        MANUAL_NAME_MAP.put("CARROT", "胡萝卜");
        MANUAL_NAME_MAP.put("POTATO", "马铃薯");
        MANUAL_NAME_MAP.put("BEETROOT", "甜菜根");
        MANUAL_NAME_MAP.put("CHEST", "箱子");
        MANUAL_NAME_MAP.put("LAPIS_LAZULI", "青金石");
        MANUAL_NAME_MAP.put("GLISTERING_MELON_SLICE", "闪烁的西瓜片");
        MANUAL_NAME_MAP.put("WHITE_WOOL", "白色羊毛");
        MANUAL_NAME_MAP.put("FISHING_ROD", "钓鱼竿");
        MANUAL_NAME_MAP.put("BOOK", "书");
        MANUAL_NAME_MAP.put("POTION", "药水");
        MANUAL_NAME_MAP.put("MAP", "地图");
        MANUAL_NAME_MAP.put("ARROW", "箭");
        MANUAL_NAME_MAP.put("BREAD", "面包");
        MANUAL_NAME_MAP.put("COAL", "煤炭");
        MANUAL_NAME_MAP.put("CHARCOAL", "木炭");
        MANUAL_NAME_MAP.put("IRON_SWORD", "铁剑");
        MANUAL_NAME_MAP.put("WOODEN_SWORD", "木剑");
        MANUAL_NAME_MAP.put("STONE_SWORD", "石剑");
        MANUAL_NAME_MAP.put("DIAMOND_SWORD", "钻石剑");
        MANUAL_NAME_MAP.put("GOLDEN_SWORD", "金剑");
        MANUAL_NAME_MAP.put("IRON_PICKAXE", "铁镐");
        MANUAL_NAME_MAP.put("WOODEN_PICKAXE", "木镐");
        MANUAL_NAME_MAP.put("STONE_PICKAXE", "石镐");
        MANUAL_NAME_MAP.put("DIAMOND_PICKAXE", "钻石镐");
        MANUAL_NAME_MAP.put("GOLDEN_PICKAXE", "金镐");
        MANUAL_NAME_MAP.put("OAK_LOG", "橡木原木");
        MANUAL_NAME_MAP.put("SPRUCE_LOG", "云杉原木");
        MANUAL_NAME_MAP.put("BIRCH_LOG", "白桦原木");
        MANUAL_NAME_MAP.put("JUNGLE_LOG", "丛林原木");
        MANUAL_NAME_MAP.put("ACACIA_LOG", "金合欢原木");
        MANUAL_NAME_MAP.put("DARK_OAK_LOG", "深色橡木原木");
        MANUAL_NAME_MAP.put("OAK_PLANKS", "橡木木板");
        MANUAL_NAME_MAP.put("SPRUCE_PLANKS", "云杉木板");
        MANUAL_NAME_MAP.put("BIRCH_PLANKS", "白桦木板");
        MANUAL_NAME_MAP.put("JUNGLE_PLANKS", "丛林木板");
        MANUAL_NAME_MAP.put("ACACIA_PLANKS", "金合欢木板");
        MANUAL_NAME_MAP.put("DARK_OAK_PLANKS", "深色橡木木板");
        MANUAL_NAME_MAP.put("COBBLESTONE", "圆石");
        MANUAL_NAME_MAP.put("STONE", "石头");
        MANUAL_NAME_MAP.put("SAND", "沙子");
        MANUAL_NAME_MAP.put("GRAVEL", "沙砾");
        MANUAL_NAME_MAP.put("FLINT", "燧石");
        MANUAL_NAME_MAP.put("LEATHER", "皮革");
        MANUAL_NAME_MAP.put("PAPER", "纸");
        MANUAL_NAME_MAP.put("FEATHER", "羽毛");
        MANUAL_NAME_MAP.put("GUNPOWDER", "火药");
        MANUAL_NAME_MAP.put("REDSTONE", "红石");
        MANUAL_NAME_MAP.put("SLIME_BALL", "黏液球");
        MANUAL_NAME_MAP.put("ENDER_PEARL", "末影珍珠");
        MANUAL_NAME_MAP.put("BLAZE_ROD", "烈焰棒");
        MANUAL_NAME_MAP.put("MAGMA_CREAM", "岩浆膏");
        MANUAL_NAME_MAP.put("GHAST_TEAR", "恶魂之泪");
        MANUAL_NAME_MAP.put("NETHER_WART", "下界疣");
        MANUAL_NAME_MAP.put("SPIDER_EYE", "蜘蛛眼");
        MANUAL_NAME_MAP.put("FERMENTED_SPIDER_EYE", "发酵蛛眼");
        MANUAL_NAME_MAP.put("BLAZE_POWDER", "烈焰粉");
        MANUAL_NAME_MAP.put("MILK_BUCKET", "牛奶桶");
        MANUAL_NAME_MAP.put("WATER_BUCKET", "水桶");
        MANUAL_NAME_MAP.put("LAVA_BUCKET", "岩浆桶");
        MANUAL_NAME_MAP.put("BUCKET", "铁桶");
        MANUAL_NAME_MAP.put("EGG", "鸡蛋");
        MANUAL_NAME_MAP.put("SNOWBALL", "雪球");
        MANUAL_NAME_MAP.put("CLAY_BALL", "黏土球");
        MANUAL_NAME_MAP.put("BRICK", "红砖");
        MANUAL_NAME_MAP.put("NETHER_BRICK", "下界砖");
        MANUAL_NAME_MAP.put("QUARTZ", "下界石英");
        MANUAL_NAME_MAP.put("PRISMARINE_SHARD", "海晶碎片");
        MANUAL_NAME_MAP.put("PRISMARINE_CRYSTALS", "海晶灯碎片");
        MANUAL_NAME_MAP.put("ENDER_EYE", "末影之眼");
        MANUAL_NAME_MAP.put("SPECKLED_MELON", "闪烁的西瓜片");
        MANUAL_NAME_MAP.put("CACTUS", "仙人掌");
        MANUAL_NAME_MAP.put("VINE", "藤蔓");
        MANUAL_NAME_MAP.put("LILY_PAD", "睡莲");
        MANUAL_NAME_MAP.put("TORCH", "火把");
        MANUAL_NAME_MAP.put("REDSTONE_TORCH", "红石火把");
        MANUAL_NAME_MAP.put("STICK", "木棍");
        MANUAL_NAME_MAP.put("BOWL", "碗");
        MANUAL_NAME_MAP.put("MUSHROOM_STEW", "蘑菇煲");
        MANUAL_NAME_MAP.put("STRING", "线");
        MANUAL_NAME_MAP.put("TRIPWIRE_HOOK", "绊线钩");
        MANUAL_NAME_MAP.put("LEAD", "拴绳");
        MANUAL_NAME_MAP.put("NAME_TAG", "命名牌");
        MANUAL_NAME_MAP.put("SADDLE", "鞍");
        MANUAL_NAME_MAP.put("BONE", "骨头");
        MANUAL_NAME_MAP.put("ROTTEN_FLESH", "腐肉");
        MANUAL_NAME_MAP.put("ENDER_CHEST", "末影箱");
        // 可继续扩展...
    }

    /**
     * 初始化工具类（在主插件 onEnable 时调用）
     */
    public static void init(Plugin plugin) {
        ItemNameUtil.plugin = plugin;
        ItemNameUtil.itemFactory = Bukkit.getItemFactory();
    }

    /**
     * 获取物品的可读名称（支持原版 + ItemsAdder）
     * @param itemKey 物品键名（如 "DIAMOND" 或 "itemsadder:myitems:sword"）
     * @return 可读的物品名称
     */
    public static String getItemDisplayName(String itemKey) {
        if (itemKey == null || itemKey.isEmpty()) {
            return "未知物品";
        }

        // 1. 先查缓存
        return NAME_CACHE.computeIfAbsent(itemKey, key -> {
            try {
                // 2. 处理 ItemsAdder 物品
                if (key.startsWith("itemsadder:")) {
                    return getItemsAdderDisplayName(key);
                }

                // 3. 处理原版物品
                Material material = Material.valueOf(key.toUpperCase());
                return getMaterialDisplayName(material);
            } catch (IllegalArgumentException e) {
                return key; // 未知物品，直接返回键名
            }
        });
    }

    /**
     * 获取原版物品的显示名称（优先使用游戏本地化）
     */
    private static String getMaterialDisplayName(Material material) {
        try {
            // 使用 ItemFactory 获取 ItemMeta（包含本地化名称）
            ItemMeta meta = itemFactory.getItemMeta(material);
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        } catch (Exception e) {
            plugin.getLogger().finest("无法获取 " + material + " 的本地化名称: " + e.getMessage());
        }

        // 回退1：手动映射
        String manualName = MANUAL_NAME_MAP.get(material.name());
        if (manualName != null) {
            return manualName;
        }

        // 回退2：格式化 Material 名称
        return formatMaterialName(material.name());
    }

    /**
     * 获取 ItemsAdder 物品的显示名称
     */
    private static String getItemsAdderDisplayName(String itemKey) {
        String[] parts = itemKey.split(":");
        if (parts.length < 3) {
            return "未知ItemsAdder物品(" + itemKey + ")";
        }

        String namespace = parts[1];
        String itemName = parts[2];
        String fullKey = namespace + ":" + itemName;

        Plugin iaPlugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        if (iaPlugin == null || !iaPlugin.isEnabled()) {
            return "ItemsAdder未加载(" + fullKey + ")";
        }

        try {
            // 调用 ItemsAdder API 获取 ItemStack
            Class<?> registryClass = Class.forName("dev.lone.itemsadder.api.ItemsAdder");
            Object item = registryClass.getMethod("getItem", String.class).invoke(null, fullKey);

            if (item != null) {
                ItemStack itemStack = (ItemStack) item.getClass().getMethod("getItemStack").invoke(item);
                if (itemStack != null && itemStack.hasItemMeta()) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta.hasDisplayName()) {
                        return meta.getDisplayName();
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().finest("无法获取 ItemsAdder 物品名称: " + fullKey + " - " + e.getMessage());
        }

        // 回退：返回命名空间+物品名（可读格式）
        return formatItemsAdderName(namespace, itemName);
    }

    /**
     * 格式化 ItemsAdder 物品名称（如 myitems:sword -> 我的剑）
     * 你可以在这里添加自定义映射
     */
    private static String formatItemsAdderName(String namespace, String itemName) {
        // 示例：你可以添加特定映射
        // if ("myitems".equals(namespace) && "sword".equals(itemName)) return "我的剑";

        // 默认：返回命名空间.物品名（可被手动映射覆盖）
        String key = (namespace + "_" + itemName).toUpperCase();
        return MANUAL_NAME_MAP.getOrDefault(key, namespace + "." + itemName);
    }

    /**
     * 格式化 Material 名称为可读中文（仅回退使用）
     */
    private static String formatMaterialName(String name) {
        StringBuilder formatted = new StringBuilder();
        boolean nextUpper = true;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                formatted.append(' ');
                nextUpper = true;
            } else {
                if (nextUpper) {
                    formatted.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    formatted.append(Character.toLowerCase(c));
                }
            }
        }
        return formatted.toString();
    }

    /**
     * 清除名称缓存（调试用）
     */
    public static void clearCache() {
        NAME_CACHE.clear();
    }
}