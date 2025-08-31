package cn.popcraft.villagepro.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class VillageUpgrade {
    private static final long serialVersionUID = 1L;
    private UUID villageId;
    private Map<ProfessionSkill, Integer> skillLevels = new HashMap<>();
    private int level;
    private long lastUpgraded;
    private boolean isUpgrading;
    
    public VillageUpgrade() {
        this.level = 0;
        this.lastUpgraded = 0;
        this.isUpgrading = false;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
        this.lastUpgraded = System.currentTimeMillis();
    }
    
    public long getLastUpgraded() {
        return lastUpgraded;
    }
    
    public boolean isUpgrading() {
        return isUpgrading;
    }
    
    public void setUpgrading(boolean upgrading) {
        this.isUpgrading = upgrading;
    }

    public UUID getVillageId() {
        return villageId;
    }

    public void setVillageId(UUID villageId) {
        this.villageId = villageId;
    }

    public int getSkillLevel(ProfessionSkill skill) {
        return skillLevels.getOrDefault(skill, 0);
    }

    public void setSkillLevel(ProfessionSkill skill, int level) {
        skillLevels.put(skill, level);
    }

    @Override
    public String toString() {
        return "VillageUpgrade{villageId=" + villageId + ", skillLevels=" + skillLevels + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VillageUpgrade that = (VillageUpgrade) o;
        return Objects.equals(villageId, that.villageId) &&
                Objects.equals(skillLevels, that.skillLevels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(villageId, skillLevels);
    }

    @Override
    public VillageUpgrade clone() {
        try {
            VillageUpgrade clone = (VillageUpgrade) super.clone();
            clone.skillLevels = new HashMap<>(this.skillLevels);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
}