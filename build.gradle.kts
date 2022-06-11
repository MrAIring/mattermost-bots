plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.kotlin.kapt") version "1.6.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.3.2"
    id("io.micronaut.aot") version "3.3.2"
    id("net.afanasev.sekret") version "0.1.1"
    id("org.flywaydb.flyway") version "8.5.10"
    id("nu.studer.jooq") version "7.1.1"
}

version = "0.1"
group = "io.github.mrairing.mattermost"

val kotlinVersion = project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

configurations {
    create("flywayMigration")
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.data:micronaut-data-processor")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.1")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.r2dbc:micronaut-r2dbc-core")
    implementation("io.micronaut.sql:micronaut-jooq")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut:micronaut-validation")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.h2database:h2")
    jooqGenerator("com.h2database:h2:1.4.200")
    add("flywayMigration", "com.h2database:h2:1.4.200")
    runtimeOnly("io.r2dbc:r2dbc-h2")


    compileOnly("org.graalvm.nativeimage:svm")
    compileOnly("net.afanasev:sekret-annotation:0.1.1")

    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
}


application {
    mainClass.set("io.github.mrairing.mattermost.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            javaParameters = true
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
            javaParameters = true
        }
    }
}
graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("io.github.mrairing.mattermost.*")
    }
    aot {
        // optimizations configuration
        optimizeServiceLoading.set(true)
        convertYamlToJava.set(true)
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
    }
}

flyway {
    configurations = arrayOf("flywayMigration")
    cleanOnValidationError = true
    url = "jdbc:h2:${project.buildDir.absolutePath}/flyway/jooq;AUTO_SERVER=TRUE"
    user = "sa"
    password = ""
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = flyway.url
                    user = flyway.user
                    password = flyway.password
                }
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "PUBLIC"
                        isOutputSchemaToDefault = true
                        includes = ".*"
                        excludes = "flyway_schema_history"
                    }
                    target.packageName = "io.github.mrairing.mattermost.jooq"
                }
            }
        }
    }
}

// configure jOOQ task such that it only executes when something has changed that potentially affects the generated JOOQ sources
// - the jOOQ configuration has changed (Jdbc, Generator, Strategy, etc.)
// - the classpath used to execute the jOOQ generation tool has changed (jOOQ library, database driver, strategy classes, etc.)
// - the schema files from which the schema is generated and which is used by jOOQ to generate the sources have changed (scripts added, modified, etc.)
tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq").configure {
    // ensure database schema has been prepared by Flyway before generating the jOOQ sources
    dependsOn(tasks.named("flywayMigrate"))

    // declare Flyway migration scripts as inputs on the jOOQ task
    inputs.files(
        fileTree("src/main/resources/db/migration"),
        fileTree("build/generated-src/jooq"),
        fileTree("build/flyway"),
    )
        .withPropertyName("migrations")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    // make jOOQ task participate in incremental builds (and build caching)
    allInputsDeclared.set(true)
}