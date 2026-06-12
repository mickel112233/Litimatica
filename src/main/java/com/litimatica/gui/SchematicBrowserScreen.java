package com.litimatica.gui;

import com.litimatica.schematic.SchematicManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicBrowserScreen extends Screen {
    private final Screen parent;
    private SchematicListWidget list;

    public SchematicBrowserScreen(Screen parent) {
        super(Text.literal("Schematic Browser"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new SchematicListWidget(this.client, this.width, this.height - 80, 40, 25);
        this.addSelectableChild(this.list);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Load Selected"), button -> {
            SchematicListWidget.Entry entry = this.list.getSelectedOrNull();
            if (entry != null) {
                if (SchematicManager.loadSchematic(entry.file)) {
                    this.client.setScreen(new SchematicPlacementScreen(this));
                }
            }
        }).dimensions(this.width / 2 - 102, this.height - 35, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> {
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 + 2, this.height - 35, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.list.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    class SchematicListWidget extends AlwaysSelectedEntryListWidget<SchematicListWidget.Entry> {
        public SchematicListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);

            File dir = new File(client.runDirectory, "schematics");
            if (!dir.exists()) dir.mkdirs();
            File[] files = dir.listFiles((d, name) -> name.endsWith(".litematic") || name.endsWith(".schem") || name.endsWith(".schematic"));
            if (files != null) {
                for (File f : files) {
                    this.addEntry(new Entry(f));
                }
            }
        }

        class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            final File file;

            public Entry(File file) {
                this.file = file;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawCenteredTextWithShadow(textRenderer, file.getName(), x + entryWidth / 2, y + 5, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                SchematicListWidget.this.setSelected(this);
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public Text getNarration() {
                return Text.literal(file.getName());
            }
        }
    }
}
