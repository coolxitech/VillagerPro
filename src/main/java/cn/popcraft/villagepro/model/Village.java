package cn.popcraft.villagepro.model;


import cn.popcraft.villagepro.model.UpgradeType;
import java.util.*;

public class Village {
    private UUID ownerUuid;   // 玩家 UUID

    private List<UUID> villagerIds = new ArrayList<>(); // 已招募村民 UUID

    private Map<UpgradeType, Integer> upgradeLevels = new HashMap<>();
    
    private Map<String, Integer> cropStorage = new HashMap<>(); // 作物存储

    private boolean followEnabled = false; // 是否启用跟随

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public List<UUID> getVillagerIds() {
        return villagerIds;
    }

    public void setVillagerIds(List<UUID> villagerIds) {
        this.villagerIds = villagerIds;
    }

    public Map<UpgradeType, Integer> getUpgradeLevels() {
        return upgradeLevels;
    }

    public void setUpgradeLevels(Map<UpgradeType, Integer> upgradeLevels) {
        this.upgradeLevels = upgradeLevels;
    }
    
    public Map<String, Integer> getCropStorage() {
        return cropStorage;
    }
    
    public void setCropStorage(Map<String, Integer> cropStorage) {
        this.cropStorage = cropStorage != null ? cropStorage : new HashMap<>();
    }
    
    public int getCropAmount(String cropType) {
        return cropStorage.getOrDefault(cropType.toLowerCase(), 0);
    }
    
    public void addCrop(String cropType, int amount) {
        if (amount <= 0) return;
        String key = cropType.toLowerCase();
        cropStorage.put(key, cropStorage.getOrDefault(key, 0) + amount);
    }
    
    public boolean removeCrop(String cropType, int amount) {
        if (amount <= 0) return false;
        String key = cropType.toLowerCase();
        int current = cropStorage.getOrDefault(key, 0);
        if (current < amount) return false;
        
        if (current == amount) {
            cropStorage.remove(key);
        } else {
            cropStorage.put(key, current - amount);
        }
        return true;
    }

    public boolean isFollowEnabled() {
        return followEnabled;
    }

    public void setFollowEnabled(boolean followEnabled) {
        this.followEnabled = followEnabled;
    }

    public int getUpgradeLevel(UpgradeType type) {
        return upgradeLevels.getOrDefault(type, 0);
    }

    public void setUpgradeLevel(UpgradeType type, int level) {
        upgradeLevels.put(type, level);
    }
}