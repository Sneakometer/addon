package de.hdskins.labymod.shared.utils;

import com.github.derklaro.requestbuilder.RequestBuilder;
import com.github.derklaro.requestbuilder.method.RequestMethod;
import com.github.derklaro.requestbuilder.result.RequestResult;
import com.github.derklaro.requestbuilder.result.http.StatusCode;
import com.github.derklaro.requestbuilder.types.MimeTypes;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import net.labymod.main.LabyMod;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class ServerHelper {

    private ServerHelper() {
        throw new UnsupportedOperationException();
    }

    public static ServerResult uploadToServer(Path path, MinecraftAdapter minecraftAdapter, ConfigObject config) {
        if (config.getServerUrl() == null) {
            return ServerResult.of(StatusCode.NOT_ACCEPTABLE, "No server url in the config");
        }

        if (!minecraftAdapter.authorize()) {
            return ServerResult.of(StatusCode.FORBIDDEN, "Authorization failed");
        }
        RequestBuilder builder = RequestBuilder.newBuilder(config.getServerUrl() + "/uploadSkin")
                .requestMethod(RequestMethod.PUT)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .mimeType(MimeTypes.getMimeType("png"))
                .connectTimeout(5, TimeUnit.SECONDS)
                .disableCaches()
                .enableOutput();
        if (config.getToken() != null) {
            builder.addHeader("token", config.getToken());
        }

        try (RequestResult requestResult = builder.fireAndForget()) {
            byte[] bytes = Files.readAllBytes(path);
            try (OutputStream outputStream = requestResult.getOutputStream()) {
                outputStream.write(bytes);
                outputStream.flush();
            }

            return ServerResult.of(requestResult);
        } catch (final Exception ex) {
            ex.printStackTrace();
            return ServerResult.of(ex);
        }
    }

    public static ServerResult reportSkin(PlayerProfile reportedPlayer, MinecraftAdapter minecraftAdapter, ConfigObject config) {
        if (config.getServerUrl() == null) {
            return ServerResult.of(StatusCode.NOT_ACCEPTABLE, "No server url in the config");
        }

        if (!minecraftAdapter.authorize()) {
            return ServerResult.of(StatusCode.FORBIDDEN, "Authorization failed");
        }
        RequestBuilder builder = RequestBuilder.newBuilder(config.getServerUrl() + "/reportSkin")
                .setRequestMethod(RequestMethod.POST)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .addHeader("reportedUniqueId", reportedPlayer.getUniqueId().toString().replace("-", ""))
                .addHeader("reportedName", reportedPlayer.getName())
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();
        if (config.getToken() != null) {
            builder.addHeader("token", config.getToken());
        }

        return ServerResult.of(builder);
    }

    public static ServerResult deleteSkin(MinecraftAdapter minecraftAdapter, ConfigObject config) {
        if (config.getServerUrl() == null) {
            return ServerResult.of(StatusCode.NOT_ACCEPTABLE, "No server url in the config");
        }

        if (!minecraftAdapter.authorize()) {
            return ServerResult.of(StatusCode.FORBIDDEN, "Authorization failed");
        }
        RequestBuilder builder = RequestBuilder.newBuilder(config.getServerUrl() + "/deleteSkin")
                .setRequestMethod(RequestMethod.DELETE)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();
        if (config.getToken() != null) {
            builder.addHeader("token", config.getToken());
        }

        return ServerResult.of(builder);
    }

    public static boolean isSlim(ConfigObject configObject) {
        if (configObject.getServerUrl() == null) {
            return false;
        }

        RequestBuilder builder = RequestBuilder.newBuilder(configObject.getServerUrl() + "/isSlim")
                .setRequestMethod(RequestMethod.GET)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();
        if (configObject.getToken() != null) {
            builder.addHeader("token", configObject.getToken());
        }

        try (RequestResult requestResult = builder.fireAndForget()) {
            return requestResult.getStatusCode() == 200;
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static ServerResult setSlim(MinecraftAdapter minecraftAdapter, ConfigObject config, boolean slim) {
        if (config.getServerUrl() == null) {
            return ServerResult.of(StatusCode.NOT_ACCEPTABLE, "No server url in the config");
        }

        if (!minecraftAdapter.authorize()) {
            return ServerResult.of(StatusCode.FORBIDDEN, "Authorization failed");
        }
        RequestBuilder builder = RequestBuilder.newBuilder(config.getServerUrl() + "/setSlim")
                .setRequestMethod(RequestMethod.POST)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();
        if (config.getToken() != null) {
            builder.addHeader("token", config.getToken());
        }

        if (slim) {
            builder.addHeader("slim", "1");
        }

        return ServerResult.of(builder);
    }

    private static String getUndashedPlayerUniqueId() {
        return LabyMod.getInstance().getPlayerUUID().toString().replace("-", "");
    }
}