package cn.popcraft.villagepro.manager;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.util.CMICompat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;

/**
 * 经济管理器，用于处理各种经济插件的兼容性，包括CMI、EssentialsX等
 */
public class EconomyManager {
    private final VillagePro plugin;
    private Economy economy = null;
    private boolean cmiEconomySupported = false;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public EconomyManager(VillagePro plugin) {
        this.plugin = plugin;
        setupEconomy();
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
        }
        
        return economy != null;
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
     * 获取玩家余额
     * 
     * @param player 玩家
     * @return 余额
     */
    public double getBalance(Player player) {
        if (economy == null) {
            return 0.0;
        }
        
        // 优先使用CMICompat工具类获取余额
        if (cmiEconomySupported && player != null) {
            double cmiBalance = CMICompat.getBalance(player);
            if (cmiBalance >= 0) {
                plugin.getLogger().info(plugin.getMessageManager().getMessage("economy.cmi-fallback"));
                return cmiBalance;
            }
        }
        
        // 如果CMI不可用，使用Vault的通用方式
        if (player != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }
    
    /**
     * 扣除玩家余额
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功扣除
     */
    public boolean withdraw(Player player, double amount) {
        if (economy == null || player == null || amount <= 0) {
            return false;
        }
        
        // 优先使用CMICompat工具类进行余额操作
        if (cmiEconomySupported) {
            if (CMICompat.withdraw(player, amount)) {
                return true;
            } else {
                plugin.getLogger().warning(plugin.getMessageManager().getMessage("economy.cmi-withdraw-failure"));
            }
        }
        
        // 如果CMI不可用，使用Vault的通用方式
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * 向玩家账户添加余额
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功添加
     */
    public boolean deposit(Player player, double amount) {
        if (economy == null || player == null || amount <= 0) {
            return false;
        }
        
        // 优先使用CMICompat工具类进行余额操作
        if (cmiEconomySupported) {
            if (CMICompat.deposit(player, amount)) {
                return true;
            } else {
                plugin.getLogger().warning(plugin.getMessageManager().getMessage("economy.cmi-deposit-failure"));
            }
        }
        
        // 如果CMI不可用，使用Vault的通用方式
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * 格式化金额
     * 
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public String format(double amount) {
        return decimalFormat.format(amount);
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