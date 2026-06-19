package com.chara.some_interesting;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BoundItemStorage {

    private static final long COOLDOWN_MS = 24 * 60 * 60 * 1000L;
    private static final String COOLDOWNS_KEY = "__cooldowns__";

    private static final BoundItemStorage INSTANCE = new BoundItemStorage();
    private final Map<String, List<ItemStack>> boundItems = new HashMap<>();
    private final Map<String, Map<Integer, Long>> craftCooldowns = new HashMap<>();

    public static BoundItemStorage get() {
        return INSTANCE;
    }

    public static String getPlayerKey(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server.usesAuthentication()) {
            return player.getStringUUID();
        }
        return player.getScoreboardName();
    }

    public void addBoundItem(ServerPlayer player, ItemStack stack) {
        String key = getPlayerKey(player);
        boundItems.computeIfAbsent(key, k -> new ArrayList<>()).add(stack.copy());
    }

    public List<ItemStack> getBoundItems(ServerPlayer player) {
        return boundItems.getOrDefault(getPlayerKey(player), List.of());
    }

    public boolean isOnCooldown(String playerKey, int itemIndex) {
        Map<Integer, Long> cooldowns = craftCooldowns.get(playerKey);
        if (cooldowns == null) return false;
        Long lastCraft = cooldowns.get(itemIndex);
        if (lastCraft == null) return false;
        return System.currentTimeMillis() - lastCraft < COOLDOWN_MS;
    }

    public long getRemainingCooldownMs(String playerKey, int itemIndex) {
        Map<Integer, Long> cooldowns = craftCooldowns.get(playerKey);
        if (cooldowns == null) return 0;
        Long lastCraft = cooldowns.get(itemIndex);
        if (lastCraft == null) return 0;
        return Math.max(0, COOLDOWN_MS - (System.currentTimeMillis() - lastCraft));
    }

    public void setCooldown(String playerKey, int itemIndex) {
        craftCooldowns.computeIfAbsent(playerKey, k -> new HashMap<>()).put(itemIndex, System.currentTimeMillis());
    }

    private Path getSavePath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("some_interesting_bound_items.nbt");
    }

    public void save(MinecraftServer server) {
        if (boundItems.isEmpty() && craftCooldowns.isEmpty()) return;

        Path path = getSavePath(server);
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
        CompoundTag root = new CompoundTag();
        int totalItems = 0;

        for (Map.Entry<String, List<ItemStack>> entry : boundItems.entrySet()) {
            ListTag list = new ListTag();
            for (ItemStack stack : entry.getValue()) {
                DataResult<Tag> result = ItemStack.CODEC.encodeStart(ops, stack);
                result.ifSuccess(list::add)
                      .ifError(err -> SomeInteresting.LOGGER.warn("[BoundItems] Encode failed: {}", err.message()));
            }
            root.put(entry.getKey(), list);
            totalItems += list.size();
        }

        CompoundTag cooldownTag = new CompoundTag();
        for (Map.Entry<String, Map<Integer, Long>> entry : craftCooldowns.entrySet()) {
            CompoundTag playerCd = new CompoundTag();
            for (Map.Entry<Integer, Long> cd : entry.getValue().entrySet()) {
                playerCd.putLong(cd.getKey().toString(), cd.getValue());
            }
            cooldownTag.put(entry.getKey(), playerCd);
        }
        root.put(COOLDOWNS_KEY, cooldownTag);

        try {
            Files.createDirectories(path.getParent());
            NbtIo.writeCompressed(root, path);
            SomeInteresting.LOGGER.info("[BoundItems] Saved {} items for {} players to {}", totalItems, boundItems.size(), path);
        } catch (IOException e) {
            SomeInteresting.LOGGER.error("[BoundItems] Failed to save to {}", path, e);
        }
    }

    public void load(MinecraftServer server) {
        Path path = getSavePath(server);
        if (!Files.exists(path)) {
            SomeInteresting.LOGGER.info("[BoundItems] No save file found at {}", path);
            return;
        }

        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());

        try {
            CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            boundItems.clear();
            craftCooldowns.clear();
            int totalItems = 0;

            Optional<CompoundTag> cooldownOpt = root.getCompound(COOLDOWNS_KEY);
            if (cooldownOpt.isPresent()) {
                CompoundTag cooldownTag = cooldownOpt.get();
                for (String playerKey : cooldownTag.keySet()) {
                    Optional<CompoundTag> playerCdOpt = cooldownTag.getCompound(playerKey);
                    if (playerCdOpt.isPresent()) {
                        CompoundTag playerCd = playerCdOpt.get();
                        Map<Integer, Long> cdMap = new HashMap<>();
                        for (String idx : playerCd.keySet()) {
                            cdMap.put(Integer.parseInt(idx), playerCd.getLong(idx).orElse(0L));
                        }
                        if (!cdMap.isEmpty()) {
                            craftCooldowns.put(playerKey, cdMap);
                        }
                    }
                }
            }

            for (String key : root.keySet()) {
                if (key.equals(COOLDOWNS_KEY)) continue;
                ListTag list = root.getListOrEmpty(key);
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    Tag tag = list.get(i);
                    DataResult<ItemStack> result = ItemStack.CODEC.parse(ops, tag);
                    result.ifSuccess(items::add)
                          .ifError(err -> SomeInteresting.LOGGER.warn("[BoundItems] Decode failed: {}", err.message()));
                }
                if (!items.isEmpty()) {
                    boundItems.put(key, items);
                    totalItems += items.size();
                }
            }
            SomeInteresting.LOGGER.info("[BoundItems] Loaded {} items for {} players from {}", totalItems, boundItems.size(), path);
        } catch (IOException e) {
            SomeInteresting.LOGGER.error("[BoundItems] Failed to load from {}", path, e);
        }
    }

    public void clear() {
        boundItems.clear();
        craftCooldowns.clear();
    }
}
