plugins {
    kotlin("jvm") version "2.1.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.opengauss:opengauss-jdbc:6.0.0")
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets["main"].kotlin.srcDirs("src/main")

application {
    mainClass = "MainKt"
}



