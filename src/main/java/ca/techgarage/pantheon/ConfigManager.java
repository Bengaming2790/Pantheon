package ca.techgarage.pantheon;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getGameDir()
            .resolve("config")
            .resolve("pantheon.yaml");

    public static void load(Class<?> configClass) {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                createDefault(configClass);
            }

            Map<String, String> values = readFile();

            boolean updated = false;

            for (Field field : configClass.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;

                String key = field.getName();

                field.setAccessible(true);

                // If key missing → append to file
                if (!values.containsKey(key)) {
                    appendMissingField(field);
                    updated = true;
                    continue;
                }

                // Otherwise load existing value
                String raw = values.get(key);
                Class<?> type = field.getType();

                if (type == boolean.class) {
                    field.setBoolean(null, Boolean.parseBoolean(raw));
                } else if (type == int.class) {
                    field.setInt(null, Integer.parseInt(raw));
                } else if (type == double.class) {
                    field.setDouble(null, Double.parseDouble(raw));
                } else if (type == String.class) {
                    field.set(null, raw);
                }
            }

            // If we added new values reload so everything is consistent
            if (updated) {
                load(configClass);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDefault(Class<?> configClass) throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {

            for (Field field : configClass.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;

                field.setAccessible(true);

                // Write placeholder comment
                writer.write("# " + field.getName());
                writer.newLine();

                Object value = field.get(null);

                writer.write(field.getName() + ": " + value);
                writer.newLine();
                writer.newLine();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> readFile() throws IOException {
        Map<String, String> map = new HashMap<>();

        List<String> lines = Files.readAllLines(CONFIG_PATH);

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;

            if (!line.contains(":")) continue;

            String[] split = line.split(":", 2);

            String key = split[0].trim();
            String value = split[1].trim();

            map.put(key, value);
        }

        return map;
    }

    private static void appendMissingField(Field field) throws IOException, IllegalAccessException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                CONFIG_PATH,
                java.nio.file.StandardOpenOption.APPEND
        )) {

            writer.newLine();

            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                writer.write("# " + comment.value());
                writer.newLine();
            }

            Object value = field.get(null);
            writer.write(field.getName() + ": " + value);
            writer.newLine();
        }
    }
}
