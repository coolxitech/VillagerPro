// src/main/java/cn/popcraft/villagepro/model/CropStorage.java
package cn.popcraft.villagepro.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CropStorage {
    private UUID playerId;
    private Map<String, Integer> crops;
    
    public CropStorage(UUID playerId) {
        this.playerId = playerId;
        this.crops = new HashMap<>();
    }
    
    public CropStorage(UUID playerId, Map<String, Integer> crops) {
        this.playerId = playerId;
        this.crops = crops != null ? crops : new HashMap<>();
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public Map<String, Integer> getCrops() {
        return new HashMap<>(crops);
    }
    
    public int getCropAmount(String cropType) {
        return crops.getOrDefault(cropType.toLowerCase(), 0);
    }
    
    public void addCrop(String cropType, int amount) {
        if (amount <= 0) return;
        String key = cropType.toLowerCase();
        crops.put(key, crops.getOrDefault(key, 0) + amount);
    }
    
    public boolean removeCrop(String cropType, int amount) {
        if (amount <= 0) return false;
        String key = cropType.toLowerCase();
        int current = crops.getOrDefault(key, 0);
        if (current < amount) return false;
        
        if (current == amount) {
            crops.remove(key);
        } else {
            crops.put(key, current - amount);
        }
        return true;
    }
    
    public void setCropAmount(String cropType, int amount) {
        if (amount <= 0) {
            crops.remove(cropType.toLowerCase());
        } else {
            crops.put(cropType.toLowerCase(), amount);
        }
    }
    
    public boolean hasCrop(String cropType, int amount) {
        return getCropAmount(cropType) >= amount;
    }
    
    public boolean isEmpty() {
        return crops.isEmpty();
    }
    
    public int getTotalCrops() {
        return crops.values().stream().mapToInt(Integer::intValue).sum();
    }
}
