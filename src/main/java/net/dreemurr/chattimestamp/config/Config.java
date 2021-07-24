package net.dreemurr.chattimestamp.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dreemurr.chattimestamp.ChatTimeStamp;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final Map<String, ConfigEntry> entries = new HashMap<>();

    private static final File file = new File(FabricLoader.getInstance().getConfigDir().resolve("chatTimeStamp.json").toString());

    public static void initialize() {
        setDefaults();
        loadConfig();
        saveConfig();
    }

    public static void loadConfig() {
        try {
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                JsonObject json = new JsonParser().parse(br).getAsJsonObject();

                for (Map.Entry<String, ConfigEntry> entryMap : entries.entrySet()) {
                    ConfigEntry entry = entryMap.getValue();

                    try {
                        String jsonValue = json.getAsJsonPrimitive(entryMap.getKey()).getAsString();
                        entry.setValue(jsonValue);

                        if (entry.modValue != null)
                            entry.setValue(String.valueOf((Integer.parseInt(jsonValue) + (int) entry.modValue) % (int) entry.modValue));
                        else
                            entry.setValue(jsonValue);
                    } catch (Exception e) {
                        entry.value = entry.defaultValue;
                    }
                }

                br.close();
            }
        } catch (Exception e) {
            ChatTimeStamp.LOGGER.warn("Failed to load config file! Generating a new one...");
            e.printStackTrace();
            setDefaults();
            saveConfig();
        }
    }

    public static void saveConfig() {
        try {
            JsonObject config = new JsonObject();

            for (Map.Entry<String, ConfigEntry> entry : entries.entrySet()) {
                if (entry.getValue().value instanceof Number)
                    config.addProperty(entry.getKey(), (Number) entry.getValue().value);
                else if (entry.getValue().value instanceof Boolean)
                    config.addProperty(entry.getKey(), (boolean) entry.getValue().value);
                else
                    config.addProperty(entry.getKey(), String.valueOf(entry.getValue().value));
            }

            FileWriter fileWriter = new FileWriter(file);
            String jsonString = config.toString().replaceAll(":",": ").replaceAll(",",",\n  ").replaceAll("\\{","{\n  ").replaceAll("}","\n}");

            fileWriter.write(jsonString);
            fileWriter.close();
        } catch (Exception e) {
            ChatTimeStamp.LOGGER.error("Failed to save config file!");
            e.printStackTrace();
        }
    }

    public static void copyConfig() {
        entries.forEach((s, configEntry) -> configEntry.setValue(configEntry.configValue.toString()));
    }

    public static void discardConfig() {
        entries.forEach((s, configEntry) -> configEntry.configValue = configEntry.value);
    }

    public static void setDefaults() {
        entries.clear();
        entries.put("showSeconds", new ConfigEntry<>(false));
        entries.put("onMessage", new ConfigEntry<>(false));
        entries.put("twelveHour", new ConfigEntry<>(true));
        entries.put("enableClock", new ConfigEntry<>(true));
        entries.put("enableAntiSpam", new ConfigEntry<>(true));
    }

    public static class ConfigEntry<T> {
        public T value;
        public T defaultValue;
        public T configValue;
        public T modValue;

        public ConfigEntry(T value) {
            this(value, null);
        }

        public ConfigEntry(T value, T modValue) {
            this.value = value;
            this.defaultValue = value;
            this.configValue = value;
            this.modValue = modValue;
        }

        @SuppressWarnings("unchecked")
        private void setValue(String text) {
            try {
                if (value instanceof String)
                    value = (T) text;
                else if (value instanceof Boolean)
                    value = (T) Boolean.valueOf(text);
                else if (value instanceof Integer)
                    value = (T) Integer.valueOf(text);
                else if (value instanceof Float)
                    value = (T) Float.valueOf(text);
                else if (value instanceof Long)
                    value = (T) Long.valueOf(text);
                else if (value instanceof Double)
                    value = (T) Double.valueOf(text);
                else if (value instanceof Byte)
                    value = (T) Byte.valueOf(text);
                else if (value instanceof Short)
                    value = (T) Short.valueOf(text);
            } catch (Exception e) {
                value = defaultValue;
            }

            configValue = value;
        }
    }
}