
plugins {
    id("com.gtnewhorizons.gtnhconvention")
     id("maven-publish")
}

tasks.withType<JavaCompile>().configureEach {
    options.annotationProcessorPath = configurations.annotationProcessor.get()
}

// Forge's cpw.mods.fml.common.patcher.ClassPatchManager references java.util.jar.Pack200, which no
// longer exists in modern JDKs. Gradle is fine because RFG compiles build/rfg/minecraft-src/java with
// a real JDK 8 toolchain (compilePatchedMcJava), while this mod compiles with a modern one. Eclipse
// has a single JRE container for the whole project, so it cannot do that: whichever JDK is picked,
// that source folder fails. Workaround: keep a dummy ClassPatchManager in src/main/java so JDT can
// resolve the type, hide the real one from JDT, and keep the dummy out of the real build.
val dummyClassPatchManager = "cpw/mods/fml/common/patcher/ClassPatchManager.java"

// Filter on the TASK, not on sourceSets.main.java: a source-set filter is also exported to the
// generated Eclipse .classpath, which would exclude the dummy from Eclipse as well (that is why the
// exclusion kept "jumping" to src/main/java after every re-import).
tasks.compileJava {
    exclude(dummyClassPatchManager)
}

// GTNHGradle's IdeIntegrationModule defaults Eclipse to compliance/source = toolchain version (25),
// class file target = 1.8 and the JavaSE-1.8 JRE container. Pin everything to 21 instead.
// Declare the Eclipse-side exclusion in the build script too, so a Gradle re-import reproduces it
// instead of wiping a manually edited .classpath. Buildship applies whenMerged hooks to its model.
eclipse {
    jdt {
        sourceCompatibility = JavaVersion.VERSION_21 // -> compiler.source + compiler.compliance
        targetCompatibility = JavaVersion.VERSION_21 // -> compiler.codegen.targetPlatform
        javaRuntimeName = "JavaSE-21"                // -> JRE_CONTAINER in .classpath
    }
    classpath {
        file {
            whenMerged {
                val classpath = this as org.gradle.plugins.ide.eclipse.model.Classpath
                classpath.entries.filterIsInstance<org.gradle.plugins.ide.eclipse.model.SourceFolder>()
                    .forEach { folder ->
                        if (folder.path.contains("minecraft-src/java")) {
                            // real Forge source: invisible to JDT, still compiled by Gradle
                            if (!folder.excludes.contains(dummyClassPatchManager)) {
                                folder.excludes = folder.excludes + dummyClassPatchManager
                            }
                        } else {
                            // make sure the dummy always stays visible to JDT
                            folder.excludes = folder.excludes.filterNot { it == dummyClassPatchManager }
                        }
                    }
            }
        }
    }
}

group = "com.github.reobf"
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
