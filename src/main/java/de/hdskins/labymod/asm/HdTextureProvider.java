package de.hdskins.labymod.asm;

import com.github.derklaro.requestbuilder.RequestBuilder;
import com.github.derklaro.requestbuilder.method.RequestMethod;
import com.github.derklaro.requestbuilder.result.RequestResult;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import java.io.IOException;
import java.util.Map;

public class HdTextureProvider {

    public static void fillProperties(GameProfile profile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
        String downloadUrl = tryDownload(profile);
        if (downloadUrl != null) {
            textures.put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture(downloadUrl, null));
        }
    }

    private static String tryDownload(GameProfile profile) {
        try {
            String downloadUrl = "https://api.hdskins.de/downloadSkin?uuid=" + profile.getId().toString().replace("-", "");
            RequestResult requestResult = RequestBuilder.newBuilder(downloadUrl)
                    .setRequestMethod(RequestMethod.GET)
                    .disableCaches()
                    .enableOutput()
                    .fireAndForget();

            return requestResult.getStatusCode() == 200 ? downloadUrl : null;
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}