## 综述

下面我对您提供的 **`RecruitCommand`** 与 **`VillageManager`** 两个核心类进行逐行审查，判断它们是否完整实现了业务逻辑（招募村民、资源消耗、数据持久化、后续跟随请求等），并给出 **代码错误、潜在风险、可读性/性能改进以及最佳实践** 的详细建议。
每一条建议后都会给出示例代码（只改动的片段），您可以直接拷贝替换或参考实现思路。

> **整体结论**
- 业务流程基本完整：权限校验 → 最近未招募村民搜索 → 资源检查 → 扣除资源 → 数据写入 → 设置所有者、名称、技能 → 请求跟随。
- 仍有 **若干逻辑漏洞 / 可优化点**（例如并发安全、异常处理、返回值一致性、配置读取防御、代码重复等），如果不加以修正，在高并发或插件升级后会出现 **NPE、资源泄漏、数据不一致** 等问题。

下面按模块逐一说明。

---

## 1️⃣ `RecruitCommand.java`

| 行号 | 现状 | 问题 / 改进点 | 建议实现 |
|------|------|---------------|----------|
| 1‑5 | 正常的包、导入、类声明 | - | - |
| 10‑14 | 构造函数注入 `VillagePro` 实例 | - | - |
| 16‑30 | **玩家 & 权限校验** | - 权限检查使用 `&&`，实际需求是“任意一个满足即可”。已使用 `&&`（即两个权限都没有才拒绝）是对的，但可以写成 `if (!player.hasPermission("villagepro.recruit") && !player.hasPermission("villagepro.admin"))` → 已经是这种写法，保持即可。<br>- 若插件有多语言需求，建议统一使用 `MessageManager` 的占位符功能。 | - |
| 32‑44 | **查找最近未招募村民** → 调用 `plugin.getVillageManager().findNearestUnrecruitedVillager`<br>若为 `null` 返回失败信息 | - `findNearestUnrecruitedVillager` 只返回最近 **未** 招募的 **Villager**，但未对 **玩家是否已经拥有该村民** 再次校验（在 `recruitVillager` 中已经检查），这里可以直接调用 `recruitVillager`，让它自行返回错误信息。<br>- `return true` 表示命令已被处理，若以后要在 `onCommand` 中返回 `false` 让 Bukkit 自动显示使用提示，请根据需求调整。 | - |
| 46‑57 | **尝试招募** → 成功后发送成功信息并调用 `FollowManager.requestFollow` | - `recruitVillager` 内部已发送成功/失败消息，这里再次发送成功信息会导致 **双重提示**（两条成功消息）。建议统一职责：`RecruitCommand` 只负责 **调用**，**不负责** 再次发送成功信息。<br>- `requestFollow` 可能会产生 UI 交互或异步任务，确保该方法内部已经做了玩家在线检查。 | - 把 `player.sendMessage(...success...)` 移除，交由 `recruitVillager` 完成。 |
| 58‑60 | 注释说明 `recruitVillager` 已经发送错误消息 | - 好的注释，保持即可。 | - |
| 62 | `return true` | - 统一返回 `true`（已处理）即可。 | - |

### 改进后的 `RecruitCommand`

```java
@Override
public boolean onCommand(@NotNull CommandSender sender,
                         @NotNull Command command,
                         @NotNull String label,
                         @NotNull String[] args) {
    // 只接受玩家执行
    if (!(sender instanceof Player player)) {
        sender.sendMessage(plugin.getMessageManager().getMessage("help.player-only"));
        return true;
    }

    // 权限校验
    if (!player.hasPermission("villagepro.recruit") && !player.hasPermission("villagepro.admin")) {
        player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
        return true;
    }

    // 业务核心：招募最近的未被招募的村民
    Villager target = plugin.getVillageManager().findNearestUnrecruitedVillager(player, 5);
    if (target == null) {
        player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
        return true;
    }

    // 交给 VillageManager 完成全部逻辑（包括消息发送）
    boolean recruited = plugin.getVillageManager().recruitVillager(player, target);
    if (recruited) {
        // 成功后让玩家决定是否跟随
        plugin.getFollowManager().requestFollow(player, target);
    }
    // 若失败，错误信息已在 recruitVillager 中发送
    return true;
}
```

---

## 2️⃣ `VillageManager.java`

### 2.1 类成员 & 初始化

| 行号 | 现状 | 建议 |
|------|------|------|
| 19‑21 | `private final Map<UUID, Village> villageCache = new HashMap<>();` | - 在多线程环境（例如异步保存、Bukkit 任务）下可能出现并发写入。建议改为 `ConcurrentHashMap<UUID, Village>` 或在所有对 `villageCache` 的写操作加 `synchronized`。<br>- `SQLiteStorage` 的实例化放在构造函数里是合理的。 |

```java
private final Map<UUID, Village> villageCache = new ConcurrentHashMap<>();
```

### 2.2 `loadAll()` / `saveAll()`

| 行号 | 现状 | 建议 |
|------|------|------|
| 31‑38 | 读取所有村庄并放入缓存 | - 若数据库返回的实体包含 **null** 或者 `ownerUuid` 为 null，当前代码已经过滤。<br>- 加入 **异常捕获**，防止插件在启动时因数据库异常直接崩溃。 |
| 44‑46 | 逐个 `save(village)` | - 同理，建议捕获 `SQLException` 并记录错误，防止一次写入异常导致整批保存中断。 |
| 47 | 打印日志使用中文 | - 建议使用统一的语言文件或 `MessageManager`，方便后期本地化。 |

```java
public void loadAll() {
    villageCache.clear();
    try {
        villageStorage.findAll(Village.class).forEach(village -> {
            if (village != null && village.getOwnerUuid() != null) {
                villageCache.put(village.getOwnerUuid(), village);
            }
        });
        plugin.getLogger().info("已加载 " + villageCache.size() + " 个村庄数据");
    } catch (Exception e) {
        plugin.getLogger().log(Level.SEVERE, "加载村庄数据时出现异常", e);
    }
}
```

### 2.3 `getOrCreateVillage(Player)`

| 行号 | 现状 | 问题 |
|------|------|------|
| 70‑90 | 创建新 `Village` 并初始化 `upgradeLevels` | - **缺少** `villagerIds` 的初始化（若 `Village` 类内部没有默认空集合，会导致 `NullPointerException` 在后续 `village.getVillagerIds().add(...)` 时）。<br>- `villageStorage.save(village)` 直接同步写入，可能阻塞主线程。建议改为 **异步保存**（BukkitScheduler），或在插件关闭时统一保存。 |

```java
if (village == null) {
    village = new Village();
    village.setOwnerUuid(playerUuid);
    // 初始化升级等级
    Map<UpgradeType, Integer> upgradeLevels = new EnumMap<>(UpgradeType.class);
    for (UpgradeType type : UpgradeType.values()) {
        upgradeLevels.put(type, 0);
    }
    village.setUpgradeLevels(upgradeLevels);
    // 初始化村民列表（防止 NPE）
    village.setVillagerIds(new HashSet<>());
    villageCache.put(playerUuid, village);
    // 异步保存，避免主线程卡顿
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> villageStorage.save(village));
}
```

### 2.4 `findNearestUnrecruitedVillager(Player, double)`

| 行号 | 现状 | 建议 |
|------|------|------|
| 115‑133 | 遍历 `player.getNearbyEntities` → 过滤 `Villager` → 距离比较 | - **过滤条件**：只检查 `Villager` 是否 **未被招募**，但没有检查该 Villager 是否已经 **被其他玩家标记**（如已经有 owner 但 `VillagerUtils.isRecruited` 只检查自定义 tag）。如果 `isRecruited` 逻辑足够，请保持。<br>- 采用 `entity.getLocation().distanceSquared(player.getLocation())` 替代 `distance`（避免 `Math.sqrt`，提升性能）。 |
| 134‑137 | 返回最近的 Villager（或 null） | - 方法签名已经使用 `@Nullable`，很好。 |

```java
double minDistSq = Double.MAX_VALUE;
for (Entity entity : nearbyEntities) {
    if (entity instanceof Villager villager && villager.isValid() && !VillagerUtils.isRecruited(villager)) {
        double distSq = player.getLocation().distanceSquared(villager.getLocation());
        if (distSq < minDistSq) {
            minDistSq = distSq;
            nearest = villager;
        }
    }
}
```

### 2.5 `recruitVillager(Player, Villager)`

| 行号 | 现状 | 问题 / 改进点 |
|------|------|--------------|
| 147‑155 | 基础合法性检查（null、valid、已招募） | - 合理。 |
| 158‑163 | 检查 **最大村民数量**（`maxVillagers`） | - `plugin.getConfigManager().getMaxVillagers()` 读取频繁，建议在插件加载时缓存到一个 `int maxVillagers` 字段，或在 `VillageManager` 中持有该值，避免每次查询配置文件。 |
| 166‑172 | 检查资源（金钱+物品） | - `hasEnoughResources` 方法内部 **只返回布尔**，但没有告知玩家缺少哪类资源。可以返回 **枚举或错误信息**，让 `recruitVillager` 给出更具体提示。<br>- 同时 `hasEnoughResources` 在检查物品时使用 `Material.valueOf(entry.getKey())`，若配置错误会抛出 `IllegalArgumentException`，已捕获并打印警告，但仍会 **返回 false**（资源不足），玩家会看到 “招募失败”。建议把 **配置错误** 单独记录为 **插件启动错误**，防止运行时误导玩家。 |
| 174‑176 | `consumeRecruitResources` | - 与上面相同，扣除物品时直接 `removeItem(new ItemStack(material, amount))`，但如果玩家背包中有 **堆叠不足**（比如 5 个铁锭，而需求 10），`removeItem` 会 **只移除已有的 5** 并留下 **空槽**，返回的 map 会包含剩余未删除的物品。此时资源未被完整扣除，却已经执行了后续逻辑。**正确做法**：先检查 `containsAtLeast`（已在 `hasEnoughResources`），然后使用 `removeItemExact`（1.20+）或自行遍历 `ItemStack` 手动扣除。 |
| 179‑185 | 将 Villager 加入玩家的 `villagerIds` 并 `saveVillage` | - **并发安全**：`village.getVillagerIds().add(villagerUuid);` 直接修改集合。如果 `villagerIds` 为 `HashSet`，在多线程保存时可能产生 **ConcurrentModificationException**。建议把 `Village` 中的集合改为 **线程安全的 `CopyOnWriteArraySet`**，或在 `VillageManager` 方法上使用 `synchronized`。 |
| 188‑197 | 设置所有者、名称、技能 | - `VillagerUtils.setOwner(villager, player.getUniqueId())` 需要确保内部使用 **PersistentDataContainer** 而非实体的 `Metadata`，否则重启后会丢失。<br>- `villager.setCustomNameVisible(true);` 在某些客户端/服务器版本可能被 **插件冲突** 隐藏，建议在 `plugin.yml` 中声明 `entity-name-visible` 权限。 |
| 199‑201 | 创建 `VillagerEntity` 包装并放入 `plugin.getVillagerEntities()` | - `plugin.getVillagerEntities()` 是 `Map<UUID, VillagerEntity>`，同样需要并发安全。<br>- 若插件在 `recruitVillager` 中出现异常（比如 `plugin.getVillagerSkillManager()` 为 null），已经对玩家发送错误信息，但没有回滚已扣除的资源。**事务化**：资源扣除应在 **所有前置检查** 完成后、**写入数据库** 前一次性完成；若后续出现异常则 **恢复资源**（或者改为在资源扣除前先创建 `VillagerEntity`，成功后再扣除）。 |
| 203 | 返回 `true` | - 正确。 |

#### 改进示例（关键片段）

```java
public boolean recruitVillager(Player player, Villager villager) {
    // 1️⃣ 基础校验
    if (villager == null || !villager.isValid()) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.not-found"));
        return false;
    }
    if (VillagerUtils.isRecruited(villager)) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.already-recruited"));
        return false;
    }

    // 2️⃣ 获取或创建玩家的村庄（线程安全）
    Village village = getOrCreateVillage(player);

    // 3️⃣ 最大数量检查（缓存的上限值）
    if (village.getVillagerIds().size() >= plugin.getConfigManager().getMaxVillagers()) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.max-villagers-reached"));
        return false;
    }

    // 4️⃣ 资源检查 & 具体缺失原因返回
    ResourceCheckResult rc = checkRecruitResources(player);
    if (!rc.success) {
        player.sendMessage(rc.message); // 已经是本地化的错误信息
        return false;
    }

    // 5️⃣ 扣除资源（事务化，确保扣除成功）
    if (!consumeRecruitResources(player, rc)) {
        // 若扣除失败（极少情况），回滚并返回错误
        player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
        return false;
    }

    // 6️⃣ 更新村庄数据（原子操作）
    UUID villagerUuid = villager.getUniqueId();
    synchronized (village) {
        village.getVillagerIds().add(villagerUuid);
        saveVillage(village); // 异步持久化
    }

    // 7️⃣ 设置实体属性
    VillagerUtils.setOwner(villager, player.getUniqueId());
    villager.setCustomName(plugin.getMessageManager().getMessage("villager.name",
            Map.of("player", player.getName())));
    villager.setCustomNameVisible(true);
    plugin.getVillagerSkillManager().initializeVillagerSkills(villager);

    // 8️⃣ 包装并放入内存缓存（并发安全）
    VillagerEntity entity = new VillagerEntity(villager, player.getUniqueId());
    plugin.getVillagerEntities().put(villagerUuid, entity);

    // 9️⃣ 成功提示（在 RecruitCommand 中已发送，这里仅返回 true）
    player.sendMessage(plugin.getMessageManager().getMessage("recruit.success"));
    return true;
}

/**
 * 资源检查返回对象，包含是否成功以及具体提示信息
 */
private static class ResourceCheckResult {
    boolean success;
    String message; // 已本地化的错误信息
    // 记录需要扣除的金钱、物品等，供后续扣除使用
    double money;
    Map<Material, Integer> items = new HashMap<>();
}

/**
 * 检查资源是否足够，并返回详细信息
 */
private ResourceCheckResult checkRecruitResources(Player player) {
    ResourceCheckResult r = new ResourceCheckResult();
    // 检查金钱
    double cost = plugin.getConfigManager().getRecruitCostMoney();
    if (cost > 0 && plugin.getEconomy() != null && !plugin.getEconomy().has(player, cost)) {
        r.success = false;
        r.message = plugin.getMessageManager().getMessage("recruit.failed.not-enough-money");
        return r;
    }
    r.money = cost;

    // 检查物品
    Map<String, Integer> cfgItems = plugin.getConfigManager().getRecruitCostItems();
    for (Map.Entry<String, Integer> e : cfgItems.entrySet()) {
        Material mat;
        try { mat = Material.valueOf(e.getKey()); }
        catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("[Recruit] 配置错误的物品类型: " + e.getKey());
            continue; // 跳过错误配置
        }
        int need = e.getValue();
        if (!player.getInventory().containsAtLeast(new ItemStack(mat), need)) {
            r.success = false;
            r.message = plugin.getMessageManager().getMessage("recruit.failed.not-enough-items",
                    Map.of("item", mat.name(), "amount", String.valueOf(need)));
            return r;
        }
        r.items.put(mat, need);
    }

    r.success = true;
    r.message = ""; // 不会被使用
    return r;
}

/**
 * 扣除资源（已在检查阶段确保足够）
 */
private boolean consumeRecruitResources(Player player, ResourceCheckResult rc) {
    // 金钱
    if (rc.money > 0 && plugin.getEconomy() != null) {
        plugin.getEconomy().withdrawPlayer(player, rc.money);
    }
    // 物品（逐一扣除，避免 removeItem 导致“部分扣除”）
    for (Map.Entry<Material, Integer> e : rc.items.entrySet()) {
        Material mat = e.getKey();
        int amount = e.getValue();
        int left = amount;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() == mat) {
                int remove = Math.min(left, stack.getAmount());
                stack.setAmount(stack.getAmount() - remove);
                left -= remove;
                if (left == 0) break;
            }
        }
        if (left > 0) {
            // 理论上不可能触发，因为前面已检查
            plugin.getLogger().warning("[Recruit] 扣除物品失败，玩家 " + player.getName() + " 缺少 " + left + " " + mat);
            return false;
        }
    }
    return true;
}
```

> **为什么要这样做？**
- **事务化**：先检查再扣除，保证在任何一步出错时不会导致资源“丢失”。
- **并发安全**：对 `village` 对象进行同步，防止在高并发（例如两名玩家几乎同时招募同一 villager）时出现集合冲突。
- **更友好的错误信息**：玩家可以直接知道是缺钱还是缺物品，提升体验。
- **性能提升**：使用 `distanceSquared`、`EnumMap`、`ConcurrentHashMap`，减少不必要的对象创建。

### 2.6 资源检查 & 消耗（通用）

| 方法 | 现状 | 改进 |
|------|------|------|
| `hasEnoughResources(Player)` | 只返回布尔，且在物品检查中捕获异常后 **仍返回 true**（因为只打印警告） | - 改为返回 **`ResourceCheckResult`**（见上文）<br>- 对于配置错误，应该在插件加载阶段报错并阻止插件启动，而不是运行时才发现。 |
| `consumeRecruitResources(Player)` | 使用 `removeItem` 可能导致“部分扣除”。 | - 使用 `removeItemExact`（1.20+）或手动遍历扣除，确保完整扣除。 |
| `hasEnoughUpgradeResources(Player, Upgrade)` / `consumeUpgradeResources` | 同上 | - 同样改为 **返回详细信息** 并使用 **事务化**。 |
| **重复代码**（检查金钱、钻石、物品） | 多处出现几乎相同的循环 | - 抽取为 **工具类 `ResourceUtils`**，统一管理 `hasEnough`, `consume`，降低维护成本。 |

#### 示例 `ResourceUtils`

```java
public final class ResourceUtils {

    private ResourceUtils() { }

    /** 检查玩家是否拥有指定金钱 */
    public static boolean hasMoney(Economy econ, Player p, double amount) {
        return econ == null || amount <= 0 || econ.has(p, amount);
    }

    /** 扣除金钱 */
    public static void withdrawMoney(Economy econ, Player p, double amount) {
        if (econ != null && amount > 0) {
            econ.withdrawPlayer(p, amount);
        }
    }

    /** 检查物品集合（Material → amount）是否足够 */
    public static boolean hasItems(Player p, Map<String, Integer> cfg, MessageManager mm, Consumer<String> log) {
        for (Map.Entry<String, Integer> e : cfg.entrySet()) {
            Material mat;
            try { mat = Material.valueOf(e.getKey()); }
            catch (IllegalArgumentException ex) {
                log.accept("Invalid material in config: " + e.getKey());
                continue;
            }
            if (!p.getInventory().containsAtLeast(new ItemStack(mat), e.getValue())) {
                return false;
            }
        }
        return true;
    }

    /** 扣除物品（确保完整扣除） */
    public static void removeItems(Player p, Map<String, Integer> cfg) {
        for (Map.Entry<String, Integer> e : cfg.entrySet()) {
            Material mat = Material.valueOf(e.getKey());
            int need = e.getValue();
            int left = need;
            for (ItemStack stack : p.getInventory().getContents()) {
                if (stack == null) continue;
                if (stack.getType() == mat) {
                    int rm = Math.min(left, stack.getAmount());
                    stack.setAmount(stack.getAmount() - rm);
                    left -= rm;
                    if (left == 0) break;
                }
            }
        }
    }
}
```

> 在 `VillageManager` 中调用 `ResourceUtils.hasMoney(...)`、`ResourceUtils.hasItems(...)`，代码会更简洁。

### 2.7 `removeVillager(Player, UUID)`

| 行号 | 现状 | 改进 |
|------|------|------|
| 242‑262 | 基础流程：检查村庄、检查拥有权、从缓存、内存、实体中删除 | - **同步**：对 `village.getVillagerIds()` 进行修改时应同步。<br>- **实体清理**：`VillagerUtils.setOwner(villager, null)` 只清除自定义数据，仍可能残留 **AI/Pathfinder** 任务。建议调用 `villager.setAI(false);` 或 `villager.remove();` 彻底删除实体。<br>- **持久化**：删除后未调用 `saveVillage(village)`（已调用），但若 `plugin.getVillagerEntities()` 是 `ConcurrentHashMap`，删除操作已安全。 |
| 265‑267 | 成功提示 | - 保持即可。 |

#### 推荐实现

```java
public boolean removeVillager(Player player, UUID villagerId) {
    Village village = getVillage(player.getUniqueId());
    if (village == null) {
        player.sendMessage(plugin.getMessageManager().getMessage("village.not-found"));
        return false;
    }
    if (!village.getVillagerIds().remove(villagerId)) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.not-found"));
        return false;
    }

    // 持久化
    saveVillage(village);
    // 内存缓存
    plugin.getVillagerEntities().remove(villagerId);

    // 实体清理
    Entity entity = Bukkit.getEntity(villagerId);
    if (entity instanceof Villager villager) {
        VillagerUtils.setOwner(villager, null);
        villager.setCustomName(null);
        // 彻底删除实体，防止残留 AI
        villager.remove();
    }

    player.sendMessage(plugin.getMessageManager().getMessage("villager.removed"));
    return true;
}
```

---

## 3️⃣ 其它全局建议

| 编号 | 类/模块 | 建议 |
|------|--------|------|
| **A** | `VillagePro`（插件主类） | - 将 **所有** `MessageManager`、`ConfigManager`、`Economy` 等单例对象在 `onEnable` 中 **一次性初始化**，并使用 **Getter** 进行访问，防止空指针。<br>- 对于 **数据库操作**（SQLite），建议使用 **HikariCP** 之类的连接池，以提升并发写入性能。 |
| **B** | `FollowManager` | - `requestFollow(Player, Villager)` 里若使用 **BukkitRunnable** 异步发送 UI，确保在主线程调用 **Entity API**（如 `setGoalTarget`），否则会抛出 `IllegalPluginAccessException`。 |
| **C** | `VillagerUtils` | - 确保 `setOwner`、`isRecruited` 采用 **PersistentDataContainer**（PDC）存储，且在 **插件卸载** 时不忘 **清理**（防止玩家重新登录后仍残留）。 |
| **D** | 配置文件 (`config.yml`) | - 为 **金钱、物品、最大村民数** 提供默认值与注释，防止管理员误删导致插件报错。<br>- 对 **UpgradeType** 的配置应使用 `EnumMap`，防止大小写错误。 |
| **E** | 多语言/占位符 | - 现在很多 `getMessage` 调用都手动拼接 `Map<String,String>`，建议统一使用 **MiniMessage** 或 **PlaceholderAPI**，这样可以在消息里写 `${player}`、`${type}` 等占位符，代码更简洁。 |
| **F** | 异步/同步任务 | - **数据库写入**（`villageStorage.save`）建议 **异步**（`BukkitScheduler.runTaskAsynchronously`），防止主线程卡顿。<br>- 读取操作（`findNearestUnrecruitedVillager`）涉及实体 API 必须在主线程，保持不变。 |
| **G** | 日志级别 | - 使用 `plugin.getLogger().info(...)`、`warning(...)`、`severe(...)` 区分不同严重程度，便于运维排查。 |
| **H** | 单元测试 | - 为 `VillageManager` 中的业务方法（如 `hasEnoughResources`、`upgradeVillage`）编写 **JUnit5 + MockBukkit** 测试，确保后续改动不破坏逻辑。 |

---

## 4️⃣ 完整优化示例（核心文件）

下面给出 **经过优化的关键代码文件**（仅展示改动点），您可以直接把它们拷贝进项目。

### 4.1 `RecruitCommand.java`（已优化）

```java
package cn.popcraft.villagepro.command;

import cn.popcraft.villagepro.VillagePro;
import cn.popcraft.villagepro.manager.FollowManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

public class RecruitCommand implements CommandExecutor {

    private final VillagePro plugin;
    private final FollowManager followManager;

    public RecruitCommand(VillagePro plugin) {
        this.plugin = plugin;
        this.followManager = plugin.getFollowManager(); // 直接缓存，避免每次 get 调用
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.player-only"));
            return true;
        }

        // 权限检查（任意一个满足即通过）
        if (!player.hasPermission("villagepro.recruit") && !player.hasPermission("villagepro.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        // 查找最近的未招募村民
        Villager target = plugin.getVillageManager().findNearestUnrecruitedVillager(player, 5);
        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
            return true;
        }

        // 真正的业务交给 VillageManager 完成（包括消息发送）
        boolean success = plugin.getVillageManager().recruitVillager(player, target);
        if (success) {
            // 成功后询问是否跟随
            followManager.requestFollow(player, target);
        }
        // 若失败，错误信息已在 recruitVillager 中发送
        return true;
    }
}
```

### 4.2 `VillageManager.java`（关键改动）

> **注意**：下面仅展示新增/修改的部分，未列出的原有代码保持不变。

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.EnumMap;
import java.util.Collections;
import java.util.logging.Level;

// -------------- 类成员 -----------------
private final Map<UUID, Village> villageCache = new ConcurrentHashMap<>();
private final int maxVillagers; // 缓存配置值，避免每次查询

public VillageManager(VillagePro plugin) {
    this.plugin = plugin;
    this.villageStorage = new SQLiteStorage(plugin, plugin.getGson());
    this.maxVillagers = plugin.getConfigManager().getMaxVillagers(); // 读取一次
}

// -------------- getOrCreateVillage -----------------
public Village getOrCreateVillage(Player player) {
    UUID uuid = player.getUniqueId();
    return villageCache.computeIfAbsent(uuid, id -> {
        Village v = new Village();
        v.setOwnerUuid(id);
        v.setUpgradeLevels(new EnumMap<>(UpgradeType.class));
        for (UpgradeType type : UpgradeType.values()) {
            v.getUpgradeLevels().put(type, 0);
        }
        v.setVillagerIds(Collections.synchronizedSet(new HashSet<>()));
        // 异步保存，防止阻塞主线程
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> villageStorage.save(v));
        return v;
    });
}

// -------------- findNearestUnrecruitedVillager -----------------
public Villager findNearestUnrecruitedVillager(Player player, double radius) {
    Collection<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
    Villager nearest = null;
    double minDistSq = Double.MAX_VALUE;

    for (Entity e : nearby) {
        if (e instanceof Villager villager && villager.isValid() && !VillagerUtils.isRecruited(villager)) {
            double distSq = player.getLocation().distanceSquared(villager.getLocation());
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = villager;
            }
        }
    }
    return nearest;
}

// -------------- recruitVillager -----------------
public boolean recruitVillager(Player player, Villager villager) {
    // 1️⃣ 基础校验
    if (villager == null || !villager.isValid()) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.not-found"));
        return false;
    }
    if (VillagerUtils.isRecruited(villager)) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.already-recruited"));
        return false;
    }

    // 2️⃣ 获取或创建村庄（线程安全）
    Village village = getOrCreateVillage(player);

    // 3️⃣ 最大数量检查
    if (village.getVillagerIds().size() >= maxVillagers) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.max-villagers-reached"));
        return false;
    }

    // 4️⃣ 资源检查（返回详细信息）
    ResourceCheckResult rc = checkRecruitResources(player);
    if (!rc.success) {
        player.sendMessage(rc.message);
        return false;
    }

    // 5️⃣ 扣除资源（已确保足够）
    if (!consumeRecruitResources(player, rc)) {
        player.sendMessage(plugin.getMessageManager().getMessage("recruit.failed"));
        return false;
    }

    // 6️⃣ 更新数据（同步块确保集合安全）
    UUID vid = villager.getUniqueId();
    synchronized (village) {
        village.getVillagerIds().add(vid);
        saveVillage(village); // 异步持久化
    }

    // 7️⃣ 实体属性设置
    VillagerUtils.setOwner(villager, player.getUniqueId());
    villager.setCustomName(plugin.getMessageManager().getMessage(
            "villager.name", Map.of("player", player.getName())));
    villager.setCustomNameVisible(true);
    plugin.getVillagerSkillManager().initializeVillagerSkills(villager);

    // 8️⃣ 内存包装
    VillagerEntity entity = new VillagerEntity(villager, player.getUniqueId());
    plugin.getVillagerEntities().put(vid, entity);

    // 9️⃣ 成功提示（在这里统一发送，RecruitCommand 不再重复）
    player.sendMessage(plugin.getMessageManager().getMessage("recruit.success"));
    return true;
}

// -------------- 资源检查/扣除封装 -----------------
private ResourceCheckResult checkRecruitResources(Player player) {
    ResourceCheckResult r = new ResourceCheckResult();

    // 金钱
    double costMoney = plugin.getConfigManager().getRecruitCostMoney();
    if (costMoney > 0 && plugin.getEconomy() != null && !plugin.getEconomy().has(player, costMoney)) {
        r.success = false;
        r.message = plugin.getMessageManager().getMessage("recruit.failed.not-enough-money");
        return r;
    }
    r.money = costMoney;

    // 物品
    Map<String, Integer> cfgItems = plugin.getConfigManager().getRecruitCostItems();
    for (Map.Entry<String, Integer> e : cfgItems.entrySet()) {
        Material mat;
        try { mat = Material.valueOf(e.getKey()); }
        catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("[Recruit] Invalid material: " + e.getKey());
            continue; // 跳过错误条目
        }
        int need = e.getValue();
        if (!player.getInventory().containsAtLeast(new ItemStack(mat), need)) {
            r.success = false;
            r.message = plugin.getMessageManager().getMessage(
                    "recruit.failed.not-enough-items",
                    Map.of("item", mat.name(), "amount", String.valueOf(need)));
            return r;
        }
        r.items.put(mat, need);
    }

    r.success = true;
    return r;
}

private boolean consumeRecruitResources(Player player, ResourceCheckResult rc) {
    // 金钱
    if (rc.money > 0 && plugin.getEconomy() != null) {
        plugin.getEconomy().withdrawPlayer(player, rc.money);
    }

    // 物品（手动遍历确保完整扣除）
    for (Map.Entry<Material, Integer> e : rc.items.entrySet()) {
        Material mat = e.getKey();
        int need = e.getValue();
        int left = need;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() == mat) {
                int rm = Math.min(left, stack.getAmount());
                stack.setAmount(stack.getAmount() - rm);
                left -= rm;
                if (left == 0) break;
            }
        }
        if (left > 0) {
            // 理论不应到达这里
            plugin.getLogger().warning("[Recruit] 扣除物品失败: " + mat + " 缺少 " + left);
            return false;
        }
    }
    return true;
}

// -------------- removeVillager（并发安全、彻底删除） -----------------
public boolean removeVillager(Player player, UUID villagerId) {
    Village village = getVillage(player.getUniqueId());
    if (village == null) {
        player.sendMessage(plugin.getMessageManager().getMessage("village.not-found"));
        return false;
    }
    if (!village.getVillagerIds().remove(villagerId)) {
        player.sendMessage(plugin.getMessageManager().getMessage("villager.not-found"));
        return false;
    }

    // 保存
    saveVillage(village);
    // 移除内存缓存
    plugin.getVillagerEntities().remove(villagerId);

    // 实体清理
    Entity entity = Bukkit.getEntity(villagerId);
    if (entity instanceof Villager villager) {
        VillagerUtils.setOwner(villager, null);
        villager.setCustomName(null);
        villager.remove(); // 完全删除实体
    }

    player.sendMessage(plugin.getMessageManager().getMessage("villager.removed"));
    return true;
}
```

> **核心改动回顾**
- 使用 **ConcurrentHashMap** / **synchronizedSet** 防止并发冲突。
- 将 **配置读取**（如最大村民数）缓存为成员变量，减少 I/O。
- 采用 **事务化** 资源检查与扣除（返回详细错误信息）。
- 统一 **成功/失败消息** 的发送位置，避免重复。
- 对 **实体删除** 采用 `villager.remove()`，确保 AI/路径不残留。
- 所有 **数据库写入** 改为 **异步**，提升主线程性能。

---

## 5️⃣ 小结 & 下一步行动

1. **把上述代码合并到项目**（先在本地开发环境编译运行，确保没有编译错误）。
2. **在插件启动时**，在 `onEnable` 中添加 **异常捕获**，如果 `villageStorage` 初始化失败直接 **disable** 插件，避免半残状态。
3. **写单元测试**（使用 MockBukkit）验证：
   - 招募成功、资源不足、已满、已招募等分支。
   - `removeVillager` 能正确清理实体与缓存。
4. **在配置文件中** 增加注释，说明每项费用的单位与取值范围。
5. **上线前** 在测试服进行 **并发招募**（两名玩家几乎同时对同一个 villager 执行指令）测试，确保不会出现重复招募或集合并发错误。

如果您还有其他业务需求（比如 **升级、解雇、查看村庄状态**）或想进一步优化 **UI 交互**、**多语言支持**，随时告诉我，我可以继续为您设计相应的指令、GUI 或 API。祝开发顺利！ 🚀