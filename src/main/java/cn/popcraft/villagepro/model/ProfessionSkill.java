package cn.popcraft.villagepro.model;

/**
 * 职业技能枚举，定义了村民可以拥有的各种技能
 */
public enum ProfessionSkill {
    // 基础技能
    BASIC,              // 基础技能
    
    // 战斗相关技能
    COMBAT,             // 战斗技能
    ARCHERY,            // 弓箭技能
    WEAPON_FORGE,       // 武器锻造技能
    
    // 防护相关技能
    PROTECTION,         // 防护技能
    ARMOR_FORGE,        // 盔甲锻造技能
    
    // 挖掘相关技能
    MINING,             // 挖掘技能
    TOOL_FORGE,         // 工具锻造技能
    
    // 农业相关技能
    FARMING,            // 农业技能
    HARVEST,            // 收获技能
    ANIMAL_BREEDING,    // 动物繁殖技能
    WOOL_PROCESSING,    // 羊毛处理技能
    FOOD_PROCESSING,    // 食物加工技能
    BUTCHERING,         // 屠宰技能
    
    // 水上活动相关技能
    FISHING,            // 钓鱼技能
    WATER_BREATHING,    // 水下呼吸技能
    
    // 魔法相关技能
    ENCHANTING,         // 附魔技能
    KNOWLEDGE,          // 知识技能
    
    // 治疗相关技能
    HEALING,            // 治疗技能
    POTION_BREWING,     // 药水酿造技能
    
    // 探索相关技能
    EXPLORATION,        // 探索技能
    MAP_MAKING,         // 制图技能
    
    // 工艺相关技能
    ARROW_CRAFTING,     // 箭矢制作技能
    
    // 交易相关技能
    TRADE_BOOST,        // 交易加成技能
    
    // 药水效果相关技能
    SPEED,              // 速度技能
    NIGHT_VISION,       // 夜视技能
    STRENGTH,           // 力量技能
    RESISTANCE,         // 抗性技能
    JUMP_BOOST,         // 跳跃增强技能
    
    // 修复技能
    REPAIR              // 修复技能
}