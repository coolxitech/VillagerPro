package cn.popcraft.villagepro.util;

import cn.popcraft.villagepro.VillagePro;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * PlayerPoints兼容工具类
 * 用于处理PlayerPoints经济插件的兼容性
 */
public class PlayerPointsCompat {
    private final VillagePro plugin;
    private Plugin playerPointsPlugin;
    private boolean available = false;

    public PlayerPointsCompat(VillagePro plugin) {
        this.plugin = plugin;
        setupPlayerPoints();
    }

    /**
     * 设置PlayerPoints系统
     */
    private void setupPlayerPoints() {
        try {
            playerPointsPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
            if (playerPointsPlugin != null && playerPointsPlugin.isEnabled()) {
                // 尝试验证API是否可访问
                boolean apiAvailable = false;
                
                // 尝试多种方式获取API
                try {
                    Object api = playerPointsPlugin.getClass().getMethod("getAPI").invoke(playerPointsPlugin);
                    if (api != null) {
                        apiAvailable = true;
                    }
                } catch (Exception e1) {
                    try {
                        Object api = playerPointsPlugin.getClass().getMethod("getApi").invoke(playerPointsPlugin);
                        if (api != null) {
                            apiAvailable = true;
                        }
                    } catch (Exception e2) {
                        try {
                            java.lang.reflect.Field apiField = playerPointsPlugin.getClass().getDeclaredField("api");
                            apiField.setAccessible(true);
                            Object api = apiField.get(playerPointsPlugin);
                            if (api != null) {
                                apiAvailable = true;
                            }
                        } catch (Exception e3) {
                            plugin.getLogger().log(Level.WARNING, "PlayerPoints插件API访问失败: 无法通过反射获取API", e3);
                        }
                    }
                }
                
                if (apiAvailable) {
                    available = true;
                    plugin.getLogger().log(Level.INFO, "成功连接到PlayerPoints插件");
                } else {
                    plugin.getLogger().log(Level.WARNING, "PlayerPoints插件已找到但API不可用，点券系统功能将不可用");
                }
            } else {
                plugin.getLogger().log(Level.WARNING, "未找到PlayerPoints插件，点券系统功能将不可用");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "检查PlayerPoints插件时发生错误: " + e.getMessage() + "，点券系统功能将不可用", e);
            available = false;
        }
    }

    /**
     * 检查PlayerPoints系统是否可用
     *
     * @return PlayerPoints系统是否可用
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * 检查玩家是否有足够的点券
     *
     * @param player 玩家
     * @param amount 点券数量
     * @return 是否有足够的点券
     */
    public boolean has(Player player, int amount) {
        if (!available) {
            return true;
        }
        try {
            Object api = playerPointsPlugin.getClass().getMethod("getAPI").invoke(playerPointsPlugin);
            Object points = api.getClass().getMethod("look", Player.class).invoke(api, player);
            if (points instanceof Integer) {
                return (Integer) points >= amount;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "检查玩家点券数量时发生错误: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * 从玩家扣除点券
     *
     * @param player 玩家
     * @param amount 点券数量
     * @return 是否成功扣除
     */
    public boolean takePoints(Player player, int amount) {
        if (!available) {
            return true;
        }
        try {
            Object api = playerPointsPlugin.getClass().getMethod("getAPI").invoke(playerPointsPlugin);
            return (boolean) api.getClass().getMethod("take", Player.class, int.class).invoke(api, player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "扣除玩家点券时发生错误: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 给玩家增加点券
     *
     * @param player 玩家
     * @param amount 点券数量
     * @return 是否成功增加
     */
    public boolean givePoints(Player player, int amount) {
        if (!available) {
            return true;
        }
        try {
            Object api = playerPointsPlugin.getClass().getMethod("getAPI").invoke(playerPointsPlugin);
            return (boolean) api.getClass().getMethod("give", Player.class, int.class).invoke(api, player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "给玩家增加点券时发生错误: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取玩家点券余额
     *
     * @param player 玩家
     * @return 点券余额
     */
    public int getPoints(Player player) {
        if (!available) {
            return 0;
        }
        try {
            Object api = playerPointsPlugin.getClass().getMethod("getAPI").invoke(playerPointsPlugin);
            Object points = api.getClass().getMethod("look", Player.class).invoke(api, player);
            if (points instanceof Integer) {
                return (Integer) points;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "获取玩家点券余额时发生错误: " + e.getMessage(), e);
        }
        return 0;
    }
}