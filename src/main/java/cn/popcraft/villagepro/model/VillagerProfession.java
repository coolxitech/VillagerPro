package cn.popcraft.villagepro.model;

import org.bukkit.entity.Villager.Profession;
import java.util.Set;
import java.util.HashSet;

/**
 * 村民职业枚举，包含职业技能
 */
public enum VillagerProfession {
    // 农民 - 拥有农业和收获技能
    FARMER("农民", Set.of(ProfessionSkill.FARMING, ProfessionSkill.SPEED, ProfessionSkill.REGENERATION)),
    
    // 渔夫 - 拥有钓鱼和水下呼吸技能
    FISHERMAN("渔夫", Set.of(ProfessionSkill.FISHING, ProfessionSkill.WATER_BREATHING, ProfessionSkill.LUCK)),
    
    // 图书管理员 - 拥有附魔和知识技能
    LIBRARIAN("图书管理员", Set.of(ProfessionSkill.ENCHANTING, ProfessionSkill.NIGHT_VISION, ProfessionSkill.LUCK)),
    
    // 武器匠 - 拥有武器锻造和战斗技能
    WEAPONSMITH("武器匠", Set.of(ProfessionSkill.STRENGTH, ProfessionSkill.RESISTANCE, ProfessionSkill.FIRE_RESISTANCE)),
    
    // 牧羊人 - 拥有动物繁殖和羊毛处理技能
    SHEPHERD("牧羊人", Set.of(ProfessionSkill.BREEDING, ProfessionSkill.SPEED, ProfessionSkill.JUMP_BOOST)),
    
    // 制箭师 - 拥有弓箭和箭矢制作技能
    FLETCHER("制箭师", Set.of(ProfessionSkill.STRENGTH, ProfessionSkill.LUCK, ProfessionSkill.INVISIBILITY)),
    
    // 祭司 - 拥有治疗和药水酿造技能
    CLERIC("祭司", Set.of(ProfessionSkill.HEALING, ProfessionSkill.REGENERATION, ProfessionSkill.NIGHT_VISION)),
    
    // 盔甲匠 - 拥有盔甲锻造和防护技能
    ARMORER("盔甲匠", Set.of(ProfessionSkill.RESISTANCE, ProfessionSkill.STRENGTH, ProfessionSkill.FIRE_RESISTANCE)),
    
    // 工具匠 - 拥有工具锻造和挖掘技能
    TOOLSMITH("工具匠", Set.of(ProfessionSkill.MINING, ProfessionSkill.SPEED, ProfessionSkill.STRENGTH)),
    
    // 皮匠 - 拥有食物加工和屠宰技能
    BUTCHER("皮匠", Set.of(ProfessionSkill.HEALING, ProfessionSkill.REGENERATION, ProfessionSkill.LUCK)),
    
    // 制图师 - 拥有探索和制图技能
    CARTOGRAPHER("制图师", Set.of(ProfessionSkill.SPEED, ProfessionSkill.NIGHT_VISION, ProfessionSkill.LUCK)),
    
    // 无职业村民
    NONE("无职业", new HashSet<>());

    private final String displayName;
    private final Set<ProfessionSkill> skills;

    VillagerProfession(String displayName, Set<ProfessionSkill> skills) {
        this.displayName = displayName;
        this.skills = skills;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<ProfessionSkill> getSkills() {
        return new HashSet<>(skills);
    }

    /**
     * 从Bukkit职业转换为插件职业枚举
     *
     * @param bukkitProfession Bukkit职业
     * @return 插件职业枚举
     */
    public static VillagerProfession fromBukkit(Profession bukkitProfession) {
        if (bukkitProfession == null) return NONE;
        
        switch (bukkitProfession) {
            case FARMER:
                return FARMER;
            case FISHERMAN:
                return FISHERMAN;
            case LIBRARIAN:
                return LIBRARIAN;
            case WEAPONSMITH:
                return WEAPONSMITH;
            case SHEPHERD:
                return SHEPHERD;
            case FLETCHER:
                return FLETCHER;
            case CLERIC:
                return CLERIC;
            case ARMORER:
                return ARMORER;
            case TOOLSMITH:
                return TOOLSMITH;
            case BUTCHER:
                return BUTCHER;
            case CARTOGRAPHER:
                return CARTOGRAPHER;
            default:
                return NONE;
        }
    }
}