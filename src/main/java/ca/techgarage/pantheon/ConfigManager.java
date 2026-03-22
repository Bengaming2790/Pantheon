package ca.techgarage.pantheon;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;

public class ConfigManager {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getGameDir()
            .resolve("config")
            .resolve("pantheon.yaml");

    public static void load(Class<?> configClass) {
        try {
            Map<String, String> existing = Files.exists(CONFIG_PATH)
                    ? readFile()
                    : Collections.emptyMap();


            for (Field field : declaredStaticFields(configClass)) {
                field.setAccessible(true);
                String key = field.getName();
                String raw = existing.get(key);

                if (raw != null) {
                    applyValue(field, raw);
                }
            }

            writeFile(configClass);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyValue(Field field, String raw) {
        try {
            Class<?> type = field.getType();
            if      (type == boolean.class) field.setBoolean(null, Boolean.parseBoolean(raw));
            else if (type == int.class)     field.setInt    (null, Integer.parseInt(raw));
            else if (type == double.class)  field.setDouble (null, Double.parseDouble(raw));
            else if (type == float.class)   field.setFloat  (null, Float.parseFloat(raw));
            else if (type == long.class)    field.setLong   (null, Long.parseLong(raw));
            else if (type == String.class)  field.set       (null, raw);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            System.err.println("[Pantheon] Could not parse value for '" + field.getName()
                    + "': \"" + raw + "\" — using default.");
        }
    }

    private static void writeFile(Class<?> configClass) throws IOException, IllegalAccessException {
        Files.createDirectories(CONFIG_PATH.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            for (Field field : declaredStaticFields(configClass)) {
                field.setAccessible(true);

                Comment comment = field.getAnnotation(Comment.class);
                String commentText = (comment != null) ? comment.value() : field.getName();

                writer.write("# " + commentText);
                writer.newLine();
                writer.write(field.getName() + ": " + field.get(null));
                writer.newLine();
                writer.newLine();
            }
        }
    }

    private static Map<String, String> readFile() throws IOException {
        // LinkedHashMap preserves file order, though we don't rely on it here
        Map<String, String> map = new LinkedHashMap<>();

        for (String line : Files.readAllLines(CONFIG_PATH)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || !line.contains(":")) continue;

            String[] parts = line.split(":", 2);
            map.put(parts[0].trim(), parts[1].trim());
        }

        return map;
    }

    /** Returns only the public static fields in declaration order. */
    private static List<Field> declaredStaticFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                result.add(f);
            }
        }
        return result;
    }
}