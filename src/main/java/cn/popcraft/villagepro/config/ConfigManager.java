package cn.popcraft.villagepro.config;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.Upgrade;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import java.util.Map;
import java.util.HashMap;


public class ConfigManager {
    private final VillagePro plugin;
    
    public ConfigManager(VillagePro plugin) {
        this.plugin = plugin;
    }
    
    public int getMaxVillagers() {
        return plugin.getConfig().getInt("villager.max-count", 10);
    }
    
    public double getRecruitCostMoney() {
        return plugin.getConfig().getDouble("villager.cost.money", 0.0);
    }
    
    public Map<String, Integer> getRecruitCostItems() {
        Map<String, Integer> costItems = new HashMap<>();
        FileConfiguration config = plugin.getConfig();
        if (config.isConfigurationSection("villager.cost.items")) {
            for (String key : config.getConfigurationSection("villager.cost.items").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    int amount = config.getInt("villager.cost.items." + key, 0);
                    if (amount > 0) {
                        costItems.put(material.name(), amount);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的物品配置: " + key);
                }
            }
        }
        return costItems;
    }
    
    public Upgrade getUpgrade(String upgradeType, int nextLevel) {
        // 实现根据升级类型和等级获取升级配置
        return null;
    }
}