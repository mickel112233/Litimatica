package com.litimatica.schematic;

import net.sandrohc.schematic4j.SchematicLoader;
import net.sandrohc.schematic4j.exception.ParsingException;
import net.sandrohc.schematic4j.schematic.Schematic;
import com.litimatica.Litimatica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SchematicManager {
    private static Schematic currentSchematic;
    private static String currentSchematicName;

    public static boolean loadSchematic(File file) {
        try {
            currentSchematic = SchematicLoader.load(file.toPath());
            currentSchematicName = file.getName();
            Litimatica.LOGGER.info("Successfully loaded schematic: {}", currentSchematicName);
            return true;
        } catch (IOException | ParsingException e) {
            Litimatica.LOGGER.error("Failed to load schematic: " + file.getName(), e);
            return false;
        }
    }

    public static Schematic getCurrentSchematic() {
        return currentSchematic;
    }

    public static String getCurrentSchematicName() {
        return currentSchematicName;
    }
}
