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
import net.minecraftforge.gradle.user.UserBaseExtension
import java.util.*

buildscript {
    repositories {
        jcenter()
        mavenCentral()

        maven {
            name = "forge"
            url = uri("http://files.minecraftforge.net/maven")
        }
    }

    dependencies.classpath("net.minecraftforge.gradle:ForgeGradle:2.1-SNAPSHOT")
}

apply(plugin = "net.minecraftforge.gradle.forge")

configure<UserBaseExtension> {
    version = "1.8.9-11.15.1.1855"
    runDir = "run"
    mappings = "stable_20"
}

dependencies {
    "compileOnly"(files("../libs/lm_api.jar"))
    "compileOnly"("de.hdskins.protocol:client:" + ext["dependencyNetworkClientVersion"] as String)
}

afterEvaluate {
    generateLanguagesFile()
}

fun generateLanguagesFile() {
    val stringBuilder = StringBuilder()
            .append("# Automatically generated file, do not edit.")
            .append('\n')
            .append("# This file was created with project version ")
            .append(project.version)
            .append(" on git revision ")
            .append(project.ext["currentShortGitRevision"] as String)
            .append(" at ")
            .append(Date().toString())
            .append("\n\n")

    val entryDirectory = file("./src/main/resources/lang")
    computeLanguageFiles(entryDirectory, entryDirectory, mutableMapOf()).forEach {
        stringBuilder.append(it.key).append(':').append(it.value).append('\n')
    }

    file("./src/main/resources/languages").writeText(stringBuilder.toString(), Charsets.UTF_8)
}

fun computeLanguageFiles(dir: File, base: File, languageFilesMap: MutableMap<String, String>): Map<String, String> {
    dir.listFiles()?.forEach {
        if (it.isDirectory) {
            computeLanguageFiles(it, base, languageFilesMap)
        } else {
            var name = it.name
            if (name.endsWith(".properties")) {
                val lastSlashIndex = name.lastIndexOf('/')
                if (lastSlashIndex != -1) {
                    name = name.substring(lastSlashIndex)
                }

                if (name.indexOf(':') == -1 && !languageFilesMap.containsKey(name)) {
                    languageFilesMap[name.replaceFirst("(?s)(.*).properties".toRegex(), "$1")] =
                            it.toRelativeString(base).replace('\\', '/')
                }
            }
        }
    }

    return languageFilesMap
}
