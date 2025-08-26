package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.Upgrade;
import cn.popcraft.villagepro.model.UpgradeType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final VillagePro plugin;
    private final Map<UpgradeType, Map<Integer, Upgrade>> upgradeConfigs = new HashMap<>();
    private double recruitCostMoney;
    private int maxVillagers;
    private Map<String, Integer> recruitCostItems = new HashMap<>();

    public ConfigManager(VillagePro plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        
        // 加载招募配置
        recruitCostMoney = config.getDouble("recruit.cost-money", 5000);
        maxVillagers = config.getInt("recruit.max-villagers", 5);
        
        // 加载招募所需物品
        ConfigurationSection recruitItemsSection = config.getConfigurationSection("recruit.cost-items");
        if (recruitItemsSection != null) {
            for (String itemKey : recruitItemsSection.getKeys(false)) {
                int amount = recruitItemsSection.getInt(itemKey);
                recruitCostItems.put(itemKey, amount);
            }
        }
        
        // 加载升级配置
        ConfigurationSection upgradesSection = config.getConfigurationSection("upgrades");
        if (upgradesSection != null) {
            for (String typeKey : upgradesSection.getKeys(false)) {
                UpgradeType type;
                try {
                    type = UpgradeType.valueOf(typeKey.toUpperCase());
                } catch (IllegalArgumentException e) {
                    if (plugin.getMessageManager() != null) {
                        plugin.getLogger().warning(plugin.getMessageManager().getMessage("config.invalid-upgrade-type", Map.of("type", typeKey)));
                    } else {
                        plugin.getLogger().warning("未知的升级类型: " + typeKey);
                    }
                    continue;
                }
                
                ConfigurationSection levelSection = upgradesSection.getConfigurationSection(typeKey);
                if (levelSection != null) {
                    Map<Integer, Upgrade> levelMap = new HashMap<>();
                    
                    for (String levelKey : levelSection.getKeys(false)) {
                        try {
                            int level = Integer.parseInt(levelKey);
                            ConfigurationSection upgradeSection = levelSection.getConfigurationSection(levelKey);
                            
                            if (upgradeSection != null) {
                                Upgrade upgrade = new Upgrade();
                                upgrade.setType(type);
                                upgrade.setLevel(level);
                                upgrade.setCostMoney(upgradeSection.getDouble("cost-money", 0));
                                upgrade.setCostDiamonds(upgradeSection.getInt("cost-diamonds", 0));
                                
                                // 加载升级所需物品
                                Map<String, Integer> costItems = new HashMap<>();
                                ConfigurationSection itemsSection = upgradeSection.getConfigurationSection("cost-items");
                                if (itemsSection != null) {
                                    for (String itemKey : itemsSection.getKeys(false)) {
                                        int amount = itemsSection.getInt(itemKey);
                                        costItems.put(itemKey, amount);
                                    }
                                }
                                upgrade.setCostItems(costItems);
                                
                                levelMap.put(level, upgrade);
                            }
                        } catch (NumberFormatException e) {
                            if (plugin.getMessageManager() != null) {
                                plugin.getLogger().warning(plugin.getMessageManager().getMessage("config.invalid-upgrade-level", Map.of("level", levelKey)));
                            } else {
                                plugin.getLogger().warning("无效的升级等级: " + levelKey);
                            }
                        }
                    }
                    
                    upgradeConfigs.put(type, levelMap);
                }
            }
        }
        
        plugin.getLogger().info("已加载 " + upgradeConfigs.size() + " 种升级类型配置");
    }

    public Upgrade getUpgrade(UpgradeType type, int level) {
        Map<Integer, Upgrade> levelMap = upgradeConfigs.get(type);
        return levelMap != null ? levelMap.get(level) : null;
    }

    public double getRecruitCostMoney() {
        return recruitCostMoney;
    }

    public int getMaxVillagers() {
        return maxVillagers;
    }

    public Map<String, Integer> getRecruitCostItems() {
        return recruitCostItems;
    }
}