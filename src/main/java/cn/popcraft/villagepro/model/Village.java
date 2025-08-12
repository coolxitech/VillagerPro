package cn.popcraft.villagepro.model;

import org.simplelite.annotation.*;
import java.util.*;

@Table(name = "villages")
public class Village {
    @PrimaryKey
    private UUID ownerUuid;   // 玩家 UUID

    @Column
    private List<UUID> villagerIds = new ArrayList<>(); // 已招募村民 UUID

    @Column
    private Map<UpgradeType, Integer> upgradeLevels = new HashMap<>();

    @Column
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

    public boolean isFollowEnabled() {
        return followEnabled;
    }

    public void setFollowEnabled(boolean followEnabled) {
        this.followEnabled = followEnabled;
    }
}