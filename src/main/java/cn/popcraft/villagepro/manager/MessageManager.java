package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.UpgradeType;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理器
 */
public class MessageManager {
    private final VillagePro plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<UpgradeType, String> upgradeTypeNames = new HashMap<>();
    
    public MessageManager(VillagePro plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    /**
     * 获取消息前缀
     * @return 消息前缀
     */
    public String getPrefix() {
        return messages.get("prefix");
    }

    /**
     * 加载消息配置
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // 加载基本消息
        messages.put("prefix", messagesConfig.getString("prefix", "&a[VillagePro] &r"));
        messages.put("no-permission", messagesConfig.getString("no-permission", "&c你没有权限执行此命令!"));
        
        // 加载村民相关消息
        messages.put("villager.name", messagesConfig.getString("villager.name", "&a{player}的村民"));
        
        // 加载配置相关消息
        messages.put("config.invalid-upgrade-type", messagesConfig.getString("config.invalid-upgrade-type", "未知的升级类型: {type}"));
        messages.put("config.invalid-upgrade-level", messagesConfig.getString("config.invalid-upgrade-level", "无效的升级等级: {level}"));
        
        // 加载作物相关消息
        messages.put("crop.invalid-crop-type", messagesConfig.getString("crop.invalid-crop-type", "无效的作物类型: {type}"));
        
        // 加载招募相关消息
        messages.put("recruit.success", messagesConfig.getString("recruit.success", "&a成功招募了一名村民!"));
        messages.put("recruit.failed", messagesConfig.getString("recruit.failed", "&c招募失败，请确保你有足够的资源!"));
        messages.put("recruit.max-villagers-reached", messagesConfig.getString("recruit.max-villagers-reached", "&c你已达到最大村民数量限制!"));
        
        // 加载升级相关消息
        messages.put("upgrade.success", messagesConfig.getString("upgrade.success", "&a成功升级了村民的 {type} 能力到 {level} 级!"));
        messages.put("upgrade.failed", messagesConfig.getString("upgrade.failed", "&c升级失败，请确保你有足够的资源!"));
        messages.put("upgrade.max-level-reached", messagesConfig.getString("upgrade.max-level-reached", "&c该能力已达到最高等级!"));
        
        // 加载升级描述
        messages.put("upgrade.trade.description", messagesConfig.getString("upgrade.trade.description", "提升村民交易效率"));
        messages.put("upgrade.health.description", messagesConfig.getString("upgrade.health.description", "提升村民生命值"));
        messages.put("upgrade.speed.description", messagesConfig.getString("upgrade.speed.description", "提升村民移动速度"));
        messages.put("upgrade.protection.description", messagesConfig.getString("upgrade.protection.description", "提升村民抗性"));
        messages.put("upgrade.restock-speed.description", messagesConfig.getString("upgrade.restock-speed.description", "提升村民补货速度"));
        messages.put("upgrade.crop-growth.description", messagesConfig.getString("upgrade.crop-growth.description", "提升作物生长速度和收获量"));
        messages.put("upgrade.trade-amount.description", messagesConfig.getString("upgrade.trade-amount.description", "提升村民交易货物数量"));
        
        // 加载跟随相关消息
        messages.put("follow.start", messagesConfig.getString("follow.start", "&a村民开始跟随你"));
        messages.put("follow.stop", messagesConfig.getString("follow.stop", "&a村民停止跟随"));
        messages.put("follow.stay", messagesConfig.getString("follow.stay", "&a村民将停留在此处"));
        messages.put("follow.free", messagesConfig.getString("follow.free", "&a村民将自由活动"));
        messages.put("follow.request", messagesConfig.getString("follow.request", "&a发送 y 让村民跟随，n 取消跟随"));
        messages.put("follow.cancel", messagesConfig.getString("follow.cancel", "&a村民将不会跟随你"));
        
        // 加载作物相关消息
        messages.put("crop.collected", messagesConfig.getString("crop.collected", "&a你的村民收集了 {amount} 个 {crop}!"));
        messages.put("crop.withdraw-success", messagesConfig.getString("crop.withdraw-success", "&a成功取出 {amount} 个 {crop}!"));
        messages.put("crop.withdraw-failed", messagesConfig.getString("crop.withdraw-failed", "&c取出失败，请确保你有足够的 {crop}!"));
        messages.put("crop.storage-empty", messagesConfig.getString("crop.storage-empty", "&c你的作物存储是空的!"));
        
        // 加载GUI相关消息
        messages.put("gui.upgrade-title", messagesConfig.getString("gui.upgrade-title", "&6村民升级"));
        messages.put("gui.confirm-title", messagesConfig.getString("gui.confirm-title", "&6确认升级: {type}"));
        messages.put("gui.confirm-prefix", messagesConfig.getString("gui.confirm-prefix", "&6确认升级"));
        messages.put("gui.close", messagesConfig.getString("gui.close", "&c关闭"));
        messages.put("gui.confirm", messagesConfig.getString("gui.confirm", "&a确认升级"));
        messages.put("gui.cancel", messagesConfig.getString("gui.cancel", "&c取消"));
        messages.put("gui.upgrade-info", messagesConfig.getString("gui.upgrade-info", "&6升级信息"));
        messages.put("gui.current-level", messagesConfig.getString("gui.current-level", "&7当前等级: &e{level}"));
        messages.put("gui.target-level", messagesConfig.getString("gui.target-level", "&7目标等级: &e{level}"));
        messages.put("gui.cost", messagesConfig.getString("gui.cost", "&7费用:"));
        messages.put("gui.cost-money", messagesConfig.getString("gui.cost-money", "&7 - 金币: &e{amount}"));
        messages.put("gui.cost-diamond", messagesConfig.getString("gui.cost-diamond", "&7 - 钻石: &e{amount}"));
        messages.put("gui.cost-item", messagesConfig.getString("gui.cost-item", "&7 - {item}: &e{amount}"));
        messages.put("gui.level-display", messagesConfig.getString("gui.level-display", "&7当前等级: &e{level}/{max}"));
        messages.put("gui.click-to-upgrade", messagesConfig.getString("gui.click-to-upgrade", "&e点击升级"));
        
        // 加载升级类型名称
        for (UpgradeType type : UpgradeType.values()) {
            String path = "upgrade-types." + type.name();
            String defaultName = type.name().toLowerCase().replace('_', ' ');
            upgradeTypeNames.put(type, messagesConfig.getString(path, defaultName));
        }
        
        plugin.getLogger().info("已加载 " + messages.size() + " 条消息和 " + upgradeTypeNames.size() + " 个升级类型名称");
    }
    
    /**
     * 获取消息
     * @param key 消息键
     * @return 格式化后的消息
     */
    public String getMessage(String key) {
        String message = messages.getOrDefault(key, key);
        return ChatColor.translateAlternateColorCodes('&', messages.getOrDefault("prefix", "") + message);
    }
    
    /**
     * 获取消息（带替换）
     * @param key 消息键
     * @param replacements 替换内容
     * @return 格式化后的消息
     */
    public String getMessage(String key, Map<String, String> replacements) {
        String message = messages.getOrDefault(key, key);
        
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return ChatColor.translateAlternateColorCodes('&', messages.getOrDefault("prefix", "") + message);
    }
    
    /**
     * 获取升级类型名称
     * @param type 升级类型
     * @return 升级类型名称
     */
    public String getUpgradeTypeName(UpgradeType type) {
        return upgradeTypeNames.getOrDefault(type, type.name().toLowerCase().replace('_', ' '));
    }
}