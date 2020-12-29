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
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.ReobfTaskFactory.ReobfTaskWrapper
import net.minecraftforge.gradle.user.UserBaseExtension

buildscript {
  repositories {
    jcenter()
    mavenCentral()

    maven {
      name = "forge"
      url = uri("https://files.minecraftforge.net/maven")
    }
  }

  dependencies {
    classpath("com.github.jengelman.gradle.plugins:shadow:4.0.4")
    classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
  }
}

apply(plugin = "java")
apply(plugin = "checkstyle")
apply(plugin = "net.minecraftforge.gradle.forge")
apply(plugin = "com.github.johnrengelman.shadow")

val compileJava: JavaCompile by tasks
val processResources: ProcessResources by tasks
val Project.minecraft: UserBaseExtension get() = extensions.getByName<UserBaseExtension>("minecraft")

tasks.withType<ShadowJar> {
  archiveName = "HDSkins-1.12.jar"
  exclude("dummyThing")
}

compileJava.doFirst {
  // Copy the shared source files between minecraft 1.8 and up
  copy {
    from("./../skin-manager-shared/src/main/java")
    into("./build/sources/main/java")
  }
  // Copy the shared resources files between minecraft 1.8 and up
  copy {
    from("./../skin-manager-shared/src/main/resources")
    into("./build/resources/main")
  }
  // Copy the source files which are created in the module
  copy {
    from("./src/main/java")
    into("./build/sources/main/java")
  }
  // Copy the resource files which are created in the module
  copy {
    from("./src/main/resources")
    into("./build/resources/main")
  }
  // Copy the license into the resources folder
  copy {
    from("./../license.txt")
    into("./build/resources/main")
  }
}

configure<UserBaseExtension> {
  version = "1.12.2-14.23.0.2512"
  runDir = "run"
  mappings = "snapshot_20171003"
}

tasks.withType<ShadowJar> {
  inputs.properties += "version" to project.version
  inputs.properties += "mcversion" to project.minecraft.version
}

dependencies {
  "compileOnly"(project(":skin-manager-shared"))
  "compileOnly"(files("../libs/lm_api.jar"))
  "implementation"("de.hdskins.protocol:client:" + ext["dependencyNetworkClientVersion"] as String)
}

afterEvaluate {
  val reobf = extensions.getByName<NamedDomainObjectContainer<ReobfTaskWrapper>>("reobf")
  reobf.maybeCreate("shadowJar").run {
    mappingType = ReobfMappingType.NOTCH
    task.doLast {
      copy {
        from("./build/libs/HDSkins-1.12.jar")
        into("./../build/libs")
      }

      delete(fileTree("./../build/libs").matching {
        include("**/skin-manager-root-" + project.version + ".jar")
      })
    }
  }
}

artifacts {
  add("archives", tasks.getByName("shadowJar"))
}
