package net.resoucebundle.config;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.*;
import java.util.function.Function;

public class ConfigurableClassResourceBundle<T> extends ResourceBundle {
    public static class Control<T> extends ResourceBundle.Control {
        private final String suffix;
        private final Function<InputStream, T> converter;

        public Control(String suffix, Function<InputStream, T> converter) {
            this.suffix = suffix;
            this.converter = converter;
        }
        @Override
        public List<String> getFormats(String baseName) {
            return List.of(suffix);
        }
        @Override
        public ConfigurableClassResourceBundle<T> newBundle(
                String baseName,
                Locale locale,
                String format,
                ClassLoader loader,
                boolean reload
        ) throws IOException {

            if (!format.equals(this.suffix)) {
                throw new IllegalArgumentException("Expected format '" + this.suffix + "' but got '" + format + "'");
            }
            String resourceName = toResourceName(toBundleName(baseName, locale), format);
            InputStream inputStream = null;
            if (reload) {
                try {
                    URLConnection conn = Objects.requireNonNull(loader.getResource(resourceName)).openConnection();
                    conn.setUseCaches(false);
                    inputStream = conn.getInputStream();
                } catch (NullPointerException e) {
                    // do nothing
                }
            } else {
                try {
                    inputStream = loader.getResourceAsStream(resourceName);
                } catch (NullPointerException e) {
                    // do nothing
                }
            }
            if (inputStream != null) {
                try {
                    return new ConfigurableClassResourceBundle<>(this.converter.apply(inputStream));
                } finally {
                    inputStream.close();
                }
            } else {
                return null;
            }
        }
    }
    private static final String KEY = "KEY";

    private final T value;
    protected ConfigurableClassResourceBundle(T value) {
        this.value = value;
    }

    @Override
    protected T handleGetObject(String key) {
        if (key.equals(KEY)) return value;
        else return null;
    }

    @Override
    @Nonnull
    public Enumeration<String> getKeys() {
        return Collections.enumeration(List.of(KEY));
    }

    public T getValue() {
        return value;
    }

    public static <T> ConfigurableClassResourceBundle<T> getBundle(
            String baseName,
            Locale locale,
            ClassLoader loader,
            ConfigurableClassResourceBundle.Control<T> control)
            throws NullPointerException, MissingResourceException {
        @SuppressWarnings("unchecked")
        ConfigurableClassResourceBundle<T> bundle =
                (ConfigurableClassResourceBundle<T>) ResourceBundle.getBundle(baseName, locale, loader, control);
        return bundle;
    }

    public static <T> ConfigurableClassResourceBundle<T> getBundle(
            String baseName,
            ClassLoader loader,
            ConfigurableClassResourceBundle.Control<T> control)
            throws NullPointerException, MissingResourceException {
        return getBundle(baseName, Locale.getDefault(), loader, control);
    }
}