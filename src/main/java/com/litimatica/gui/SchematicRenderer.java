package com.litimatica.gui;

import com.litimatica.schematic.SchematicManager;
import com.litimatica.schematic.LitimaticaSettings;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchematicRenderer {
    private static BlockPos lastPlayerPos = null;
    private static List<CachedBlock> renderCache = new ArrayList<>();

    public static void clearCache() {
        renderCache.clear();
        lastPlayerPos = null;
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Schematic schematic = SchematicManager.getCurrentSchematic();
        if (schematic == null || vertexConsumers == null || !LitimaticaSettings.hologramEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos origin = SchematicPlacementScreen.placementPos;
        int rotation = SchematicPlacementScreen.rotation;
        String mirror = SchematicPlacementScreen.mirror;

        matrices.push();
        matrices.translate(origin.getX() - cameraX, origin.getY() - cameraY, origin.getZ() - cameraZ);

        if (mirror.equals("X")) {
            matrices.translate(schematic.width(), 0, 0);
            matrices.scale(-1, 1, 1);
        }
        if (mirror.equals("Z")) {
            matrices.translate(0, 0, schematic.length());
            matrices.scale(1, 1, -1);
        }

        if (rotation != 0) {
            if (rotation == 90) {
                matrices.translate(schematic.length(), 0, 0);
                matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            } else if (rotation == 180) {
                matrices.translate(schematic.width(), 0, schematic.length());
                matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-180));
            } else if (rotation == 270) {
                matrices.translate(0, 0, schematic.width());
                matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-270));
            }
        }

        if (client.player != null) {
            BlockPos playerPos = client.player.getBlockPos();
            if (lastPlayerPos == null || !lastPlayerPos.equals(playerPos)) {
                updateCache(schematic, playerPos, origin);
                lastPlayerPos = playerPos;
            }

            for (CachedBlock cached : renderCache) {
                matrices.push();
                matrices.translate(cached.x, cached.y, cached.z);
                client.getBlockRenderManager().renderBlockAsEntity(cached.state, matrices, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV);
                matrices.pop();
            }
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        drawOutline(matrices, vertexConsumer, 0, 0, 0, schematic.width(), schematic.height(), schematic.length(), 0.5f, 0.5f, 1.0f, 1.0f);

        matrices.pop();
    }

    private static void updateCache(Schematic schematic, BlockPos playerPos, BlockPos origin) {
        renderCache.clear();
        int range = 16;
        int sx = playerPos.getX() - origin.getX();
        int sy = playerPos.getY() - origin.getY();
        int sz = playerPos.getZ() - origin.getZ();

        int minX = Math.max(0, sx - range);
        int maxX = Math.min(schematic.width() - 1, sx + range);
        int minY = Math.max(0, sy - range);
        int maxY = Math.min(schematic.height() - 1, sy + range);
        int minZ = Math.max(0, sz - range);
        int maxZ = Math.min(schematic.length() - 1, sz + range);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    SchematicBlock sBlock = schematic.block(x, y, z);
                    if (sBlock == null || sBlock.block().equals("minecraft:air")) continue;
                    BlockState state = getBlockState(sBlock);
                    if (state != null) {
                        renderCache.add(new CachedBlock(x, y, z, state));
                    }
                }
            }
        }
    }

    private static BlockState getBlockState(SchematicBlock sBlock) {
        try {
            Identifier id = Identifier.of(sBlock.block());
            net.minecraft.block.Block block = Registries.BLOCK.get(id);
            BlockState state = block.getDefaultState();
            for (Map.Entry<String, String> entry : sBlock.states().entrySet()) {
                state = applyProperty(state, entry.getKey(), entry.getValue());
            }
            return state;
        } catch (Exception e) {
            return null;
        }
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, String name, String value) {
        for (net.minecraft.state.property.Property<?> prop : state.getProperties()) {
            if (prop.getName().equals(name)) {
                return apply(state, prop, value);
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState apply(BlockState state, net.minecraft.state.property.Property<T> prop, String value) {
        Optional<T> optional = prop.parse(value);
        if (optional.isPresent()) {
            return state.with(prop, optional.get());
        }
        return state;
    }

    private static void drawOutline(MatrixStack matrices, VertexConsumer vertexConsumer, float x, float y, float z, float w, float h, float d, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        line(entry, vertexConsumer, x, y, z, x + w, y, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z, x + w, y, z + d, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z + d, x, y, z + d, r, g, b, a);
        line(entry, vertexConsumer, x, y, z + d, x, y, z, r, g, b, a);
        line(entry, vertexConsumer, x, y + h, z, x + w, y + h, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y + h, z, x + w, y + h, z + d, r, g, b, a);
        line(entry, vertexConsumer, x + w, y + h, z + d, x, y + h, z + d, r, g, b, a);
        line(entry, vertexConsumer, x, y + h, z + d, x, y + h, z, r, g, b, a);
        line(entry, vertexConsumer, x, y, z, x, y + h, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z, x + w, y + h, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z + d, x + w, y + h, z + d, r, g, b, a);
        line(entry, vertexConsumer, x, y, z + d, x, y + h, z + d, r, g, b, a);
    }

    private static void line(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r, g, b, a).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(r, g, b, a).normal(0, 1, 0);
    }

    private static record CachedBlock(int x, int y, int z, BlockState state) {}
}
