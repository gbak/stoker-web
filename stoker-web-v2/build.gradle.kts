plugins {
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
}

group = "com.gbak"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("commons-net:commons-net:3.11.1")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ─── Frontend build ────────────────────────────────────────────────────────────

val frontendDir = file("frontend")

val npm = if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd" else "npm"

val npmInstall by tasks.registering(Exec::class) {
    description = "Install frontend npm dependencies"
    workingDir = frontendDir
    inputs.file("frontend/package-lock.json")
    outputs.dir("frontend/node_modules")
    commandLine(npm, "install")
}

val buildFrontend by tasks.registering(Exec::class) {
    description = "Build the React frontend with Vite"
    dependsOn(npmInstall)
    workingDir = frontendDir
    inputs.dir("frontend/src")
    inputs.files("frontend/index.html", "frontend/vite.config.ts", "frontend/tsconfig.json")
    outputs.dir("src/main/resources/static")
    commandLine(npm, "run", "build")
}

tasks.named("processResources") {
    dependsOn(buildFrontend)
}
