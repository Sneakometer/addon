package de.hdskins.labymod.shared.gui;

import net.labymod.main.Source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GuidelineUtils {

    private static Collection<String> loaded;

    public static Collection<String> readGuidelines(String url) {
        if (loaded != null) {
            return loaded;
        }

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", Source.getUserAgent());

            try (InputStream inputStream = connection.getInputStream();
                 Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                Collection<String> lines = new ArrayList<>();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }

                return loaded = lines;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return Collections.emptyList();
    }

}
