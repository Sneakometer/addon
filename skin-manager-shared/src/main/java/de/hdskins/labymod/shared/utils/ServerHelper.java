package de.hdskins.labymod.shared.utils;

import com.github.derklaro.requestbuilder.RequestBuilder;
import com.github.derklaro.requestbuilder.method.RequestMethod;
import com.github.derklaro.requestbuilder.result.RequestResult;
import com.github.derklaro.requestbuilder.result.http.StatusCode;
import com.github.derklaro.requestbuilder.result.stream.StreamType;
import com.github.derklaro.requestbuilder.types.MimeTypes;
import de.hdskins.labymod.shared.role.UserRole;
import de.hdskins.labymod.shared.config.ConfigObject;
import de.hdskins.labymod.shared.minecraft.MinecraftAdapter;
import de.hdskins.labymod.shared.profile.PlayerProfile;
import net.labymod.main.LabyMod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
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
                .requestMethod(RequestMethod.POST)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .addHeader("reportedUniqueId", reportedPlayer.getUniqueId().toString().replace("-", ""))
                .addHeader("reportedName", reportedPlayer.getName())
                .connectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();

        return ServerResult.of(builder);
    }

    public static ServerResult forceDeleteSkin(MinecraftAdapter minecraftAdapter, ConfigObject config, UUID target) {
        return deleteSkin0(minecraftAdapter, config, target.toString().replace("-", ""), getUndashedPlayerUniqueId());
    }

    public static ServerResult deleteSkin(MinecraftAdapter minecraftAdapter, ConfigObject config) {
        return deleteSkin0(minecraftAdapter, config, getUndashedPlayerUniqueId(), getUndashedPlayerUniqueId());
    }

    public static ServerResult deleteSkin0(MinecraftAdapter minecraftAdapter, ConfigObject config, String target, String executor) {
        if (config.getServerUrl() == null) {
            return ServerResult.of(StatusCode.NOT_ACCEPTABLE, "No server url in the config");
        }

        if (!minecraftAdapter.authorize()) {
            return ServerResult.of(StatusCode.FORBIDDEN, "Authorization failed");
        }
        RequestBuilder builder = RequestBuilder.newBuilder(config.getServerUrl() + "/deleteSkin")
                .requestMethod(RequestMethod.DELETE)
                .addHeader("uuid", executor)
                .addHeader("target", target)
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .connectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();

        try (RequestResult result = builder.fireAndForget()) {
            return ServerResult.ofEmpty(result);
        } catch (Exception ex) {
            return ServerResult.of(ex);
        }
    }

    public static boolean isSlim(ConfigObject configObject) {
        if (configObject.getServerUrl() == null) {
            return false;
        }

        RequestBuilder builder = RequestBuilder.newBuilder(configObject.getServerUrl() + "/isSlim")
                .requestMethod(RequestMethod.GET)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .connectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();

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
                .requestMethod(RequestMethod.POST)
                .addHeader("uuid", getUndashedPlayerUniqueId())
                .addHeader("name", LabyMod.getInstance().getPlayerName())
                .connectTimeout(5, TimeUnit.SECONDS)
                .disableCaches();

        if (slim) {
            builder.addHeader("slim", "1");
        }

        return ServerResult.of(builder);
    }

    public static UserRole getSelfRank(ConfigObject config, MinecraftAdapter minecraftAdapter) {
        return getRankOfUser(config, minecraftAdapter, getUndashedPlayerUniqueId());
    }

    public static UserRole getRankOfUser(ConfigObject config, MinecraftAdapter minecraftAdapter, String uniqueId) {
        if (config.getServerUrl() == null) {
            return UserRole.USER;
        }

        try (RequestResult result = RequestBuilder.newBuilder(config.getServerUrl() + "/getUserRank")
                .requestMethod(RequestMethod.POST)
                .addHeader("uuid", uniqueId)
                .connectTimeout(5, TimeUnit.SECONDS)
                .disableCaches()
                .fireAndForget()
        ) {
            if (result.getStatusCode() != 200) {
                return UserRole.USER;
            }

            return minecraftAdapter.getJsonElement(result.getSuccessResultAsString(), "name", name -> UserRole.getByName(name).orElse(UserRole.USER));
        } catch (Exception exception) {
            exception.printStackTrace();
            return UserRole.USER;
        }
    }

    public static DownloadedSkin downloadSkin(ConfigObject config, String uniqueId) {
        if (config.getServerUrl() == null) {
            return null;
        }

        try (RequestResult result = RequestBuilder.newBuilder(config.getServerUrl() + "/downloadSkin?uuid=" + uniqueId)
             .connectTimeout(5, TimeUnit.SECONDS)
             .disableCaches()
             .fireAndForget()
        ) {
            if (result.getStatusCode() == 404) {
                return null;
            }

            BufferedImage image = ImageIO.read(result.getStream(StreamType.CHOOSE));
            boolean slim = result.getStatusCode() == 418;
            if (image == null) {
                return null;
            }

            return new DownloadedSkin(image, slim);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getUndashedPlayerUniqueId() {
        return LabyMod.getInstance().getPlayerUUID().toString().replace("-", "");
    }
}