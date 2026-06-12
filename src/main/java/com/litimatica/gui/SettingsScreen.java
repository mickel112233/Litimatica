package com.litimatica.gui;

import com.litimatica.schematic.CommandQueue;
import com.litimatica.schematic.LitimaticaSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget speedField;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Litimatica Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        speedField = new TextFieldWidget(textRenderer, centerX - 50, 50, 100, 20, Text.literal("Commands Per Second"));
        speedField.setText(String.valueOf(CommandQueue.getCommandsPerSecond()));
        this.addSelectableChild(speedField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Hologram: " + (LitimaticaSettings.hologramEnabled ? "ON" : "OFF")), button -> {
            LitimaticaSettings.hologramEnabled = !LitimaticaSettings.hologramEnabled;
            button.setMessage(Text.literal("Hologram: " + (LitimaticaSettings.hologramEnabled ? "ON" : "OFF")));
        }).dimensions(centerX - 100, 80, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-Place: " + (LitimaticaSettings.autoPlaceEnabled ? "ON" : "OFF")), button -> {
            LitimaticaSettings.autoPlaceEnabled = !LitimaticaSettings.autoPlaceEnabled;
            button.setMessage(Text.literal("Auto-Place: " + (LitimaticaSettings.autoPlaceEnabled ? "ON" : "OFF")));
        }).dimensions(centerX - 100, 105, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Placement Locked: " + (LitimaticaSettings.placementLocked ? "ON" : "OFF")), button -> {
            LitimaticaSettings.placementLocked = !LitimaticaSettings.placementLocked;
            button.setMessage(Text.literal("Placement Locked: " + (LitimaticaSettings.placementLocked ? "ON" : "OFF")));
        }).dimensions(centerX - 100, 130, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> {
            try {
                CommandQueue.setCommandsPerSecond(Integer.parseInt(speedField.getText()));
            } catch (NumberFormatException ignored) {}
            this.client.setScreen(parent);
        }).dimensions(centerX - 100, 170, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Commands Per Second"), width / 2, 38, 0xA0A0A0);
        speedField.render(context, mouseX, mouseY, delta);
    }
}
