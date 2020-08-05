package de.hdskins.labymod.shared;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException();
    }

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public static String aboutMcVersion = "unknown";
    public static final String LOG_PREFIX = "[HD-Skins] ";
}
