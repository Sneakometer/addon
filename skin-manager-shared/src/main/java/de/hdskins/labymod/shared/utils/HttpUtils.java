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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;

public final class HttpUtils {

  private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig()).build();

  private HttpUtils() {
    throw new UnsupportedOperationException();
  }

  public static <T> T doGet(@Nonnull String requestUrl, int expectedResponseCode, @Nonnull Function1T<HttpResponse, T, IOException> resultMapper, T defaultValue) {
    try {
      final HttpResponse response = HTTP_CLIENT.execute(new HttpGet(requestUrl));
      if (response.getStatusLine().getStatusCode() == expectedResponseCode) {
        return resultMapper.apply(response);
      }
      return defaultValue;
    } catch (IOException exception) {
      exception.printStackTrace();
      return defaultValue;
    }
  }

  @Nonnull
  private static RequestConfig createRequestConfig() {
    return RequestConfig.custom()
      .setConnectTimeout(5000)
      .setConnectionRequestTimeout(5000)
      .setSocketTimeout(5000)
      .build();
  }

  @FunctionalInterface
  public interface Function1T<I, O, T extends Throwable> {
    O apply(I i) throws T;
  }
}
