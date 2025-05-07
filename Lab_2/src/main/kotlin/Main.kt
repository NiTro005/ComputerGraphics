import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.*
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

fun main() {
    if (!glfwInit()) {
        throw RuntimeException("Failed to initialize GLFW")
    }

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    val window = glfwCreateWindow(800, 600, "Transparent Glass between Cubes", NULL, NULL)
        ?: throw RuntimeException("Failed to create GLFW window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    // Шейдер для обычных объектов
    val vertexShaderSource = """
        #version 330 core
        layout (location = 0) in vec3 aPos;
        layout (location = 1) in vec3 aNormal;

        out vec3 Normal;
        out vec3 FragPos;

        uniform mat4 projection;
        uniform mat4 view;
        uniform mat4 model;

        void main() {
            gl_Position = projection * view * model * vec4(aPos, 1.0);
            FragPos = vec3(model * vec4(aPos, 1.0));
            Normal = mat3(transpose(inverse(model))) * aNormal;
        }
    """.trimIndent()

    val fragmentShaderSource = """
        #version 330 core
        in vec3 Normal;
        in vec3 FragPos;

        out vec4 FragColor;

        uniform vec3 lightPos;
        uniform vec3 viewPos;
        uniform vec3 lightColor;
        uniform vec3 objectColor;

        void main() {
            vec3 norm = normalize(Normal);

            // Ambient
            float ambientStrength = 0.1;
            vec3 ambient = ambientStrength * lightColor;

            // Diffuse
            vec3 lightDir = normalize(lightPos - FragPos);
            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * lightColor;

            // Specular
            float specularStrength = 0.5;
            vec3 viewDir = normalize(viewPos - FragPos);
            vec3 reflectDir = reflect(-lightDir, norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
            vec3 specular = specularStrength * spec * lightColor;

            vec3 result = (ambient + diffuse + specular) * objectColor;
            FragColor = vec4(result, 1.0);
        }
    """.trimIndent()

    // Шейдер для стекла
    val glassVertexShaderSource = vertexShaderSource // Используем тот же вершинный шейдер

    val glassFragmentShaderSource = """
        #version 330 core
        in vec3 Normal;
        in vec3 FragPos;

        out vec4 FragColor;

        uniform vec3 lightPos;
        uniform vec3 viewPos;
        uniform vec3 glassColor;

        void main() {
            vec3 norm = normalize(Normal);
            vec3 viewDir = normalize(viewPos - FragPos);
            vec3 lightDir = normalize(lightPos - FragPos);

            // Эффект стекла с небольшой прозрачностью
            float transparency = 0.3;

            // Небольшое преломление (искажение)
            vec3 refracted = refract(lightDir, norm, 1.0 / 1.52);

            // Небольшое отражение
            vec3 reflected = reflect(lightDir, norm);
            float reflectivity = 0.2;

            // Основа стекла
            vec3 glassBase = glassColor * 0.8;

            // Финальный цвет с прозрачностью
            FragColor = vec4(mix(glassBase, reflected, reflectivity), transparency);
        }
    """.trimIndent()

    // Компилируем шейдеры
    val objectShader = createShaderProgram(vertexShaderSource, fragmentShaderSource)
    val glassShader = createShaderProgram(glassVertexShaderSource, glassFragmentShaderSource)

    val vertices = floatArrayOf(
        // Позиции          // Нормали
        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,

        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
        0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
        -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,

        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,

        0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
        0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
        0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
        0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
        0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
        0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,

        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
        0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,

        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
        -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f
    )

    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()

    glBindVertexArray(vao)
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0L)
    glEnableVertexAttribArray(0)

    glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, (3 * Float.SIZE_BYTES).toLong())
    glEnableVertexAttribArray(1)

    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glBindVertexArray(0)

    val projection = Matrix4f()
        .perspective(
            Math.toRadians(45.0).toFloat(),
            800f / 600f,
            0.1f,
            100.0f
        )

    val view = Matrix4f()
        .translate(0.0f, 0.0f, -5.0f)
        .rotateX(Math.toRadians(-20.0).toFloat())
        .rotateY(Math.toRadians(-30.0).toFloat())

    val lightPos = Vector3f(2.0f, 2.0f, 2.0f)
    val lightColor = Vector3f(1.0f, 1.0f, 1.0f)
    val objectColor = Vector3f(1.0f, 0.5f, 0.31f)
    val glassColor = Vector3f(0.7f, 0.9f, 1.0f) // Голубоватый цвет стекла

    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f)

        val time = glfwGetTime().toFloat()

        // Расчет радиуса орбиты и скорости
        val orbitRadius = 2.0f
        val orbitSpeed = 0.5f

        // Первый куб, вращающийся вокруг стеклянной панели
        val modelCube1 = Matrix4f()
            .translate(
                orbitRadius * Math.cos((time * orbitSpeed).toDouble()).toFloat(),
                0.0f,
                orbitRadius * Math.sin((time * orbitSpeed).toDouble()).toFloat()
            )
            .rotate(time, Vector3f(0.5f, 1.0f, 0.0f).normalize())
            .scale(0.8f)

        // Второй куб, вращающийся вокруг стеклянной панели в противоположном направлении
        val modelCube2 = Matrix4f()
            .translate(
                orbitRadius * Math.cos(time * orbitSpeed + Math.PI).toFloat(),
                0.0f,
                orbitRadius * Math.sin(time * orbitSpeed + Math.PI).toFloat()
            )
            .rotate(time * 0.7f, Vector3f(0.0f, 1.0f, 0.5f).normalize())
            .scale(0.8f)

        // Стеклянная панель в центре
        val modelGlass = Matrix4f()
            .translate(0.0f, 0.0f, 0.0f)
            .scale(0.05f, 2.0f, 2.0f)

        // Рисуем объекты
        glUseProgram(objectShader)

        val projectionLoc = glGetUniformLocation(objectShader, "projection")
        val viewLoc = glGetUniformLocation(objectShader, "view")
        val modelLoc = glGetUniformLocation(objectShader, "model")
        val lightPosLoc = glGetUniformLocation(objectShader, "lightPos")
        val viewPosLoc = glGetUniformLocation(objectShader, "viewPos")
        val lightColorLoc = glGetUniformLocation(objectShader, "lightColor")
        val objectColorLoc = glGetUniformLocation(objectShader, "objectColor")

        val viewPos = Vector3f()
        view.getTranslation(viewPos)
        viewPos.negate()

        glUniformMatrix4fv(projectionLoc, false, projection.get(FloatArray(16)))
        glUniformMatrix4fv(viewLoc, false, view.get(FloatArray(16)))
        glUniform3f(lightPosLoc, lightPos.x, lightPos.y, lightPos.z)
        glUniform3f(viewPosLoc, viewPos.x, viewPos.y, viewPos.z)
        glUniform3f(lightColorLoc, lightColor.x, lightColor.y, lightColor.z)

        // Первый куб
        glUniformMatrix4fv(modelLoc, false, modelCube1.get(FloatArray(16)))
        glUniform3f(objectColorLoc, objectColor.x, objectColor.y, objectColor.z)
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Второй куб (другого цвета)
        glUniformMatrix4fv(modelLoc, false, modelCube2.get(FloatArray(16)))
        glUniform3f(objectColorLoc, 0.5f, 0.8f, 0.31f)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Рисуем прозрачную стеклянную панель
        glUseProgram(glassShader)

        val glassProjectionLoc = glGetUniformLocation(glassShader, "projection")
        val glassViewLoc = glGetUniformLocation(glassShader, "view")
        val glassModelLoc = glGetUniformLocation(glassShader, "model")
        val glassLightPosLoc = glGetUniformLocation(glassShader, "lightPos")
        val glassViewPosLoc = glGetUniformLocation(glassShader, "viewPos")
        val glassColorLoc = glGetUniformLocation(glassShader, "glassColor")

        glUniformMatrix4fv(glassProjectionLoc, false, projection.get(FloatArray(16)))
        glUniformMatrix4fv(glassViewLoc, false, view.get(FloatArray(16)))
        glUniform3f(glassLightPosLoc, lightPos.x, lightPos.y, lightPos.z)
        glUniform3f(glassViewPosLoc, viewPos.x, viewPos.y, viewPos.z)
        glUniform3f(glassColorLoc, glassColor.x, glassColor.y, glassColor.z)

        // Стеклянная панель
        glUniformMatrix4fv(glassModelLoc, false, modelGlass.get(FloatArray(16)))
        glDrawArrays(GL_TRIANGLES, 0, 36)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    glDeleteVertexArrays(vao)
    glDeleteBuffers(vbo)
    glDeleteProgram(objectShader)
    glDeleteProgram(glassShader)

    glfwTerminate()
}

fun createShaderProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
    val shaderProgram = glCreateProgram()
    val vertexShader = compileShader(vertexShaderSource, GL_VERTEX_SHADER)
    val fragmentShader = compileShader(fragmentShaderSource, GL_FRAGMENT_SHADER)

    glAttachShader(shaderProgram, vertexShader)
    glAttachShader(shaderProgram, fragmentShader)
    glLinkProgram(shaderProgram)

    if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
        println("Shader program linking error: ${glGetProgramInfoLog(shaderProgram)}")
    }

    glDeleteShader(vertexShader)
    glDeleteShader(fragmentShader)

    return shaderProgram
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
