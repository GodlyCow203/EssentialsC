plugins {
    id("com.gradleup.shadow")
}

tasks.shadowJar {
    archiveBaseName = "EssentialsCProfiles"
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":plugin"))
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.godlycow.org.expansions.profiles.EssentialsCProfiles"
    }
}
