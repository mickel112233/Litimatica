package com.litimatica.schematic;

import net.minecraft.client.MinecraftClient;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandQueue {
    private static final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private static int commandsPerSecond = 10; // Default requested by user
    private static double commandAccumulator = 0;

    public static void addCommands(Collection<String> commands) {
        queue.addAll(commands);
    }

    public static void onTick() {
        if (queue.isEmpty()) {
            commandAccumulator = 0;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            queue.clear();
            return;
        }

        // 20 ticks per second
        double commandsPerTick = (double) commandsPerSecond / 20.0;
        commandAccumulator += commandsPerTick;

        int toSend = (int) commandAccumulator;
        commandAccumulator -= toSend;

        for (int i = 0; i < toSend && !queue.isEmpty(); i++) {
            String command = queue.poll();
            if (command != null) {
                client.player.networkHandler.sendChatCommand(command.startsWith("/") ? command.substring(1) : command);
            }
        }
    }

    public static void setCommandsPerSecond(int count) {
        commandsPerSecond = Math.max(1, count);
    }

    public static int getCommandsPerSecond() {
        return commandsPerSecond;
    }

    public static void clear() {
        queue.clear();
    }
}
