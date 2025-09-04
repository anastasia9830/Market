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

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
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

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}