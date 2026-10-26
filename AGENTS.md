# Termux Fork (owner888) —— Agent 协作与维护纪律

本项目是基于官方 **Termux（termux/termux-app）** 的定制 Fork，核心目标是打造适合现代 Android 全面屏、多模态 AI（如 pig）及中文打字体验的高可用终端。

---

## 1. 仓库关系与跟进纪律（Rebase 浮顶铁律）

- **上游源**：`upstream = https://github.com/termux/termux-app.git`
- **本仓库**：`origin = git@github.com:owner888/termux-app.git`
- **同步上游更新绝对禁止直接 `git merge`**（会产生网状交叉 commit 污染历史）。
- **必须严格遵循 Rebase 策略**，保持我们的定制 commit 永远整洁地浮在 `upstream/master` 顶层：
  ```bash
  git fetch upstream master
  git rebase upstream/master
  ./gradlew testDebugUnitTest :app:assembleDebug
  git push origin master --force-with-lease
  ```

---

## 2. 本 Fork 的四大核心定制能力（严禁破坏）

1. **Gboard 中文与 CJK 组合输入（IME Composing Preview）**：
   - 依赖 `TerminalView.onCreateInputConnection` 中根据 `shouldEnableImeComposing()` 暴露 `TYPE_CLASS_TEXT`；
   - 自动检测默认输入法是否为 Gboard 并开箱即用自动开启；
   - 严禁随意更改 `EditorInfo` 的 `inputType`，否则会使 Gboard 锁死在纯英文。
2. **全屏/大范围侧滑扩展（DrawerUtils）**：
   - 通过 `DrawerUtils` 锁定 `mLeftDragger` 的 `mEdgeSize` 为屏幕 60% 宽度，并在 `OnPreDrawListener` 中防止被 `onLayout` 冲掉；
   - 彻底避开 Android 10+ 全面屏系统边缘返回手势（0~25dp）的拦截。
3. **Material Design 3 原生化现代 UI**：
   - 侧边栏为 310dp 宽度、24dp 右圆角、包含终端 Header 与 24dp Pill 胶囊选中项；
   - 彻底移除了所有历史遗留的纯大红色（`#FF0000`）标题栏，升级为 Material 3 Surface TopAppBar；
   - 设置项全量配备 Material 矢量图标；弹窗统一使用 `MaterialAlertDialogBuilder`。
4. **剪贴板与 Gboard 图片自动落盘与路径粘贴（AI / pig 专供）**：
   - `ClipboardImageUtils` 自动将剪贴板的图片（或 Gboard 富文本卡片）保存至 `~/.cache/clipboard/clip_yyyyMMdd_HHmmss.png`；
   - 自动将本地绝对路径粘贴进终端，无缝对接多模态 AI 命令行工具。

---

## 3. 新增功能时的架构护城河

- **独立工具类优先**：新增特性优先编写独立的 Helper/Utils（如 `DrawerUtils`、`ClipboardImageUtils`），严禁对官方主干类大拆大改，最大化降低未来与上游更新的代码冲突率。
- **发布操作必须单轮显式授权**：严禁自行推测或顺便打 Git Tag、创建 GitHub Release。任何发布行为必须依赖用户当前轮明确指示（如“发版”），严禁自作主张。
- **修改后必须本地验证**：
  ```bash
  ./gradlew testDebugUnitTest :app:assembleDebug
  ```
