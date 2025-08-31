package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.VillagerProfession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class SkillListener implements Listener {
    private final VillagePro plugin;

    public SkillListener(VillagePro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            // 使用村庄升级系统替代旧的技能系统
            int skillLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), cn.popcraft.villagepro.model.UpgradeType.HEALTH);
            // 根据技能级别调整伤害
            event.setDamage(event.getDamage() * (1 + skillLevel * 0.1));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // 使用村庄升级系统替代旧的技能系统
        int skillLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), cn.popcraft.villagepro.model.UpgradeType.TRADE);
        if (skillLevel > 0) {
            // 根据技能级别增加掉落物
            event.setDropItems(true);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            // 使用村庄升级系统替代旧的技能系统
            int combatSkillLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), cn.popcraft.villagepro.model.UpgradeType.HEALTH);
            if (combatSkillLevel > 0) {
                int additionalExp = (int) (event.getDroppedExp() * (0.1 * combatSkillLevel));
                event.setDroppedExp(event.getDroppedExp() + additionalExp);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Villager)) return;
        
        Player player = event.getPlayer();
        Villager villager = (Villager) event.getRightClicked();
        
        // 检查是否是被招募的村民
        if (cn.popcraft.villagepro.util.VillagerUtils.isOwnedBy(villager, player)) {
            // 获取村民的职业和交易升级等级
            VillagerProfession profession = VillagerProfession.fromBukkit(villager.getProfession());
            int tradeBoostLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), cn.popcraft.villagepro.model.UpgradeType.TRADE);
            
            // 如果村民有技能且有交易加成，则应用到交易中
            if (!profession.getSkills().isEmpty() && tradeBoostLevel > 0) {
                List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
                
                // 根据村民职业和交易等级添加高级交易配方
                addAdvancedTrades(recipes, profession, tradeBoostLevel);
                
                // 应用修改后的交易配方
                villager.setRecipes(recipes);
            }
        }
    }
    
    /**
     * 为村民添加高级交易配方
     * @param recipes 交易配方列表
     * @param profession 村民职业
     * @param tradeLevel 交易等级
     */
    private void addAdvancedTrades(List<MerchantRecipe> recipes, VillagerProfession profession, int tradeLevel) {
        // 根据村民职业类型添加特定的高级交易配方
        switch (profession) {
            case FARMER:
                addFarmerTrades(recipes, tradeLevel);
                break;
            case FISHERMAN:
                addFishermanTrades(recipes, tradeLevel);
                break;
            case LIBRARIAN:
                addLibrarianTrades(recipes, tradeLevel);
                break;
            case WEAPONSMITH:
                addWeaponsmithTrades(recipes, tradeLevel);
                break;
            case SHEPHERD:
                addShepherdTrades(recipes, tradeLevel);
                break;
            case FLETCHER:
                addFletcherTrades(recipes, tradeLevel);
                break;
            case CLERIC:
                addClericTrades(recipes, tradeLevel);
                break;
            case ARMORER:
                addArmorerTrades(recipes, tradeLevel);
                break;
            case TOOLSMITH:
                addToolsmithTrades(recipes, tradeLevel);
                break;
            case BUTCHER:
                addButcherTrades(recipes, tradeLevel);
                break;
            case CARTOGRAPHER:
                addCartographerTrades(recipes, tradeLevel);
                break;
            default:
                // 添加通用交易配方
                addGenericTrades(recipes, tradeLevel);
                break;
        }
    }
    
    /**
     * 为农民添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addFarmerTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 高级农作物交易
            MerchantRecipe wheatRecipe = new MerchantRecipe(new ItemStack(Material.WHEAT, 24), 0, 12, true);
            wheatRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(wheatRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 黄金苹果交易
            MerchantRecipe goldenAppleRecipe = new MerchantRecipe(new ItemStack(Material.GOLDEN_APPLE, 1), 0, 2, true);
            goldenAppleRecipe.addIngredient(new ItemStack(Material.EMERALD, 8));
            recipes.add(goldenAppleRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 附魔金苹果交易
            MerchantRecipe enchantedGoldenAppleRecipe = new MerchantRecipe(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), 0, 1, true);
            enchantedGoldenAppleRecipe.addIngredient(new ItemStack(Material.EMERALD, 32));
            recipes.add(enchantedGoldenAppleRecipe);
        }
    }
    
    /**
     * 为渔夫添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addFishermanTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 热带鱼交易
            MerchantRecipe tropicalFishRecipe = new MerchantRecipe(new ItemStack(Material.TROPICAL_FISH, 6), 0, 6, true);
            tropicalFishRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(tropicalFishRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 河豚交易
            MerchantRecipe pufferfishRecipe = new MerchantRecipe(new ItemStack(Material.PUFFERFISH, 4), 0, 4, true);
            pufferfishRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(pufferfishRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 鞘翅交易
            MerchantRecipe elytraRecipe = new MerchantRecipe(new ItemStack(Material.ELYTRA, 1), 0, 1, true);
            elytraRecipe.addIngredient(new ItemStack(Material.EMERALD, 64));
            elytraRecipe.addIngredient(new ItemStack(Material.PHANTOM_MEMBRANE, 8));
            recipes.add(elytraRecipe);
        }
    }
    
    /**
     * 为图书管理员添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addLibrarianTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 经验瓶交易
            MerchantRecipe expBottleRecipe = new MerchantRecipe(new ItemStack(Material.EXPERIENCE_BOTTLE, 4), 0, 4, true);
            expBottleRecipe.addIngredient(new ItemStack(Material.EMERALD, 3));
            recipes.add(expBottleRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 附魔书交易（随机附魔）
            MerchantRecipe enchantmentRecipe = new MerchantRecipe(createRandomEnchantedBook(), 0, 1, true);
            enchantmentRecipe.addIngredient(new ItemStack(Material.EMERALD, 20));
            recipes.add(enchantmentRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 命名牌交易
            MerchantRecipe nameTagRecipe = new MerchantRecipe(new ItemStack(Material.NAME_TAG, 1), 0, 2, true);
            nameTagRecipe.addIngredient(new ItemStack(Material.EMERALD, 15));
            recipes.add(nameTagRecipe);
        }
    }
    
    /**
     * 为武器匠添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addWeaponsmithTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 铁剑交易
            MerchantRecipe ironSwordRecipe = new MerchantRecipe(new ItemStack(Material.IRON_SWORD, 1), 0, 3, true);
            ironSwordRecipe.addIngredient(new ItemStack(Material.EMERALD, 5));
            recipes.add(ironSwordRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 钻石剑交易
            MerchantRecipe diamondSwordRecipe = new MerchantRecipe(new ItemStack(Material.DIAMOND_SWORD, 1), 0, 2, true);
            diamondSwordRecipe.addIngredient(new ItemStack(Material.EMERALD, 12));
            recipes.add(diamondSwordRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 附魔钻石剑交易
            ItemStack enchantedSword = new ItemStack(Material.DIAMOND_SWORD);
            // 添加随机附魔
            enchantedSword = addRandomEnchantment(enchantedSword);
            MerchantRecipe enchantedSwordRecipe = new MerchantRecipe(enchantedSword, 0, 1, true);
            enchantedSwordRecipe.addIngredient(new ItemStack(Material.EMERALD, 25));
            recipes.add(enchantedSwordRecipe);
        }
    }
    
    /**
     * 为牧羊人添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addShepherdTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 彩色羊毛交易
            for (int i = 0; i < 16; i++) {
                MerchantRecipe woolRecipe = new MerchantRecipe(new ItemStack(Material.WHITE_WOOL, 3), 0, 6, true);
                woolRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
                recipes.add(woolRecipe);
            }
        }
        
        if (tradeLevel >= 3) {
            // 染料交易
            MerchantRecipe dyeRecipe = new MerchantRecipe(new ItemStack(Material.RED_DYE, 8), 0, 8, true);
            dyeRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(dyeRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 画交易
            MerchantRecipe paintingRecipe = new MerchantRecipe(new ItemStack(Material.PAINTING, 1), 0, 2, true);
            paintingRecipe.addIngredient(new ItemStack(Material.EMERALD, 4));
            recipes.add(paintingRecipe);
        }
    }
    
    /**
     * 为制箭师添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addFletcherTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 箭交易
            MerchantRecipe arrowRecipe = new MerchantRecipe(new ItemStack(Material.ARROW, 32), 0, 8, true);
            arrowRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(arrowRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 光灵箭交易
            MerchantRecipe spectralArrowRecipe = new MerchantRecipe(new ItemStack(Material.SPECTRAL_ARROW, 8), 0, 4, true);
            spectralArrowRecipe.addIngredient(new ItemStack(Material.EMERALD, 4));
            recipes.add(spectralArrowRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 火箭交易
            MerchantRecipe tippedArrowRecipe = new MerchantRecipe(new ItemStack(Material.TIPPED_ARROW, 4), 0, 2, true);
            tippedArrowRecipe.addIngredient(new ItemStack(Material.EMERALD, 6));
            recipes.add(tippedArrowRecipe);
        }
    }
    
    /**
     * 为祭司添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addClericTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 末影珍珠交易
            MerchantRecipe enderPearlRecipe = new MerchantRecipe(new ItemStack(Material.ENDER_PEARL, 1), 0, 4, true);
            enderPearlRecipe.addIngredient(new ItemStack(Material.EMERALD, 8));
            recipes.add(enderPearlRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 附魔金苹果交易
            MerchantRecipe enchantedGoldenAppleRecipe = new MerchantRecipe(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), 0, 1, true);
            enchantedGoldenAppleRecipe.addIngredient(new ItemStack(Material.EMERALD, 32));
            recipes.add(enchantedGoldenAppleRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 龙息交易
            MerchantRecipe dragonBreathRecipe = new MerchantRecipe(new ItemStack(Material.DRAGON_BREATH, 1), 0, 2, true);
            dragonBreathRecipe.addIngredient(new ItemStack(Material.EMERALD, 25));
            recipes.add(dragonBreathRecipe);
        }
    }
    
    /**
     * 为盔甲匠添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addArmorerTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 铁头盔交易
            MerchantRecipe ironHelmetRecipe = new MerchantRecipe(new ItemStack(Material.IRON_HELMET, 1), 0, 2, true);
            ironHelmetRecipe.addIngredient(new ItemStack(Material.EMERALD, 5));
            recipes.add(ironHelmetRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 钻石头盔交易
            MerchantRecipe diamondHelmetRecipe = new MerchantRecipe(new ItemStack(Material.DIAMOND_HELMET, 1), 0, 1, true);
            diamondHelmetRecipe.addIngredient(new ItemStack(Material.EMERALD, 15));
            recipes.add(diamondHelmetRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 附魔钻石盔甲交易
            ItemStack enchantedChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
            // 添加随机附魔
            enchantedChestplate = addRandomEnchantment(enchantedChestplate);
            MerchantRecipe enchantedArmorRecipe = new MerchantRecipe(enchantedChestplate, 0, 1, true);
            enchantedArmorRecipe.addIngredient(new ItemStack(Material.EMERALD, 30));
            recipes.add(enchantedArmorRecipe);
        }
    }
    
    /**
     * 为工具匠添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addToolsmithTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 铁镐交易
            MerchantRecipe ironPickaxeRecipe = new MerchantRecipe(new ItemStack(Material.IRON_PICKAXE, 1), 0, 3, true);
            ironPickaxeRecipe.addIngredient(new ItemStack(Material.EMERALD, 5));
            recipes.add(ironPickaxeRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 钻石镐交易
            MerchantRecipe diamondPickaxeRecipe = new MerchantRecipe(new ItemStack(Material.DIAMOND_PICKAXE, 1), 0, 2, true);
            diamondPickaxeRecipe.addIngredient(new ItemStack(Material.EMERALD, 12));
            recipes.add(diamondPickaxeRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 附魔钻石工具交易
            ItemStack enchantedAxe = new ItemStack(Material.DIAMOND_AXE);
            // 添加随机附魔
            enchantedAxe = addRandomEnchantment(enchantedAxe);
            MerchantRecipe enchantedToolRecipe = new MerchantRecipe(enchantedAxe, 0, 1, true);
            enchantedToolRecipe.addIngredient(new ItemStack(Material.EMERALD, 20));
            recipes.add(enchantedToolRecipe);
        }
    }
    
    /**
     * 为皮匠添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addButcherTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 猪肉交易
            MerchantRecipe porkchopRecipe = new MerchantRecipe(new ItemStack(Material.COOKED_PORKCHOP, 4), 0, 6, true);
            porkchopRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(porkchopRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 牛排交易
            MerchantRecipe steakRecipe = new MerchantRecipe(new ItemStack(Material.COOKED_BEEF, 4), 0, 6, true);
            steakRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(steakRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 美味的牛排交易
            MerchantRecipe deliciousSteakRecipe = new MerchantRecipe(new ItemStack(Material.COOKED_BEEF, 8), 0, 3, true);
            deliciousSteakRecipe.addIngredient(new ItemStack(Material.EMERALD, 4));
            recipes.add(deliciousSteakRecipe);
        }
    }
    
    /**
     * 为制图师添加高级交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addCartographerTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 地图交易
            MerchantRecipe mapRecipe = new MerchantRecipe(new ItemStack(Material.MAP, 1), 0, 4, true);
            mapRecipe.addIngredient(new ItemStack(Material.EMERALD, 3));
            recipes.add(mapRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 指南针交易
            MerchantRecipe compassRecipe = new MerchantRecipe(new ItemStack(Material.COMPASS, 1), 0, 2, true);
            compassRecipe.addIngredient(new ItemStack(Material.EMERALD, 8));
            recipes.add(compassRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 空白地图交易
            MerchantRecipe emptyMapRecipe = new MerchantRecipe(new ItemStack(Material.MAP, 3), 0, 2, true);
            emptyMapRecipe.addIngredient(new ItemStack(Material.EMERALD, 15));
            recipes.add(emptyMapRecipe);
        }
    }
    
    /**
     * 添加通用交易配方
     * @param recipes 交易配方列表
     * @param tradeLevel 交易等级
     */
    private void addGenericTrades(List<MerchantRecipe> recipes, int tradeLevel) {
        if (tradeLevel >= 2) {
            // 红石交易
            MerchantRecipe redstoneRecipe = new MerchantRecipe(new ItemStack(Material.REDSTONE, 16), 0, 8, true);
            redstoneRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            recipes.add(redstoneRecipe);
        }
        
        if (tradeLevel >= 3) {
            // 钻石交易
            MerchantRecipe diamondRecipe = new MerchantRecipe(new ItemStack(Material.DIAMOND, 2), 0, 4, true);
            diamondRecipe.addIngredient(new ItemStack(Material.EMERALD, 10));
            recipes.add(diamondRecipe);
        }
        
        if (tradeLevel >= 4) {
            // 下界之星交易
            MerchantRecipe netherStarRecipe = new MerchantRecipe(new ItemStack(Material.NETHER_STAR, 1), 0, 1, true);
            netherStarRecipe.addIngredient(new ItemStack(Material.EMERALD, 64));
            recipes.add(netherStarRecipe);
        }
    }
    
    /**
     * 创建随机附魔书
     * @return 附魔书
     */
    private ItemStack createRandomEnchantedBook() {
        // 创建一个带随机附魔的附魔书
        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
        return addRandomEnchantment(enchantedBook);
    }
    
    /**
     * 为物品添加随机附魔
     * @param itemStack 物品
     * @return 附魔后的物品
     */
    private ItemStack addRandomEnchantment(ItemStack itemStack) {
        // 获取物品类型以确定可能的附魔
        Material material = itemStack.getType();
        
        // 创建附魔映射（根据物品类型确定合适的附魔）
        java.util.Map<org.bukkit.enchantments.Enchantment, Integer> possibleEnchants = 
            new java.util.HashMap<>();
        
        // 根据物品类型添加可能的附魔
        if (material == Material.DIAMOND_SWORD) {
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 5); // 锋利V
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2); // 火焰附加II
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.KNOCKBACK, 2); // 击退II
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS, 3); // 抢夺III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.SWEEPING_EDGE, 3); // 横扫之刃III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 3); // 耐久III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.MENDING, 1); // 经验修补I
        } else if (material == Material.DIAMOND_CHESTPLATE) {
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4); // 保护IV
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.THORNS, 3); // 荆棘III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 3); // 耐久III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.MENDING, 1); // 经验修补I
        } else if (material == Material.DIAMOND_AXE) {
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DIG_SPEED, 5); // 效率V
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 4); // 锋利IV
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 3); // 耐久III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.MENDING, 1); // 经验修补I
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.SILK_TOUCH, 1); // 精准采集I
        } else if (material == Material.DIAMOND_PICKAXE) {
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DIG_SPEED, 5); // 效率V
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 3); // 耐久III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.MENDING, 1); // 经验修补I
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.SILK_TOUCH, 1); // 精准采集I
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS, 3); // 幸运III
        } else if (material == Material.BOW) {
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.ARROW_DAMAGE, 5); // 力量V
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.ARROW_KNOCKBACK, 2); // 冲击II
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.ARROW_FIRE, 1); // 火矢I
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.ARROW_INFINITE, 1); // 无限I
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 3); // 耐久III
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.MENDING, 1); // 经验修补I
        } else if (material == Material.ENCHANTED_BOOK) {
            // 为附魔书添加随机附魔
            org.bukkit.enchantments.Enchantment[] enchantments = {
                org.bukkit.enchantments.Enchantment.DAMAGE_ALL,
                org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL,
                org.bukkit.enchantments.Enchantment.DIG_SPEED,
                org.bukkit.enchantments.Enchantment.ARROW_DAMAGE,
                org.bukkit.enchantments.Enchantment.LUCK,
                org.bukkit.enchantments.Enchantment.DEPTH_STRIDER,
                org.bukkit.enchantments.Enchantment.FROST_WALKER,
                org.bukkit.enchantments.Enchantment.SOUL_SPEED
            };
            
            // 随机选择1-3个附魔
            java.util.Random random = new java.util.Random();
            int numEnchants = random.nextInt(3) + 1; // 1-3个附魔
            
            for (int i = 0; i < numEnchants; i++) {
                org.bukkit.enchantments.Enchantment enchant = 
                    enchantments[random.nextInt(enchantments.length)];
                int level = random.nextInt(enchant.getMaxLevel()) + 1;
                possibleEnchants.put(enchant, level);
            }
        } else {
            // 通用附魔（适用于大多数物品）
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 3);
            possibleEnchants.put(org.bukkit.enchantments.Enchantment.MENDING, 1);
        }
        
        // 应用随机附魔
        if (!possibleEnchants.isEmpty()) {
            java.util.Random random = new java.util.Random();
            java.util.List<org.bukkit.enchantments.Enchantment> enchantList = 
                new java.util.ArrayList<>(possibleEnchants.keySet());
            
            // 随机选择1-3个附魔应用到物品上
            int numToApply = Math.min(random.nextInt(3) + 1, enchantList.size());
            for (int i = 0; i < numToApply; i++) {
                int index = random.nextInt(enchantList.size());
                org.bukkit.enchantments.Enchantment enchant = enchantList.get(index);
                int level = Math.min(possibleEnchants.get(enchant), enchant.getMaxLevel());
                itemStack.addUnsafeEnchantment(enchant, level);
                enchantList.remove(index);
            }
        }
        
        return itemStack;
    }
}