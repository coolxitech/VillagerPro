package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.util.PlayerPointsCompat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;

/**
 * 经济管理器，用于处理各种经济插件的兼容性，包括CMI、EssentialsX、PlayerPoints等
 */
public class EconomyManager {
    private final VillagePro plugin;
    private Economy economy = null;
    private boolean cmiEconomySupported = false;
    private PlayerPointsCompat playerPointsCompat = null;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public EconomyManager(VillagePro plugin) {
        this.plugin = plugin;
        setupEconomy();
        setupPlayerPoints();
    }

    /**
     * 设置经济系统
     *
     * @return 是否成功设置经济系统
     */
    private boolean setupEconomy() {
        // 检查Vault是否存在
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("未找到 Vault 插件，经济系统将不可用");
            return false;
        }

        // 尝试获取经济服务
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                plugin.getLogger().warning("未找到经济系统提供商，经济系统将不可用");
                return false;
            }

            economy = rsp.getProvider();
            
            // 检查是否为CMI经济系统
            String economyName = economy.getName();
            if (economyName != null) {
                cmiEconomySupported = economyName.contains("CMI");
                plugin.getLogger().info("经济系统已启用: " + economyName + 
                    (cmiEconomySupported ? " (检测到CMI经济系统)" : ""));
            } else {
                plugin.getLogger().info("经济系统已启用");
            }
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("设置经济系统时发生错误: " + e.getMessage() + "，经济系统将不可用");
            economy = null;
            return false;
        }
    }

    /**
     * 检查玩家是否有足够的资金
     *
     * @param player 玩家
     * @param amount 金额
     * @return 是否有足够的资金
     */
    public boolean has(Player player, double amount) {
        if (economy == null || amount <= 0) {
            return true;
        }
        return economy.has(player, amount);
    }

    /**
     * 从玩家账户扣除资金
     *
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功扣除
     */
    public boolean withdraw(Player player, double amount) {
        if (economy == null || amount <= 0) {
            return true;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * 向玩家账户存入资金
     *
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功存入
     */
    public boolean deposit(Player player, double amount) {
        if (economy == null || amount <= 0) {
            return true;
        }
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * 获取玩家账户余额
     *
     * @param player 玩家
     * @return 账户余额
     */
    public double getBalance(Player player) {
        if (economy == null) {
            return 0;
        }
        return economy.getBalance(player);
    }

    /**
     * 格式化金额显示
     *
     * @param amount 金额
     * @return 格式化后的金额字符串
     */
    public String format(double amount) {
        if (economy != null) {
            return economy.format(amount);
        }
        return decimalFormat.format(amount);
    }

    /**
     * 设置PlayerPoints系统
     */
    void setupPlayerPoints() {
        playerPointsCompat = new PlayerPointsCompat(plugin);
        if (playerPointsCompat.isAvailable()) {
            plugin.getLogger().info("PlayerPoints系统已启用");
        }
    }

    /**
     * 检查经济系统是否可用
     *
     * @return 经济系统是否可用
     */
    public boolean isAvailable() {
        return economy != null;
    }
    
    /**
     * 检查PlayerPoints系统是否可用
     *
     * @return PlayerPoints系统是否可用
     */
    public boolean isPlayerPointsAvailable() {
        return playerPointsCompat != null && playerPointsCompat.isAvailable();
    }
    
    /**
     * 检查玩家是否有足够的点券
     *
     * @param player 玩家
     * @param amount 点券数量
     * @return 是否有足够的点券
     */
    public boolean hasPoints(Player player, int amount) {
        if (playerPointsCompat == null || amount <= 0) {
            return true;
        }
        return playerPointsCompat.has(player, amount);
    }

    /**
     * 从玩家扣除点券
     *
     * @param player 玩家
     * @param amount 点券数量
     * @return 是否成功扣除
     */
    public boolean withdrawPoints(Player player, int amount) {
        if (playerPointsCompat == null || amount <= 0) {
            return true;
        }
        return playerPointsCompat.takePoints(player, amount);
    }

    /**
     * 给玩家增加点券
     *
     * @param player 玩家
     * @param amount 点券数量
     * @return 是否成功增加
     */
    public boolean depositPoints(Player player, int amount) {
        if (playerPointsCompat == null || amount <= 0) {
            return true;
        }
        return playerPointsCompat.givePoints(player, amount);
    }

    /**
     * 获取玩家点券余额
     *
     * @param player 玩家
     * @return 点券余额
     */
    public int getPointsBalance(Player player) {
        if (playerPointsCompat == null) {
            return 0;
        }
        return playerPointsCompat.getPoints(player);
    }

    /**
     * 获取经济系统名称
     *
     * @return 经济系统名称
     */
    public String getEconomyName() {
        return economy != null ? economy.getName() : "None";
    }

    /**
     * 检查是否支持CMI经济系统
     *
     * @return 是否支持CMI经济系统
     */
    public boolean isCmiEconomySupported() {
        return cmiEconomySupported;
    }
}