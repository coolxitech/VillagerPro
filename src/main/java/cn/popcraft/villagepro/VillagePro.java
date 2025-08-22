package cn.popcraft.villagepro;

import cn.popcraft.villagepro.command.CropCommand;
import cn.popcraft.villagepro.command.RecruitCommand;
import cn.popcraft.villagepro.command.UpgradeCommand;
import cn.popcraft.villagepro.command.VillageCommand;
import cn.popcraft.villagepro.command.VillagerCommand;
import cn.popcraft.villagepro.listener.CropListener;
import cn.popcraft.villagepro.listener.GUIListener;
import cn.popcraft.villagepro.listener.VillagerListener;
import cn.popcraft.villagepro.listener.SkillListener;
import cn.popcraft.villagepro.manager.ConfigManager;
import cn.popcraft.villagepro.manager.CropManager;
import cn.popcraft.villagepro.manager.FollowManager;
import cn.popcraft.villagepro.manager.MessageManager;
import cn.popcraft.villagepro.manager.VillageManager;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.manager.VillagerSkillManager;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.storage.SQLiteStorage;
import cn.popcraft.villagepro.util.VillagerUtils;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import net.milkbowl.vault.economy.Economy;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class VillagePro extends JavaPlugin {
    private static VillagePro instance;
    private SQLiteStorage database;
    private VillageManager villageManager;
    private ConfigManager configManager;
    private FollowManager followManager;
    private CropManager cropManager;
    private MessageManager messageManager;
    private TaskManager taskManager;
    private VillagerSkillManager villagerSkillManager;
    private VillagerListener villagerListener;
    private Economy economy;
    private final Map<UUID, VillagerEntity> villagerEntities = new HashMap<>();
    private final Random random = new Random();
    private final Gson gson = new Gson();

    @Override
    public void onEnable() {
        // 设置经济系统
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化配置管理器
        this.configManager = new ConfigManager(this);
        
        // 初始化数据库
        this.database = new SQLiteStorage(this, gson);
        
        // 初始化村庄管理器
        this.villageManager = new VillageManager(this);
        
        // 初始化任务管理器
        this.taskManager = new TaskManager(this);
        
        // 初始化跟随管理器
        this.followManager = new FollowManager(this);
        
        // 初始化作物管理器
        this.cropManager = new CropManager(this);
        
        // 初始化消息管理器
        this.messageManager = new MessageManager(this);
        
        // 初始化村民技能管理器
        this.villagerSkillManager = new VillagerSkillManager(this);
        
        // 初始化经济系统
        setupEconomy();
        
        // 初始化村民监听器
        this.villagerListener = new VillagerListener(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new cn.popcraft.villagepro.listener.VillageProTaskListener(this, this.taskManager), this);
        getServer().getPluginManager().registerEvents(new SkillListener(this), this);
        getServer().getPluginManager().registerEvents(villagerListener, this);
        getServer().getPluginManager().registerEvents(new CropListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        // 注册命令
        getCommand("village").setExecutor(new VillageCommand(this));
        getCommand("recruit").setExecutor(new RecruitCommand(this));
        getCommand("upgrade").setExecutor(new UpgradeCommand(this));
        getCommand("crop").setExecutor(new CropCommand(this));
        
        // 注册Tab补全
        VillagerCommand villagerCommand = new VillagerCommand(this);
        getCommand("villager").setExecutor(villagerCommand);
        getCommand("villager").setTabCompleter(villagerCommand);
        
        // 加载在线村民
        loadOnlineVillagers();
        
        // 启动农民收获任务
        villagerListener.startFarmerHarvestTask();
        
        getLogger().info("VillagePro 插件已启用!");
    }

    @Override
    public void onDisable() {
        // 保存数据
        if (villageManager != null) {
            villageManager.saveAll();
        }
        
        // 保存作物数据并关闭连接
        if (cropManager != null) {
            cropManager.close();
        }
        
        // 关闭数据库连接
        if (database != null) {
            database.close();
        }
        
        getLogger().info("VillagePro 插件已禁用!");
    }
    
    /**
     * 设置经济系统
     */
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("未找到 Vault 插件，经济功能将被禁用!");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("未找到经济系统，经济功能将被禁用!");
            return;
        }
        
        economy = rsp.getProvider();
        getLogger().info("已连接到经济系统: " + economy.getName());
    }
    
    /**
     * 加载所有在线村民
     */
    private void loadOnlineVillagers() {
        getServer().getWorlds().forEach(world -> 
            world.getEntitiesByClass(org.bukkit.entity.Villager.class).forEach(villager -> {
                UUID owner = VillagerUtils.getOwner(villager);
                if (owner != null) {
                    VillagerEntity villagerEntity = new VillagerEntity(villager, owner);
                    villagerEntities.put(villager.getUniqueId(), villagerEntity);
                    // 应用村民技能
                    villagerSkillManager.applyVillagerSkills(villager);
                }
            })
        );
        
        getLogger().info("已加载 " + villagerEntities.size() + " 个在线村民");
    }

    @NotNull
    public static VillagePro getInstance() {
        return instance;
    }

    @NotNull
    public Economy getEconomy() {
        return this.economy;
    }
    
    @NotNull
    public Random getRandom() {
        return random;
    }
    
    @NotNull
    public Gson getGson() {
        return gson;
    }

    @NotNull
    public VillageManager getVillageManager() {
        return villageManager;
    }
    
    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    @NotNull
    public FollowManager getFollowManager() {
        return followManager;
    }

    @NotNull
    public Map<UUID, VillagerEntity> getVillagerEntities() {
        return villagerEntities;
    }
    
    @NotNull
    public CropManager getCropManager() {
        return cropManager;
    }
    
    @NotNull
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    @NotNull
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    @NotNull
    public VillagerSkillManager getVillagerSkillManager() {
        return villagerSkillManager;
    }
    
    @NotNull
    public SQLiteStorage getDatabase() {
        return database;
    }
    
}