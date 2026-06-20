# 技术设计文档

本文档描述 Some Interesting 模组的技术架构、核心系统设计和数据流。

## 架构概览

```
┌─────────────────────────────────────────────────────┐
│                     Minecraft                       │
├─────────────┬──────────────────┬────────────────────┤
│  事件系统     │   数据组件系统     │    网络层           │
│ AttackEvent │ EnhanceComponent │ PayloadTypeRegistry│
│ BreakEvent  │ SOUL_BOUND       │ ServerPlayNet      │
│ RightEvent  │ BIND_TIME        │ ClientPlayNet      │
│ ArmorEvent  │                  │                    │
│ ...         │                  │                    │
├─────────────┴──────────────────┴────────────────────┤
│                  核心模块                             │
├──────────────┬──────────────────┬───────────────────┤
│ 熟练度系统     │  灵魂绑定系统      │   UI 系统          │
│ 16种装备      │ BindingStoneItem │ SoulBindingScreen │
│ 4级成长       │ BoundItemStorage │ SoulBindingMenu   │
│ 自动升级      │ 冷却 + 持久化      │ 合成 + 选择         │
└──────────────┴──────────────────┴───────────────────┘
```

## 核心系统

### 1. 武器熟练度系统

**职责**：追踪玩家使用工具/武器/护甲的次数，达到阈值时自动升级。

**数据存储**：每种装备类型一个 `DataComponentType`（记录类），存储在物品的数据组件中，随物品一起保存。

**组件结构**（以剑为例）：
```
SwordsEnhanceComponent {
    int normal_count      // 普通攻击次数
    int super_count       // 暴击次数
    boolean is_adept      // 是否粗通
    boolean is_synchronized  // 是否精通
    boolean is_soulbound  // 是否灵魂相通
}
```

**升级流程**：
```
玩家使用物品 → 事件回调触发 → 读取组件数据 → 计数+1
→ 检查阈值 → 达标则升级（设置标志位 + 强化属性 + 发送消息）
```

**涉及文件**：
- `component/*EnhanceComponent.java`（16 个，每种装备一个）
- `EventCallBack/*Event.java`（7 个，按事件类型分类）

### 2. 灵魂绑定系统

**职责**：允许玩家将灵魂相通的物品"绑定"到灵魂，并通过合成台复制。

**绑定流程**：
```
主手绑定粉 + 副手灵魂相通物品
→ 检查 SOUL_BOUND 标记（防重复绑定）
→ 设置 SOUL_BOUND + BIND_TIME 组件
→ 复制物品到 BoundItemStorage
→ 消耗绑定粉
→ 网络同步到客户端
```

**合成流程**：
```
玩家打开界面(K键) → 客户端发 OpenSoulBindingPayload → 服务端创建 Menu
→ 玩家点击左侧选择物品 → 客户端发 SelectBoundItemPayload → 服务端设置选中项
→ 放入合成材料 → slotsChanged 匹配配方
→ 配方产物类型匹配选中物品 → 输出 = 选中物品副本（满耐久）
→ 取出产物 → 设置 24h 冷却
```

**持久化**：
```
BoundItemStorage
├── boundItems: Map<String, List<ItemStack>>    // 玩家 → 绑定物品列表
├── craftCooldowns: Map<String, Map<Int, Long>> // 玩家 → (物品索引 → 时间戳)
├── save(): NbtIo.writeCompressed + RegistryOps + ItemStack.CODEC
├── load(): NbtIo.readCompressed + RegistryOps + ItemStack.CODEC
└── 文件路径: <世界>/data/some_interesting_bound_items.nbt

生命周期事件:
  SERVER_STARTED → load()
  BEFORE_SAVE   → save()
  SERVER_STOPPING → save()
  SERVER_STOPPED  → clear()
```

**玩家身份策略**：
```
server.usesAuthentication() == true  → 使用 UUID（正版在线模式）
server.usesAuthentication() == false → 使用玩家名（离线模式）
```

### 3. 网络系统

**数据包一览**：

| 包名 | 方向 | 用途 |
|------|------|------|
| `BoundItemsSyncPayload` | S→C | 同步绑定物品列表到客户端 |
| `OpenSoulBindingPayload` | C→S | 请求打开灵魂绑定界面 |
| `SelectBoundItemPayload` | C→S | 选择要合成的绑定物品 |

**同步时机**：
- 玩家加入服务器（JOIN 事件）
- 绑定成功后（BindingStoneItem.use）

### 4. UI 系统

**SoulBindingScreen**（AbstractContainerScreen）：
```
┌─────────────────────────────────────────────┐
│ 灵魂绑定          │  合成                    │
├───────────────────┤                          │
│ [图标] 物品名      │  [□][□][□]              │
│       2026-06-19  │  [□][□][□]  ➤  [□]      │
│                   │  [□][□][□]              │
│ [图标] 物品名      │                          │
│       2026-06-19  │  请先选择左侧物品         │
│ ▲ 1-4 / 10 ▼     │                          │
├───────────────────┴──────────────────────────┤
│ 物品栏                                       │
│ [玩家背包 3×9]                                │
│ [快捷栏 1×9]                                  │
└─────────────────────────────────────────────┘
```

**渲染分层**：
- `extractBackground()` — 面板背景、边框、分割线、格子背景、箭头
- `extractRenderState()` — 文字标签、物品图标、绑定时间、悬停 Tooltip

## 关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 熟练度存储 | DataComponent | 数据跟随物品，自动序列化，无需额外存储 |
| 绑定物品持久化 | 手动 NBT + RegistryOps | SavedData/Codec 在 ItemStack 序列化时静默失败 |
| 网络同步 | Fabric Networking API | 标准 Fabric 方案，CustomPacketPayload + StreamCodec |
| 合成系统 | Container Menu | 需要服务端物品管理，不能用纯客户端 Screen |
| 防重复绑定 | SOUL_BOUND 组件 | 一个标记同时解决原物品和副本的绑定限制 |
| 合成冷却顺序 | setCooldown 在 super.onTake 之前 | 防止材料充足时通过快速点击绕过冷却 |
