# AGENTS.md

This file provides context for AI coding assistants working on this project.

## Project Overview
- **Type**: Minecraft Fabric Mod
- **Minecraft Version**: 26.1.1 (main branch), 26.2 (branch26.2)
- **Java Version**: 25
- **Fabric Loader**: 0.19.2
- **Fabric Loom**: 1.16-SNAPSHOT
- **Mod ID**: `some-interesting`

## Build & Run

```bash
./gradlew build              # Compile and package
./gradlew clean build         # Clean rebuild
./gradlew runClient           # Launch test client
```

## Key Directories

```
src/main/java/        # Shared code (client + server)
src/client/java/      # Client-only code (screens, keybindings)
src/main/resources/   # Assets, lang files, recipes, mod metadata
src/client/resources/  # Client mixin config
```

## Important Conventions

- Split source sets: `src/main/` (shared) vs `src/client/` (client-only)
- No code comments (project convention)
- `build.gradle` uses `filteringCharset = 'UTF-8'` for resource processing
- Language files: always update all three (zh_cn, en_us, zh_tw)
- MC 26.x rendering: use `GuiGraphicsExtractor` with `extractRenderState()`/`extractBackground()`
- MC 26.x input: `KeyEvent`/`MouseButtonEvent` record classes
- MC 26.x colors: text rendering requires ARGB format (e.g., `0xFFFFFFFF` not `0xFFFFFF`)
- Run configs: `.idea/runConfigurations/` use `$PROJECT_DIR$` for paths

## Architecture Notes

- 16 EnhanceComponent types (one per equipment), all follow the same record pattern
- BoundItemStorage: manual NBT file I/O with RegistryOps (not SavedData)
- Networking: 3 custom payloads (BoundItemsSync, OpenSoulBinding, SelectBoundItem)
- SoulBindingMenu: Container Menu with vanilla recipe matching, filtered to damageable items only
