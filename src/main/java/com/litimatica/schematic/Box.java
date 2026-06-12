package com.litimatica.schematic;

import net.minecraft.util.math.BlockPos;

public record Box(BlockPos min, BlockPos max, String blockId) {
    public int volume() {
        return (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
    }
}
