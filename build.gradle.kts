
plugins {
    id("com.gtnewhorizons.gtnhconvention")
     id("maven-publish")
}

tasks.compileJava {
    // this is a dummy file to keep eclipse compiler shut, so do not actually compile it
    project.sourceSets {
        main {
            java {
                exclude("cpw/mods/fml/common/patcher/ClassPatchManager.java")
            }
        }
    }
}

group = "com.github.reobf"
version = "1.0.0"
