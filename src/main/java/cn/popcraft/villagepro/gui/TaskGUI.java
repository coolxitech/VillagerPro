package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.model.Task;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class TaskGUI implements Listener {
    private final VillagePro plugin;
    private final TaskManager taskManager;

    public TaskGUI(VillagePro plugin, TaskManager taskManager) {
        this.plugin = plugin;
        this.taskManager = taskManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 打开任务GUI
     */
    public void openTaskGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "任务中心");

        // 添加任务生成按钮
        ItemStack generateTask = new ItemStack(Material.BOOK);
        ItemMeta generateMeta = generateTask.getItemMeta();
        if (generateMeta != null) {
            generateMeta.setDisplayName(ChatColor.GREEN + "生成随机任务");
            generateMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "点击生成一个随机任务",
                ChatColor.YELLOW + "消耗: 100金币"
            ));
            generateTask.setItemMeta(generateMeta);
        }
        gui.setItem(13, generateTask);

        // 添加查看当前任务按钮
        ItemStack viewTasks = new ItemStack(Material.PAPER);
        ItemMeta viewMeta = viewTasks.getItemMeta();
        if (viewMeta != null) {
            viewMeta.setDisplayName(ChatColor.BLUE + "查看当前任务");
            viewMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "查看你的所有任务",
                ChatColor.GRAY + "包括进行中和已完成的任务"
            ));
            viewTasks.setItemMeta(viewMeta);
        }
        gui.setItem(11, viewTasks);

        // 添加任务商店按钮
        ItemStack taskShop = new ItemStack(Material.EMERALD);
        ItemMeta shopMeta = taskShop.getItemMeta();
        if (shopMeta != null) {
            shopMeta.setDisplayName(ChatColor.GOLD + "任务商店");
            shopMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "使用任务积分购买物品",
                ChatColor.YELLOW + "当前积分: " + getPlayerTaskPoints(player)
            ));
            taskShop.setItemMeta(shopMeta);
        }
        gui.setItem(15, taskShop);

        player.openInventory(gui);
    }

    /**
     * 打开玩家任务列表GUI
     */
    public void openPlayerTasksGUI(Player player) {
        List<Task> playerTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        
        int size = Math.max(9, ((playerTasks.size() + 8) / 9) * 9); // 计算需要的行数
        Inventory gui = Bukkit.createInventory(null, size, "我的任务");

        for (int i = 0; i < playerTasks.size() && i < size; i++) {
            Task task = playerTasks.get(i);
            ItemStack taskItem = createTaskItem(task);
            gui.setItem(i, taskItem);
        }

        player.openInventory(gui);
    }

    /**
     * 创建任务物品
     */
    private ItemStack createTaskItem(Task task) {
        Material material = getTaskMaterial(task.getType());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + task.getType().toString());
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "类型: " + task.getType().toString(),
                ChatColor.GRAY + "进度: " + task.getProgress() + "/" + task.getTargetAmount(),
                ChatColor.GRAY + "奖励: " + task.getRewardAmount() + "金币",
                task.getProgress() >= task.getTargetAmount() ? ChatColor.GREEN + "已完成" : ChatColor.RED + "进行中"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * 根据任务类型获取对应的材料
     */
    private Material getTaskMaterial(Task.TaskType taskType) {
        switch (taskType) {
            case COLLECT_WHEAT:
                return Material.WHEAT;
            case KILL_ZOMBIE:
                return Material.ZOMBIE_HEAD;
            case DELIVER_POTION:
                return Material.POTION;
            case MINE_IRON:
                return Material.IRON_ORE;
            case MINE_DIAMOND:
                return Material.DIAMOND_ORE;
            case BAKE_BREAD:
                return Material.BREAD;
            case KILL_SKELETON:
                return Material.SKELETON_SKULL;
            case KILL_CREEPER:
                return Material.CREEPER_HEAD;
            case REACH_LEVEL:
                return Material.EXPERIENCE_BOTTLE;
            default:
                return Material.PAPER;
        }
    }

    /**
     * 获取玩家任务积分
     */
    private int getPlayerTaskPoints(Player player) {
        // 这里应该从数据库或配置中获取玩家的任务积分
        // 暂时返回0，你需要根据实际需求实现
        return 0;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (!title.equals("任务中心") && !title.equals("我的任务")) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (title.equals("任务中心")) {
            handleTaskCenterClick(player, clickedItem);
        } else if (title.equals("我的任务")) {
            handlePlayerTasksClick(player, clickedItem);
        }
    }

    /**
     * 处理任务中心点击事件
     */
    private void handleTaskCenterClick(Player player, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());

        switch (displayName) {
            case "生成随机任务":
                generateRandomTask(player);
                break;
            case "查看当前任务":
                player.closeInventory();
                openPlayerTasksGUI(player);
                break;
            case "任务商店":
                player.sendMessage(ChatColor.YELLOW + "任务商店功能正在开发中...");
                break;
            default:
                break;
        }
    }

    /**
     * 处理玩家任务点击事件
     */
    private void handlePlayerTasksClick(Player player, ItemStack clickedItem) {
        // 这里可以添加任务详情查看或完成任务的逻辑
        player.sendMessage(ChatColor.GRAY + "点击查看任务详情...");
    }

    /**
     * 生成随机任务
     */
    private void generateRandomTask(Player player) {
        // 检查经济系统
        if (plugin.getEconomy() == null) {
            player.sendMessage(ChatColor.RED + "经济系统不可用！");
            return;
        }

        double cost = 100.0;
        
        // 检查玩家余额
        if (!plugin.getEconomy().has(player, cost)) {
            player.sendMessage(ChatColor.RED + "余额不足！需要: " + plugin.getEconomy().format(cost));
            return;
        }

        // 扣除费用
        if (plugin.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
            // 生成随机任务
            Task newTask = taskManager.generateRandomTask(player);
            if (newTask != null) {
                player.sendMessage(ChatColor.GREEN + "成功生成新任务: " + newTask.getType().toString());
                player.sendMessage(ChatColor.GRAY + "花费: " + plugin.getEconomy().format(cost));
                player.closeInventory();
            } else {
                // 如果任务生成失败，退还费用
                plugin.getEconomy().depositPlayer(player, cost);
                player.sendMessage(ChatColor.RED + "任务生成失败！");
            }
        } else {
            player.sendMessage(ChatColor.RED + "扣费失败！");
        }
    }
}
