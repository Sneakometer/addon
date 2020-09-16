package de.hdskins.labymod.shared.utils;

import com.github.derklaro.requestbuilder.RequestBuilder;
import com.github.derklaro.requestbuilder.result.RequestResult;
import com.github.derklaro.requestbuilder.result.http.StatusCode;

public class ServerResult {

    private final StatusCode code;
    private final String message;

    private ServerResult(StatusCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ServerResult unknown(StatusCode code) {
        return of(code, "unknown");
    }

    public static ServerResult of(RequestBuilder requestBuilder) {
        try (RequestResult requestResult = requestBuilder.fireAndForget()) {
            return of(requestResult);
        } catch (Exception exception) {
            exception.printStackTrace();
            return of(exception);
        }
    }

    public static ServerResult ofEmpty(RequestResult requestResult) {
        return of(requestResult.getStatus(), requestResult.getStatusCode() >= 200 && requestResult.getStatusCode() < 300 ? "success" : "failed");
    }

    public static ServerResult of(RequestResult requestResult) {
        return of(requestResult.getStatus(), requestResult.getStatusCode() >= 200 && requestResult.getStatusCode() < 300 ? "success" : requestResult.getResultAsString());
    }

    public static ServerResult of(Throwable throwable) {
        return of(StatusCode.INTERNAL_SERVER_ERROR, throwable.getClass().getName() + ": " + throwable.getMessage());
    }

    public static ServerResult of(StatusCode code, String message) {
        return new ServerResult(code, message);
    }

    public StatusCode getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
