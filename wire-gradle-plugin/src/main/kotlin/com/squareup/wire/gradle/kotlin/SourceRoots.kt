/*
 * Copyright (C) 2020 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.wire.gradle.kotlin

import com.android.build.api.dsl.AndroidSourceDirectorySet
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.squareup.wire.gradle.*
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

/**
 * @return A list of source roots and their dependencies.
 *
 * Examples:
 *   Multiplatform Environment. Ios target labeled "ios".
 *     -> iosMain deps [commonMain]
 *
 *   Android environment. internal, production, release, debug variants.
 *     -> internalDebug deps [internal, debug, main]
 *     -> internalRelease deps [internal, release, main]
 *     -> productionDebug deps [production, debug, main]
 *     -> productionRelease deps [production, release, main]
 *
 *    Multiplatform environment with android target (oh boy)
 */
internal fun WirePlugin.sourceRoots(project: Project, kotlin: Provider<Boolean>, java: Provider<Boolean>): ListProperty<Source> {
  val result = objects.listProperty<Source>()

  // Multiplatform project.
  this@WirePlugin.project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.let {
    result.addAll(it.sourceRoots(this@WirePlugin.project))
  }

  // Java project.
  this@WirePlugin.project.withJavaPlugin {
    val sourceSets = this@WirePlugin.project.property("sourceSets") as SourceSetContainer
    result.add(
      Source(
        type = KotlinPlatformType.jvm,
        kotlinSourceDirectorySet = this@WirePlugin.project.objects.property(),
        javaSourceDirectorySet = sourceSets.named("main").map { WireSourceDirectorySet.Normal(it.java) },
        name = "main",
        sourceSets = listOf("main"),
      ),
    )
  }

  // Android project.
  this@WirePlugin.project.withAndroidPlugin {
    val androidExtension = this@WirePlugin.project.extensions.getByName<BaseExtension>("android")
    result.addAll(

      androidExtension.sourceRoots(this@WirePlugin.project, kotlin)
    )
  }

  // Kotlin project.
  this@WirePlugin.project.withKotlinPlugin {
    val kotlinExtension = this@WirePlugin.project.extensions.getByName<KotlinProjectExtension>("kotlin")
    val sourceSets = kotlinExtension.sourceSets
    val sourceDirectorySet = sourceSets.named("main").map { WireSourceDirectorySet.Normal(it.kotlin) }

    result.add(
      Source(
        type = KotlinPlatformType.jvm,
        kotlinSourceDirectorySet = sourceDirectorySet,
        javaSourceDirectorySet = sourceDirectorySet,
        name = "main",
        sourceSets = listOf("main"),
      ),
    )
  }

  return result
}

private fun KotlinMultiplatformExtension.sourceRoots(): List<Source> {
  // Wire only supports commonMain as in other cases, we'd be expected to generate both
  // `expect` and `actual` classes which doesn't make much sense for what Wire does.
  return listOf(
    Source(
      type = KotlinPlatformType.common,
      name = "commonMain",
      variantName = null,
      kotlinSourceDirectorySet = sourceSets.named("commonMain").map { WireSourceDirectorySet.Normal(it.kotlin) },
      javaSourceDirectorySet = project.provider { null },
      sourceSets = listOf("commonMain"),
    ),
  )
}

private fun BaseExtension.sourceRoots(project: Project, kotlin: Provider<Boolean>): List<Source> {
  val variants: DomainObjectSet<out BaseVariant> = when (this) {
    is AppExtension -> applicationVariants
    is LibraryExtension -> libraryVariants
    else -> throw IllegalStateException("Unknown Android plugin $this")
  }
  val androidSourceSets = project.objects.mapProperty<String, AndroidSourceDirectorySet>().apply {
    putAll(
      kotlin.map { hasKotlin ->
        if (hasKotlin) {
          mapOf()
        } else {
          sourceSets.associate { sourceSet ->
            sourceSet.name to sourceSet.java
          }
        }
      }
    )
  }

  val sourceSets = project.objects.mapProperty<String, SourceDirectorySet>().apply {
    putAll(
      kotlin.map { hasKotlin ->
        if (hasKotlin) {
          val kotlinSourceSets = project.extensions.getByName<KotlinProjectExtension>("kotlin").sourceSets
          sourceSets
            .associate { sourceSet ->
              sourceSet.name to kotlinSourceSets.getByName(sourceSet.name).kotlin
            }
        } else {
          mapOf()
        }
      }
    )
  }

  return variants.map { variant ->
    val kotlinSourceDirectSet = kotlin.zip(sourceSets) { hasKotlin, sourceSets ->
      if (hasKotlin) {
        val sourceDirectorySet = sourceSets.getValue(variant.name)
        WireSourceDirectorySet.Normal(sourceDirectorySet)
      } else {
        null
      }
    }


    val androidSourceDirectorySet = androidSourceSets.getting(variant.name)
    val javaSourceDirectorySet = androidSourceDirectorySet.map {
      WireSourceDirectorySet.Android(it)
    }

    Source(
      type = KotlinPlatformType.androidJvm,
      kotlinSourceDirectorySet = kotlinSourceDirectSet,
      javaSourceDirectorySet = javaSourceDirectorySet,
      name = variant.name,
      variantName = variant.name,
      sourceSets = variant.sourceSets.map { it.name },
      registerGeneratedDirectory = { outputDirectory ->
        variant.addJavaSourceFoldersToModel(outputDirectory.get().files)
      },
      registerTaskDependency = { task ->
        variant.registerJavaGeneratingTask(task, project.files(task.map { it.outputDirectories.files }).files)
        val compileTaskName =
          if (kotlin) {
            """compile${variant.name.capitalize()}Kotlin"""
          } else {
            """compile${variant.name.capitalize()}Sources"""
          }
        project.tasks.named(compileTaskName).dependsOn(task)
      },
    )
  }
}

internal data class Source(
  val type: KotlinPlatformType,
  val kotlinSourceDirectorySet: Provider<out WireSourceDirectorySet>,
  val javaSourceDirectorySet: Provider<out WireSourceDirectorySet>,
  val name: String,
  val variantName: String? = null,
  val sourceSets: List<String>,
  val registerGeneratedDirectory: ((Provider<ConfigurableFileCollection>) -> Unit)? = null,
  val registerTaskDependency: ((TaskProvider<WireTask>) -> Unit)? = null,
)

internal sealed interface WireSourceDirectorySet {
  data class Android(val androidSourceDirectorySet: AndroidSourceDirectorySet) : WireSourceDirectorySet {
    override fun srcDir(path: Any): WireSourceDirectorySet {
      androidSourceDirectorySet.srcDir(path)
      return this
    }
  }

  data class Normal(val sourceDirectorySet: SourceDirectorySet) : WireSourceDirectorySet {
    override fun srcDir(path: Any): WireSourceDirectorySet {
      sourceDirectorySet.srcDir(path)
      return this
    }
  }

  /**
   * Adds the [path] to this set. [path] is evaluated the same as [Project.file].
   */
  fun srcDir(path: Any): WireSourceDirectorySet

  companion object {
    fun of(sourceDirectorySet: SourceDirectorySet): WireSourceDirectorySet {
      return Normal(sourceDirectorySet)
    }

    fun of(androidSourceDirectorySet: AndroidSourceDirectorySet): WireSourceDirectorySet {
      return Android(androidSourceDirectorySet)
    }
  }
}
