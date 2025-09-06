package cn.popcraft.villagepro;

import cn.popcraft.villagepro.command.*;
import cn.popcraft.villagepro.gui.ProductionGUI;
import cn.popcraft.villagepro.gui.TaskGUI;
import cn.popcraft.villagepro.gui.UpgradeGUI;
import cn.popcraft.villagepro.listener.*;
import cn.popcraft.villagepro.manager.*;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.storage.VillageStorage;
import cn.popcraft.villagepro.util.VillagerUtils;
import com.google.gson.Gson;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final Map<UUID, VillagerEntity> villagerEntities = new HashMap<>();
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
        
        // 启动定时任务
        startVillagerFollowTask();
        startAutoSaveTask();
        
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
    
    // 启动自动保存任务
    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            // 保存所有村庄数据
            getVillageManager().saveAll();
            
            // 保存所有任务数据
            getTaskManager().saveAll();
            
            // 保存所有作物数据
            getCropManager().saveAll();
            
            getLogger().info("自动保存所有数据完成");
        }, 6000L, 6000L); // 每5分钟保存一次 (6000 ticks = 5 minutes)
    }
}