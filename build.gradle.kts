plugins {
    id("java")
    id("application")
}

group = "com.alexstrive"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "com.alexstrive.MainClass"
}

tasks.test {
    useJUnitPlatform()
}