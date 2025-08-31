package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.model.FollowMode;
import cn.popcraft.villagepro.model.VillagerEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FollowManager {
    private final VillagePro plugin;
    private final Map<UUID, UUID> pendingFollowRequests = new HashMap<>(); // 玩家UUID -> 村民UUID
    
    public FollowManager(VillagePro plugin) {
        this.plugin = plugin;
        startFollowTask();
    }
    
    /**
     * 切换村民的跟随模式
     * @param player 玩家
     */
    public void toggleFollowMode(Player player) {
        // 这里应该实现切换跟随模式的逻辑
        // 例如：在自由活动、跟随玩家、停留原地三种模式之间切换
        player.sendMessage("跟随模式切换功能正在开发中...");
    }
    
    /**
     * 设置村民的跟随模式
     * @param villager 村民实体
     * @param mode 跟随模式
     */
    public void setFollowMode(Villager villager, FollowMode mode) {
        UUID villagerUuid = villager.getUniqueId();
        VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
        
        if (villagerEntity != null) {
            villagerEntity.setFollowMode(mode);
            
            // 如果是停留模式，保存当前位置
            if (mode == FollowMode.STAY) {
                villager.setAI(false);
            } else {
                villager.setAI(true);
            }
        }
    }
    
    /**
     * 请求村民跟随玩家
     * @param player 玩家
     * @param villager 村民
     */
    public void requestFollow(Player player, Villager villager) {
        pendingFollowRequests.put(player.getUniqueId(), villager.getUniqueId());
        player.sendMessage(plugin.getMessageManager().getMessage("follow.request"));
    }
    
    /**
     * 处理玩家的跟随响应
     * @param player 玩家
     * @param response 响应（y/n）
     * @return 是否成功处理
     */
    public boolean handleFollowResponse(Player player, String response) {
        UUID playerUuid = player.getUniqueId();
        
        if (!pendingFollowRequests.containsKey(playerUuid)) {
            return false;
        }
        
        UUID villagerUuid = pendingFollowRequests.remove(playerUuid);
        VillagerEntity villagerEntity = plugin.getVillagerEntities().get(villagerUuid);
        
        if (villagerEntity == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }
        
        Villager villager = villagerEntity.getBukkitEntity();
        
        if (response.equalsIgnoreCase("y")) {
            setFollowMode(villager, FollowMode.FOLLOW);
            player.sendMessage(plugin.getMessageManager().getMessage("follow.start"));
        } else {
            setFollowMode(villager, FollowMode.NONE);
            player.sendMessage(plugin.getMessageManager().getMessage("follow.cancel"));
        }
        
        return true;
    }
    
    /**
     * 启动跟随任务
     */
    private void startFollowTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (VillagerEntity villagerEntity : plugin.getVillagerEntities().values()) {
                    if (villagerEntity.getFollowMode() == FollowMode.FOLLOW) {
                        Villager villager = villagerEntity.getBukkitEntity();
                        UUID ownerUuid = villagerEntity.getOwnerId();
                        Player owner = plugin.getServer().getPlayer(ownerUuid);
                        
                        if (owner != null && owner.isOnline() && villager.isValid()) {
                            // 检查距离，如果太远就传送
                            if (owner.getWorld().equals(villager.getWorld()) && 
                                    owner.getLocation().distance(villager.getLocation()) > 30) {
                                Location teleportLoc = owner.getLocation().clone().add(2, 0, 2);
                                villager.teleport(teleportLoc);
                            } else if (owner.getWorld().equals(villager.getWorld()) && 
                                    owner.getLocation().distance(villager.getLocation()) > 5) {
                                // 如果距离适中，就让村民走向玩家
                                // 使用 Bukkit API 1.16+ 的导航方法
                                villager.teleport(owner.getLocation());
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // 每秒执行一次
    }
}