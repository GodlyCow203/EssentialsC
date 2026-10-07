dependencies {
    testImplementation(project(":api"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

tasks.withType<org.gradle.api.tasks.testing.Test> {
    useJUnitPlatform()
}
