package com.litimatica.gui;

import com.litimatica.schematic.SchematicManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.sandrohc.schematic4j.schematic.Schematic;

public class SchematicRenderer {
    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        Schematic schematic = SchematicManager.getCurrentSchematic();
        if (schematic == null || vertexConsumers == null) return;

        BlockPos origin = SchematicPlacementScreen.placementPos;
        int rotation = SchematicPlacementScreen.rotation;
        String mirror = SchematicPlacementScreen.mirror;

        matrices.push();
        matrices.translate(origin.getX() - cameraX, origin.getY() - cameraY, origin.getZ() - cameraZ);

        // Apply same transformations as PasteOptimizer for the preview box
        if (mirror.equals("X")) {
            matrices.translate(schematic.width(), 0, 0);
            matrices.scale(-1, 1, 1);
        }
        if (mirror.equals("Z")) {
            matrices.translate(0, 0, schematic.length());
            matrices.scale(1, 1, -1);
        }

        if (rotation != 0) {
            // Rotate around the center of the schematic roughly or match PasteOptimizer logic
            // PasteOptimizer rotates around (0,0) of the schematic grid
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

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        float w = schematic.width();
        float h = schematic.height();
        float d = schematic.length();

        drawOutline(matrices, vertexConsumer, 0, 0, 0, w, h, d, 0.5f, 0.5f, 1.0f, 1.0f);

        matrices.pop();
    }

    private static void drawOutline(MatrixStack matrices, VertexConsumer vertexConsumer, float x, float y, float z, float w, float h, float d, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();

        // Bottom edges
        line(entry, vertexConsumer, x, y, z, x + w, y, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z, x + w, y, z + d, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z + d, x, y, z + d, r, g, b, a);
        line(entry, vertexConsumer, x, y, z + d, x, y, z, r, g, b, a);

        // Top edges
        line(entry, vertexConsumer, x, y + h, z, x + w, y + h, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y + h, z, x + w, y + h, z + d, r, g, b, a);
        line(entry, vertexConsumer, x + w, y + h, z + d, x, y + h, z + d, r, g, b, a);
        line(entry, vertexConsumer, x, y + h, z + d, x, y + h, z, r, g, b, a);

        // Vertical edges
        line(entry, vertexConsumer, x, y, z, x, y + h, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z, x + w, y + h, z, r, g, b, a);
        line(entry, vertexConsumer, x + w, y, z + d, x + w, y + h, z + d, r, g, b, a);
        line(entry, vertexConsumer, x, y, z + d, x, y + h, z + d, r, g, b, a);
    }

    private static void line(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        vertexConsumer.vertex(entry, x1, y1, z1).color(r, g, b, a).normal(entry, 0, 1, 0);
        vertexConsumer.vertex(entry, x2, y2, z2).color(r, g, b, a).normal(entry, 0, 1, 0);
    }
}
