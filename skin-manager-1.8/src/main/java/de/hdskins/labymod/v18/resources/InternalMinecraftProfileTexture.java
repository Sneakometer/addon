package de.hdskins.labymod.v18.resources;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class InternalMinecraftProfileTexture extends MinecraftProfileTexture {

    private static final LoadingCache<String, Long> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .ticker(Ticker.systemTicker())
            .build(new CacheLoader<String, Long>() {
                @Override
                public Long load(String key) {
                    return System.currentTimeMillis();
                }
            });

    public InternalMinecraftProfileTexture(String url, String hash, boolean force) {
        super(url, null);
        if (force) {
            this.hash = hash + System.nanoTime();
        } else {
            this.hash = hash + CACHE.getUnchecked(hash);
        }
    }

    private final String hash;
    private Map<String, String> metadata;

    @Override
    public String getHash() {
        return this.hash;
    }

    public Map<String, String> getMetadata() {
        return metadata == null ? (metadata = new HashMap<>()) : metadata;
    }

    @Override
    public String getMetadata(String key) {
        if (metadata == null) {
            return null;
        }

        return metadata.get(key);
    }
}
