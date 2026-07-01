plugins {
    id("com.gradleup.shadow")
}

tasks.shadowJar {
    archiveBaseName = "MySQLDatabaseExpansion"
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":plugin"))
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks.shadowJar {
    relocate("com.zaxxer.hikari", "net.godlycow.org.essc.expansion.mysql.libs.hikari")
}
