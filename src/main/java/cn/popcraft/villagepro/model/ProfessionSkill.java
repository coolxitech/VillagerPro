package cn.popcraft.villagepro.model;

/**
 * 村民职业技能枚举
 */
public enum ProfessionSkill {
    // 基础技能
    SPEED("速度", "增加移动速度"),
    WATER_BREATHING("水下呼吸", "在水下呼吸"),
    NIGHT_VISION("夜视", "在黑暗中视物"),
    STRENGTH("力量", "增加攻击力"),
    RESISTANCE("抗性", "减少受到的伤害"),
    JUMP_BOOST("跳跃提升", "增加跳跃高度"),
    
    // 高级技能
    REGENERATION("生命恢复", "持续恢复生命值"),
    FIRE_RESISTANCE("火焰抗性", "免疫火焰伤害"),
    INVISIBILITY("隐身", "使村民隐身"),
    LUCK("幸运", "增加幸运值"),
    
    // 职业特定技能
    FARMING("农业", "提高农作物产量"),
    FISHING("钓鱼", "提高钓鱼效率"),
    MINING("挖掘", "提高挖掘速度"),
    ENCHANTING("附魔", "提高附魔能力"),
    BREEDING("繁殖", "提高动物繁殖效率"),
    HEALING("治疗", "提高治疗能力");

    private final String displayName;
    private final String description;

    ProfessionSkill(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}