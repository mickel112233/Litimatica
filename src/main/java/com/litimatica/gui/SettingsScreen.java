package com.litimatica.gui;

import com.litimatica.schematic.CommandQueue;
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
        speedField = new TextFieldWidget(textRenderer, width / 2 - 50, 80, 100, 20, Text.literal("Commands Per Tick"));
        speedField.setText(String.valueOf(CommandQueue.getCommandsPerTick()));
        this.addSelectableChild(speedField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Back"), button -> {
            try {
                CommandQueue.setCommandsPerTick(Integer.parseInt(speedField.getText()));
            } catch (NumberFormatException ignored) {}
            this.client.setScreen(parent);
        }).dimensions(width / 2 - 100, 120, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Commands Per Tick (Anti-Kick)"), width / 2, 65, 0xA0A0A0);
        speedField.render(context, mouseX, mouseY, delta);
    }
}
