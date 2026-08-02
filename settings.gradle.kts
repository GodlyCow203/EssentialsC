plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "essentialsc"
include("api")
include("expansions:mysql")
include("plugin")
include("test-migration")
// include("module-test")
