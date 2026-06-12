package com.litimatica;

import com.litimatica.gui.SchematicBrowserScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class LitimaticaClient implements ClientModInitializer {
    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.litimatica.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.litimatica"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                client.setScreen(new SchematicBrowserScreen(null));
            }

            if (client.player != null && client.player.getMainHandStack().isOf(Items.STICK)) {
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) {
                    com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.north();
                }
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) {
                    com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.south();
                }
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) {
                    com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.west();
                }
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) {
                    com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.east();
                }
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_PAGE_UP)) {
                    com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.up();
                }
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_PAGE_DOWN)) {
                    com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.down();
                }
            }

            com.litimatica.schematic.CommandQueue.onTick();
        });
    }
}
