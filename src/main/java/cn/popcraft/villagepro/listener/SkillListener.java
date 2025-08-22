package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.VillageUpgrade;
import cn.popcraft.villagepro.model.ProfessionSkill;
import cn.popcraft.villagepro.model.UpgradeType;
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
            VillageUpgrade upgrade = plugin.getVillageManager().getVillageUpgrade(player.getUniqueId());
            if (upgrade != null) {
                int skillLevel = upgrade.getSkillLevel(ProfessionSkill.REPAIR);
                // 根据技能级别调整伤害
                event.setDamage(event.getDamage() * (1 + skillLevel * 0.1));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        VillageUpgrade upgrade = plugin.getVillageManager().getVillageUpgrade(player.getUniqueId());
        if (upgrade != null) {
            int skillLevel = upgrade.getSkillLevel(ProfessionSkill.MINING);
            // 根据技能级别调整掉落物
            if (skillLevel > 0) {
                event.setDropItems(false);
                // 增加额外掉落
                List<ItemStack> drops = new ArrayList<>(event.getBlock().getDrops());
                // 根据技能等级增加额外掉落
                for (int i = 0; i < skillLevel; i++) {
                    drops.addAll(event.getBlock().getDrops());
                }
                player.getInventory().addItem(drops.toArray(new ItemStack[0]));
            }
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // 检查击杀者是否为玩家
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            VillageUpgrade upgrade = plugin.getVillageManager().getVillageUpgrade(player.getUniqueId());
            
            if (upgrade != null) {
                // 根据战斗技能等级增加额外掉落经验
                int combatSkillLevel = upgrade.getSkillLevel(ProfessionSkill.COMBAT);
                if (combatSkillLevel > 0) {
                    int additionalExp = (int) (event.getDroppedExp() * (0.1 * combatSkillLevel));
                    event.setDroppedExp(event.getDroppedExp() + additionalExp);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // 检查是否与村民交互
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            Player player = event.getPlayer();
            
            // 检查是否是被招募的村民
            if (plugin.getVillagerEntities().containsKey(villager.getUniqueId())) {
                // 根据村民职业应用技能加成到交易中
                VillagerProfession profession = VillagerProfession.fromBukkit(villager.getProfession());
                int tradeBoostLevel = plugin.getVillageManager().getUpgradeLevel(player.getUniqueId(), cn.popcraft.villagepro.model.UpgradeType.TRADE);
                
                // 如果村民有交易加成技能，则应用到交易中
                if (profession.getSkills().length > 0 && tradeBoostLevel > 0) {
                    List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
                    villager.setRecipes(recipes);
                    
                    // 示例：为每个交易配方添加额外绿宝石
                    for (MerchantRecipe recipe : recipes) {
                        ItemStack additionalEmerald = new ItemStack(Material.EMERALD);
                        recipe.addIngredient(additionalEmerald);
                    }
                    
                    // 应用修改后的交易配方
                    villager.setRecipes(recipes);
                }
            }
        }
    }
}