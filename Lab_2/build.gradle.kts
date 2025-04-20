plugins {
    kotlin("jvm") version "1.9.22" // Стабильная версия вместо 2.0.20
    application // Для запуска main-класса
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21) // Явное указание JDK 21
}

dependencies {
    implementation("org.lwjgl:lwjgl:3.3.3")
    implementation("org.lwjgl:lwjgl-opengl:3.3.3")
    implementation("org.lwjgl:lwjgl-glfw:3.3.3")

    implementation("org.joml:joml:1.10.5")

    runtimeOnly("org.lwjgl:lwjgl:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.3:natives-windows")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.example.MainKt") // Укажите ваш main-класс
}

tasks.withType<JavaCompile> {
    options.release.set(21) // Компиляция под Java 21
}