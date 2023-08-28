package com.squareup.wire.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.concurrent.atomic.AtomicBoolean


private val androidPluginIds = setOf(
    "com.android.application",
    "com.android.library",
    "com.android.instantapp",
    "com.android.feature",
    "com.android.dynamic-feature",
)

fun Project.withAndroidPlugin(action: () -> Unit) = withAnyPluginOnce(androidPluginIds, action)

private val kotlinPluginIds = setOf(
    "org.jetbrains.kotlin.multiplatform",
    "org.jetbrains.kotlin.android",
    "org.jetbrains.kotlin.jvm",
    "org.jetbrains.kotlin.js",
    "kotlin2js",
)

fun Project.withKotlinPlugin(action: () -> Unit) = withAnyPluginOnce(kotlinPluginIds, action)

private val javaPluginIds = setOf(
    "java",
    "java-library",
    "java-test-fixtures",
)

fun Project.withJavaPlugin(action: () -> Unit) = withAnyPluginOnce(javaPluginIds, action)

private inline fun Project.withAnyPluginOnce(pluginIds: Set<String>, crossinline action: () -> Unit) {
    val executed = AtomicBoolean(false)

    for (pluginId in pluginIds) {
        plugins.withId(pluginId) {
            if (!executed.compareAndSet(true, true)) {
                action()
            }
        }
    }
}
