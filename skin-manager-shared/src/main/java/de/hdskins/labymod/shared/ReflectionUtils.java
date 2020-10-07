/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 HD-Skins <https://github.com/HDSkins>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.hdskins.labymod.shared;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ReflectionUtils {

    private static final Field MODIFIERS_FIELD;

    static {
        try {
            MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
            MODIFIERS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            // unreachable code
            throw new RuntimeException(exception);
        }
    }

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

        set(instance, newValue, field);
    }

    public static void set(Object instance, Object newValue, Field field) {
        try {
            if (MODIFIERS_FIELD != null && Modifier.isFinal(field.getModifiers())) {
                MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }

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
