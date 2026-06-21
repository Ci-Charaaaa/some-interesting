@echo off
echo === Version Check ===
echo.
for /f "tokens=2 delims==" %%a in ('findstr "minecraft_version" gradle.properties') do echo MC Version: %%a
for /f "tokens=2 delims==" %%a in ('findstr "fabric_api_version" gradle.properties') do echo Fabric API: %%a
findstr "minecraft" src\main\resources\fabric.mod.json | findstr "~"
findstr "modmenu\|cloth-config" build.gradle
findstr "BlockEntityTypeMixin" src\main\resources\some-interesting.mixins.json >nul 2>&1 && echo Mixin: BlockEntityTypeMixin PRESENT (26.1.1 mode) || echo Mixin: BlockEntityTypeMixin ABSENT (26.2 mode)
findstr "client.screen" src\client\java\com\chara\some_interesting\client\ModKeyBindings.java >nul 2>&1 && echo Screen: client.screen check PRESENT (26.1.1 mode) || echo Screen: client.screen check ABSENT (26.2 mode)
echo.
echo === Done ===
pause
