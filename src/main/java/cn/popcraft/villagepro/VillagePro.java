package cn.popcraft.villagepro;

import cn.popcraft.villagepro.command.RecruitCommand;
import cn.popcraft.villagepro.command.UpgradeCommand;
import cn.popcraft.villagepro.command.VillageCommand;
import cn.popcraft.villagepro.listener.VillagerListener;
import cn.popcraft.villagepro.manager.ConfigManager;
import cn.popcraft.villagepro.manager.FollowManager;
import cn.popcraft.villagepro.manager.VillageManager;
import cn.popcraft.villagepro.model.VillagerEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.simplelite.SimpleDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagePro extends JavaPlugin {
    private static VillagePro instance;
    private SimpleDatabase database;
    private VillageManager villageManager;
    private ConfigManager configManager;
    private FollowManager followManager;
    private final Map<UUID, VillagerEntity> villagerEntities = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化配置管理器
        this.configManager = new ConfigManager(this);
        
        // 初始化数据库
        this.database = new SimpleDatabase(this, "villages");
        
        // 初始化村庄管理器
        this.villageManager = new VillageManager(this);
        
        // 初始化跟随管理器
        this.followManager = new FollowManager(this);
        
        // 注册命令
        getCommand("village").setExecutor(new VillageCommand(this));
        getCommand("recruit").setExecutor(new RecruitCommand(this));
        getCommand("upgrade").setExecutor(new UpgradeCommand(this));
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        
        // 加载在线村民
        loadOnlineVillagers();
        
        getLogger().info("VillagePro 插件已启用!");
    }

    @Override
    public void onDisable() {
        // 保存数据
        if (villageManager != null) {
            villageManager.saveAll();
        }
        
        // 关闭数据库连接
        if (database != null) {
            database.close();
        }
        
        getLogger().info("VillagePro 插件已禁用!");
    }
    
    /**
     * 加载所有在线村民
     */
    private void loadOnlineVillagers() {
        getServer().getWorlds().forEach(world -> 
            world.getEntitiesByClass(org.bukkit.entity.Villager.class).forEach(villager -> {
                UUID owner = cn.popcraft.villagepro.util.VillagerUtils.getOwner(villager);
                if (owner != null) {
                    VillagerEntity villagerEntity = new VillagerEntity(villager, owner);
                    villagerEntities.put(villager.getUniqueId(), villagerEntity);
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
    public SimpleDatabase getDatabase() {
        return database;
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
}