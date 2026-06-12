package com.litimatica.gui;

import com.litimatica.schematic.SchematicManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicBrowserScreen extends Screen {
    private final Screen parent;
    private final List<File> schematicFiles = new ArrayList<>();
    private int scrollOffset = 0;

    public SchematicBrowserScreen(Screen parent) {
        super(Text.literal("Schematic Browser"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        refreshFileList();

        int y = 40;
        int count = 0;
        for (File file : schematicFiles) {
            if (y + 20 > height - 40) {
                this.addDrawableChild(ButtonWidget.builder(Text.literal("... more files in folder"), button -> {})
                        .dimensions(width / 2 - 100, y, 200, 20).build()).active = false;
                break;
            }
            this.addDrawableChild(ButtonWidget.builder(Text.literal(file.getName()), button -> {
                if (SchematicManager.loadSchematic(file)) {
                    this.client.setScreen(new SchematicPlacementScreen(this));
                }
            }).dimensions(width / 2 - 100, y, 200, 20).build());
            y += 25;
            count++;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> {
            this.client.setScreen(parent);
        }).dimensions(width / 2 - 100, height - 30, 200, 20).build());
    }

    private void refreshFileList() {
        schematicFiles.clear();
        File dir = new File(MinecraftClient.getInstance().runDirectory, "schematics");
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".litematic") || name.endsWith(".schem") || name.endsWith(".schematic"));
        if (files != null) {
            for (File f : files) schematicFiles.add(f);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
