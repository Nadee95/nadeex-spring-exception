plugins {
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version "3.2.3" apply false
    id("io.spring.dependency-management") version "1.1.4"
    jacoco
}

group = "com.nadeex.spring"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Credential resolution: local gradle.properties (gpr.user / gpr.key) → env vars → empty string
// Set gpr.user and gpr.key in ~/.gradle/gradle.properties for local publishing — never commit them here.
val githubUser: String = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR") ?: ""
val githubToken: String = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN") ?: ""

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages-Common"
        url = uri("https://maven.pkg.github.com/Nadee95/nadeex-spring-common")
        credentials {
            username = githubUser
            password = githubToken
        }
    }
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    api("com.nadeex.spring:common:0.1.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.nadeex.spring"
            artifactId = "exception"
            version = "0.1.0"
            from(components["java"])
            pom {
                name.set("Nadeex Spring Exception")
                description.set("Structured exception handling and error responses for Spring Boot applications")
                url.set("https://github.com/Nadee95/nadeex-spring-exception")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("nadee95")
                        name.set("Nadeeka Dilhan")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Nadee95/nadeex-spring-exception.git")
                    developerConnection.set("scm:git:ssh://github.com/Nadee95/nadeex-spring-exception.git")
                    url.set("https://github.com/Nadee95/nadeex-spring-exception")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Nadee95/nadeex-spring-exception")
            credentials {
                username = githubUser
                password = githubToken
            }
        }
    }
}
