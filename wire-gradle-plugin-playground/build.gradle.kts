import com.squareup.wire.schema.EventListener

plugins {
  id("java-library")
  kotlin("jvm")
  id("com.squareup.wire")
}

class MyEventListenerFactory : EventListener.Factory {
  override fun create(): EventListener {
    return object : EventListener() {}
  }
}

val outputDirectory = objects.property<String>()
outputDirectory.set("$buildDir/something-wrong")

wire {
  protoLibrary = true

  sourcePath {
    srcDir("src/main/proto")
  }

  eventListenerFactory(MyEventListenerFactory())

  kotlin {
    out = outputDirectory.orNull
  }
}

outputDirectory.set(null as String?)

dependencies {
  implementation(projects.wireGrpcClient)
  implementation(libs.okio.core)
  implementation(projects.wireCompiler)
  implementation(projects.wireSchema)
  implementation(projects.wireGsonSupport)
  implementation(projects.wireMoshiAdapter)
  implementation(libs.assertj)
  implementation(libs.junit)
  implementation(libs.protobuf.javaUtil)
  implementation(projects.wireTestUtils)
}
