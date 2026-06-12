package com.litimatica.schematic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;

public class LitimaticaSettings {
    public static boolean hologramEnabled = true;
    public static boolean autoPlaceEnabled = false;
    public static int autoPlaceRange = 5;
    public static int autoPlaceDelay = 5; // ticks between placements
    public static boolean placementLocked = false;
    private static int lastPlaceTick = 0;

    public static void onTick() {
        if (!autoPlaceEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        lastPlaceTick++;
        if (lastPlaceTick < autoPlaceDelay) return;
        lastPlaceTick = 0;

        Schematic schematic = SchematicManager.getCurrentSchematic();
        if (schematic == null) return;

        BlockPos origin = com.litimatica.gui.SchematicPlacementScreen.placementPos;
        BlockPos playerPos = client.player.getBlockPos();

        // Scan nearby area for missing blocks
        for (int x = -autoPlaceRange; x <= autoPlaceRange; x++) {
            for (int y = -autoPlaceRange; y <= autoPlaceRange; y++) {
                for (int z = -autoPlaceRange; z <= autoPlaceRange; z++) {
                    BlockPos worldPos = playerPos.add(x, y, z);

                    // Convert world pos to schematic pos
                    int sx = worldPos.getX() - origin.getX();
                    int sy = worldPos.getY() - origin.getY();
                    int sz = worldPos.getZ() - origin.getZ();

                    // Rough check for bounds (ignores rotation/mirroring for simplicity in auto-place skeleton)
                    if (sx >= 0 && sx < schematic.width() && sy >= 0 && sy < schematic.height() && sz >= 0 && sz < schematic.length()) {
                        SchematicBlock sBlock = schematic.block(sx, sy, sz);
                        if (sBlock != null && !sBlock.block().equals("minecraft:air")) {
                            if (client.world.getBlockState(worldPos).isAir()) {
                                // Simple auto-place via command to ensure it works in both modes
                                String command = String.format("setblock %d %d %d %s", worldPos.getX(), worldPos.getY(), worldPos.getZ(), sBlock.block());
                                client.player.networkHandler.sendChatCommand(command);
                                return; // Place one block per delay interval
                            }
                        }
                    }
                }
            }
        }
    }
}
