package org.example

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.*
import org.joml.Matrix4f
import org.joml.Vector3f

fun main() {
    // Инициализация GLFW
    if (!glfwInit()) {
        throw RuntimeException("Failed to initialize GLFW")
    }

    // Настройка окна
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    val window = glfwCreateWindow(800, 600, "Colored 3D Cube", NULL, NULL)
        ?: throw RuntimeException("Failed to create GLFW window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glEnable(GL_DEPTH_TEST)

    // Вершинный шейдер
    val vertexShaderSource = """
        #version 330 core
        layout (location = 0) in vec3 aPos;
        layout (location = 1) in vec3 aColor;
        out vec3 ourColor;
        uniform mat4 projection;
        uniform mat4 view;
        uniform mat4 model;
        void main() {
            gl_Position = projection * view * model * vec4(aPos, 1.0);
            ourColor = aColor;
        }
    """.trimIndent()

    // Фрагментный шейдер
    val fragmentShaderSource = """
        #version 330 core
        in vec3 ourColor;
        out vec4 FragColor;
        void main() {
            FragColor = vec4(ourColor, 1.0);
        }
    """.trimIndent()

    // Компиляция шейдеров
    val shaderProgram = glCreateProgram()
    val vertexShader = compileShader(vertexShaderSource, GL_VERTEX_SHADER)
    val fragmentShader = compileShader(fragmentShaderSource, GL_FRAGMENT_SHADER)

    glAttachShader(shaderProgram, vertexShader)
    glAttachShader(shaderProgram, fragmentShader)
    glLinkProgram(shaderProgram)

    // Вершины куба с цветами (по 4 вершины на грань)
    val vertices = floatArrayOf(
        // Передняя грань (красная)
        -0.5f, -0.5f,  0.5f,  1.0f, 0.0f, 0.0f,
        0.5f, -0.5f,  0.5f,  1.0f, 0.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  1.0f, 0.0f, 0.0f,
        -0.5f,  0.5f,  0.5f,  1.0f, 0.0f, 0.0f,

        // Задняя грань (зеленая)
        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f, 0.0f,
        0.5f, -0.5f, -0.5f,  0.0f, 1.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  0.0f, 1.0f, 0.0f,
        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f, 0.0f,

        // Левая грань (синяя)
        -0.5f, -0.5f, -0.5f,  0.0f, 0.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f, 1.0f,
        -0.5f,  0.5f,  0.5f,  0.0f, 0.0f, 1.0f,
        -0.5f,  0.5f, -0.5f,  0.0f, 0.0f, 1.0f,

        // Правая грань (желтая)
        0.5f, -0.5f,  0.5f,  1.0f, 1.0f, 0.0f,
        0.5f, -0.5f, -0.5f,  1.0f, 1.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  1.0f, 1.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  1.0f, 1.0f, 0.0f,

        // Верхняя грань (голубая)
        -0.5f,  0.5f,  0.5f,  0.0f, 1.0f, 1.0f,
        0.5f,  0.5f,  0.5f,  0.0f, 1.0f, 1.0f,
        0.5f,  0.5f, -0.5f,  0.0f, 1.0f, 1.0f,
        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f, 1.0f,

        // Нижняя грань (пурпурная)
        -0.5f, -0.5f, -0.5f,  1.0f, 0.0f, 1.0f,
        0.5f, -0.5f, -0.5f,  1.0f, 0.0f, 1.0f,
        0.5f, -0.5f,  0.5f,  1.0f, 0.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,  1.0f, 0.0f, 1.0f
    )

    // Индексы вершин
    val indices = intArrayOf(
        0, 1, 2,  2, 3, 0,    // Передняя грань
        4, 5, 6,  6, 7, 4,    // Задняя грань
        8, 9,10, 10,11, 8,    // Левая грань
        12,13,14, 14,15,12,    // Правая грань
        16,17,18, 18,19,16,    // Верхняя грань
        20,21,22, 22,23,20     // Нижняя грань
    )

    // Создание VAO, VBO и EBO
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()
    val ebo = glGenBuffers()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

    // Атрибут позиции
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0L)
    glEnableVertexAttribArray(0)

    // Атрибут цвета
    glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, (3 * 4).toLong())
    glEnableVertexAttribArray(1)

    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glBindVertexArray(0)

    // Матрицы проекции и вида
    val projection = Matrix4f()
        .perspective(
            Math.toRadians(45.0).toFloat(),
            800f / 600f,
            0.1f,
            100.0f
        )

    val view = Matrix4f()
        .translate(0.0f, 0.0f, -3.0f)

    // Главный цикл рендеринга
    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        val model = Matrix4f()
            .rotate(
                glfwGetTime().toFloat() * 0.5f,
                Vector3f(0.5f, 1.0f, 0.0f)
            )

        glUseProgram(shaderProgram)

        // Получаем location uniform-переменных
        val projectionLoc = glGetUniformLocation(shaderProgram, "projection")
        val viewLoc = glGetUniformLocation(shaderProgram, "view")
        val modelLoc = glGetUniformLocation(shaderProgram, "model")

        glUniformMatrix4fv(projectionLoc, false, projection.get(FloatArray(16)))
        glUniformMatrix4fv(viewLoc, false, view.get(FloatArray(16)))
        glUniformMatrix4fv(modelLoc, false, model.get(FloatArray(16)))

        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    // Освобождение ресурсов
    glDeleteVertexArrays(vao)
    glDeleteBuffers(vbo)
    glDeleteBuffers(ebo)
    glDeleteProgram(shaderProgram)
    glDeleteShader(vertexShader)
    glDeleteShader(fragmentShader)

    glfwTerminate()
}

fun compileShader(source: String, type: Int): Int {
    val shader = glCreateShader(type)
    glShaderSource(shader, source)
    glCompileShader(shader)

    if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
        println("Shader compilation error: ${glGetShaderInfoLog(shader)}")
    }
    return shader
}