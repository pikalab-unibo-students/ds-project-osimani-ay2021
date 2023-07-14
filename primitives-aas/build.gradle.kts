import com.google.protobuf.gradle.id

plugins {
    application
    `kotlin-jvm-only`
    `kotlin-doc`
    `publish-on-maven`
    alias(libs.plugins.protobuf)
}
dependencies {
    implementation(project(mapOf("path" to ":solve")))
    implementation(project(mapOf("path" to ":solve")))
}

kotlin {
    sourceSets {
        main {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                api(libs.grpc.protobuf)
                api(libs.grpc.stub)
                api(project(":core"))
                api(project(":dsl-theory"))
                api(project(":solve-classic"))
                api(project(":parser-core"))
                api(project(":io-lib"))
                compileOnly(libs.tomcat.annotations)
                runtimeOnly(libs.grpc.netty.shaded)
                implementation("org.litote.kmongo:kmongo:4.8.0")
            }
        }

        test {
            dependencies {
                implementation("org.testng:testng:7.1.0")
                implementation("com.github.pgreze:kotlin-process:1.4.1")
                implementation(libs.grpc.testing)
                implementation(kotlin("test"))
            }
        }
    }
}

protobuf {
    protoc { artifact = libs.protobuf.protoc.get().toString() }
    plugins {
        id("grpc") { artifact = libs.grpc.generator.java.get().toString() }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}

tasks.create<Exec>("installPythonDependencies") {
    commandLine = listOf("pip3", "install", "--upgrade", "prolog-primitives")
    group = "verification"
    tasks.getByName("test").dependsOn(this)
}
