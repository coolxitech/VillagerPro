package cn.popcraft.villagepro.model;

import java.util.HashMap;
import java.util.Map;

public class Upgrade {
    private UpgradeType type;
    private int level;
    private String name;
    private double costMoney;
    private double costDiamonds;
    private int costPoints; // 新增点券成本属性
    private Map<String, Integer> costItems = new HashMap<>();
    private int levelRequirement;
    private double timeRequired;

    public Upgrade() {
        this.costItems = new HashMap<>();
        this.levelRequirement = 0;
        this.timeRequired = 0;
    }

    public Upgrade(String name, double costMoney, double costDiamonds, Map<String, Integer> costItems) {
        this.name = name;
        this.costMoney = costMoney;
        this.costDiamonds = costDiamonds;
        this.costItems = costItems;
        this.levelRequirement = 0;
        this.timeRequired = 0;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCostMoney() {
        return costMoney;
    }

    public void setCostMoney(double costMoney) {
        this.costMoney = costMoney;
    }

    public double getCostDiamonds() {
        return costDiamonds;
    }

    public void setCostDiamonds(double costDiamonds) {
        this.costDiamonds = costDiamonds;
    }

    public Map<String, Integer> getCostItems() {
        return costItems;
    }

    public void setCostItems(Map<String, Integer> costItems) {
        this.costItems = costItems;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public void setLevelRequirement(int levelRequirement) {
        this.levelRequirement = levelRequirement;
    }

    public double getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(double timeRequired) {
        this.timeRequired = timeRequired;
    }

    public int getCostPoints() {
        return costPoints;
    }

    public void setCostPoints(int costPoints) {
        this.costPoints = costPoints;
    }
}