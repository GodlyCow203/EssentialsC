plugins {
    java
}

group = "net.godlycow.org"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.release = 21
}

tasks.jar {
    archiveBaseName = "EssentialsCMigrationTest"
}
