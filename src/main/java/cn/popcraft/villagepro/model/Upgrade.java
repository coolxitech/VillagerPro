package cn.popcraft.villagepro.model;

import org.simplelite.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Table(name = "upgrades")
public class Upgrade {
    @PrimaryKey
    @Column
    private UpgradeType type;   // enum: TRADE, HEALTH, SPEED, PROTECTION

    @Column
    private int level;        // 1-5

    // 该升级的消耗
    @Column
    private double costMoney;
    
    @Column
    private int costDiamonds;
    
    @Column
    private Map<String, Integer> costItems = new HashMap<>();

    public UpgradeType getType() {
        return type;
    }

    public void setType(UpgradeType type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getCostMoney() {
        return costMoney;
    }

    public void setCostMoney(double costMoney) {
        this.costMoney = costMoney;
    }

    public int getCostDiamonds() {
        return costDiamonds;
    }

    public void setCostDiamonds(int costDiamonds) {
        this.costDiamonds = costDiamonds;
    }

    public Map<String, Integer> getCostItems() {
        return costItems;
    }

    public void setCostItems(Map<String, Integer> costItems) {
        this.costItems = costItems;
    }
}