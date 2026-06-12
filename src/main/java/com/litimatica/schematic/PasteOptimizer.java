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
    public static List<String> generateCommands(Schematic schematic, BlockPos origin) {
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
                        BlockPos min = origin.add(box.min());
                        BlockPos max = origin.add(box.max());
                        String blockState = getBlockStateString(block);

                        if (box.volume() > 1) {
                            commands.add(String.format("/fill %d %d %d %d %d %d %s",
                                    min.getX(), min.getY(), min.getZ(),
                                    max.getX(), max.getY(), max.getZ(),
                                    blockState));
                        } else {
                            commands.add(String.format("/setblock %d %d %d %s",
                                    min.getX(), min.getY(), min.getZ(),
                                    blockState));
                        }
                    }
                }
            }
        }

        return commands;
    }

    private static String getBlockStateString(SchematicBlock block) {
        Map<String, String> states = block.states();
        if (states == null || states.isEmpty()) {
            return block.block();
        }
        String props = states.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
        return block.block() + "[" + props + "]";
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
