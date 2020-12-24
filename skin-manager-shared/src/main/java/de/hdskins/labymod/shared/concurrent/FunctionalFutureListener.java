/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 HD-Skins <https://github.com/HDSkins>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.hdskins.labymod.shared.concurrent;

import com.google.common.base.Preconditions;
import de.hdskins.protocol.concurrent.FutureListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class FunctionalFutureListener<T> implements FutureListener<T> {

    private final Consumer<T> resultHandler;
    private final Runnable noResultHandler;

    protected FunctionalFutureListener(Consumer<T> resultHandler, Runnable noResultHandler) {
        this.resultHandler = resultHandler;
        this.noResultHandler = noResultHandler;
    }

    @Nonnull
    public static <X> FutureListener<X> listener(@Nonnull Consumer<X> resultHandler) {
        return listener(resultHandler, null);
    }

    @Nonnull
    public static <X> FutureListener<X> listener(@Nonnull Runnable noResultHandler) {
        return listener(null, noResultHandler);
    }

    @Nonnull
    public static <X> FutureListener<X> listener(@Nullable Consumer<X> resultHandler, @Nullable Runnable noResultHandler) {
        Preconditions.checkArgument(resultHandler != null || noResultHandler != null);
        return new FunctionalFutureListener<>(resultHandler, noResultHandler);
    }

    @Override
    public void nullResult() {
        if (this.noResultHandler != null) {
            this.noResultHandler.run();
        }
    }

    @Override
    public void nonNullResult(@Nonnull T t) {
        if (this.resultHandler != null) {
            this.resultHandler.accept(t);
        }
    }

    @Override
    public void cancelled() {
        if (this.noResultHandler != null) {
            this.noResultHandler.run();
        }
    }
}
