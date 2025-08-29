package cn.popcraft.villagepro.model;


import cn.popcraft.villagepro.model.UpgradeType;
import java.util.*;

public class Village {
    private UUID ownerUuid;   // 玩家 UUID

    private List<UUID> villagerIds = new ArrayList<>(); // 已招募村民 UUID

    private Map<UpgradeType, Integer> upgradeLevels = new HashMap<>();

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

    public int getUpgradeLevel(UpgradeType type) {
        return upgradeLevels.getOrDefault(type, 0);
    }

    public void setUpgradeLevel(UpgradeType type, int level) {
        upgradeLevels.put(type, level);
    }
}