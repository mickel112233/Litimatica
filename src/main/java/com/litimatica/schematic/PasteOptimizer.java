package com.litimatica.schematic;

import net.minecraft.util.math.BlockPos;
import net.sandrohc.schematic4j.schematic.Schematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PasteOptimizer {
    public static List<String> generateCommands(Schematic schematic, BlockPos origin, int rotation, String mirror) {
        List<String> commands = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();

        for (int y = 0; y < schematic.height(); y++) {
            for (int x = 0; x < schematic.width(); x++) {
                for (int z = 0; z < schematic.length(); z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    if (visited.contains(currentPos)) continue;

                    SchematicBlock block = schematic.block(x, y, z);
                    if (block == null || block.block().equals("minecraft:air")) {
                        visited.add(currentPos);
                        continue;
                    }

                    Box box = findMaxBox(schematic, x, y, z, visited);
                    if (box != null) {
                        // Apply rotation and mirroring to box coordinates
                        BlockPos min = transform(box.min(), schematic, rotation, mirror);
                        BlockPos max = transform(box.max(), schematic, rotation, mirror);

                        // Correct min/max for /fill after transformation
                        int x1 = Math.min(min.getX(), max.getX());
                        int y1 = Math.min(min.getY(), max.getY());
                        int z1 = Math.min(min.getZ(), max.getZ());
                        int x2 = Math.max(min.getX(), max.getX());
                        int y2 = Math.max(min.getY(), max.getY());
                        int z2 = Math.max(min.getZ(), max.getZ());

                        BlockPos realMin = origin.add(x1, y1, z1);
                        BlockPos realMax = origin.add(x2, y2, z2);

                        String blockState = getBlockStateString(block, rotation, mirror);

                        if (box.volume() > 1) {
                            commands.add(String.format("/fill %d %d %d %d %d %d %s",
                                    realMin.getX(), realMin.getY(), realMin.getZ(),
                                    realMax.getX(), realMax.getY(), realMax.getZ(),
                                    blockState));
                        } else {
                            commands.add(String.format("/setblock %d %d %d %s",
                                    realMin.getX(), realMin.getY(), realMin.getZ(),
                                    blockState));
                        }
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

        // Mirror
        if (mirror.equals("X")) x = schematic.width() - 1 - x;
        else if (mirror.equals("Z")) z = schematic.length() - 1 - z;

        // Rotation (around origin 0,0,0 relative to schematic)
        int tx = x;
        int tz = z;
        if (rotation == 90) {
            tx = schematic.length() - 1 - z;
            tz = x;
        } else if (rotation == 180) {
            tx = schematic.width() - 1 - x;
            tz = schematic.length() - 1 - z;
        } else if (rotation == 270) {
            tx = z;
            tz = schematic.width() - 1 - x;
        }

        return new BlockPos(tx, y, tz);
    }

    private static String getBlockStateString(SchematicBlock block, int rotation, String mirror) {
        Map<String, String> states = block.states();
        if (states == null || states.isEmpty()) {
            return block.block();
        }

        // Simple rotation/mirroring for 'facing' property
        Map<String, String> newStates = states.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    String key = e.getKey();
                    String val = e.getValue();
                    if (key.equals("facing")) {
                        return rotateFacing(val, rotation, mirror);
                    }
                    return val;
                }));

        String props = newStates.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
        return block.block() + "[" + props + "]";
    }

    private static String rotateFacing(String facing, int rotation, String mirror) {
        // This is a simplified implementation. Full block state rotation is complex.
        String[] directions = {"north", "east", "south", "west"};
        int idx = -1;
        for (int i = 0; i < directions.length; i++) {
            if (directions[i].equals(facing)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return facing;

        if (mirror.equals("X") && (facing.equals("west") || facing.equals("east"))) idx = (idx + 2) % 4;
        if (mirror.equals("Z") && (facing.equals("north") || facing.equals("south"))) idx = (idx + 2) % 4;

        idx = (idx + (rotation / 90)) % 4;
        return directions[idx];
    }

    private static Box findMaxBox(Schematic schematic, int startX, int startY, int startZ, Set<BlockPos> visited) {
        SchematicBlock startBlock = schematic.block(startX, startY, startZ);
        if (startBlock == null) return null;

        int maxX = startX, maxY = startY, maxZ = startZ;

        // Expand X
        for (int x = startX; x < schematic.width(); x++) {
            SchematicBlock b = schematic.block(x, startY, startZ);
            if (isSameBlock(b, startBlock) && !visited.contains(new BlockPos(x, startY, startZ))) {
                if ((x - startX + 1) > 32768) break;
                maxX = x;
            } else break;
        }

        // Expand Z
        outer:
        for (int z = startZ + 1; z < schematic.length(); z++) {
            for (int x = startX; x <= maxX; x++) {
                SchematicBlock b = schematic.block(x, startY, z);
                if (!isSameBlock(b, startBlock) || visited.contains(new BlockPos(x, startY, z))) {
                    break outer;
                }
            }
            if ((long)(maxX - startX + 1) * (z - startZ + 1) > 32768) break;
            maxZ = z;
        }

        // Expand Y
        outer:
        for (int y = startY + 1; y < schematic.height(); y++) {
            for (int x = startX; x <= maxX; x++) {
                for (int z = startZ; z <= maxZ; z++) {
                    SchematicBlock b = schematic.block(x, y, z);
                    if (!isSameBlock(b, startBlock) || visited.contains(new BlockPos(x, y, z))) {
                        break outer;
                    }
                }
            }
            if ((long)(maxX - startX + 1) * (maxZ - startZ + 1) * (y - startY + 1) > 32768) break;
            maxY = y;
        }

        // Mark as visited
        for (int x = startX; x <= maxX; x++) {
            for (int y = startY; y <= maxY; y++) {
                for (int z = startZ; z <= maxZ; z++) {
                    visited.add(new BlockPos(x, y, z));
                }
            }
        }

        return new Box(new BlockPos(startX, startY, startZ), new BlockPos(maxX, maxY, maxZ), startBlock.block());
    }

    private static boolean isSameBlock(SchematicBlock b1, SchematicBlock b2) {
        if (b1 == null || b2 == null) return false;
        if (!b1.block().equals(b2.block())) return false;
        return b1.states().equals(b2.states());
    }
}
