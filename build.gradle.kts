/*
 * The HD-Skins LabyMod addon.
 * Copyright (C) 2020 - 2021 HD-Skins <https://github.com/HDSkins>
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
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

plugins {
  id("java")
  id("checkstyle")
}

defaultTasks("clean", "build")

val majorVersion = 2
val labyStoreVersion = 2

allprojects {
  version = "$majorVersion.$labyStoreVersion"
  group = "de.hdskins"

  tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
    // options
    options.isIncremental = true
    options.encoding = StandardCharsets.UTF_8.name()
    options.compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
  }

  repositories {
    mavenLocal()
    mavenCentral()
    maven {
      name = "jitpack.io"
      url = uri("https://jitpack.io")
    }
    maven {
      name = "HDSkins"
      url = uri("https://repo.hdskins.de")
    }
  }

  project.ext.set("dependencyNetworkClientVersion", "1.23-SNAPSHOT")
  project.ext.set("currentShortGitRevision", getCurrentShortGitRevision())
}

fun getCurrentShortGitRevision(): String {
  val stream = ByteArrayOutputStream()
  exec {
    commandLine("git", "rev-parse", "HEAD")
    standardOutput = stream
  }
  return stream.toString().trim().substring(0, 8)
}
