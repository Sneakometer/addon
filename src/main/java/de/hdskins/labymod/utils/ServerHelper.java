package de.hdskins.labymod.utils;

import com.github.derklaro.requestbuilder.RequestBuilder;
import com.github.derklaro.requestbuilder.method.RequestMethod;
import com.github.derklaro.requestbuilder.result.RequestResult;
import com.github.derklaro.requestbuilder.result.http.StatusCode;

import java.io.IOException;
import java.io.InputStream;

public class ServerHelper {

    public static StatusCode uploadToServer(final InputStream stream) {
        try {
            RequestResult requestResult = RequestBuilder.newBuilder("https://api.hdskins.de")
                    .setRequestMethod(RequestMethod.POST)
                    .disableCaches()
                    .enableOutput()
                    .fireAndForget();

            byte[] bytes = new byte[128 * 1024];
            while (stream.read(bytes) != -1) {
                requestResult.getOutputStream().write(bytes);
                requestResult.getOutputStream().flush();
            }

            return requestResult.getStatus();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        return StatusCode.INTERNAL_SERVER_ERROR;
    }
}