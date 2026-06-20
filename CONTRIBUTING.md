# 贡献指南

感谢你对 Some Interesting 的关注！本文档帮助你搭建开发环境并了解项目规范。

## 开发环境

### 前置要求
- **JDK 25**（项目使用 Java 25 特性）
- **IntelliJ IDEA**（推荐）或其他支持 Gradle 的 IDE
- **Git**

### 搭建步骤

```bash
# 1. 克隆仓库
git clone <仓库地址>
cd some-interesting-26.1.1

# 2. 构建项目（首次会下载 Minecraft 和依赖，可能需要几分钟）
./gradlew build

# 3. 生成 IDE 运行配置
# IntelliJ：打开项目后 Gradle 会自动同步

# 4. 启动游戏测试
# 使用 IntelliJ 运行 "Minecraft Client" 配置
```

### 项目结构

```
src/
├── main/                          # 通用代码（客户端 + 服务端）
│   ├── java/com/chara/some_interesting/
│   │   ├── SomeInteresting.java       # 主入口 (ModInitializer)
│   │   ├── ModItems.java              # 物品注册
│   │   ├── ModBlocks.java             # 方块注册
│   │   ├── ModNetworking.java         # 网络包注册 + 生命周期事件
│   │   ├── BindingStoneItem.java      # 绑定粉物品逻辑
│   │   ├── BoundItemStorage.java      # 绑定物品持久化存储
│   │   ├── component/                 # 16 种熟练度数据组件
│   │   ├── EventCallBack/             # 攻击/挖掘/右键等事件回调
│   │   ├── Menu/                      # 容器菜单（升锻台、灵魂绑定）
│   │   ├── Blocks/                    # 自定义方块
│   │   ├── ModBlockEntities/          # 方块实体
│   │   └── mixin/                     # Mixin 注入
│   └── resources/
│       ├── fabric.mod.json            # 模组元数据
│       ├── assets/some-interesting/   # 贴图、模型、语言文件
│       └── data/some-interesting/     # 配方、战利品表、标签
│
├── client/                        # 客户端专用代码
│   ├── java/com/chara/some_interesting/
│   │   ├── client/
│   │   │   ├── SomeInterestingClient.java  # 客户端入口
│   │   │   ├── ModKeyBindings.java         # 快捷键注册
│   │   │   └── ClientBoundItemData.java    # 客户端缓存
│   │   └── Screens/                        # UI 界面
│   └── resources/
│       └── some-interesting.client.mixins.json
│
└── test/                          # 测试代码（暂未使用）
```

### 常用命令

| 命令 | 用途 |
|------|------|
| `./gradlew build` | 编译并打包 |
| `./gradlew clean build` | 清理后重新编译 |
| `./gradlew genSources` | 生成 Minecraft 反编译源码（便于查看原版 API） |
| `./gradlew runClient` | 从命令行启动客户端测试 |

## 代码规范

### 通用规范
- 方法和变量使用 snake_case（与项目现有风格一致）
- 类名使用 PascalCase
- 每个文件保持单一职责

### 分源代码集
项目使用 Fabric Loom 的 `splitEnvironmentSourceSets()`：
- `src/main/` — 客户端和服务端共用的代码
- `src/client/` — 仅客户端的代码（Screen、渲染、按键绑定等）
- 客户端代码**不能**引用 `src/main/` 以外的服务端专属类

### 语言文件
新增功能时需同步更新三个语言文件：
- `zh_cn.json`（简体中文，主要语言）
- `en_us.json`（英语）
- `zh_tw.json`（繁体中文）

### 构建编码
`build.gradle` 中已配置 `filteringCharset = 'UTF-8'`，确保资源文件中文不乱码。

## 分支管理

| 分支 | 用途 |
|------|------|
| `main` | 主力开发分支，对应 Minecraft 26.1.1 |
| `branch26.2` | Minecraft 26.2 适配分支 |

- 日常开发在 `main` 分支进行
- 新版本适配：从 `main` 创建分支 → 修改版本号 → 修复编译错误
- 同步更新：`git checkout branch26.2 && git merge main` → 解决冲突 → 编译验证

## 提交规范

提交信息简明描述改动内容，推荐格式：
```
<类型>: <简述>

类型：
- feat: 新功能
- fix: 修复 Bug
- refactor: 重构（不影响功能）
- docs: 文档更新
- style: 代码风格/格式调整
```

示例：
```
feat: add 24h crafting cooldown for soul binding
fix: bound items lost after game restart
docs: add project documentation
```
