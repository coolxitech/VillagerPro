package cn.popcraft.villagepro.model;

import org.bukkit.entity.Villager;
import java.util.EnumMap;
import java.util.Map;

/**
 * 村民职业枚举，定义了不同职业的村民及其对应的技能
 */
public enum VillagerProfession {
    // 农民 - 与农业相关的技能
    FARMER(Villager.Profession.FARMER, new ProfessionSkill[]{ProfessionSkill.FARMING, ProfessionSkill.HARVEST}),
    
    // 渔夫 - 与钓鱼和水上活动相关的技能
    FISHERMAN(Villager.Profession.FISHERMAN, new ProfessionSkill[]{ProfessionSkill.FISHING, ProfessionSkill.WATER_BREATHING}),
    
    // 牧羊人 - 与动物和羊毛相关的技能
    SHEPHERD(Villager.Profession.SHEPHERD, new ProfessionSkill[]{ProfessionSkill.ANIMAL_BREEDING, ProfessionSkill.WOOL_PROCESSING}),
    
    // 制箭师 - 与远程攻击和箭矢相关的技能
    FLETCHER(Villager.Profession.FLETCHER, new ProfessionSkill[]{ProfessionSkill.ARCHERY, ProfessionSkill.ARROW_CRAFTING}),
    
    // 图书管理员 - 与魔法和知识相关的技能
    LIBRARIAN(Villager.Profession.LIBRARIAN, new ProfessionSkill[]{ProfessionSkill.ENCHANTING, ProfessionSkill.KNOWLEDGE}),
    
    // 祭司 - 与治疗和药水相关的技能
    CLERIC(Villager.Profession.CLERIC, new ProfessionSkill[]{ProfessionSkill.HEALING, ProfessionSkill.POTION_BREWING}),
    
    // 武器匠 - 与武器制造和战斗相关的技能
    WEAPONSMITH(Villager.Profession.WEAPONSMITH, new ProfessionSkill[]{ProfessionSkill.WEAPON_FORGE, ProfessionSkill.COMBAT}),
    
    // 盔甲匠 - 与盔甲制造和防护相关的技能
    ARMORER(Villager.Profession.ARMORER, new ProfessionSkill[]{ProfessionSkill.ARMOR_FORGE, ProfessionSkill.PROTECTION}),
    
    // 工具匠 - 与工具制造和挖掘相关的技能
    TOOLSMITH(Villager.Profession.TOOLSMITH, new ProfessionSkill[]{ProfessionSkill.TOOL_FORGE, ProfessionSkill.MINING}),
    
    // 皮匠 - 与皮革和耐久相关的技能
    BUTCHER(Villager.Profession.BUTCHER, new ProfessionSkill[]{ProfessionSkill.FOOD_PROCESSING, ProfessionSkill.BUTCHERING}),
    
    // 制图师 - 与探索和地图相关的技能
    CARTOGRAPHER(Villager.Profession.CARTOGRAPHER, new ProfessionSkill[]{ProfessionSkill.EXPLORATION, ProfessionSkill.MAP_MAKING}),
    
    // 无职业村民
    NONE(Villager.Profession.NONE, new ProfessionSkill[]{ProfessionSkill.BASIC}),
    
    // 村民职业未知
    NITWIT(Villager.Profession.NITWIT, new ProfessionSkill[]{ProfessionSkill.BASIC});

    private final Villager.Profession bukkitProfession;
    private final ProfessionSkill[] skills;

    VillagerProfession(Villager.Profession bukkitProfession, ProfessionSkill[] skills) {
        this.bukkitProfession = bukkitProfession;
        this.skills = skills;
    }

    public Villager.Profession getBukkitProfession() {
        return bukkitProfession;
    }

    public ProfessionSkill[] getSkills() {
        return skills;
    }

    // 根据Bukkit职业获取对应的枚举
    public static VillagerProfession fromBukkit(Villager.Profession profession) {
        for (VillagerProfession villagerProfession : values()) {
            if (villagerProfession.bukkitProfession == profession) {
                return villagerProfession;
            }
        }
        return NONE;
    }
}