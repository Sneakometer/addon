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
package de.hdskins.labymod.shared.settings.countdown;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class SettingsCountdownRegistry {

    private static final Collection<Task> TASKS = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    static {
        EXECUTOR.scheduleAtFixedRate(() -> {
            for (Task task : TASKS) {
                task.consumer.accept(task.executions.getAndDecrement());
                if (task.executions.get() < 0) {
                    TASKS.remove(task);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private SettingsCountdownRegistry() {
        throw new UnsupportedOperationException();
    }

    public static void registerTask(Consumer<Long> consumer, long runningSeconds) {
        TASKS.add(new Task(consumer, runningSeconds));
    }

    private static final class Task {
        private final Consumer<Long> consumer;
        private final AtomicLong executions = new AtomicLong();

        public Task(Consumer<Long> consumer, long executions) {
            this.consumer = consumer;
            this.executions.set(executions);
        }
    }
}
