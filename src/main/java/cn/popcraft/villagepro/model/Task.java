package cn.popcraft.villagepro.model;

import java.util.UUID;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class Task {
    public enum TaskType {
        COLLECT_WHEAT,      // 采集稻草
        KILL_ZOMBIE,        // 击杀僵尸
        DELIVER_POTION,     // 送药水
        MINE_IRON,          // 挖铁矿石
        MINE_DIAMOND,       // 挖钻石
        BAKE_BREAD,         // 烤面包
        KILL_SKELETON,      // 击杀骷髅
        KILL_CREEPER,       // 击杀苦力怕
        REACH_LEVEL,        // 达到经验等级
        FISH_ITEM,          // 钓鱼
        CRAFT_ITEM,         // 制作物品
        ENCHANT_ITEM,       // 附魔物品
        BREED_ANIMAL,       // 繁殖动物
        HARVEST_CROP,       // 收获作物
        EXPLORE_BIOME,      // 探索生物群系
        TRADE_WITH_VILLAGER,// 与村民交易
        COLLECT_WOOD,       // 收集木材
        MINE_STONE,         // 挖掘石头
        KILL_SPIDER,        // 击杀蜘蛛
        KILL_ENDERMAN,      // 击杀末影人
        COLLECT_FLOWER,     // 收集花朵
        SHEAR_SHEEP,        // 剪羊毛
        MILK_COW,           // 挤牛奶
        TAME_ANIMAL,        // 驯服动物
        BREW_POTION         // 酿造药水
    }

    private UUID playerUuid;
    private UUID taskId;           // 兼容旧代码：任务唯一ID
    private TaskType type;
    private String targetItem;     // 兼容旧代码：目标物品
    private int targetAmount;
    private int progress;
    private int rewardExp;
    private double rewardMoney;
    private String description;    // 任务描述
    private List<ItemStack> itemRewards;

    public List<ItemStack> getItemRewards() {
        return itemRewards;
    }

    public void setItemRewards(List<ItemStack> itemRewards) {
        this.itemRewards = itemRewards;
    }

    public double getMoneyReward() {
        return rewardMoney;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCurrentProgress() {
        return progress;
    }

    public int getExpReward() {
        return rewardExp;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public void setRewardExp(int rewardExp) {
        this.rewardExp = rewardExp;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public void setRewardMoney(double rewardMoney) {
        this.rewardMoney = rewardMoney;
    }
    
    public double getRewardAmount() {
        return rewardMoney;
    }
    
    public String getRewardDescription() {
        StringBuilder desc = new StringBuilder();
        
        if (rewardExp > 0) {
            desc.append(rewardExp).append(" 经验");
        }
        
        if (rewardMoney > 0) {
            if (desc.length() > 0) desc.append(", ");
            desc.append(rewardMoney).append(" 金币");
        }
        
        return desc.toString();
    }

    // ===== 以下是为兼容旧代码新增的方法 =====
    public UUID getTaskId() {
        return taskId != null ? taskId : playerUuid; // 如果旧ID为空，返回玩家UUID
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public UUID getId() {
        return getTaskId();
    }

    public String getTargetItem() {
        return targetItem;
    }

    public void setTargetItem(String targetItem) {
        this.targetItem = targetItem;
    }
}