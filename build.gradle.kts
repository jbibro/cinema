
plugins {
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.google.cloud.tools.jib") version "3.1.4"
}

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2021.0.0"
extra["testcontainersVersion"] = "1.16.2"

val asciidoctorExtensions: Configuration by configurations.creating

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.restdocs:spring-restdocs-webtestclient:2.0.6.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.32.0")
    asciidoctorExtensions("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks {
    val snippetsDir = file("$buildDir/generated-snippets")

    clean {
        delete("src/main/resources/static/docs")
    }

    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    test {
        useJUnitPlatform()
        systemProperty("org.springframework.restdocs.outputDir", snippetsDir)
        outputs.dir(snippetsDir)
    }

    build {
        dependsOn(asciidoctor)
    }

    asciidoctor {
        dependsOn(test)

        attributes(
            mapOf("snippets" to snippetsDir)
        )
        inputs.dir(snippetsDir)
        doFirst {
            delete("src/main/resources/static/docs")
        }
        doLast {
            copy {
                from(outputDir)
                into("src/main/resources/static/docs")
            }
        }
    }
}

jib {
    container {
        jvmFlags = listOf("-Dspring.profiles.active=docker")
    }
}
