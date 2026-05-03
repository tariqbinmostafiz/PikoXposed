import groovy.xml.MarkupBuilder
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val gitCommitHashProvider = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
    workingDir = rootProject.rootDir
}.standardOutput.asText!!

val gitCommitDateProvider = providers.exec {
    commandLine("git log -1 --format=%ct".split(" "))
    workingDir = rootProject.rootDir
}.standardOutput.asText!!

android {
    namespace = "io.github.pikoxposed"

    defaultConfig {
        applicationId = "io.github.pikoxposed"
        versionCode = 102
        versionName = "1.0.$versionCode"
        val pikoPropsFile = rootProject.file("piko/gradle.properties")
        val patchVersion = if (pikoPropsFile.exists()) {
            Properties().apply { pikoPropsFile.inputStream().use { load(it) } }["version"] ?: "unknown"
        } else { "unknown" }
        buildConfigField("String", "PATCH_VERSION", "\"$patchVersion\"")
        buildConfigField("String", "COMMIT_HASH", "\"${gitCommitHashProvider.get().trim()}\"")
        buildConfigField("long", "COMMIT_DATE", "${gitCommitDateProvider.get().trim()}L")
    }
    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x4b")
    }
    packaging.resources {
        excludes.addAll(
            arrayOf(
                "META-INF/**", "**.bin"
            )
        )
    }
    val ksFile = rootProject.file("signing.properties")
    signingConfigs {
        if (ksFile.exists()) {
            create("release") {
                val properties = Properties().apply {
                    ksFile.inputStream().use { load(it) }
                }

                storePassword = properties["KEYSTORE_PASSWORD"] as String
                keyAlias = properties["KEYSTORE_ALIAS"] as String
                keyPassword = properties["KEYSTORE_ALIAS_PASSWORD"] as String
                storeFile = file(properties["KEYSTORE_FILE"] as String)
            }
        }
    }
    buildFeatures.buildConfig = true
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            if (ksFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    lint {
        checkReleaseBuilds = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        named("main") {
            val pikoDirs = listOf(
                "../piko/extensions/shared/library/src/main/java",
                "../piko/extensions/twitter/src/main/java",
                "../piko/extensions/instagram/src/main/java",
            ).filter { project.file(it).exists() }
            java.directories += pikoDirs
            kotlin.directories += pikoDirs

        }
    }
}

// which are not available in the Xposed module build context.
tasks.withType<JavaCompile>().configureEach {
    exclude(
        "**/patches/HideRelatedVideosPatch.java",
        "**/patches/playback/quality/PrioritizeVideoQualityPatch.java",
        "**/OAuth2Preference.java",
        "**/SpoofVideoStreamsSignInPreference.java",
    )
}
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xno-param-assertions",
            "-Xno-receiver-assertions",
            "-Xno-call-assertions",
            "-Xcontext-parameters"
        )
        jvmTarget = JvmTarget.JVM_17
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
//    implementation(libs.dexkit)

    // DexKit fork with instruction operand introspection
    // https://github.com/NexAlloy/DexKit/commit/71e3765ac3e337206606c1de0236a09a9d30c633
    implementation(":dexkit-android@aar")
    implementation("com.google.flatbuffers:flatbuffers-java:23.5.26") // dexkit dependency
    implementation(libs.annotation)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.fuel)
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.jadx.core)
    testImplementation(libs.slf4j.simple)
    debugImplementation(kotlin("reflect"))
    compileOnly(libs.xposed)
//    implementation(project(":extensions"))
    compileOnly(project(":stub"))
}


abstract class GenerateStringsTask @Inject constructor(
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private fun writeNode(builder: MarkupBuilder, node: Any?) {
        if (node !is NodeChild) return
        val attributes = node.attributes()
        builder.withGroovyBuilder {
            if (node.children().any()) {
                node.name()(attributes) {
                    node.children().forEach {
                        writeNode(builder, it)
                    }
                }
            } else {
                node.name()(attributes, node.text())
            }
        }
    }

    /**
     * Morphe addresources structure:
     *   values/youtube/strings.xml, values/shared/strings.xml, etc.
     * Each XML is flat: <resources> <string name="...">...</string> ... </resources>
     *
     * Merge all subdirectory XMLs into a single output file per variant.
     */
    private fun mergeResources(inputFiles: List<File>, output: File) {
        output.parentFile.mkdirs()
        output.writer().use { writer ->
            val builder = MarkupBuilder(writer)
            builder.doubleQuotes = true
            builder.withGroovyBuilder {
                val keys = mutableSetOf<String>()
                "resources" {
                    for (inputFile in inputFiles) {
                        if (!inputFile.exists()) continue
                        val inputXml = XmlSlurper().parse(inputFile)
                        // Flat structure: direct children of <resources>
                        inputXml.children().forEach {
                            if (it !is NodeChild) return@forEach
                            val key = it.attributes()["name"] as? String ?: return@forEach
                            if (keys.contains(key)) return@forEach
                            writeNode(builder, it)
                            keys.add(key)
                        }
                    }
                }
            }
        }
    }

    // Subdirectories within each variant that contain resource files.
    private val subDirs = listOf("shared", "twitter")

    @TaskAction
    fun action() {
        val inputDir = inputDirectory.get().asFile
        val outputDir = outputDirectory.get().asFile

        runCatching {
            // Process each variant directory (values, values-xx-rYY, ...)
            inputDir.listFiles()?.filter { it.isDirectory }?.forEach { variant ->
                val genResDir = File(outputDir, variant.name).apply { mkdirs() }

                // Merge strings.xml from all subdirectories
                val stringFiles = subDirs.map { File(variant, "$it/strings.xml") }
                mergeResources(stringFiles, File(genResDir, "strings.xml"))

                // Merge arrays.xml from all subdirectories
                val arrayFiles = subDirs.map { File(variant, "$it/arrays.xml") }
                if (arrayFiles.any { it.exists() }) {
                    mergeResources(arrayFiles, File(genResDir, "arrays.xml"))
                }
            }
        }.onFailure {
            System.err.println(it)
            throw it
        }
    }
}

abstract class CopyResourcesTask @Inject constructor() : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun action() {
        val baseDir = inputDirectory.get().asFile
        val outputDir = outputDirectory.get().asFile
        outputDir.deleteRecursively()

        val resourcePaths = mapOf(
            "twitter/settings/layout" to null,
            "twitter/bringbacktwitter/drawable" to null,
        )

        for ((resourcePath, excludes) in resourcePaths) {
            val dir = resourcePath.substringAfter('/')
            val sourceDir = File(baseDir, resourcePath)
            val targetDir = File(outputDir, dir)
            sourceDir.listFiles()?.forEach { file ->
                if (excludes == null || !excludes.contains(file.name)) {
                    file.copyTo(File(targetDir, file.name), overwrite = true)
                }
            }
        }
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.packaging.resources.excludes.add("kotlin/**")
    }

    onVariants { variant ->
        val variantName = variant.name.uppercaseFirstChar()
        val pikoAddresourcesDir = project.file("../piko/patches/src/main/resources/addresources")
        if (pikoAddresourcesDir.exists()) {
            val strTask = project.tasks.register<GenerateStringsTask>("generateStrings$variantName") {
                inputDirectory.set(pikoAddresourcesDir)
            }
            variant.sources.res?.addGeneratedSourceDirectory(
                strTask, GenerateStringsTask::outputDirectory
            )
        }

        val pikoResourcesDir = project.file("../piko/patches/src/main/resources")
        if (pikoResourcesDir.exists()) {
            val resTask = project.tasks.register<CopyResourcesTask>("copyResources$variantName") {
                inputDirectory.set(pikoResourcesDir)
            }
            variant.sources.res?.addGeneratedSourceDirectory(
                resTask, CopyResourcesTask::outputDirectory
            )
        }
    }
}
