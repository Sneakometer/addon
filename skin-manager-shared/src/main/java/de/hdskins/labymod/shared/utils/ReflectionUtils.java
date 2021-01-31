/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 - 2021 HD-Skins <https://github.com/HDSkins>
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
package de.hdskins.labymod.shared.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

  @Nullable
  public static Field getFieldByNames(@Nonnull Class<?> clazz, @Nonnull String... fieldNames) {
    for (String fieldName : fieldNames) {
      Field field = getFieldByName(clazz, fieldName);
      if (field != null) {
        return field;
      }
    }

    return null;
  }

  @Nullable
  public static Field getFieldByName(@Nonnull Class<?> clazz, @Nonnull String fieldName) {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      return field;
    } catch (NoSuchFieldException exception) {
      return null;
    }
  }

  public static void set(@Nonnull Class<?> source, @Nonnull Object instance, @Nullable Object newValue, @Nonnull String... fieldNames) {
    Field field = getFieldByNames(source, fieldNames);
    if (field != null) {
      set(instance, newValue, field);
    }
  }

  public static void set(@Nonnull Object instance, @Nullable Object newValue, @Nonnull Field field) {
    try {
      if (MODIFIERS_FIELD != null && Modifier.isFinal(field.getModifiers())) {
        MODIFIERS_FIELD.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      }

      if (!field.isAccessible()) {
        field.setAccessible(true);
      }

      field.set(instance, newValue);
    } catch (IllegalAccessException exception) {
      exception.printStackTrace();
    }
  }

  @Nullable
  public static <T> T get(@Nonnull Class<T> unused, @Nonnull Class<?> source, @Nonnull Object instance, @Nonnull String... fieldNames) {
    for (String fieldName : fieldNames) {
      Field field = getFieldByName(source, fieldName);
      if (field != null) {
        return get(unused, field, instance);
      }
    }
    return null;
  }

  @Nullable
  @SuppressWarnings({"unchecked", "unused"})
  public static <T> T get(@Nonnull Class<T> unused, @Nonnull Field field, @Nonnull Object instance) {
    try {
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      return (T) field.get(instance);
    } catch (IllegalAccessException exception) {
      return null;
    }
  }
}
