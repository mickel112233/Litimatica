package com.litimatica.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class LitimaticaMainScreen extends Screen {
    public LitimaticaMainScreen() {
        super(Text.literal("Litimatica Main Menu"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int startY = height / 2 - 50;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Schematic Browser"), button -> {
            this.client.setScreen(new SchematicBrowserScreen(this));
        }).dimensions(centerX - 100, startY, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Placement Configuration"), button -> {
            this.client.setScreen(new SchematicPlacementScreen(this));
        }).dimensions(centerX - 100, startY + 25, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("General Settings"), button -> {
            this.client.setScreen(new SettingsScreen(this));
        }).dimensions(centerX - 100, startY + 50, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§cCancel Building"), button -> {
            com.litimatica.schematic.CommandQueue.clear();
            if (this.client.player != null) this.client.player.sendMessage(Text.literal("§cBuild cancelled."), true);
        }).dimensions(centerX - 100, startY + 75, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> {
            this.close();
        }).dimensions(centerX - 100, startY + 110, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 80, 0xFFFFFF);
    }
}
