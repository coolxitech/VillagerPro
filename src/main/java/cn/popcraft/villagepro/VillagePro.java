package cn.popcraft.villagepro;

import cn.popcraft.villagepro.command.*;
import cn.popcraft.villagepro.config.ConfigManager;
import cn.popcraft.villagepro.gui.ProductionGUI;
import cn.popcraft.villagepro.gui.TaskGUI;
import cn.popcraft.villagepro.gui.UpgradeGUI;
import cn.popcraft.villagepro.listener.*;
import cn.popcraft.villagepro.manager.*;
import cn.popcraft.villagepro.model.Village;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.storage.SQLiteStorage;
import cn.popcraft.villagepro.storage.VillageStorage;
import cn.popcraft.villagepro.util.ItemNameUtil;
import cn.popcraft.villagepro.util.VillagerUtils;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class VillagePro extends JavaPlugin {
    private static VillagePro instance;
    
    // Managers
    private MessageManager messageManager;
    private ConfigManager configManager;
    private VillageManager villageManager;
    private CropManager cropManager;
    private TaskManager taskManager;
    private VillagerSkillManager villagerSkillManager;
    private FollowManager followManager;
    private EconomyManager economyManager;
    private QuestsIntegrationManager questsIntegrationManager; // 添加Quests集成管理器

    // GUIs
    private ProductionGUI productionGUI;
    private UpgradeGUI upgradeGUI;
    private TaskGUI taskGUI;
    
    // Storage
    private VillageStorage villageStorage;
    
    // Villager entities map
    private final Map<UUID, VillagerEntity> villagerEntities = new HashMap<UUID, VillagerEntity>();
    private final Gson gson = new Gson();
    private final Random random = new Random();
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置文件
        saveDefaultConfig();
        
        // 初始化配置管理器
        this.configManager = new ConfigManager(this);
        
        // 初始化消息管理器
        this.messageManager = new MessageManager(this);
        
        // 初始化存储
        villageStorage = new VillageStorage(this, gson);
        
        // 确保数据库表已初始化
        try {
            // 手动触发表初始化
            java.lang.reflect.Method initMethod = villageStorage.getClass().getDeclaredMethod("initializeTables");
            initMethod.setAccessible(true);
            initMethod.invoke(villageStorage);
            getLogger().info("数据库表初始化完成");
        } catch (Exception e) {
            getLogger().warning("手动初始化数据库表时发生错误: " + e.getMessage());
        }
        
        // 初始化经济管理器
        this.economyManager = new EconomyManager(this);
        
        // 初始化其他管理器
        this.villageManager = new VillageManager(this);
        this.cropManager = new CropManager(this);
        this.taskManager = new TaskManager(this);
        this.villagerSkillManager = new VillagerSkillManager(this);
        this.followManager = new FollowManager(this);
        this.questsIntegrationManager = new QuestsIntegrationManager(this); // 初始化Quests集成管理器
        
        // 加载所有村庄数据
        this.villageManager.loadAll();
        
        // 加载所有作物数据
        this.cropManager.loadAll();
        
        // 加载所有任务数据
        this.taskManager.loadAll();
        
        // 初始化GUI（在所有管理器初始化之后）
        this.productionGUI = new ProductionGUI(this);
        this.upgradeGUI = new UpgradeGUI(this);
        this.taskGUI = new TaskGUI(this, taskManager);
        
        // 注册命令
        this.getCommand("villager").setExecutor(new VillagerCommand(this));
        this.getCommand("village").setExecutor(new VillageCommand(this));
        this.getCommand("crop").setExecutor(new CropCommand(this));
        this.getCommand("recruit").setExecutor(new RecruitCommand(this));
        this.getCommand("upgrade").setExecutor(new UpgradeCommand(this));
        this.getCommand("task").setExecutor(new TaskCommand(this));
        
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new VillagerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CropListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SkillListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VillageProTaskListener(this, taskManager), this);

        // 注册 Quests 事件监听器（如果 Quests 插件已加载）
        if (getServer().getPluginManager().isPluginEnabled("Quests")) {
            try {
                Bukkit.getPluginManager().registerEvents(new QuestsEventListener(this), this);
                getLogger().info("已注册 Quests 任务完成监听器");
            } catch (Exception e) {
                getLogger().warning("注册 Quests 事件监听器时出错: " + e.getMessage());
            }
        }
        
        // 启动定时任务
        startVillagerFollowTask();
        startAutoSaveTask();

        // 物品名称映射初始化
        ItemNameUtil.init(this);
        
        getLogger().info("VillagePro 已启用!");
    }
    
    @Override
    public void onDisable() {
        // 清理村民实体
        for (VillagerEntity villagerEntity : villagerEntities.values()) {
            org.bukkit.entity.Villager villager = villagerEntity.getBukkitEntity();
            if (villager != null) {
                // 清除自定义名称和所有者信息
                villager.setCustomName(null);
                villager.setCustomNameVisible(false);
                VillagerUtils.setOwner(villager, null);
            }
        }
        
        // 保存并关闭所有数据管理器
        if (villageManager != null) {
            villageManager.close();
        }
        
        if (cropManager != null) {
            cropManager.close();
        }
        
        getLogger().info("VillagePro 已禁用!");
    }
    
    // 获取插件实例
    public static VillagePro getInstance() {
        return instance;
    }
    
    // 获取管理器
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public VillageManager getVillageManager() {
        return villageManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public VillagerSkillManager getVillagerSkillManager() {
        return villagerSkillManager;
    }
    
    public FollowManager getFollowManager() {
        return followManager;
    }
    
    /**
     * 获取Quests集成管理器
     * @return Quests集成管理器实例
     */
    public QuestsIntegrationManager getQuestsIntegrationManager() {
        return questsIntegrationManager;
    }
    
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    public CropManager getCropManager() {
        return cropManager;
    }
    
    // 获取GUI
    public ProductionGUI getProductionGUI() {
        return productionGUI;
    }
    
    public UpgradeGUI getUpgradeGUI() {
        return upgradeGUI;
    }
    
    public TaskGUI getTaskGUI() {
        return taskGUI;
    }
    
    // 获取存储
    public VillageStorage getDatabase() {
        return villageStorage;
    }
    
    // 获取村民实体映射
    public Map<UUID, VillagerEntity> getVillagerEntities() {
        return villagerEntities;
    }
    
    // 获取Gson实例
    public Gson getGson() {
        return gson;
    }
    
    // 获取随机数生成器
    public Random getRandom() {
        return random;
    }
    
    // 启动村民跟随任务
    private void startVillagerFollowTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (VillagerEntity villagerEntity : villagerEntities.values()) {
                org.bukkit.entity.Villager villager = villagerEntity.getBukkitEntity();
                if (villager != null && villager.isValid()) {
                    villagerEntity.updateLocation();
                }
            }
        }, 20L, 20L); // 每秒更新一次
    }
    
    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        // 每30分钟保存一次数据
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            villageManager.saveAll();
            taskManager.saveAll();
            getLogger().info("数据已自动保存");
        }, 36000L, 36000L); // 36000 ticks = 30 minutes
    }
    
    /**
     * 根据任务名称计算应发放的积分
     * 可以根据任务名称、难度等来决定积分数量
     */
    public int calculatePointsForQuest(String questName) {
        // 这里可以根据任务名称来决定奖励的积分数量
        // 你可以根据需要修改这个逻辑
        switch (questName.toLowerCase()) {
            case "daily_mining":
                return 50;
            case "epic_boss_kill":
                return 200;
            case "收集木材":
                return 30;
            case "挖矿任务":
                return 80;
            case "击败僵尸":
                return 40;
            case "建造房屋":
                return 100;
            case "钓鱼任务":
                return 60;
            case "农业任务":
                return 50;
            case "探险任务":
                return 120;
            default:
                return 30; // 默认奖励积分
        }
    }
}