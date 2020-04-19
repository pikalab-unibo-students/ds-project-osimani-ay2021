import kotlin.String
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version.
 */
object Versions {
    const val io_github_javaeden_orchid: String = "0.20.0"

    const val org_jetbrains_kotlin: String = "1.3.72"

    const val org_antlr: String = "4.8-1"

    const val org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin: String = "0.2.2"

    const val com_github_breadmoirai_github_release_gradle_plugin: String = "2.2.12"

    const val org_jetbrains_kotlin_multiplatform_gradle_plugin: String = "1.3.72"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.7.0"

    const val com_eden_orchidplugin_gradle_plugin: String = "0.20.0"

    const val org_jetbrains_dokka_gradle_plugin: String = "0.10.1"

    const val com_jfrog_bintray_gradle_plugin: String = "1.8.5"

    const val plantuml: String = "1.2020.2" // available: "8059"

    const val kt_math: String = "0.1.3"

    /**
     * Current version: "6.3"
     * See issue 19: How to update Gradle itself?
     * https://github.com/jmfayard/buildSrcVersions/issues/19
     */
    const val gradleLatestVersion: String = "6.3"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
    inline get() =
            id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
