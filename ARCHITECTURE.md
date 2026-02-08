# SoulslikeSpells 项目架构说明

## 一、模块总览

```
soulslikespells/
├── api/                    # 对外 API（供其他 mod 调用）
├── block/                  # 篝火方块与方块实体
├── catalyst/               # 法杖/触媒 scaling 系统
├── client/                 # 仅客户端（按键绑定）
├── command/                # 游戏命令
├── config/                 # 配置
├── data/                   # 玩家数据模型
├── event/                  # 事件处理器
├── gui/                    # 界面
│   └── bonfire/           # 篝火界面
├── integration/            # 与其他 mod 的集成
├── item/                   # 物品
├── network/                # 网络包
├── registry/               # 注册表与注册项
├── scaling/                # 属性 scaling 管理
├── service/                # 统一业务服务（本优化新增）
└── util/                   # 工具类
```

## 二、分层与依赖关系

```
┌─────────────────────────────────────────────────────────────┐
│  GUI / Client / Command（表现层）                             │
│  BonfireScreen, SoulStatsScreen, Commands                   │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│  Network / Event（请求/事件层）                               │
│  Payloads, AttributeEventHandler, CatalystEventHandler       │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│  Service（业务服务层）← 新增，统一入口                         │
│  PlayerStatService, RespecService                            │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│  Core（核心领域层）                                           │
│  ScalingManager, CatalystAttributeHandler, CatalystModifierHelper │
│  ScalingProfileManager, LevelCostCalculator, PlayerSoulData  │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│  Registry / Config / Util（基础设施层）                        │
└─────────────────────────────────────────────────────────────┘
```

## 三、模块设计分析

### 3.1 API 层 (api/)

- **职责**：对外暴露稳定接口，供其他 mod 调用。
- **内聚**：高，集中在 stat、level、modifier 相关能力。
- **耦合**：依赖 `ModAttachments`、`ScalingManager`、`CatalystAttributeHandler`，现已改为依赖 `PlayerStatService`。
- **评价**：接口清晰，适合作为扩展点。

### 3.2 Catalyst 层 (catalyst/)

- **职责**：从 datapack 加载 ScalingProfile，按 stat/装备计算属性修饰符。
- **内聚**：高，围绕 scaling 配置与计算。
- **耦合**：依赖 `PlayerSoulData`、`ModDataMaps`、属性注册表。
- **评价**：数据驱动设计合理，`ScalingEntry`、`SourceExpr`、`CurveMath` 职责划分清楚。

### 3.3 Scaling 层 (scaling/)

- **职责**：管理 stat 相关 scaling；`LevelCostCalculator` 负责升级消耗。
- **内聚**：高。
- **耦合**：依赖 `ScalingProfileManager`、`CatalystModifierHelper`、`ModAttachments`。
- **评价**：与 catalyst 分工明确，stat scaling 与 item scaling 分离。

### 3.4 Service 层 (service/)（新增）

- **PlayerStatService**：统一入口，负责「重算 scaling + 重算 catalyst + 同步客户端」。
- **RespecService**：统一洗点物品检查和消耗逻辑。
- **评价**：减少重复代码，降低 Network/Event/Command 与核心逻辑的耦合。

### 3.5 Network 层 (network/)

- **职责**：C2S/S2C 网络包定义与处理。
- **内聚**：高，按功能拆分 payload。
- **耦合**：依赖 Service、Config、Data、Registry。
- **评价**：Payload 职责单一，Handler 只做校验与编排，业务逻辑下沉到 Service。

### 3.6 GUI 层 (gui/)

- **BonfireScreen**：篝火主界面，升级与洗点交互。
- **BonfireDialogScreen**：统一弹窗（确认/提示）。
- **SoulStatsScreen**：属性查看界面。
- **评价**：弹窗已统一，界面逻辑清晰；需注意 SoulStatsScreen 与 BonfireScreen 的 stat 列表避免重复硬编码。

### 3.7 Event 层 (event/)

- **AttributeEventHandler**：登录、重生、跨维度时触发属性重算。
- **CatalystEventHandler**：装备变更时设置 ThreadLocal，供 ItemAttributeModifierHandler 使用。
- **ItemAttributeModifierHandler**：通过 NeoForge 的 ItemAttributeModifierEvent 添加装备 scaling。
- **PlayerDeathHandler**：死亡惩罚（可选）。
- **评价**：事件职责明确；ThreadLocal 使用需谨慎，但当前用法符合装备变更流程。

## 四、已实施的优化

### 4.1 PlayerStatService

- **问题**：`ScalingManager.recalculateAll` + `CatalystAttributeHandler.applyCatalystModifiers` + `SyncSoulDataPayload.sendToPlayer` 在多个位置重复。
- **解决**：抽出 `PlayerStatService.recalculateAndSync(ServerPlayer)`。
- **影响**：AttributeEventHandler、ConfirmLevelUpPayload、RespecApplyPayload、SoulslikeCommands、SoulslikeSpellsAPI。

### 4.2 RespecService

- **问题**：洗点物品检查与消耗逻辑在 `RespecRequestPayload` 和 `RespecApplyPayload` 中重复。
- **解决**：抽出 `RespecService.hasRespecItem` 和 `RespecService.consumeRespecItem`。
- **影响**：RespecRequestPayload、RespecApplyPayload。

## 五、后续可选优化建议

### 5.1 Stat 配置驱动化

- **现状**：`BonfireMenu`、`BonfireScreen`、`ModStatTypes` 中 stat 列表硬编码。
- **建议**：从 `ModStatTypes` 或配置中动态生成 stat 列表，供 Menu/Screen 共用，便于新增 stat。

### 5.2 事件注册集中化

- **现状**：`SoulslikeSpells` 中大量 `addListener` 调用。
- **建议**：抽到 `ModEventRegistrations` 等类，按职责分组注册，便于维护。

### 5.3 常量提取

- **现状**：`BonfireScreen`、`SoulStatsScreen` 等存在重复颜色常量（如 `0xFF8B7355`）。
- **建议**：抽到 `ModColors` 或 `GuiStyles` 统一管理。

### 5.4 数据包编解码复用

- **现状**：`Map<ResourceLocation, Integer>` 的编解码在多个 Payload 中重复。
- **建议**：抽到 `NetworkCodecs` 或 `PayloadCodecs` 中复用。

## 六、依赖图（简化）

```
SoulslikeSpells (主入口)
    ├── ModRegistries, ModAttachments, ModBlocks, ModItems...
    ├── ModNetworking
    ├── AttributeEventHandler, PlayerDeathHandler, CatalystEventHandler
    ├── SoulslikeCommands
    └── SoulslikeCommonConfig, SoulslikeClientConfig

SoulslikeSpellsClient (客户端入口)
    ├── BonfireScreen
    └── IConfigScreenFactory

Network Payloads
    ├── PlayerStatService
    ├── RespecService
    └── Config, ModAttachments, ModRegistries...
```
