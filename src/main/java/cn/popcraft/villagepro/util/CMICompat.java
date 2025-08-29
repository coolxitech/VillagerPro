package cn.popcraft.villagepro.util;

import org.bukkit.entity.Player;

/**
 * CMI 兼容工具类，提供对 CMI 经济系统的直接访问
 * 用于在 Vault API 返回不准确的余额时提供替代实现
 */
public class CMICompat {
    /**
     * 获取玩家的 CMI 余额
     * 
     * @param player 玩家
     * @return 余额
     */
    public static double getBalance(Player player) {
        if (player == null) {
            return 0.0;
        }
        
        try {
            // 使用CMI的API获取余额
            Class<?> cmiEconomyClass = Class.forName("com.elikill58.negativity.api.CMICompat");
            java.lang.reflect.Method getBalanceMethod = cmiEconomyClass.getMethod("getBalance", Player.class);
            return (double) getBalanceMethod.invoke(null, player);
        } catch (Exception e) {
            // 如果CMI不可用，返回0
            return 0.0;
        }
    }
    
    /**
     * 从玩家账户扣除CMI余额
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功扣除
     */
    public static boolean withdraw(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }
        
        try {
            // 使用CMI的API进行余额操作
            Class<?> cmiEconomyClass = Class.forName("com.elikill58.negativity.api.CMICompat");
            java.lang.reflect.Method withdrawMethod = cmiEconomyClass.getMethod("withdraw", Player.class, double.class);
            return (boolean) withdrawMethod.invoke(null, player, amount);
        } catch (Exception e) {
            // 如果CMI不可用，返回false
            return false;
        }
    }
    
    /**
     * 向玩家账户添加CMI余额
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功添加
     */
    public static boolean deposit(Player player, double amount) {
        if (player == null || amount <= 0) {
            return false;
        }
        
        try {
            // 使用CMI的API进行余额操作
            Class<?> cmiEconomyClass = Class.forName("com.elikill58.negativity.api.CMICompat");
            java.lang.reflect.Method depositMethod = cmiEconomyClass.getMethod("deposit", Player.class, double.class);
            return (boolean) depositMethod.invoke(null, player, amount);
        } catch (Exception e) {
            // 如果CMI不可用，返回false
            return false;
        }
    }
}