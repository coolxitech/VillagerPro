# VillagerPro - 村民与村庄管理插件

## 插件介绍
VillagerPro 是一个 Minecraft 服务器插件，专注于村庄和村民的管理与扩展。通过此插件，玩家可以创建、升级村庄，招募特殊村民，并完成自定义任务以获得奖励。

## 功能列表
- **村庄管理**：创建、升级和扩展村庄。
- **村民招募**：招募不同类型的村民（农民、渔夫、牧羊人等）。
- **村民管理**：管理已招募村民的跟随模式、升级和产出。
- **任务系统**：为玩家生成随机任务（采集、击杀、交付等），完成任务后获得奖励。
- **经济系统**：支持金币和经验奖励，兼容 Vault 和 PlayerPoints。
- **作物系统**：村民自动收集作物并存储，玩家可以存取作物。
- **事件监听**：自动检测任务进度（如采集物品、击杀怪物）。

## 安装指南
1. 将插件 JAR 文件放入服务器的 `plugins` 文件夹。
2. 确保已安装 Vault 插件（如果需要经济系统支持）。
3. （可选）安装 PlayerPoints 插件（如果需要点券系统支持）。
4. 重启服务器以加载插件。
5. 插件会自动生成默认配置文件 `config.yml` 和 `messages.yml`。

## 配置文档
### `config.yml`
```yaml
# 村庄配置
village:
  # 村民数量上限
  max_villagers: 10
  # 村庄最大等级
  max_level: 5
  # 村庄升级费用（金币）
  upgrade_cost: 1000
  # 招募村民所需金币
  recruit_cost: 500

# 任务配置
task:
  # 任务生成费用（金币）
  generate_cost: 100
  # 默认任务奖励经验
  reward_exp: 50
  # 默认任务奖励金币
  reward_money: 100

# 作物系统配置
crop:
  # 农民村民收集作物的概率（0.0-1.0）
  collection_probability: 0.3
  # 每次收集的最大作物数量
  max_collection_amount: 5
```

## 命令列表
| 命令 | 描述 | 权限 |
|------|------|------|
| `/village create` | 创建村庄 | `villagerpro.village.create` |
| `/village upgrade` | 升级村庄 | `villagerpro.village.upgrade` |
| `/village info` | 查看村庄信息 | `villagerpro.village.info` |
| `/task` | 打开任务界面 | `villagerpro.task` |
| `/villager recruit` | 招募村民 | `villagerpro.villager.recruit` |
| `/villager production` | 打开村民产出界面 | `villagerpro.villager.production` |
| `/villager upgrade` | 打开村民升级界面 | `villagerpro.villager.upgrade` |
| `/villager follow` | 切换村民跟随模式 | `villagerpro.villager.follow` |
| `/villager info` | 显示村民信息 | `villagerpro.villager.info` |
| `/villager list` | 列出所有村民 | `villagerpro.villager.list` |
| `/villager remove` | 移除村民 | `villagerpro.villager.remove` |
| `/crop balance` | 查看作物余额 | `villagerpro.crop.balance` |
| `/crop withdraw` | 取出作物 | `villagerpro.crop.withdraw` |
| `/crop deposit` | 存储作物 | `villagerpro.crop.deposit` |

## 权限节点

### 村庄权限
- `villagerpro.village.create` - 允许创建村庄
- `villagerpro.village.upgrade` - 允许升级村庄
- `villagerpro.village.info` - 允许查看村庄信息

### 任务权限
- `villagerpro.task` - 允许使用任务系统

### 村民权限
- `villagerpro.villager.recruit` - 允许招募村民
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

### 管理员权限
- `villagerpro.admin` - 管理员权限，可绕过所有限制

## 经济系统支持

VillagerPro 支持多种经济插件，包括：
- 所有与 Vault 兼容的经济插件（如 EssentialsX Economy、CMI Economy、TheNewEconomy 等）
- PlayerPoints 点券系统

### CMI 经济系统配置

如果您使用 CMI 经济系统，请确保:
1. 安装了 Vault 插件
2. 在 CMI 配置文件中启用了经济系统 (`Economy enabled: true`)
3. (可选) 使用兼容 CMI 的 Vault 版本或 CMIInjector

### PlayerPoints 配置

如果您使用 PlayerPoints 点券系统，请确保:
1. 安装了 PlayerPoints 插件
2. PlayerPoints 插件已正确配置并运行
3. 玩家拥有足够的点券来执行相关操作

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
1. 玩家使用 `/task` 打开任务界面。
2. 在任务中心界面中点击"生成随机任务"创建新任务，需要消耗100金币。
3. 完成任务目标后，系统自动发放奖励。
4. 奖励包括经验和金币。

## 村民招募系统
### 招募流程
1. 玩家寻找未被招募的村民。
2. 玩家使用 `/recruit` 命令招募附近的村民。
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
3. 使用 `/villager upgrade` 命令可以打开升级界面，提升村民的各项能力。
4. 使用 `/villager production` 命令可以打开产出界面，获取村民生产的物品。
5. 使用 `/villager info` 命令可以查看最近的村民信息。
6. 使用 `/villager list` 命令可以列出所有已招募的村民。
7. 使用 `/villager remove <ID>` 命令可以移除指定的村民。
