package cn.popcraft.villagepro.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import cn.popcraft.villagepro.VillagePro;

import java.util.UUID;

public class VillagerUtils {
    private static final NamespacedKey OWNER_KEY = new NamespacedKey(VillagePro.getInstance(), "owner");
    
    /**
     * 设置村民的所有者
     * @param villager 村民实体
     * @param ownerUuid 所有者UUID
     */
    public static void setOwner(Villager villager, UUID ownerUuid) {
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        pdc.set(OWNER_KEY, PersistentDataType.STRING, ownerUuid.toString());
    }
    
    /**
     * 获取村民的所有者
     * @param villager 村民实体
     * @return 所有者UUID，如果没有则返回null
     */
    public static UUID getOwner(Villager villager) {
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        String uuidStr = pdc.get(OWNER_KEY, PersistentDataType.STRING);
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }
    
    /**
     * 检查村民是否已被招募
     * @param villager 村民实体
     * @return 是否已被招募
     */
    public static boolean isRecruited(Villager villager) {
        return getOwner(villager) != null;
    }
    
    /**
     * 检查村民是否属于指定玩家
     * @param villager 村民实体
     * @param player 玩家
     * @return 是否属于该玩家
     */
    public static boolean isOwnedBy(Villager villager, Player player) {
        UUID owner = getOwner(villager);
        return owner != null && owner.equals(player.getUniqueId());
    }
}