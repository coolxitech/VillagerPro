package cn.popcraft.villagepro.listener;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.FollowMode;
import cn.popcraft.villagepro.model.VillagerEntity;
import cn.popcraft.villagepro.util.VillagerUtils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

public class VillagerListener implements Listener {
    private final VillagePro plugin;

    public VillagerListener(VillagePro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // 只处理主手交互
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        // 只处理村民实体
        if (!(event.getRightClicked() instanceof Villager villager)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // 检查是否是玩家自己的村民
        if (VillagerUtils.isOwnedBy(villager, player)) {
            // 如果玩家潜行，切换跟随模式
            if (player.isSneaking()) {
                UUID villagerUuid = villager.getUniqueId();
                VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
                
                if (villagerEntity != null) {
                    FollowMode currentMode = villagerEntity.getFollowMode();
                    FollowMode newMode;
                    
                    // 循环切换模式：NONE -> FOLLOW -> STAY -> NONE
                    switch (currentMode) {
                        case NONE:
                            newMode = FollowMode.FOLLOW;
                            player.sendMessage("§a[VillagePro] 村民将跟随你");
                            break;
                        case FOLLOW:
                            newMode = FollowMode.STAY;
                            player.sendMessage("§a[VillagePro] 村民将停留在此处");
                            break;
                        case STAY:
                            newMode = FollowMode.NONE;
                            player.sendMessage("§a[VillagePro] 村民将自由活动");
                            break;
                        default:
                            newMode = FollowMode.NONE;
                            break;
                    }
                    
                    plugin.getFollowManager().setFollowMode(villager, newMode);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // 只处理村民实体
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        
        // 检查是否是被招募的村民
        UUID villagerUuid = villager.getUniqueId();
        if (plugin.getVillagerEntities().containsKey(villagerUuid)) {
            // 从缓存中移除
            VillagerEntity villagerEntity = plugin.getVillagerEntities().remove(villagerUuid);
            
            // 从村庄数据中移除
            UUID ownerUuid = villagerEntity.getOwnerUuid();
            plugin.getVillageManager().getVillage(ownerUuid).getVillagerIds().remove(villagerUuid);
            plugin.getVillageManager().saveVillage(plugin.getVillageManager().getVillage(ownerUuid));
            
            // 通知玩家
            Player owner = plugin.getServer().getPlayer(ownerUuid);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage("§c[VillagePro] 你的一名村民死亡了!");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // 只处理村民实体
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        
        // 检查是否是被招募的村民
        UUID villagerUuid = villager.getUniqueId();
        if (plugin.getVillagerEntities().containsKey(villagerUuid)) {
            VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
            UUID ownerUuid = villagerEntity.getOwnerUuid();
            
            // 获取村民保护等级
            int protectionLevel = plugin.getVillageManager().getUpgradeLevel(ownerUuid, cn.popcraft.villagepro.model.UpgradeType.PROTECTION);
            
            // 根据保护等级减少伤害
            if (protectionLevel > 0) {
                double reduction = 0.1 * protectionLevel; // 每级减少10%伤害
                double newDamage = event.getDamage() * (1 - reduction);
                event.setDamage(newDamage);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim().toLowerCase();
        
        // 检查是否是跟随响应
        if (message.equals("y") || message.equals("n")) {
            if (plugin.getFollowManager().handleFollowResponse(player, message)) {
                event.setCancelled(true);
            }
        }
    }
}