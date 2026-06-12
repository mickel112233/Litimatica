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
        matrices.push();
        matrices.translate(origin.getX() - cameraX, origin.getY() - cameraY, origin.getZ() - cameraZ);

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
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r, g, b, a).normal(entry, 0, 0, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(r, g, b, a).normal(entry, 0, 0, 0);
    }
}
