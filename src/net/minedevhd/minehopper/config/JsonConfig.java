package net.minedevhd.minehopper.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("unchecked")
public final class JsonConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Object> DATA = new HashMap<>();
    private static File file;

    public static void init(JavaPlugin plugin, String fileName) {
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
                saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadFile();
        }
    }
    
    public static File getFile() {
        return file;
    }

    public static Set<String> getAllKeys() {
        return DATA.keySet();
    }

    public static void deleteSection(String prefix) {
        DATA.keySet().removeIf(k -> k.startsWith(prefix));
        saveFile();
    }

    public static void save(String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = DATA;
        
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new HashMap<>());
        }
        current.put(parts[parts.length - 1], value);
        saveFile();
    }

    public static <T> T get(String key, Class<T> type) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = DATA;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                return null;
            }
            current = (Map<String, Object>) next;
        }

        Object value = current.get(parts[parts.length - 1]);
        if (value == null) return null;

        return GSON.fromJson(GSON.toJson(value), type);
    }

    public static String asString() {
        return GSON.toJson(DATA);
    }

    private static void loadFile() {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			Map<String, Object> map = GSON.fromJson(reader, Map.class);
            if (map != null) DATA.putAll(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveFile() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(DATA, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean exists(String key) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = DATA;
        
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) return false;
            current = (Map<String, Object>) next;
        }
        return current.containsKey(parts[parts.length - 1]);
    }
    
    public static void delete(String key) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = DATA;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                return; // Key existiert nicht vollst§ndig
            }
            current = (Map<String, Object>) next;
        }

        current.remove(parts[parts.length - 1]);
        saveFile();
    }
    
    public static String compLocation(Location location) {
    	return location.getBlockX() + ";" + 
    		   location.getBlockY() + ";" + 
    		   location.getBlockZ();
    }
    
    public static Set<String> getSectionKeys(String path) {
        Object node = get(path, Object.class);
        if (node instanceof Map) return ((Map<String, Object>) node).keySet();
        return new java.util.HashSet<>();
    }
    
}
