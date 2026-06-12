package com.litimatica.schematic;

import net.sandrohc.schematic4j.SchematicLoader;
import net.sandrohc.schematic4j.exception.ParsingException;
import net.sandrohc.schematic4j.schematic.Schematic;
import com.litimatica.Litimatica;
import com.litimatica.gui.SchematicRenderer;

import java.io.File;
import java.io.IOException;

public class SchematicManager {
    private static Schematic currentSchematic;
    private static String currentSchematicName;

    public static boolean loadSchematic(File file) {
        try {
            // Reset existing state before loading new one
            currentSchematic = null;
            currentSchematicName = null;
            CommandQueue.clear();
            SchematicRenderer.clearCache();

            currentSchematic = SchematicLoader.load(file.toPath());
            currentSchematicName = file.getName();
            Litimatica.LOGGER.info("Successfully loaded schematic: {}", currentSchematicName);
            return true;
        } catch (IOException | ParsingException e) {
            Litimatica.LOGGER.error("Failed to load schematic: " + file.getName(), e);
            return false;
        }
    }

    public static void unload() {
        currentSchematic = null;
        currentSchematicName = null;
        CommandQueue.clear();
        SchematicRenderer.clearCache();
    }

    public static Schematic getCurrentSchematic() {
        return currentSchematic;
    }

    public static String getCurrentSchematicName() {
        return currentSchematicName;
    }
}
