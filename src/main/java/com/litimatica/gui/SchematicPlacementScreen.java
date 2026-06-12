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

    public SchematicPlacementScreen(Screen parent) {
        super(Text.literal("Schematic Placement"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        xField = new TextFieldWidget(textRenderer, centerX - 105, 60, 60, 20, Text.literal("X"));
        xField.setText(String.valueOf(placementPos.getX()));
        this.addSelectableChild(xField);

        yField = new TextFieldWidget(textRenderer, centerX - 30, 60, 60, 20, Text.literal("Y"));
        yField.setText(String.valueOf(placementPos.getY()));
        this.addSelectableChild(yField);

        zField = new TextFieldWidget(textRenderer, centerX + 45, 60, 60, 20, Text.literal("Z"));
        zField.setText(String.valueOf(placementPos.getZ()));
        this.addSelectableChild(zField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Paste (Fast)"), button -> {
            updatePos();
            if (SchematicManager.getCurrentSchematic() != null) {
                List<String> commands = PasteOptimizer.generateCommands(SchematicManager.getCurrentSchematic(), placementPos);
                CommandQueue.addCommands(commands);
            }
            this.client.setScreen(null);
        }).dimensions(centerX - 100, 100, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Settings"), button -> {
            this.client.setScreen(new SettingsScreen(this));
        }).dimensions(centerX - 100, 130, 200, 20).build());

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
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Placement Coordinates:"), this.width / 2, 45, 0xA0A0A0);
        xField.render(context, mouseX, mouseY, delta);
        yField.render(context, mouseX, mouseY, delta);
        zField.render(context, mouseX, mouseY, delta);
    }
}
