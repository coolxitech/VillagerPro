package cn.popcraft.villagepro.config;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.Upgrade;
import cn.popcraft.villagepro.model.UpgradeType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


public class ConfigManager {
    private final VillagePro plugin;
    private int maxVillagers;
    private double recruitCostMoney;
    private int recruitCostDiamonds;
    private int recruitCostPoints;
    private Map<String, Integer> recruitCostItems;
    
    public ConfigManager(VillagePro plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        maxVillagers = config.getInt("villager.max-count", 10);
        recruitCostMoney = config.getDouble("villager.cost.money", 0.0);
        recruitCostDiamonds = config.getInt("villager.cost.diamonds", 0);
        recruitCostPoints = config.getInt("villager.cost.points", 0);
        
        recruitCostItems = new HashMap<>();
        if (config.isConfigurationSection("villager.cost.items")) {
            for (String key : config.getConfigurationSection("villager.cost.items").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    int amount = config.getInt("villager.cost.items." + key, 0);
                    if (amount > 0) {
                        recruitCostItems.put(material.name(), amount);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的物品配置: " + key);
                }
            }
        }
    }
    
    public int getMaxVillagers() {
        return maxVillagers;
    }
    
    public double getRecruitCostMoney() {
        return recruitCostMoney;
    }
    
    public int getRecruitCostDiamonds() {
        return recruitCostDiamonds;
    }
    
    public int getRecruitCostPoints() {
        return recruitCostPoints;
    }
    
    public Map<String, Integer> getRecruitCostItems() {
        return recruitCostItems;
    }
    
    public Upgrade getUpgrade(UpgradeType upgradeType, int level) {
        FileConfiguration config = plugin.getConfig();
        String path = "upgrades." + upgradeType.name() + "." + level;
        
        if (!config.contains(path)) {
            return null;
        }
        
        double costMoney = config.getDouble(path + ".cost-money", 0);
        double costDiamonds = config.getDouble(path + ".cost-diamonds", 0);
        int costPoints = config.getInt(path + ".cost-points", 0);
        
        Map<String, Integer> costItems = new HashMap<>();
        if (config.isConfigurationSection(path + ".cost-items")) {
            for (String key : config.getConfigurationSection(path + ".cost-items").getKeys(false)) {
                int amount = config.getInt(path + ".cost-items." + key, 0);
                if (amount > 0) {
                    costItems.put(key, amount);
                }
            }
        }
        
        Upgrade upgrade = new Upgrade();
        upgrade.setCostMoney(costMoney);
        upgrade.setCostDiamonds(costDiamonds);
        upgrade.setCostPoints(costPoints);
        upgrade.getCostItems().putAll(costItems);
        return upgrade;
    }
}