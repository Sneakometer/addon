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

    public static final String SERVER_ID = "7c9d5b0044c130109a5d7b5fb5c317c02b4e28c1";

}
