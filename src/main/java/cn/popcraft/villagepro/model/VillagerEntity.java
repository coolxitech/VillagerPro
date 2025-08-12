package cn.popcraft.villagepro.model;

import org.bukkit.entity.Villager;
import java.util.UUID;

public class VillagerEntity {
    private final Villager villager;
    private final UUID ownerUuid;
    private FollowMode followMode;

    public VillagerEntity(Villager villager, UUID ownerUuid) {
        this.villager = villager;
        this.ownerUuid = ownerUuid;
        this.followMode = FollowMode.NONE;
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
}