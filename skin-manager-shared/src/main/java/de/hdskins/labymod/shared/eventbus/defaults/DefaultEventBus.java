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
package de.hdskins.labymod.shared.eventbus.defaults;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.hdskins.labymod.shared.eventbus.Cancelable;
import de.hdskins.labymod.shared.eventbus.EventBus;
import de.hdskins.labymod.shared.eventbus.EventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class DefaultEventBus implements EventBus {

  private static final Logger LOGGER = LogManager.getLogger(DefaultEventBus.class);
  private static final Comparator<EventBusEntry> ENTRY_COMPARATOR = Comparator.comparingInt(e -> e.eventListener.postOrder());

  // technically MultiMap<Event, Listener>
  private final Multimap<Class<?>, EventBusEntry> entries = HashMultimap.create();
  private final Object lock = new Object();

  @Override
  public void registerListener(Object listenerClass) {
    synchronized (this.lock) {
      for (Method method : listenerClass.getClass().getMethods()) {
        EventListener eventListener = method.getAnnotation(EventListener.class);
        if (eventListener == null || method.getParameterCount() != 1) {
          continue;
        }

        this.entries.put(method.getParameterTypes()[0], new EventBusEntry(eventListener, listenerClass, method));
      }
    }
  }

  @Override
  public void unregister(Object listenerClass) {
    synchronized (this.lock) {
      this.entries.values().removeIf(entry -> entry.listenerClass.equals(listenerClass));
    }
  }

  @Override
  public void unregisterAll() {
    synchronized (this.lock) {
      this.entries.clear();
    }
  }

  @Override
  public @Nonnull PostResult post(Object event) {
    synchronized (this.lock) {
      // get the listener subscribed to the event
      Collection<EventBusEntry> eventBusEntries = this.entries.get(event.getClass());
      if (eventBusEntries == null) {
        // no listener are subscribed, abort
        return PostResult.success();
      }

      // collect the listeners sorted by the priority to a new list
      List<EventBusEntry> targetListener = eventBusEntries.stream().sorted(ENTRY_COMPARATOR).collect(Collectors.toList());
      // Create a new lazily initialized map of the failed posts
      Multimap<Object, Throwable> exceptions = null;
      // post the event to the listeners
      for (EventBusEntry eventBusEntry : targetListener) {
        if (this.shouldPost(event, eventBusEntry)) {
          try {
            eventBusEntry.method.invoke(eventBusEntry.listenerClass, event);
          } catch (Throwable throwable) {
            if (exceptions == null) {
              // we cached the first exception - initialize the result map
              exceptions = HashMultimap.create();
            }

            exceptions.put(eventBusEntry.eventListener, throwable);
          }
        }
      }

      return exceptions == null ? PostResult.success() : PostResult.failure(exceptions);
    }
  }

  @Override
  public void postReported(Object event) {
    PostResult postResult = this.post(event);
    if (postResult.wasSuccessful()) {
      return;
    }

    for (Map.Entry<Object, Throwable> entry : postResult.getExceptions().entries()) {
      LOGGER.error("Exception posting event {} to {}", event.getClass().getName(), entry.getKey(), entry.getValue());
    }
  }

  private boolean shouldPost(Object event, EventBusEntry eventBusEntry) {
    if (!(event instanceof Cancelable)) {
      return true;
    }

    return !((Cancelable) event).isCanceled() || eventBusEntry.eventListener.consumesCanceledEvents();
  }

  private static final class EventBusEntry {

    private final EventListener eventListener;
    private final Object listenerClass;
    private final Method method;

    public EventBusEntry(EventListener eventListener, Object listenerClass, Method method) {
      this.eventListener = eventListener;
      this.listenerClass = listenerClass;
      this.method = method;
      this.method.setAccessible(true);
    }
  }
}
