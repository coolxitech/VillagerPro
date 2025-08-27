# VillagerPro - 村民与村庄管理插件

## 插件介绍
VillagerPro 是一个 Minecraft 服务器插件，专注于村庄和村民的管理与扩展。通过此插件，玩家可以创建、升级村庄，招募特殊村民，并完成自定义任务以获得奖励。

## 功能列表
- **村庄管理**：创建、升级和扩展村庄。
- **村民招募**：招募不同类型的村民（农民、守卫、商人等）。
- **任务系统**：为玩家生成随机任务（采集、击杀、交付等），完成任务后获得奖励。
- **经济系统**：支持金币和经验奖励。
- **事件监听**：自动检测任务进度（如采集物品、击杀怪物）。

## 安装指南
1. 将插件 JAR 文件放入服务器的 `plugins` 文件夹。
2. 重启服务器以加载插件。
3. 插件会自动生成默认配置文件 `config.yml`。

### config.yml
```yaml
# 村庄升级配置
upgrades:
  TRADE_AMOUNT:
    cost-money: 100
    cost-diamonds: 5
    cost-items:
      IRON_INGOT: 10
  TRADE_QUALITY:
    cost-money: 200
    cost-diamonds: 10
    cost-items:
      GOLD_INGOT: 15
  TRADE_PRICE:
    cost-money: 150
    cost-diamonds: 7
    cost-items:
      EMERALD: 5
  RESTOCK_SPEED:
    cost-money: 80
    cost-diamonds: 3
    cost-items:
      WHEAT: 20
  CROP_YIELD:
    cost-money: 120
    cost-diamonds: 6
    cost-items:
      SEEDS: 30
```

## 命令帮助

### 主命令
#### `/villagerpro help [页码]`
- 描述: 显示插件所有可用命令的帮助信息
- 权限: 默认所有玩家

#### `/villagerpro village create`
- 描述: 创建一个新的村庄
- 权限: `villagerpro.village.create` 或 `villagerpro.admin`

#### `/villagerpro village upgrade <level>`
- 描述: 升级村庄到指定等级
- 参数: `<level>` - 要升级到的等级
- 权限: `villagerpro.village.upgrade` 或 `villagerpro.admin`

#### `/villagerpro village info`
- 描述: 显示村庄详细信息
- 权限: `villagerpro.village.info` 或 `villagerpro.admin`

### 村民相关命令

#### `/villager production`
- 描述: 打开村民产出界面，可以获取村民生产的物品
- 权限: `villagerpro.villager.production` 或 `villagerpro.admin`

#### `/villager upgrade <type> <level>`
- 描述: 升级村民能力
- 参数: 
  - `<type>` - 要升级的能力类型
  - `<level>` - 要升级到的等级
- 升级类型:
  - `TRADE_AMOUNT` - 提高村民交易数量
  - `TRADE_QUALITY` - 提高村民交易物品质量
  - `TRADE_PRICE` - 提高村民交易价格
  - `RESTOCK_SPEED` - 提高村民补货速度
  - `CROP_YIELD` - 提高农民作物产量
- 权限: `villagerpro.villager.upgrade` 或 `villagerpro.admin`

#### `/villager follow [mode]`
- 描述: 切换村民跟随模式
- 模式:
  - `free` - 自由活动
  - `follow` - 跟随玩家
  - `stay` - 停留原地
- 权限: `villagerpro.villager.follow` 或 `villagerpro.admin`

#### `/villager info`
- 描述: 显示当前村民的详细信息，包括职业、技能等级等
- 权限: `villagerpro.villager.info` 或 `villagerpro.admin`

#### `/villager list`
- 描述: 列出玩家所有村民
- 权限: `villagerpro.villager.list` 或 `villagerpro.admin`

#### `/villager remove <id>`
- 描述: 移除指定ID的村民
- 参数: `<id>` - 要移除的村民ID
- 权限: `villagerpro.villager.remove` 或 `villagerpro.admin`

### 村庄相关命令

#### `/village create`
- 描述: 创建一个新的村庄
- 权限: `villagerpro.village.create` 或 `villagerpro.admin`

#### `/village upgrade <level>`
- 描述: 升级村庄到指定等级
- 参数: `<level>` - 要升级到的等级
- 权限: `villagerpro.village.upgrade` 或 `villagerpro.admin`

#### `/village info`
- 描述: 显示村庄详细信息
- 权限: `villagerpro.village.info` 或 `villagerpro.admin`

### 作物管理命令

#### `/crop balance`
- 描述: 查看玩家当前存储的作物数量
- 权限: `villagerpro.crop.balance` 或 `villagerpro.admin`

#### `/crop withdraw <type> [amount]`
- 描述: 取出指定类型的作物
- 参数: 
  - `<type>` - 要取出的作物类型
  - `[amount]` - 要取出的数量（可选）
- 权限: `villagerpro.crop.withdraw` 或 `villagerpro.admin`

#### `/crop deposit <type> [amount]`
- 描述: 存储指定类型的作物
- 参数:
  - `<type>` - 要存储的作物类型
  - `[amount]` - 要存储的数量（可选）
- 权限: `villagerpro.crop.deposit` 或 `villagerpro.admin`

## 权限节点

### 村庄权限
- `villagerpro.village.create` - 允许创建村庄
- `villagerpro.village.upgrade` - 允许升级村庄
- `villagerpro.village.info` - 允许查看村庄信息

### 村民权限
- `villagerpro.villager.production` - 允许打开产出界面
- `villagerpro.villager.upgrade` - 允许升级村民
- `villagerpro.villager.follow` - 允许切换村民跟随模式
- `villagerpro.villager.info` - 允许查看村民信息
- `villagerpro.villager.list` - 允许列出所有村民
- `villagerpro.villager.remove` - 允许移除村民

### 作物权限
- `villagerpro.crop.balance` - 允许查看作物余额
- `villagerpro.crop.withdraw` - 允许取出作物
- `villagerpro.crop.deposit` - 允许存储作物

## 任务系统
### 任务类型
1. **采集任务**：如采集 10 个稻草。
2. **击杀任务**：如击杀 5 个僵尸。
3. **交付任务**：如送 3 件药水。
4. **挖掘任务**：挖掘指定数量的矿物或方块。
5. **制造任务**：制作指定数量的物品。
6. **钓鱼任务**：钓鱼指定次数。
7. **附魔任务**：附魔指定数量的物品。
8. **繁殖任务**：繁殖指定数量的动物。
9. **收获任务**：收获指定数量的作物。
10. **探索任务**：探索指定的生物群系。
11. **交易任务**：与村民完成指定次数的交易。
12. **收集任务**：收集指定数量的木材或花朵。
13. **剪毛任务**：剪指定数量的羊。
14. **驯服任务**：驯服指定数量的动物。
15. **酿造任务**：酿造指定数量的药水。

### 任务流程
1. 玩家使用 `/task generate` 生成随机任务。
2. 完成任务目标后，系统自动发放奖励。
3. 奖励包括经验和金币，可在配置文件中自定义。

## 村民招募系统
### 招募流程
1. 玩家寻找未被招募的村民。
2. 玩家使用 `/villager recruit` 命令招募附近的村民。
3. 系统检查玩家是否满足招募条件（资源、村民数量上限等）。
4. 如果满足条件，村民将被标记为玩家所有，并获得基于其职业的技能。

### 村民职业与技能
村民招募后会根据其职业获得相应的技能：
- **农民**：拥有农业和收获技能，可以提高作物产量。
- **渔夫**：拥有钓鱼和水下呼吸技能，可以在水中更好地工作。
- **图书管理员**：拥有附魔和知识技能，可以协助玩家附魔物品。
- **武器匠**：拥有武器锻造和战斗技能，可以提高战斗能力。
- **牧羊人**：拥有动物繁殖和羊毛处理技能，可以协助管理动物。
- **制箭师**：拥有弓箭和箭矢制作技能，可以提高远程攻击能力。
- **祭司**：拥有治疗和药水酿造技能，可以协助治疗和制作药水。
- **盔甲匠**：拥有盔甲锻造和防护技能，可以提高防护能力。
- **工具匠**：拥有工具锻造和挖掘技能，可以提高挖掘效率。
- **皮匠**：拥有食物加工和屠宰技能，可以协助处理食物。
- **制图师**：拥有探索和制图技能，可以帮助玩家探索世界。

### 村民管理
1. 玩家可以与已招募的村民交互来管理他们。
2. 潜行状态下右键点击村民可以切换其跟随模式：
   - 自由活动：村民自由行动
   - 跟随：村民跟随玩家
   - 停留：村民停留在当前位置
3. 村民可以被升级以提高其技能等级。