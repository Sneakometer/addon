package de.hdskins.labymod.v112.resources;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

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

    public InternalMinecraftProfileTexture(String url, String hash) {
        super(url, null);
        this.hash = hash + CACHE.getUnchecked(hash);
    }

    private final String hash;

    @Override
    public String getHash() {
        return this.hash;
    }
}
