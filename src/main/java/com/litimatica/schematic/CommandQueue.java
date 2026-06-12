package com.litimatica.schematic;

import net.minecraft.client.MinecraftClient;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CommandQueue {
    private static final Queue<String> queue = new LinkedList<>();
    private static int commandsPerTick = 10;

    public static void addCommands(List<String> commands) {
        queue.addAll(commands);
    }

    public static void onTick() {
        if (queue.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            queue.clear();
            return;
        }

        for (int i = 0; i < commandsPerTick && !queue.isEmpty(); i++) {
            String command = queue.poll();
            if (command != null) {
                client.player.networkHandler.sendChatCommand(command.startsWith("/") ? command.substring(1) : command);
            }
        }
    }

    public static void setCommandsPerTick(int count) {
        commandsPerTick = Math.max(1, count);
    }

    public static int getCommandsPerTick() {
        return commandsPerTick;
    }

    public static void clear() {
        queue.clear();
    }
}
