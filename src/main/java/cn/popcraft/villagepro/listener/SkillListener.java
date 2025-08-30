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