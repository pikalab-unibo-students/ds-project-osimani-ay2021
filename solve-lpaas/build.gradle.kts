import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.protobuf.gradle.id


plugins {
    application
    `kotlin-jvm-only`
    `kotlin-doc`
    `publish-on-maven`
    id("com.bmuschko.docker-java-application") version "9.3.0"
    alias(libs.plugins.javafx)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.shadowJar)
}

kotlin {
    sourceSets {
        main {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.litote.kmongo:kmongo:4.8.0")
                implementation(project(":io-lib"))
                implementation(project(":oop-lib"))
                implementation(project(":test-solve"))
                implementation(project(":serialize-core"))
                api(project(":solve"))
                api(project(":solve-classic"))
                api(project(":parser-core"))
                api(project(":parser-theory:"))
                api(libs.grpc.protobuf)
                api(libs.grpc.stub)
                compileOnly(libs.tomcat.annotations)
                runtimeOnly(libs.grpc.netty.shaded)
            }
        }

        test {
            dependencies {
                implementation("org.testng:testng:7.1.0")
                implementation(libs.grpc.testing)
                implementation(project(":solve-classic"))
                libs.javafx.graphics.get().let {
                    val dependencyNotation =
                        "${it.module.group}:${it.module.name}:${it.versionConstraint.preferredVersion}"
                    listOf("win", "linux", "mac").forEach { platform ->
                        runtimeOnly("$dependencyNotation:$platform")
                    }
                }
                implementation("org.reactfx:reactfx:2.0-M5")
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
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

val entryPoint = "it.unibo.tuprolog.solve.lpaas.server.ServerMain"

application {
    mainClass.set(entryPoint)
}

/*tasks.getByName<Jar>("jar") {
    manifest {
        attributes("Main-Class" to entryPoint)
    }
    archiveBaseName.set("${rootProject.name}-${project.name}")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("redist")
    sourceSets.main {
        runtimeClasspath.filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
            .forEach { from(it) }
    }
    from(files("${rootProject.projectDir}/LICENSE"))
    dependsOn("classes")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}*/

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    manifest { attributes("Main-Class" to entryPoint) }
    archiveBaseName.set("${rootProject.name}-${project.name}")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("redist")
    sourceSets.main {
        runtimeClasspath.filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
            .forEach { from(it) }
    }
    from(files("${rootProject.projectDir}/LICENSE"))
    dependsOn("classes")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.create("testGui", JavaExec::class.java) {
    group = "application"
    dependsOn("testClasses")
    classpath = files(
        sourceSets.test.get().runtimeClasspath,
    )
    standardInput = System.`in`
    main = "testGui.Main"
}

tasks.create("buildDockerImage", DockerBuildImage::class) {
    group = "application"
    inputDir.set(file("../"))
    images.add("lpaas")
}
