package cn.popcraft.villagepro.model;

import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.util.VillagerUtils;
import java.util.UUID;

public class VillagerEntity {
    private final UUID villagerId;
    private final UUID ownerId;
    private long lastInteractionTime;
    private boolean isWorking;
    private Villager bukkitEntity;
    private FollowMode followMode = FollowMode.STAY; // 默认跟随模式

    public VillagerEntity(Villager villager, UUID ownerId) {
        this.villagerId = villager.getUniqueId();
        this.ownerId = ownerId;
        this.bukkitEntity = villager;
        this.lastInteractionTime = System.currentTimeMillis();
        this.isWorking = false;
        
        // 保存所有者ID到村民的持久化数据中
        saveOwnerToPersistentData(villager);
    }

    public UUID getVillagerId() {
        return villagerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public long getLastInteractionTime() {
        return lastInteractionTime;
    }

    public void setLastInteractionTime(long lastInteractionTime) {
        this.lastInteractionTime = lastInteractionTime;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }
    
    public Villager getBukkitEntity() {
        return bukkitEntity;
    }
    
    public void setBukkitEntity(Villager bukkitEntity) {
        this.bukkitEntity = bukkitEntity;
    }
    
    public FollowMode getFollowMode() {
        return followMode;
    }
    
    public void setFollowMode(FollowMode followMode) {
        this.followMode = followMode;
    }
    
    public void updateLocation() {
        // 更新位置的逻辑可以在这里实现
        // 当前为空实现，后续可根据需要添加
    }

    public static VillagerEntity fromEntity(Villager villager) {
        UUID ownerId = VillagerUtils.getOwner(villager);
        if (ownerId == null) {
            return null;
        }
        return new VillagerEntity(villager, ownerId);
    }
    
    private void saveOwnerToPersistentData(Villager villager) {
        PersistentDataContainer data = villager.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(VillagePro.getInstance(), "villager_owner");
        data.set(key, PersistentDataType.STRING, ownerId.toString());
    }
}