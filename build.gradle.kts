
plugins {
    id("com.gtnewhorizons.gtnhconvention")
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