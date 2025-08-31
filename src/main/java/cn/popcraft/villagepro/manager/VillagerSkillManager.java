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
        // 获取当前技能等级
        int currentLevel = getVillagerSkillLevel(villager, skill);
        
        // 检查是否已达到最高等级
        if (currentLevel >= 5) {
            // 已达到最高等级，通知所有者玩家
            UUID ownerId = cn.popcraft.villagepro.util.VillagerUtils.getOwner(villager);
            if (ownerId != null) {
                org.bukkit.entity.Player owner = plugin.getServer().getPlayer(ownerId);
                if (owner != null && owner.isOnline()) {
                    String message = String.format(
                        "&e你的村民 %s 的 %s 技能已达到最高等级!",
                        villager.getCustomName() != null ? villager.getCustomName() : "村民",
                        skill.getDisplayName()
                    );
                    owner.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
                }
            }
            return; // 已达到最高等级，不再增加经验
        }
        
        // 获取村民的技能经验映射
        UUID villagerId = villager.getUniqueId();
        Map<ProfessionSkill, Integer> skillExps = villagerSkills.computeIfAbsent(villagerId, 
            k -> new EnumMap<>(ProfessionSkill.class));
        
        // 获取当前经验
        int currentExp = skillExps.getOrDefault(skill, 0);
        int newExp = currentExp + exp;
        
        // 计算升级所需经验（基于技能等级的指数增长公式）
        // 公式：升级到下一级所需经验 = 100 * (当前等级^1.5) 向上取整到最接近的10的倍数
        int expNeeded = (int) Math.ceil(100 * Math.pow(currentLevel, 1.5) / 10) * 10;
        
        // 确保最小经验值为100（1级升2级需要100经验）
        if (currentLevel == 0) {
            expNeeded = 100;
        }
        
        // 检查是否可以升级
        if (newExp >= expNeeded && expNeeded > 0) {
            // 检查是否已达到最高等级（5级）
            if (currentLevel >= 5) {
                // 已达到最高等级，只更新经验到最大值
                skillExps.put(skill, expNeeded);
                
                // 通知所有者玩家技能已满级
                UUID ownerId = cn.popcraft.villagepro.util.VillagerUtils.getOwner(villager);
                if (ownerId != null) {
                    org.bukkit.entity.Player owner = plugin.getServer().getPlayer(ownerId);
                    if (owner != null && owner.isOnline()) {
                        String message = String.format(
                            "&e你的村民 %s 的 %s 技能已达到最高等级!",
                            villager.getCustomName() != null ? villager.getCustomName() : "村民",
                            skill.getDisplayName()
                        );
                        owner.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
                    }
                }
                return;
            }
            
            // 升级技能
            setVillagerSkillLevel(villager, skill, currentLevel + 1);
            
            // 应用新的技能效果
            applySkillEffect(villager, skill, currentLevel + 1);
            
            // 重置经验（可以保留超出部分经验）
            int remainingExp = newExp - expNeeded;
            skillExps.put(skill, remainingExp);
            
            // 通知所有者玩家技能升级
            UUID ownerId = cn.popcraft.villagepro.util.VillagerUtils.getOwner(villager);
            if (ownerId != null) {
                org.bukkit.entity.Player owner = plugin.getServer().getPlayer(ownerId);
                if (owner != null && owner.isOnline()) {
                    String message = String.format(
                        "&a你的村民 %s 的 %s 技能已升级到 %d 级!",
                        villager.getCustomName() != null ? villager.getCustomName() : "村民",
                        skill.getDisplayName(),
                        currentLevel + 1
                    );
                    owner.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        } else {
            // 更新经验
            skillExps.put(skill, newExp);
            
            // 通知玩家获得经验（仅当需要经验才能升级时）
            if (expNeeded > 0) {
                UUID ownerId = cn.popcraft.villagepro.util.VillagerUtils.getOwner(villager);
                if (ownerId != null) {
                    org.bukkit.entity.Player owner = plugin.getServer().getPlayer(ownerId);
                    if (owner != null && owner.isOnline()) {
                        // 计算当前等级已获得的经验和升级所需经验
                        int expForCurrentLevel = currentLevel * 100; // 升到当前等级所需的总经验
                        int expGainedForCurrentLevel = newExp - expForCurrentLevel; // 当前等级已获得的经验
                        int expNeededForNextLevel = expNeeded - expForCurrentLevel; // 升到下一级还需的经验
                        
                        String message = String.format(
                            "&7你的村民 %s 的 %s 技能获得了 %d 点经验! (当前等级: %d, 进度: %d/%d)",
                            villager.getCustomName() != null ? villager.getCustomName() : "村民",
                            skill.getDisplayName(),
                            exp,
                            currentLevel,
                            expGainedForCurrentLevel,
                            expNeededForNextLevel
                        );
                        owner.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
                    }
                }
            }
        }
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