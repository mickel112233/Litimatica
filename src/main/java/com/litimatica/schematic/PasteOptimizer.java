package com.litimatica.schematic;

import net.minecraft.util.math.BlockPos;
import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

public class PasteOptimizer {
    public static CompletableFuture<List<String>> generateCommandsAsync(Schematic schematic, BlockPos origin, int rotation, String mirror) {
        return CompletableFuture.supplyAsync(() -> generateCommands(schematic, origin, rotation, mirror));
    }

    public static List<String> generateCommands(Schematic schematic, BlockPos origin, int rotation, String mirror) {
        List<String> commands = new ArrayList<>();
        int width = schematic.width();
        int height = schematic.height();
        int length = schematic.length();

        // Safety cap for extremely large schematics to prevent OOM
        if ((long)width * height * length > 1000000000L) { // 1 Billion blocks
             // Log error or handle
             return commands;
        }

        BitSet visited = new BitSet(width * height * length);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    int index = (y * width * length) + (x * length) + z;
                    if (visited.get(index)) continue;

                    SchematicBlock block = schematic.block(x, y, z);
                    if (block == null || block.block().equals("minecraft:air")) {
                        visited.set(index);
                        continue;
                    }

                    int startX = x, startY = y, startZ = z;
                    int maxX = x, maxY = y, maxZ = z;

                    // Expand X
                    for (int ex = x + 1; ex < width; ex++) {
                        int eidx = (y * width * length) + (ex * length) + z;
                        if (!visited.get(eidx) && isSameBlock(schematic.block(ex, y, z), block)) {
                            if ((ex - x + 1) > 32) break; // Limit X to keep commands short or reasonable
                            maxX = ex;
                        } else break;
                    }

                    // Expand Z
                    outer:
                    for (int ez = z + 1; ez < length; ez++) {
                        for (int ex = x; ex <= maxX; ex++) {
                            int eidx = (y * width * length) + (ex * length) + ez;
                            if (visited.get(eidx) || !isSameBlock(schematic.block(ex, y, ez), block)) {
                                break outer;
                            }
                        }
                        if ((maxX - x + 1) * (ez - z + 1) > 32768) break;
                        maxZ = ez;
                    }

                    // Expand Y
                    outer:
                    for (int ey = y + 1; ey < height; ey++) {
                        for (int ex = x; ex <= maxX; ex++) {
                            for (int ez = z; ez <= maxZ; ez++) {
                                int eidx = (ey * width * length) + (ex * length) + ez;
                                if (visited.get(eidx) || !isSameBlock(schematic.block(ex, ey, ez), block)) {
                                    break outer;
                                }
                            }
                        }
                        if ((long)(maxX - x + 1) * (maxZ - z + 1) * (ey - y + 1) > 32768) break;
                        maxY = ey;
                    }

                    // Mark visited
                    for (int vx = x; vx <= maxX; vx++) {
                        for (int vy = y; vy <= maxY; vy++) {
                            for (int vz = z; vz <= maxZ; vz++) {
                                visited.set((vy * width * length) + (vx * length) + vz);
                            }
                        }
                    }

                    // Transform and add command
                    BlockPos minTrans = transform(new BlockPos(x, y, z), schematic, rotation, mirror);
                    BlockPos maxTrans = transform(new BlockPos(maxX, maxY, maxZ), schematic, rotation, mirror);

                    int rx1 = Math.min(minTrans.getX(), maxTrans.getX());
                    int ry1 = Math.min(minTrans.getY(), maxTrans.getY());
                    int rz1 = Math.min(minTrans.getZ(), maxTrans.getZ());
                    int rx2 = Math.max(minTrans.getX(), maxTrans.getX());
                    int ry2 = Math.max(minTrans.getY(), maxTrans.getY());
                    int rz2 = Math.max(minTrans.getZ(), maxTrans.getZ());

                    String stateStr = getBlockStateString(block, rotation, mirror);
                    if (rx1 == rx2 && ry1 == ry2 && rz1 == rz2) {
                        commands.add(String.format("setblock %d %d %d %s", origin.getX() + rx1, origin.getY() + ry1, origin.getZ() + rz1, stateStr));
                    } else {
                        commands.add(String.format("fill %d %d %d %d %d %d %s",
                            origin.getX() + rx1, origin.getY() + ry1, origin.getZ() + rz1,
                            origin.getX() + rx2, origin.getY() + ry2, origin.getZ() + rz2,
                            stateStr));
                    }
                }
            }
        }

        return commands;
    }

    private static BlockPos transform(BlockPos pos, Schematic schematic, int rotation, String mirror) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (mirror.equals("X")) x = schematic.width() - 1 - x;
        else if (mirror.equals("Z")) z = schematic.length() - 1 - z;

        int tx = x, tz = z;
        if (rotation == 90) { tx = schematic.length() - 1 - z; tz = x; }
        else if (rotation == 180) { tx = schematic.width() - 1 - x; tz = schematic.length() - 1 - z; }
        else if (rotation == 270) { tx = z; tz = schematic.width() - 1 - x; }
        return new BlockPos(tx, y, tz);
    }

    private static String getBlockStateString(SchematicBlock block, int rotation, String mirror) {
        Map<String, String> states = block.states();
        if (states == null || states.isEmpty()) return block.block();
        Map<String, String> newStates = states.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getKey().equals("facing") ? rotateFacing(e.getValue(), rotation, mirror) : e.getValue()));
        String props = newStates.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(","));
        return block.block() + "[" + props + "]";
    }

    private static String rotateFacing(String facing, int rotation, String mirror) {
        String[] directions = {"north", "east", "south", "west"};
        int idx = -1;
        for (int i = 0; i < directions.length; i++) if (directions[i].equals(facing)) { idx = i; break; }
        if (idx == -1) return facing;
        if (mirror.equals("X") && (facing.equals("west") || facing.equals("east"))) idx = (idx + 2) % 4;
        if (mirror.equals("Z") && (facing.equals("north") || facing.equals("south"))) idx = (idx + 2) % 4;
        idx = (idx + (rotation / 90)) % 4;
        return directions[idx];
    }

    private static boolean isSameBlock(SchematicBlock b1, SchematicBlock b2) {
        if (b1 == null || b2 == null) return false;
        return b1.block().equals(b2.block()) && b1.states().equals(b2.states());
    }
}
