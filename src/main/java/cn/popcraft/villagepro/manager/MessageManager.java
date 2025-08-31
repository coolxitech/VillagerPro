package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class MessageManager {
    private final VillagePro plugin;
    private final Map<String, String> messages = new HashMap<>();
    private FileConfiguration messagesConfig;
    
    public MessageManager(VillagePro plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        // 保存默认消息配置文件
        saveDefaultMessages();
        
        // 加载消息配置
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // 加载默认消息（如果配置文件中没有定义）
        loadDefaultMessages();
        
        // 加载配置文件中的消息
        loadConfigMessages();
        
        plugin.getLogger().info("已加载 " + messages.size() + " 条消息配置");
    }
    
    private void saveDefaultMessages() {
        // 保存默认消息配置
        plugin.saveResource("messages.yml", false);
    }
    
    private void loadDefaultMessages() {
        // 加载默认消息
        messages.put("prefix", "&a[VillagePro] &r");
        messages.put("no-permission", "&c你没有权限执行此命令!");
        
        // 村民相关消息
        messages.put("villager.name", "&a{player}的村民");
        messages.put("villager.recruited", "&a成功招募了村民!");
        messages.put("villager.not-found", "&c附近没有可招募的村民!");
        messages.put("villager.already-recruited", "&c该村民已被招募!");
        messages.put("villager.max-reached", "&c你已达到最大村民数量限制!");
        
        // 村庄相关消息
        messages.put("village.created", "&a村庄创建成功!");
        messages.put("village.upgraded", "&a村庄升级成功! 当前等级: &e{level}");
        messages.put("village.max-level", "&c村庄已达到最高等级!");
        messages.put("village.not-found", "&c你还没有村庄，请先创建一个!");
        
        // 升级相关消息
        messages.put("upgrade.trade.description", "提高村民交易数量和质量");
        messages.put("upgrade.health.description", "提高村民生命值和治疗能力");
        messages.put("upgrade.speed.description", "提高村民移动速度");
        messages.put("upgrade.protection.description", "提高村民防护能力");
        messages.put("upgrade.restock-speed.description", "提高村民补货速度");
        messages.put("upgrade.crop-yield.description", "提高农民作物产量");
        messages.put("upgrade.trade-amount.description", "提高村民交易数量");
        messages.put("upgrade.trade-quality.description", "提高村民交易物品质量");
        messages.put("upgrade.trade-price.description", "提高村民交易价格");
        
        messages.put("upgrade.success", "&a成功升级了村民的 {type} 能力到 {level} 级!");
        messages.put("upgrade.failed", "&c升级失败，请确保你有足够的资源!");
        messages.put("upgrade.max-level-reached", "&c该能力已达到最高等级!");
        
        // 任务相关消息
        messages.put("task.generated", "&a已生成新的任务: &e{description}");
        messages.put("task.progress", "&a任务进度: &e{progress}/{target} {task}");
        messages.put("task.completed", "&a任务完成! 获得奖励: &e{reward}");
        messages.put("task.reward", "&a任务奖励: &e{exp} 经验 &7| &e{money} 金币");
        messages.put("task.pending", "&e你有未完成的任务，请先完成它们!");
        
        // 作物相关消息
        messages.put("crop.collected", "&a村民收集了 {amount} 个 {crop}!");
        messages.put("crop.withdraw-success", "&a成功取出 {amount} 个 {crop}!");
        messages.put("crop.withdraw-failed", "&c取出失败，请确保你有足够的 {crop}!");
        messages.put("crop.storage-empty", "&c你的作物存储是空的!");
        messages.put("crop.balance", "&a你当前存储的 {crop}: &e{amount} 个");
        messages.put("crop.deposit-success", "&a成功存储了 {amount} 个 {crop}!");
        messages.put("crop.deposit-failed", "&c存储失败，请确保你有足够的 {crop}!");
        
        // GUI相关消息
        messages.put("gui.upgrade-title", "&6村民升级");
        messages.put("gui.production-title", "&6村民产出");
        messages.put("gui.profession-production-title", "&6{profession}产出");
        messages.put("gui.confirm-title", "&6确认升级 - {type}");
        messages.put("gui.upgrade-info", "&e升级信息");
        messages.put("gui.current-level", "&7当前等级: &e{level}");
        messages.put("gui.target-level", "&7目标等级: &e{level}");
        messages.put("gui.cost", "&7升级费用:");
        messages.put("gui.cost-money", "&f- 金币: &e{amount}");
        messages.put("gui.cost-diamond", "&f- 钻石: &e{amount}");
        messages.put("gui.cost-item", "&f- {item}: &e{amount}");
        messages.put("gui.confirm", "&a&l确认升级");
        messages.put("gui.cancel", "&c&l取消");
        messages.put("gui.close", "&c&l关闭");
        messages.put("gui.back", "&c返回");
        messages.put("gui.click-to-upgrade", "&e点击升级");
        messages.put("gui.click-to-interact", "&e点击交互");
        messages.put("gui.page-info", "&7使用 &e{command} &7查看其他页面");
        
        // 跟随相关消息
        messages.put("follow.start", "&a村民开始跟随你");
        messages.put("follow.stop", "&a村民停止跟随");
        messages.put("follow.stay", "&a村民将停留在此处");
        messages.put("follow.free", "&a村民将自由活动");
        messages.put("follow.request", "&a发送 y 让村民跟随，n 取消跟随");
        messages.put("follow.cancel", "&a村民将不会跟随你");
        
        // 招募相关消息
        messages.put("recruit.success", "&a成功招募了一名村民!");
        messages.put("recruit.failed", "&c招募失败，请确保你有足够的资源!");
        messages.put("recruit.not-enough-money", "&c招募失败，请确保你有足够的钱!");
        messages.put("recruit.not-enough-items", "&c招募失败，请确保你有足够的物品!");
        messages.put("recruit.max-villagers-reached", "&c你已达到最大村民数量限制!");
        messages.put("recruit.no-villager", "&c附近没有可招募的村民!");
        messages.put("recruit.failed.not-enough-money", "&c招募失败，请确保你有足够的钱!");

        // 配置相关消息
        messages.put("config.invalid-upgrade-type", "未知的升级类型: {type}");
        messages.put("config.invalid-upgrade-level", "无效的升级等级: {level}");
        
        // 升级类型名称
        messages.put("upgrade-types.TRADE", "交易能力");
        messages.put("upgrade-types.HEALTH", "健康能力");
        messages.put("upgrade-types.SPEED", "速度能力");
        messages.put("upgrade-types.PROTECTION", "防护能力");
        messages.put("upgrade-types.RESTOCK_SPEED", "补货速度");
        messages.put("upgrade-types.CROP_GROWTH", "作物产量");
        messages.put("upgrade-types.CROP_YIELD", "作物产量");
        messages.put("upgrade-types.TRADE_AMOUNT", "交易数量");
        messages.put("upgrade-types.TRADE_QUALITY", "交易质量");
        messages.put("upgrade-types.TRADE_PRICE", "交易价格");
        
        // 帮助相关消息
        messages.put("help.title", "&6=== VillagePro 命令帮助 ===");
        messages.put("help.page", "&e第 {current} 页 / 共 {total} 页");
        messages.put("help.command-format", "&a/villagerpro {command} {args} &7- &f{description}");
        messages.put("help.player-only", "&c该命令只能由玩家执行!");
        messages.put("help.invalid-usage", "&c无效的命令用法! 请使用 &a/villagerpro help &c查看帮助");
        messages.put("help.page-info", "&7使用 &e{command} &7查看其他页面");
        messages.put("help.page-not-found", "&c第 {page} 页不存在，总共有 {total} 页");
        
        // 村民相关消息
        messages.put("villager.not-found", "&c未找到村民!");
        messages.put("villager.already-recruited", "&c该村民已被招募!");
        messages.put("villager.max-villagers-reached", "&c你已达到最大村民数量限制!");
        messages.put("villager.recruited", "&a成功招募村民!");
        messages.put("villager.name", "&a{player}的村民");
        
        // 村庄相关消息
        messages.put("village.created", "&a村庄创建成功!");
        messages.put("village.not-found", "&c你还没有村庄!");
        messages.put("village.upgraded", "&a村庄升级成功!");
    }
    
    private void loadConfigMessages() {
        // 从配置文件加载消息，如果存在则覆盖默认消息
        if (messagesConfig != null) {
            for (String key : messagesConfig.getKeys(true)) {
                if (messagesConfig.isString(key)) {
                    messages.put(key, messagesConfig.getString(key));
                }
            }
        }
    }
    
    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', 
            messages.getOrDefault(key, "&c未找到消息: " + key));
    }
    
    public String getMessage(String key, Map<String, String> replacements) {
        String message = getMessage(key);
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }
    
    public java.util.List<String> getMessageList(String key) {
        if (messagesConfig != null && messagesConfig.isList(key)) {
            return messagesConfig.getStringList(key);
        }
        return new java.util.ArrayList<>();
    }
    
    public String getPrefix() {
        return getMessage("prefix");
    }
}