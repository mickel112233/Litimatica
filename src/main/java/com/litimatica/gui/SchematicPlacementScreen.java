package com.litimatica.gui;

import com.litimatica.schematic.CommandQueue;
import com.litimatica.schematic.PasteOptimizer;
import com.litimatica.schematic.SchematicManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SchematicPlacementScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget xField, yField, zField;
    public static BlockPos placementPos = BlockPos.ORIGIN;
    public static int rotation = 0; // 0, 90, 180, 270
    public static String mirror = "None"; // None, X, Z

    public SchematicPlacementScreen(Screen parent) {
        super(Text.literal("Schematic Placement"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        xField = new TextFieldWidget(textRenderer, centerX - 105, 50, 60, 20, Text.literal("X"));
        xField.setText(String.valueOf(placementPos.getX()));
        this.addSelectableChild(xField);

        yField = new TextFieldWidget(textRenderer, centerX - 30, 50, 60, 20, Text.literal("Y"));
        yField.setText(String.valueOf(placementPos.getY()));
        this.addSelectableChild(yField);

        zField = new TextFieldWidget(textRenderer, centerX + 45, 50, 60, 20, Text.literal("Z"));
        zField.setText(String.valueOf(placementPos.getZ()));
        this.addSelectableChild(zField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Rotation: " + rotation + "°"), button -> {
            rotation = (rotation + 90) % 360;
            button.setMessage(Text.literal("Rotation: " + rotation + "°"));
        }).dimensions(centerX - 100, 80, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Mirror: " + mirror), button -> {
            if (mirror.equals("None")) mirror = "X";
            else if (mirror.equals("X")) mirror = "Z";
            else mirror = "None";
            button.setMessage(Text.literal("Mirror: " + mirror));
        }).dimensions(centerX - 100, 105, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Paste (Throttled)"), button -> {
            updatePos();
            if (SchematicManager.getCurrentSchematic() != null) {
                button.active = false;
                button.setMessage(Text.literal("Working... (Async)"));
                PasteOptimizer.generateCommandsAsync(SchematicManager.getCurrentSchematic(), placementPos, rotation, mirror)
                        .thenAccept(commands -> {
                            CommandQueue.addCommands(commands);
                            this.client.execute(() -> this.client.setScreen(null));
                            if (this.client.player != null) {
                                this.client.player.sendMessage(Text.literal("§aOptimization Complete! Commands added to queue: " + commands.size()), true);
                            }
                        });
            }
        }).dimensions(centerX - 100, 135, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Paste (Fast)"), button -> {
            updatePos();
            if (SchematicManager.getCurrentSchematic() != null) {
                button.active = false;
                button.setMessage(Text.literal("Working..."));
                PasteOptimizer.generateCommandsAsync(SchematicManager.getCurrentSchematic(), placementPos, rotation, mirror)
                        .thenAccept(commands -> {
                            // Instead of sending all at once which crashes the network buffer,
                            // we'll put them in the queue with a very high speed
                            CommandQueue.setCommandsPerSecond(1000);
                            CommandQueue.addCommands(commands);
                            this.client.execute(() -> this.client.setScreen(null));
                            if (this.client.player != null) {
                                this.client.player.sendMessage(Text.literal("§aFast Build Started! (" + commands.size() + " commands)"), true);
                            }
                        });
            }
        }).dimensions(centerX + 2, 135, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Move to Player"), button -> {
            if (this.client.player != null) {
                placementPos = this.client.player.getBlockPos();
                xField.setText(String.valueOf(placementPos.getX()));
                yField.setText(String.valueOf(placementPos.getY()));
                zField.setText(String.valueOf(placementPos.getZ()));
            }
        }).dimensions(centerX - 100, 160, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> {
            this.client.setScreen(parent);
        }).dimensions(centerX - 100, height - 30, 200, 20).build());
    }

    private void updatePos() {
        try {
            int x = Integer.parseInt(xField.getText());
            int y = Integer.parseInt(yField.getText());
            int z = Integer.parseInt(zField.getText());
            placementPos = new BlockPos(x, y, z);
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Placement Coordinates:"), this.width / 2, 35, 0xA0A0A0);
        xField.render(context, mouseX, mouseY, delta);
        yField.render(context, mouseX, mouseY, delta);
        zField.render(context, mouseX, mouseY, delta);
    }
}
