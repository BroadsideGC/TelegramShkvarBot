import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    val kotlinVersion = "1.3.60"

    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}

plugins {
    kotlin("jvm") version "1.3.60"
    application
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("com.palantir.graal") version "0.6.0"
    id("com.hpe.kraal") version "0.0.15"
}

group = "TelegramShkvarBot"
version = "1.0-SNAPSHOT"


apply(plugin = "kotlinx-serialization")

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://jitpack.io")
    maven(url = "https://plugins.gradle.org/m2/")

}

application {
    mainClassName = "MainKt"
}

val fatjar by tasks.creating(Jar::class) {

    from(kraal.outputZipTrees) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }

    manifest {
        attributes("Main-Class" to "MainKt")
    }

    destinationDirectory.set(project.buildDir.resolve("fatjar"))
    archiveFileName.set("telegrambot.jar")
}

tasks.named("assemble").configure {
    dependsOn(fatjar)
}


val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shkvarbot")
        archiveVersion.set("")
        archiveClassifier.set("")
        mergeServiceFiles()
//        manifest {
//            attributes(mapOf("Main-Class" to "com.github.csolem.gradle.shadow.kotlin.example.App"))
//        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    //testImplementation group: "junit", name: "junit", version: "4.12"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    implementation("io.ktor:ktor-client-core-jvm:1.1.3")
    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
    compileOnly("com.oracle.substratevm:svm:19.2.0.1")
    compile("org.jetbrains.exposed", "exposed-core", "0.20.1")
    compile("org.jetbrains.exposed", "exposed-dao", "0.20.1")
    compile("org.jetbrains.exposed", "exposed-jdbc", "0.20.1")
    implementation("com.zaxxer:HikariCP:3.4.1")
    implementation("org.postgresql:postgresql:42.2.9")



    // logging
    implementation("org.slf4j:slf4j-api:1.6.1")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // telekt itself
    implementation("rocks.waffle.telekt:telekt:0.2.0")

    implementation("com.uchuhimo:konf:0.13.3")

}


