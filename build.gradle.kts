plugins {
    id("java")
    id("application")
    id("io.freefair.lombok") version "8.6"
}

group = "de.tub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

lombok {
    // Явно фиксируем современную версию, совместимую с JDK 21
    version.set("1.18.34")
}

dependencies {
    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("de.tub.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "de.tub.Main"
    }
}

// чтобы консоль работала при `gradlew run`
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
