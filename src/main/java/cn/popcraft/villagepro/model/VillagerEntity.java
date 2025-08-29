package cn.popcraft.villagepro.model;

import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.UUID;

public class VillagerEntity {
    private final Villager villager;
    private final UUID ownerUuid;
    private FollowMode followMode;
    private VillagerProfession profession; // 添加职业字段

    public VillagerEntity(Villager villager, UUID ownerUuid) {
        this.villager = villager;
        this.ownerUuid = ownerUuid;
        this.followMode = FollowMode.NONE;
        this.profession = VillagerProfession.fromBukkit(villager.getProfession()); // 初始化职业
    }

    public Villager getBukkitEntity() {
        return villager;
    }
    
    public Villager getVillager() {
        return villager;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public FollowMode getFollowMode() {
        return followMode;
    }

    public void setFollowMode(FollowMode followMode) {
        this.followMode = followMode;
    }
    
    public VillagerProfession getProfession() {
        return profession;
    }
    
    public void setProfession(VillagerProfession profession) {
        this.profession = profession;
    }
    
    /**
     * 更新村民位置（跟随玩家）
     */
    public void updateLocation() {
        if (villager == null || !villager.isValid() || followMode != FollowMode.FOLLOW) {
            return;
        }
        
        Player owner = null;
        // 使用 Bukkit.getPlayer 方法获取玩家
        if (ownerUuid != null) {
            owner = Bukkit.getPlayer(ownerUuid);
        }
        
        if (owner == null || !owner.isOnline()) {
            return;
        }
        
        Location villagerLocation = villager.getLocation();
        Location ownerLocation = owner.getLocation();
        
        // 计算距离
        double distance = villagerLocation.distance(ownerLocation);
        
        // 如果距离太远，则传送村民到玩家附近
        if (distance > 16) {
            Location teleportLocation = ownerLocation.clone();
            teleportLocation.add(2, 0, 2);
            World ownerWorld = owner.getWorld();
            if (ownerWorld != null && ownerWorld.getNearbyEntities(teleportLocation, 1, 1, 1).isEmpty()) {
                villager.teleport(teleportLocation);
            }
        }
        // 如果距离适中，则让村民走向玩家
        else if (distance > 3) {
            Vector direction = ownerLocation.toVector().subtract(villagerLocation.toVector()).normalize();
            direction.multiply(0.3); // 控制移动速度
            villager.setVelocity(direction);
        }
    }
}