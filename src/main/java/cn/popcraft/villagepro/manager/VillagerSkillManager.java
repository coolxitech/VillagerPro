package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.ProfessionSkill;
import cn.popcraft.villagepro.model.VillagerProfession;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 村民技能管理器，用于处理村民技能系统
 */
public class VillagerSkillManager {
    private final VillagePro plugin;
    private final Map<UUID, Map<ProfessionSkill, Integer>> villagerSkills = new HashMap<>();

    public VillagerSkillManager(VillagePro plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取村民的技能等级
     *
     * @param villager 村民
     * @param skill    技能
     * @return 技能等级
     */
    public int getVillagerSkillLevel(Villager villager, ProfessionSkill skill) {
        UUID villagerId = villager.getUniqueId();
        Map<ProfessionSkill, Integer> skills = villagerSkills.get(villagerId);
        if (skills == null) {
            return 0;
        }
        return skills.getOrDefault(skill, 0);
    }

    /**
     * 设置村民的技能等级
     *
     * @param villager 村民
     * @param skill    技能
     * @param level    等级
     */
    public void setVillagerSkillLevel(Villager villager, ProfessionSkill skill, int level) {
        UUID villagerId = villager.getUniqueId();
        Map<ProfessionSkill, Integer> skills = villagerSkills.computeIfAbsent(villagerId, k -> new EnumMap<>(ProfessionSkill.class));
        skills.put(skill, Math.max(0, level));
    }

    /**
     * 增加村民技能经验
     *
     * @param villager 村民
     * @param skill    技能
     * @param exp      经验值
     */
    public void addVillagerSkillExp(Villager villager, ProfessionSkill skill, int exp) {
        // 当前实现中，技能等级固定，未来可以扩展为经验系统
        // 这里暂时留空，作为未来扩展的接口
    }

    /**
     * 应用村民技能效果
     *
     * @param villager 村民
     */
    public void applyVillagerSkills(Villager villager) {
        VillagerProfession profession = VillagerProfession.fromBukkit(villager.getProfession());
        
        // 根据村民职业和技能等级应用效果
        for (ProfessionSkill skill : profession.getSkills()) {
            int level = getVillagerSkillLevel(villager, skill);
            applySkillEffect(villager, skill, level);
        }
    }

    /**
     * 应用特定技能效果
     *
     * @param villager 村民
     * @param skill    技能
     * @param level    等级
     */
    private void applySkillEffect(Villager villager, ProfessionSkill skill, int level) {
        if (level <= 0) return;

        switch (skill) {
            case SPEED:
                // 增加移动速度
                villager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, Math.min(level - 1, 3), false, false));
                break;
            case WATER_BREATHING:
                // 水下呼吸
                villager.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false));
                break;
            case NIGHT_VISION:
                // 夜视
                villager.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                break;
            case STRENGTH:
                // 增加力量
                villager.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, Math.min(level - 1, 2), false, false));
                break;
            case RESISTANCE:
                // 增加抗性
                villager.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, Math.min(level - 1, 2), false, false));
                break;
            case JUMP_BOOST:
                // 增加跳跃高度
                villager.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, Math.min(level - 1, 2), false, false));
                break;
            case REGENERATION:
                // 生命恢复
                villager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, Math.min(level - 1, 2), false, false));
                break;
            case FIRE_RESISTANCE:
                // 火焰抗性
                villager.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                break;
            case INVISIBILITY:
                // 隐身
                villager.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                break;
            case LUCK:
                // 幸运
                villager.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, Math.min(level - 1, 3), false, false));
                break;
            // 其他技能效果可以在这里添加
        }
    }

    /**
     * 移除村民的所有技能效果
     *
     * @param villager 村民
     */
    public void removeVillagerSkills(Villager villager) {
        // 移除所有药水效果
        villager.getActivePotionEffects().forEach(effect -> villager.removePotionEffect(effect.getType()));
    }

    /**
     * 初始化村民技能（招募时调用）
     *
     * @param villager 村民
     */
    public void initializeVillagerSkills(Villager villager) {
        VillagerProfession profession = VillagerProfession.fromBukkit(villager.getProfession());
        
        // 为每个职业相关技能设置初始等级（1级）
        for (ProfessionSkill skill : profession.getSkills()) {
            setVillagerSkillLevel(villager, skill, 1);
        }
        
        // 应用技能效果
        applyVillagerSkills(villager);
    }
    
    /**
     * 升级村民技能
     * @param villager 村民
     * @param skill 技能
     * @param levels 要增加的等级数
     */
    public void upgradeVillagerSkill(Villager villager, ProfessionSkill skill, int levels) {
        int currentLevel = getVillagerSkillLevel(villager, skill);
        int newLevel = Math.min(currentLevel + levels, 5); // 最大等级为5
        setVillagerSkillLevel(villager, skill, newLevel);
        
        // 重新应用技能效果
        applySkillEffect(villager, skill, newLevel);
    }
    
    /**
     * 获取村民所有技能的详细信息
     * @param villager 村民
     * @return 技能信息字符串
     */
    public String getVillagerSkillsInfo(Villager villager) {
        StringBuilder info = new StringBuilder();
        VillagerProfession profession = VillagerProfession.fromBukkit(villager.getProfession());
        
        info.append("§6=== 村民技能信息 ===\n");
        info.append("§e职业: §f").append(profession.getDisplayName()).append("\n");
        
        for (ProfessionSkill skill : profession.getSkills()) {
            int level = getVillagerSkillLevel(villager, skill);
            info.append("§e").append(skill.getDisplayName()).append(": §f等级 ").append(level).append("\n");
        }
        
        return info.toString();
    }
}