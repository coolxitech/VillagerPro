package cn.popcraft.villagepro.util;

import org.bukkit.entity.Villager;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import cn.popcraft.villagepro.VillagePro;
import java.util.UUID;

public class VillagerUtils {
    private static final NamespacedKey OWNER_KEY = new NamespacedKey(VillagePro.getInstance(), "owner");

    public static boolean isRecruited(Villager villager) {
        return getOwner(villager) != null;
    }

    public static UUID getOwner(Villager villager) {
        PersistentDataContainer data = villager.getPersistentDataContainer();
        if (data.has(OWNER_KEY, PersistentDataType.STRING)) {
            String uuidStr = data.get(OWNER_KEY, PersistentDataType.STRING);
            try {
                return UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public static void setOwner(Villager villager, UUID ownerUuid) {
        PersistentDataContainer data = villager.getPersistentDataContainer();
        if (ownerUuid != null) {
            data.set(OWNER_KEY, PersistentDataType.STRING, ownerUuid.toString());
        } else {
            data.remove(OWNER_KEY);
        }
    }
    
    /**
     * 检查村民是否属于指定玩家
     * @param villager 村民
     * @param player 玩家
     * @return 如果村民属于玩家则返回true，否则返回false
     */
    public static boolean isOwnedBy(Villager villager, Player player) {
        UUID ownerUuid = getOwner(villager);
        return ownerUuid != null && ownerUuid.equals(player.getUniqueId());
    }
}