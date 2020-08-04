package de.hdskins.labymod.shared.utils;

import com.github.derklaro.requestbuilder.RequestBuilder;
import com.github.derklaro.requestbuilder.method.RequestMethod;
import com.github.derklaro.requestbuilder.result.RequestResult;
import com.github.derklaro.requestbuilder.result.http.StatusCode;
import com.github.derklaro.requestbuilder.types.MimeTypes;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import net.labymod.main.LabyMod;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class ServerHelper {

    private ServerHelper() {
        throw new UnsupportedOperationException();
    }

    public static StatusCode uploadToServer(Path path, MinecraftAdapter minecraftAdapter, ConfigObject config) {
        if (config.getServerUrl() == null) {
            return StatusCode.NOT_ACCEPTABLE;
        }

        try (RequestResult requestResult = RequestBuilder.newBuilder(config.getServerUrl() + "/uploadSkin")
                .setRequestMethod(RequestMethod.PUT)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("session", minecraftAdapter.getSessionId())
                .setMimeType(MimeTypes.getMimeType("png"))
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .disableCaches()
                .enableOutput()
                .fireAndForget()
        ) {
            byte[] bytes = Files.readAllBytes(path);
            try (OutputStream outputStream = requestResult.getOutputStream()) {
                outputStream.write(bytes);
                outputStream.flush();
            }

            return requestResult.getStatus();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return StatusCode.INTERNAL_SERVER_ERROR;
    }

    private static String getUndashedPlayerUniqueId() {
        return LabyMod.getInstance().getPlayerUUID().toString().replace("-", "");
    }
}