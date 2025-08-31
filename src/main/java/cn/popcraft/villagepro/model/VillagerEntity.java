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
        // 只有在跟随模式下才需要更新位置
        if (followMode != FollowMode.FOLLOW || bukkitEntity == null || !bukkitEntity.isValid()) {
            return;
        }
        
        // 获取所有者玩家
        VillagePro plugin = VillagePro.getInstance();
        org.bukkit.entity.Player owner = plugin.getServer().getPlayer(ownerId);
        
        // 检查所有者是否在线且在同一世界
        if (owner == null || !owner.isOnline() || !owner.getWorld().equals(bukkitEntity.getWorld())) {
            return;
        }
        
        // 计算距离
        double distance = owner.getLocation().distance(bukkitEntity.getLocation());
        
        // 如果距离太远（超过30格），传送村民到玩家附近
        if (distance > 30) {
            org.bukkit.Location teleportLocation = owner.getLocation().clone().add(2, 0, 2);
            bukkitEntity.teleport(teleportLocation);
        }
        // 如果距离适中（5-30格），让村民走向玩家
        else if (distance > 5) {
            // 当距离适中时，村民会自然地朝向玩家
            // 这里可以留空或添加其他
        }
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