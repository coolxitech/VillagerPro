package cn.popcraft.villagepro.gui;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.TaskManager;
import cn.popcraft.villagepro.model.PlayerTaskData;
import cn.popcraft.villagepro.model.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskGUI implements Listener {
    private final VillagePro plugin;
    private final TaskManager taskManager;
    // 添加一个映射来存储玩家正在查看的任务
    private final Map<UUID, Task> playerViewingTasks = new HashMap<>();

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

        // 添加返回按钮
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.GRAY + "返回");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(26, backButton); // 放在右下角

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

        // 添加返回按钮
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.GRAY + "返回");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(size - 1, backButton); // 放在末尾

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
        // 从数据库或配置中获取玩家的任务积分
        PlayerTaskData playerTaskData = taskManager.getPlayerTaskData(player.getUniqueId());
        if (playerTaskData != null) {
            return playerTaskData.getTaskPoints();
        }
        return 0;
    }

    /**
     * 打开任务商店GUI
     */
    public void openTaskShopGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "任务商店");
        
        // 示例商品 - 可以用任务积分兑换的物品
        ItemStack speedPotion = new ItemStack(Material.POTION);
        ItemMeta speedMeta = speedPotion.getItemMeta();
        if (speedMeta != null) {
            speedMeta.setDisplayName(ChatColor.AQUA + "迅捷药水");
            speedMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "使用后获得速度II效果30秒",
                ChatColor.YELLOW + "价格: 50任务积分"
            ));
            speedPotion.setItemMeta(speedMeta);
        }
        gui.setItem(11, speedPotion);
        
        ItemStack strengthPotion = new ItemStack(Material.POTION);
        ItemMeta strengthMeta = strengthPotion.getItemMeta();
        if (strengthMeta != null) {
            strengthMeta.setDisplayName(ChatColor.RED + "力量药水");
            strengthMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "使用后获得力量效果30秒",
                ChatColor.YELLOW + "价格: 75任务积分"
            ));
            strengthPotion.setItemMeta(strengthMeta);
        }
        gui.setItem(12, strengthPotion);
        
        ItemStack jumpBoostPotion = new ItemStack(Material.POTION);
        ItemMeta jumpMeta = jumpBoostPotion.getItemMeta();
        if (jumpMeta != null) {
            jumpMeta.setDisplayName(ChatColor.GREEN + "跳跃提升药水");
            jumpMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "使用后获得跳跃提升效果30秒",
                ChatColor.YELLOW + "价格: 40任务积分"
            ));
            jumpBoostPotion.setItemMeta(jumpMeta);
        }
        gui.setItem(13, jumpBoostPotion);
        
        ItemStack healPotion = new ItemStack(Material.POTION);
        ItemMeta healMeta = healPotion.getItemMeta();
        if (healMeta != null) {
            healMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "治疗药水");
            healMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "立即恢复4颗心的生命值",
                ChatColor.YELLOW + "价格: 60任务积分"
            ));
            healPotion.setItemMeta(healMeta);
        }
        gui.setItem(14, healPotion);
        
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = diamond.getItemMeta();
        if (diamondMeta != null) {
            diamondMeta.setDisplayName(ChatColor.AQUA + "钻石");
            diamondMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "珍贵的钻石",
                ChatColor.YELLOW + "价格: 100任务积分"
            ));
            diamond.setItemMeta(diamondMeta);
        }
        gui.setItem(15, diamond);
        
        // 添加返回按钮
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.GRAY + "返回");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(26, backButton);
        
        player.openInventory(gui);
    }
    
    /**
     * 处理任务商店点击事件
     */
    private void handleTaskShopClick(Player player, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        PlayerTaskData playerTaskData = taskManager.getPlayerTaskData(player.getUniqueId());
        
        if (playerTaskData == null) {
            player.sendMessage(ChatColor.RED + "无法获取玩家任务数据!");
            return;
        }
        
        int playerPoints = playerTaskData.getTaskPoints();
        
        switch (displayName) {
            case "迅捷药水":
                if (playerPoints >= 50) {
                    playerTaskData.addTaskPoints(-50);
                    player.getInventory().addItem(createPotionItem(Material.POTION, ChatColor.AQUA + "迅捷药水", "SPEED"));
                    player.sendMessage(ChatColor.GREEN + "购买成功! 获得迅捷药水");
                    taskManager.savePlayerTaskData(playerTaskData);
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "任务积分不足! 需要50积分");
                }
                break;
            case "力量药水":
                if (playerPoints >= 75) {
                    playerTaskData.addTaskPoints(-75);
                    player.getInventory().addItem(createPotionItem(Material.POTION, ChatColor.RED + "力量药水", "STRENGTH"));
                    player.sendMessage(ChatColor.GREEN + "购买成功! 获得力量药水");
                    taskManager.savePlayerTaskData(playerTaskData);
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "任务积分不足! 需要75积分");
                }
                break;
            case "跳跃提升药水":
                if (playerPoints >= 40) {
                    playerTaskData.addTaskPoints(-40);
                    player.getInventory().addItem(createPotionItem(Material.POTION, ChatColor.GREEN + "跳跃提升药水", "JUMP"));
                    player.sendMessage(ChatColor.GREEN + "购买成功! 获得跳跃提升药水");
                    taskManager.savePlayerTaskData(playerTaskData);
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "任务积分不足! 需要40积分");
                }
                break;
            case "治疗药水":
                if (playerPoints >= 60) {
                    playerTaskData.addTaskPoints(-60);
                    player.getInventory().addItem(createPotionItem(Material.POTION, ChatColor.LIGHT_PURPLE + "治疗药水", "HEAL"));
                    player.sendMessage(ChatColor.GREEN + "购买成功! 获得治疗药水");
                    taskManager.savePlayerTaskData(playerTaskData);
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "任务积分不足! 需要60积分");
                }
                break;
            case "钻石":
                if (playerPoints >= 100) {
                    playerTaskData.addTaskPoints(-100);
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND));
                    player.sendMessage(ChatColor.GREEN + "购买成功! 获得钻石");
                    taskManager.savePlayerTaskData(playerTaskData);
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "任务积分不足! 需要100积分");
                }
                break;
            case "返回":
                player.closeInventory();
                openTaskGUI(player);
                break;
        }
    }
    
    /**
     * 创建药水物品
     */
    private ItemStack createPotionItem(Material material, String displayName, String potionType) {
        ItemStack potion = new ItemStack(material);
        ItemMeta meta = potion.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "药水类型: " + potionType));
            potion.setItemMeta(meta);
        }
        return potion;
    }
    
    /**
     * 处理任务完成时奖励积分
     */
    public void rewardTaskPoints(Player player, int points) {
        PlayerTaskData playerTaskData = taskManager.getPlayerTaskData(player.getUniqueId());
        if (playerTaskData != null) {
            playerTaskData.addTaskPoints(points);
            taskManager.savePlayerTaskData(playerTaskData);
            player.sendMessage(ChatColor.GREEN + "获得 " + points + " 任务积分! 当前积分: " + playerTaskData.getTaskPoints());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        boolean isTaskGUI = title.equals("任务中心") || title.equals("我的任务") || title.equals("任务商店") || title.equals("任务详情");
        
        // 如果是任务GUI，完全阻止所有操作
        if (isTaskGUI) {
            // 完全阻止所有可能的物品移动操作，包括Shift+点击、双击、拖拽等
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
        } else {
            return; // 不是任务GUI，直接返回
        }

        // 阻止所有类型的点击操作，包括Shift+点击、双击等
        if (event.getClickedInventory() == null) {
            return; // 点击的是虚空
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (title.equals("任务中心")) {
            handleTaskCenterClick(player, clickedItem);
        } else if (title.equals("我的任务")) {
            handlePlayerTasksClick(player, clickedItem);
        } else if (title.equals("任务商店")) {
            handleTaskShopClick(player, clickedItem);
        } else if (title.equals("任务详情")) {
            Task currentTask = playerViewingTasks.get(player.getUniqueId());
            if (currentTask != null) {
                handleTaskDetailClick(player, clickedItem, currentTask);
            }
        }
    }

    /**
     * 处理拖拽事件，防止玩家拖拽任务GUI中的物品
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String inventoryTitle = event.getView().getTitle();
        
        // 阻止在任务GUI中拖拽物品
        if (inventoryTitle.equals("任务中心") || inventoryTitle.equals("我的任务")) {
            // 完全阻止拖拽操作
            event.setCancelled(true);
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
                openTaskShopGUI(player);
                break;
            default:
                break;
        }
    }

    /**
     * 处理玩家任务点击事件
     */
    private void handlePlayerTasksClick(Player player, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());

        if (displayName.equals("返回")) {
            player.closeInventory();
            openTaskGUI(player); // 返回任务中心
            return;
        }

        // 查找对应的任务并显示详情
        List<Task> playerTasks = taskManager.getPlayerActiveTasks(player.getUniqueId());
        for (Task task : playerTasks) {
            if (ChatColor.stripColor(getTaskMaterial(task.getType()).name()).equals(displayName) ||
                ChatColor.stripColor(task.getType().toString()).equals(displayName)) {
                
                // 打开任务详情GUI而不是发送消息
                player.closeInventory();
                openTaskDetailGUI(player, task);
                break;
            }
        }
    }

    /**
     * 处理任务详情GUI点击事件
     */
    private void handleTaskDetailClick(Player player, ItemStack clickedItem, Task task) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        
        switch (displayName) {
            case "完成任务":
                if (task.getProgress() >= task.getTargetAmount()) {
                    // 发放奖励
                    if (plugin.getEconomyManager().isAvailable()) {
                        plugin.getEconomyManager().deposit(player, task.getRewardMoney());
                    }
                    player.giveExp(task.getRewardExp());
                    
                    // 奖励任务积分
                    int taskPoints = (int)(task.getRewardMoney() * 0.1);
                    rewardTaskPoints(player, taskPoints);
                    
                    // 从任务列表中移除
                    taskManager.completeTask(player.getUniqueId(), task.getTaskId());
                    
                    // 清理查看的任务
                    playerViewingTasks.remove(player.getUniqueId());
                    
                    player.sendMessage(ChatColor.GREEN + "任务完成!");
                    player.sendMessage(ChatColor.GREEN + "获得奖励: " + task.getRewardMoney() + "金币, " + 
                                     task.getRewardExp() + "经验, " + taskPoints + "任务积分");
                    player.closeInventory();
                    openTaskGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "任务尚未完成!");
                }
                break;
                
            case "放弃任务":
                // 从任务列表中移除
                taskManager.completeTask(player.getUniqueId(), task.getTaskId());
                
                // 清理查看的任务
                playerViewingTasks.remove(player.getUniqueId());
                
                player.sendMessage(ChatColor.YELLOW + "你放弃了任务: " + task.getType().toString());
                player.closeInventory();
                openTaskGUI(player);
                break;
                
            case "返回":
                // 清理查看的任务
                playerViewingTasks.remove(player.getUniqueId());
                
                player.closeInventory();
                openPlayerTasksGUI(player);
                break;
        }
    }

    /**
     * 打开任务详情GUI
     */
    private void openTaskDetailGUI(Player player, Task task) {
        // 将任务存储到映射中
        playerViewingTasks.put(player.getUniqueId(), task);
        
        Inventory gui = Bukkit.createInventory(null, 27, "任务详情");
        
        // 任务信息
        ItemStack taskInfo = new ItemStack(getTaskMaterial(task.getType()));
        ItemMeta infoMeta = taskInfo.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.GOLD + task.getType().toString());
            List<String> lore = Arrays.asList(
                ChatColor.GRAY + "任务描述: " + task.getDescription(),
                ChatColor.GRAY + "任务进度: " + task.getProgress() + "/" + task.getTargetAmount(),
                "",
                ChatColor.YELLOW + "奖励:",
                ChatColor.GRAY + "  金币: " + task.getRewardMoney(),
                ChatColor.GRAY + "  经验: " + task.getRewardExp(),
                ChatColor.GRAY + "  积分: " + (int)(task.getRewardMoney() * 0.1)
            );
            infoMeta.setLore(lore);
            taskInfo.setItemMeta(infoMeta);
        }
        gui.setItem(13, taskInfo);
        
        // 完成任务按钮（如果已完成）
        if (task.getProgress() >= task.getTargetAmount()) {
            ItemStack completeButton = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta completeMeta = completeButton.getItemMeta();
            if (completeMeta != null) {
                completeMeta.setDisplayName(ChatColor.GREEN + "完成任务");
                completeMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "点击领取奖励",
                    ChatColor.YELLOW + "金币: " + task.getRewardMoney(),
                    ChatColor.YELLOW + "经验: " + task.getRewardExp(),
                    ChatColor.YELLOW + "积分: " + (int)(task.getRewardMoney() * 0.1)
                ));
                completeButton.setItemMeta(completeMeta);
            }
            gui.setItem(11, completeButton);
        }
        
        // 放弃任务按钮
        ItemStack abandonButton = new ItemStack(Material.BARRIER);
        ItemMeta abandonMeta = abandonButton.getItemMeta();
        if (abandonMeta != null) {
            abandonMeta.setDisplayName(ChatColor.RED + "放弃任务");
            abandonMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "点击放弃此任务",
                ChatColor.RED + "注意: 此操作不可撤销!"
            ));
            abandonButton.setItemMeta(abandonMeta);
        }
        gui.setItem(15, abandonButton);
        
        // 返回按钮
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.GRAY + "返回");
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(26, backButton);
        
        player.openInventory(gui);
    }

    /**
     * 生成随机任务
     */
    private void generateRandomTask(Player player) {
        // 检查经济系统
        if (!plugin.getEconomyManager().isAvailable()) {
            player.sendMessage(ChatColor.RED + "经济系统不可用！");
            return;
        }

        double cost = 100.0;
        
        // 检查玩家余额
        if (!plugin.getEconomyManager().has(player, cost)) {
            player.sendMessage(ChatColor.RED + "余额不足！需要: " + plugin.getEconomyManager().format(cost));
            return;
        }

        // 扣除费用
        if (plugin.getEconomyManager().withdraw(player, cost)) {
            // 生成随机任务
            taskManager.assignNewTask(player);
            player.sendMessage(ChatColor.GREEN + "成功生成新任务");
            player.sendMessage(ChatColor.GRAY + "花费: " + plugin.getEconomyManager().format(cost));
            player.closeInventory();
            
            /*
            if (newTask != null) {
                player.sendMessage(ChatColor.GREEN + "成功生成新任务: " + newTask.getType().toString());
                player.sendMessage(ChatColor.GRAY + "花费: " + plugin.getEconomyManager().format(cost));
                player.closeInventory();
            } else {
                // 如果任务生成失败，退还费用
                plugin.getEconomyManager().deposit(player, cost);
                player.sendMessage(ChatColor.RED + "任务生成失败！");
            }
            */
        } else {
            player.sendMessage(ChatColor.RED + "扣费失败！");
        }
    }
}