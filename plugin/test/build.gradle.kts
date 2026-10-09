repositories {
    maven("https://repo.faststats.dev/releases")
}

dependencies {
    testImplementation(project(":api"))
    testImplementation(project(":plugin:essc"))
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    testImplementation("org.xerial:sqlite-jdbc:3.47.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

tasks.withType<org.gradle.api.tasks.testing.Test> {
    useJUnitPlatform()
}
