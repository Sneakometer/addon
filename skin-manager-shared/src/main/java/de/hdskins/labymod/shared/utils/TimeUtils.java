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

import javax.annotation.Nullable;
import java.util.Calendar;

public final class TimeUtils {

  private TimeUtils() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  public static String formatRemainingTime(long end) {
    final long current = System.currentTimeMillis();
    if (current < end) {
      final Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(end - current);

      final int year = calendar.get(Calendar.YEAR) - 1970;
      final int month = calendar.get(Calendar.MONTH);
      final int week = (calendar.get(Calendar.DAY_OF_MONTH) - 1) / 7;
      final int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
      final int hour = calendar.get(Calendar.HOUR_OF_DAY) - 1;
      final int minute = calendar.get(Calendar.MINUTE);
      final int second = calendar.get(Calendar.SECOND);

      String result = "";
      if (year > 0) {
        result += " " + year + " year" + (year > 1 ? "s" : "");
      }
      if (month > 0) {
        result += " " + month + " month" + (month > 1 ? "s" : "");
      }
      if (week > 0) {
        result += " " + week + " weeks" + (week > 1 ? "s" : "");
      }
      if (day > 0) {
        result += " " + day + " day" + (day > 1 ? "s" : "");
      }
      // hour:minutes:seconds
      result += " "
        + (hour <= 0 ? "00" : hour < 10 ? "0" + hour : hour)
        + ":" + (minute < 10 ? "0" + minute : minute)
        + ":" + (second < 10 ? "0" + second : second);
      return result;
    }
    return current == end ? "00" : null;
  }
}
