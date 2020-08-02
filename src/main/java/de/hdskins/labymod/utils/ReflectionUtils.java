package de.hdskins.labymod.utils;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }

    public static Field getFieldByNames(Class<?> clazz, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Field field = getFieldByName(clazz, fieldName);
            if (field != null) {
                return field;
            }
        }

        return null;
    }

    public static Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException exception) {
            return null;
        }
    }

    public static void set(Class<?> source, Object instance, Object newValue, String... fieldNames) {
        Field field = getFieldByNames(source, fieldNames);
        if (field == null) {
            return;
        }

        try {
            field.setAccessible(true);
            field.set(instance, newValue);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    public static <T> T get(Class<T> unused, Class<?> source, Object instance, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Field field = getFieldByName(source, fieldName);
            if (field != null) {
                return get(unused, field, instance);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> unused, Field field, Object instance) {
        try {
            if (field.isAccessible()) {
                return (T) field.get(instance);
            }

            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (IllegalAccessException exception) {
            return null;
        }
    }
}
