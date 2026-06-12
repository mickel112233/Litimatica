package com.litimatica;

import com.litimatica.gui.SchematicBrowserScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class LitimaticaClient implements ClientModInitializer {
    private static KeyBinding guiKey;
    private static int moveCooldown = 0;

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
                client.setScreen(new com.litimatica.gui.LitimaticaMainScreen());
            }

            if (client.player != null && client.player.getMainHandStack().isOf(Items.STICK)) {
                if (com.litimatica.schematic.LitimaticaSettings.placementLocked) {
                    client.player.sendMessage(Text.literal("§cPlacement Locked! Toggle in Settings."), true);
                    return;
                }

                if (moveCooldown > 0) {
                    moveCooldown--;
                } else {
                    boolean moved = false;
                    if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) {
                        com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.north();
                        moved = true;
                    }
                    if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) {
                        com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.south();
                        moved = true;
                    }
                    if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) {
                        com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.west();
                        moved = true;
                    }
                    if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) {
                        com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.east();
                        moved = true;
                    }
                    if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_PAGE_UP)) {
                        com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.up();
                        moved = true;
                    }
                    if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_PAGE_DOWN)) {
                        com.litimatica.gui.SchematicPlacementScreen.placementPos = com.litimatica.gui.SchematicPlacementScreen.placementPos.down();
                        moved = true;
                    }

                    if (moved) {
                        moveCooldown = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 5;
                        if (client.player != null) {
                            client.player.sendMessage(Text.literal("§bPos: " + com.litimatica.gui.SchematicPlacementScreen.placementPos.toShortString()), true);
                        }
                    }
                }
            } else {
                moveCooldown = 0;
            }

            com.litimatica.schematic.CommandQueue.onTick();
            com.litimatica.schematic.LitimaticaSettings.onTick();
        });
    }
}
