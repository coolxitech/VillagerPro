package cn.popcraft.villagepro.model;

import org.bukkit.entity.Villager;
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
}