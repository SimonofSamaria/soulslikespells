# SoulslikeSpells

**[中文](#功能概览)** | **[English](#soulslikespells-english)**

---

## 中文

黑魂风格的灵魂等级与属性补正系统，为 Minecraft 1.21.1 + NeoForge 设计，与 [Iron's Spells 'n Spellbooks](https://modrinth.com/mod/irons-spells-n-spellbooks) 深度集成。

## 功能概览

- **灵魂等级**：消耗经验提升等级，可自由分配至五种属性
- **篝火**：在篝火处升级或洗点（洗点消耗钻石）
- **属性补正**：Mind / Dexterity / Intelligence / Faith / Arcane 影响 ISS 法术属性
- **触媒补正**：持握带 ScalingProfile 的物品时，根据属性动态加成
- **数据驱动**：补正曲线、触媒配置均通过数据包自定义

## 属性与补正

| 属性 | 全局补正 | 说明 |
|------|----------|------|
| **Mind** | max_mana | 最大法力值 |
| **Dexterity** | cast_time_reduction, cooldown_reduction | 咏唱速度、冷却速度 |
| **Intelligence** | — | 触媒/法术强度（由物品 ScalingProfile 定义） |
| **Faith** | — | 同上 |
| **Arcane** | — | 同上 |

全局补正（Mind、Dexterity）始终生效；触媒补正仅持握对应物品时生效。法术强度等由触媒 ScalingProfile 数据驱动，不预设全局补正。

## 数据包自定义

### 补正配置路径

```
data/<命名空间>/soulslikespells/scaling_profiles/*.json
```

### 基础格式

```json
{
  "entries": [
    {
      "source": "soulslikespells:mind",
      "target": "irons_spellbooks:max_mana",
      "curve": "soft_cap_60",
      "multiplier": 358,
      "operation": "ADD_VALUE"
    }
  ]
}
```

### Source 表达式

| 格式 | 示例 | 说明 |
|------|------|------|
| 单值 | `"source": "soulslikespells:mind"` | SLS 属性或任意 attribute |
| 平均 | `"source": {"avg": ["soulslikespells:faith", "soulslikespells:intelligence"]}` | 多源平均值，source_max 保持 99 |
| 最小 | `"source": {"min": ["soulslikespells:faith", "soulslikespells:intelligence"]}` | 取多源最小值 |
| 求和 | `"source": {"sum": ["soulslikespells:faith", "soulslikespells:intelligence"]}` | 多源之和，source_max 需相应设置（如 198） |

### 曲线

- **内置**：`standard`、`soft_cap_60`、`early_bloom`、`late`
- **自定义**：`curve_nodes` 数组，每项为 `{threshold: 0..1, percentage: 0..1}`

### 触媒与物品绑定

在 `data/<ns>/soulslikespells/data_maps/item/scaling_profile.json` 中将物品绑定到 profile：

```json
{
  "values": {
    "irons_spellbooks:mage_staff": "soulslikespells:example_int_staff"
  }
}
```

### 全局属性补正

以 `stat_` 开头的 profile 会作为全局补正自动应用，例如：

- `stat_mind.json` — Mind 补正
- `stat_dexterity.json` — Dexterity 补正

## 配置

| 配置项 | 说明 |
|--------|------|
| `levelCost.a/b/c/d` | 升级经验公式（三次多项式） |
| `maxSoulLevel` | 最大灵魂等级 |
| `deathPenalty.enabled` | 是否启用死亡经验惩罚 |
| `scaling.globalMultiplier` | 全局补正倍率 |
| `respec.item` | 洗点消耗的道具 ID（如 `minecraft:diamond`），留空或配合 `respec.amount=0` 可免费洗点 |
| `respec.amount` | 每次洗点消耗的道具数量，设为 0 可免费洗点 |

## 依赖

- **NeoForge** 1.21.1+
- **Iron's Spells 'n Spellbooks**
- **Geckolib**、**PlayerAnimator**、**Curios**（可选）

## 开发

```bash
./gradlew build
```

## 许可证

MIT

---

# SoulslikeSpells (English)

A Dark Souls–style soul level and attribute scaling system for Minecraft 1.21.1 + NeoForge, designed to integrate with [Iron's Spells 'n Spellbooks](https://modrinth.com/mod/irons-spells-n-spellbooks).

## Features

- **Soul Level**: Spend experience to level up and allocate points across five stats
- **Bonfire**: Level up or respec at bonfires (respec costs diamonds)
- **Attribute Scaling**: Mind / Dexterity / Intelligence / Faith / Arcane affect ISS spell attributes
- **Catalyst Scaling**: Items with ScalingProfile apply dynamic bonuses based on attributes when held
- **Data-driven**: Scaling curves and catalyst configs are customizable via data packs

## Attributes & Scaling

| Attribute | Global Scaling | Description |
|-----------|----------------|-------------|
| **Mind** | max_mana | Maximum mana |
| **Dexterity** | cast_time_reduction, cooldown_reduction | Cast speed, cooldown reduction |
| **Intelligence** | — | Catalyst/spell power (defined by item ScalingProfile) |
| **Faith** | — | Same as above |
| **Arcane** | — | Same as above |

Global scaling (Mind, Dexterity) is always active; catalyst scaling applies only when holding the corresponding item. Spell power and similar effects are driven by catalyst ScalingProfile data, with no built-in global scaling.

## Data Pack Customization

### Scaling Profile Path

```
data/<namespace>/soulslikespells/scaling_profiles/*.json
```

### Basic Format

```json
{
  "entries": [
    {
      "source": "soulslikespells:mind",
      "target": "irons_spellbooks:max_mana",
      "curve": "soft_cap_60",
      "multiplier": 358,
      "operation": "ADD_VALUE"
    }
  ]
}
```

### Source Expressions

| Format | Example | Description |
|--------|---------|-------------|
| Single | `"source": "soulslikespells:mind"` | SLS stat or any attribute |
| Average | `"source": {"avg": ["soulslikespells:faith", "soulslikespells:intelligence"]}` | Mean of multiple sources; source_max stays 99 |
| Minimum | `"source": {"min": ["soulslikespells:faith", "soulslikespells:intelligence"]}` | Min of multiple sources |
| Sum | `"source": {"sum": ["soulslikespells:faith", "soulslikespells:intelligence"]}` | Sum of sources; source_max should be set accordingly (e.g. 198) |

### Curves

- **Built-in**: `standard`, `soft_cap_60`, `early_bloom`, `late`
- **Custom**: `curve_nodes` array, each entry `{threshold: 0..1, percentage: 0..1}`

### Binding Catalysts to Items

Assign items to profiles in `data/<ns>/soulslikespells/data_maps/item/scaling_profile.json`:

```json
{
  "values": {
    "irons_spellbooks:mage_staff": "soulslikespells:example_int_staff"
  }
}
```

### Global Attribute Scaling

Profiles whose names start with `stat_` are applied as global scaling, e.g.:

- `stat_mind.json` — Mind scaling
- `stat_dexterity.json` — Dexterity scaling

## Configuration

| Option | Description |
|--------|-------------|
| `levelCost.a/b/c/d` | Level-up experience formula (cubic polynomial) |
| `maxSoulLevel` | Maximum soul level |
| `deathPenalty.enabled` | Enable death experience penalty |
| `scaling.globalMultiplier` | Global scaling multiplier |
| `respec.item` | Item ID consumed for respec (e.g. `minecraft:diamond`). Leave empty or set `respec.amount=0` for free respec |
| `respec.amount` | Amount of item consumed per respec. Set to 0 for free respec |

## Dependencies

- **NeoForge** 1.21.1+
- **Iron's Spells 'n Spellbooks**
- **Geckolib**, **PlayerAnimator**, **Curios** (optional)

## Development

```bash
./gradlew build
```

## License

MIT
