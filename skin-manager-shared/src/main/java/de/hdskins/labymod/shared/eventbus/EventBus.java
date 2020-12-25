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
package de.hdskins.labymod.shared.eventbus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface EventBus {

  void registerListener(Object listenerClass);

  void unregister(Object listenerClass);

  void unregisterAll();

  @Nonnull
  PostResult post(Object event);

  void postReported(Object event);

  final class PostResult {
    private static final PostResult SUCCESS = new PostResult(HashMultimap.create());
    private final Multimap<Object, Throwable> exceptions;

    private PostResult(Multimap<Object, Throwable> exceptions) {
      this.exceptions = exceptions;
    }

    @Nonnull
    public static PostResult success() {
      return SUCCESS;
    }

    @Nonnull
    public static PostResult failure(Multimap<Object, Throwable> exceptions) {
      return new PostResult(exceptions);
    }

    public boolean wasSuccessful() {
      return this.exceptions.isEmpty();
    }

    @Nonnull
    public Multimap<Object, Throwable> getExceptions() {
      return this.exceptions;
    }

    public void publish() {
      if (!this.wasSuccessful()) {
        throw new PostException(this);
      }
    }

    public void print() {
      for (Throwable value : this.exceptions.values()) {
        value.printStackTrace();
      }
    }

    @Override
    public String toString() {
      if (this.wasSuccessful()) {
        return "PostResult{type=success}";
      } else {
        return "PostResult{type=failure, exceptions=" + this.exceptions + '}';
      }
    }
  }

  final class PostException extends RuntimeException {
    private final PostResult result;

    PostException(PostResult result) {
      super("Exceptions occurred whilst posting to subscribers");
      this.result = result;
    }

    public PostResult result() {
      return this.result;
    }

    @Nonnull
    public PostResult getResult() {
      return this.result;
    }
  }
}
