import com.google.protobuf.gradle.id

plugins {
    application
    `kotlin-jvm-only`
    `kotlin-doc`
    `publish-on-maven`
    alias(libs.plugins.protobuf)
}

kotlin {
    sourceSets {
        main {
            dependencies {

                api(project(":core"))
                api(project(":dsl-theory"))
                api(project(":solve-classic"))
                api(project(":parser-core"))
                api(project(":io-lib"))
                api(libs.kotlinx.coroutines.core)
                api(libs.grpc.protobuf)
                api(libs.protobuf.java)
                api(libs.grpcKotlin.stub)

                implementation("org.litote.kmongo:kmongo:4.8.0")
                compileOnly(libs.tomcat.annotations)
                runtimeOnly(libs.grpc.netty.shaded)
            }
        }

        test {
            dependencies {
                implementation("org.testng:testng:7.1.0")
                implementation(libs.grpc.testing)
            }
        }
    }
}

sourceSets {
    val main by getting { }
    main.java.srcDirs("build/generated/source/proto/main/grpc")
    main.java.srcDirs("build/generated/source/proto/main/grpckt")
    main.java.srcDirs("build/generated/source/proto/main/java")
}

protobuf {
    protoc { artifact = libs.protobuf.protoc.get().toString() }
    plugins {
        id("grpc") {
            artifact = libs.grpc.generator.java.get().toString() }
        id("grpckt") {
            artifact = libs.grpc.generator.kotlin.get().toString() + ":jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
