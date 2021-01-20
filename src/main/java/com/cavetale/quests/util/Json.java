package com.cavetale.quests.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

public final class Json {
    public static final Gson GSON;
    public static final Gson PRETTY;
    public static final JsonParser PARSER = new JsonParser();

    private Json() { }

    static {
        GSON = builder().create();
        PRETTY = builder().setPrettyPrinting().create();
    }

    private static GsonBuilder builder() {
        return new GsonBuilder().disableHtmlEscaping();
    }

    public static <T> T load(final File file, Class<T> type, Supplier<T> dfl) {
        if (!file.exists()) {
            return dfl.get();
        }
        try (FileReader fr = new FileReader(file)) {
            return GSON.fromJson(fr, type);
        } catch (FileNotFoundException fnfr) {
            return dfl.get();
        } catch (IOException ioe) {
            throw new IllegalStateException("Loading " + file, ioe);
        }
    }

    public static <T> T load(final File file, Class<T> type) {
        return load(file, type, () -> null);
    }

    public static void save(final File file, Object obj, boolean pretty) {
        try (FileWriter fw = new FileWriter(file)) {
            Gson gs = pretty ? PRETTY : GSON;
            gs.toJson(obj, fw);
        } catch (IOException ioe) {
            throw new IllegalStateException("Saving " + file, ioe);
        }
    }

    public static void save(final File file, Object obj) {
        save(file, obj, false);
    }

    public static String serialize(Object obj) {
        return GSON.toJson(obj);
    }

    public static String prettyPrint(Object obj) {
        return PRETTY.toJson(obj);
    }

    public static <T> T deserialize(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T deserialize(String json, Class<T> type, Supplier<T> dfl) {
        if (json == null) return dfl.get();
        try {
            return GSON.fromJson(json, type);
        } catch (RuntimeException re) {
            System.err.println(json);
            re.printStackTrace();
            return dfl.get();
        }
    }
}
