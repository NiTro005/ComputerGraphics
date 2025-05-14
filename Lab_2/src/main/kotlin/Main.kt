import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.*
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector2f
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

fun main() {
    if (!glfwInit()) {
        throw RuntimeException("Failed to initialize GLFW")
    }

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    val window = glfwCreateWindow(800, 600, "Textured Cube with OpenGL", NULL, NULL)
        ?: throw RuntimeException("Failed to create GLFW window")

    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    // Загрузка текстуры для правого куба
    val textureId = try {
        loadTexture("C:\\Sourse\\github\\ComputerGraphics\\Lab_2\\src\\main\\resources\\min.jpg") // Убедитесь, что файл существует в папке textures/
    } catch (e: Exception) {
        println("Ошибка загрузки текстуры: ${e.message}")
        -1
    }

    // Ваши оригинальные шейдеры (только добавил текстуру во фрагментный для объектов)
    val vertexShaderSource = """
        #version 330 core
        layout (location = 0) in vec3 aPos;
        layout (location = 1) in vec3 aNormal;
        layout (location = 2) in vec2 aTexCoords;

        out vec3 Normal;
        out vec3 FragPos;
        out vec2 TexCoords;

        uniform mat4 projection;
        uniform mat4 view;
        uniform mat4 model;

        void main() {
            gl_Position = projection * view * model * vec4(aPos, 1.0);
            FragPos = vec3(model * vec4(aPos, 1.0));
            Normal = mat3(transpose(inverse(model))) * aNormal;
            TexCoords = aTexCoords;
        }
    """.trimIndent()

    val fragmentShaderSource = """
        #version 330 core
        in vec3 Normal;
        in vec3 FragPos;
        in vec2 TexCoords;

        out vec4 FragColor;

        uniform vec3 lightPos;
        uniform vec3 viewPos;
        uniform vec3 lightColor;
        uniform vec3 objectColor;
        uniform bool isMetallic;
        uniform sampler2D texture_diffuse;

        void main() {
            vec3 norm = normalize(Normal);
            vec3 lightDir = normalize(lightPos - FragPos);
            vec3 viewDir = normalize(viewPos - FragPos);

            // Ambient
            float ambientStrength = 0.1;
            vec3 ambient = ambientStrength * lightColor;

            // Diffuse
            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * lightColor;

            // Specular
            float specularStrength = isMetallic ? 1.0 : 0.5;
            vec3 reflectDir = reflect(-lightDir, norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
            vec3 specular = specularStrength * spec * lightColor;

            // Текстура (только для правого куба)
            vec3 texColor = texture(texture_diffuse, TexCoords).rgb;
            vec3 result = (ambient + diffuse + specular) * (isMetallic ? objectColor : mix(objectColor, texColor, 0.8));

            FragColor = vec4(result, 1.0);
        }
    """.trimIndent()

    // Ваши оригинальные шейдеры для стекла (без изменений)
    val glassVertexShaderSource = """
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

            float transparency = 0.3;
            vec3 glassBase = glassColor * 0.8;

            float ratio = 1.0 / 1.52;
            vec3 refractedLight = vec3(0.7, 0.8, 1.0);

            float fresnel = pow(1.0 - dot(viewDir, norm), 2.0);
            vec3 fresnelEffect = fresnel * vec3(1.0, 1.0, 1.0);

            float specularStrength = 1.0;
            vec3 reflectDir = reflect(-lightDir, norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
            vec3 specular = specularStrength * spec * vec3(1.0, 1.0, 1.0);

            vec3 result = glassBase + refractedLight * 0.5 + fresnelEffect + specular;
            FragColor = vec4(result, transparency);
        }
    """.trimIndent()

    val objectShader = createShaderProgram(vertexShaderSource, fragmentShaderSource)
    val glassShader = createShaderProgram(glassVertexShaderSource, glassFragmentShaderSource)

    // Вершины куба с текстурными координатами (добавлены в конец)
    val vertices = floatArrayOf(
        // Передняя грань
        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
        0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,

        // Задняя грань
        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,
        0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,
        -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,

        // Левая грань
        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
        -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
        -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

        // Правая грань
        0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
        0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
        0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
        0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

        // Нижняя грань
        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
        0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,

        // Верхняя грань
        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
        -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f
    )

    // Создание VAO и VBO
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()

    glBindVertexArray(vao)
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    // Позиции
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.SIZE_BYTES, 0L)
    glEnableVertexAttribArray(0)

    // Нормали
    glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.SIZE_BYTES, (3 * Float.SIZE_BYTES).toLong())
    glEnableVertexAttribArray(1)

    // Текстурные координаты (новый атрибут)
    glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.SIZE_BYTES, (6 * Float.SIZE_BYTES).toLong())
    glEnableVertexAttribArray(2)

    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glBindVertexArray(0)

    // Матрицы и параметры освещения (как у вас было)
    val projection = Matrix4f()
        .perspective(Math.toRadians(45.0).toFloat(), 800f / 600f, 0.1f, 100.0f)

    var view = Matrix4f()
        .translate(0.0f, 0.0f, -7.0f)
        .rotateX(Math.toRadians(-15.0).toFloat())
        .rotateY(Math.toRadians(20.0).toFloat())

    var lightPos = Vector3f(2.0f, 2.0f, 2.0f)
    val lightColor = Vector3f(1.0f, 1.0f, 1.0f)
    val objectColor = Vector3f(1.0f, 0.5f, 0.31f)
    val glassColor = Vector3f(0.7f, 0.9f, 1.0f)

    // Управление (как у вас было)
    var isWPressed = false
    var isSPressed = false
    var isAPressed = false
    var isDPressed = false

    var lastMousePos = Vector2f()
    var isFirstMouse = true

    var yawSpeed = 0.0f
    var pitchSpeed = 0.0f
    val maxSpeed = 0.01f
    val acceleration = 0.0005f
    val deceleration = 0.0002f

    var lightYaw = 0.0f
    var lightPitch = 0.0f

    glfwSetKeyCallback(window) { _, key, _, action, _ ->
        when (key) {
            GLFW_KEY_W -> isWPressed = action != GLFW_RELEASE
            GLFW_KEY_S -> isSPressed = action != GLFW_RELEASE
            GLFW_KEY_A -> isAPressed = action != GLFW_RELEASE
            GLFW_KEY_D -> isDPressed = action != GLFW_RELEASE
        }
    }

    glfwSetCursorPosCallback(window) { _, xpos, ypos ->
        if (isFirstMouse) {
            lastMousePos.set(xpos.toFloat(), ypos.toFloat())
            isFirstMouse = false
        }

        var xoffset = xpos.toFloat() - lastMousePos.x
        var yoffset = lastMousePos.y - ypos.toFloat()
        lastMousePos.set(xpos.toFloat(), ypos.toFloat())

        val sensitivity = 0.1f
        xoffset *= sensitivity
        yoffset *= sensitivity

        yawSpeed = xoffset
        pitchSpeed = yoffset
    }

    // Основной цикл рендеринга
    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f)

        if (isWPressed) pitchSpeed += acceleration
        if (isSPressed) pitchSpeed -= acceleration
        if (isAPressed) yawSpeed -= acceleration
        if (isDPressed) yawSpeed += acceleration

        if (!isWPressed && !isSPressed) {
            if (pitchSpeed > 0) pitchSpeed = maxOf(0.0f, pitchSpeed - deceleration)
            if (pitchSpeed < 0) pitchSpeed = minOf(0.0f, pitchSpeed + deceleration)
        }

        if (!isAPressed && !isDPressed) {
            if (yawSpeed > 0) yawSpeed = maxOf(0.0f, yawSpeed - deceleration)
            if (yawSpeed < 0) yawSpeed = minOf(0.0f, yawSpeed + deceleration)
        }

        pitchSpeed = maxOf(-maxSpeed, minOf(maxSpeed, pitchSpeed))
        yawSpeed = maxOf(-maxSpeed, minOf(maxSpeed, yawSpeed))

        lightYaw += yawSpeed
        lightPitch += pitchSpeed

        lightPos.set(
            2.0f * kotlin.math.cos(lightYaw.toDouble()).toFloat() * kotlin.math.cos(lightPitch.toDouble()).toFloat(),
            2.0f * kotlin.math.sin(lightPitch.toDouble()).toFloat(),
            2.0f * kotlin.math.sin(lightYaw.toDouble()).toFloat() * kotlin.math.cos(lightPitch.toDouble()).toFloat()
        )

        // Отрисовка объектов
        glUseProgram(objectShader)

        val projectionLoc = glGetUniformLocation(objectShader, "projection")
        val viewLoc = glGetUniformLocation(objectShader, "view")
        val modelLoc = glGetUniformLocation(objectShader, "model")
        val lightPosLoc = glGetUniformLocation(objectShader, "lightPos")
        val viewPosLoc = glGetUniformLocation(objectShader, "viewPos")
        val lightColorLoc = glGetUniformLocation(objectShader, "lightColor")
        val objectColorLoc = glGetUniformLocation(objectShader, "objectColor")
        val isMetallicLoc = glGetUniformLocation(objectShader, "isMetallic")

        val viewPos = Vector3f()
        view.getTranslation(viewPos)
        viewPos.negate()

        glUniformMatrix4fv(projectionLoc, false, projection.get(FloatArray(16)))
        glUniformMatrix4fv(viewLoc, false, view.get(FloatArray(16)))
        glUniform3f(lightPosLoc, lightPos.x, lightPos.y, lightPos.z)
        glUniform3f(viewPosLoc, viewPos.x, viewPos.y, viewPos.z)
        glUniform3f(lightColorLoc, lightColor.x, lightColor.y, lightColor.z)

        // Левый куб (металлический)
        val modelCube1 = Matrix4f()
            .translate(-1.5f, 0.0f, 0.0f)
            .scale(0.8f)

        glUniformMatrix4fv(modelLoc, false, modelCube1.get(FloatArray(16)))
        glUniform3f(objectColorLoc, 0.8f, 0.8f, 0.8f)
        glUniform1i(isMetallicLoc, 1)
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Правый куб (с текстурой)
        val modelCube2 = Matrix4f()
            .translate(1.5f, 0.0f, 0.0f)
            .scale(0.8f)

        if (textureId != -1) {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, textureId)
            glUniform1i(glGetUniformLocation(objectShader, "texture_diffuse"), 0)
        }

        glUniformMatrix4fv(modelLoc, false, modelCube2.get(FloatArray(16)))
        glUniform3f(objectColorLoc, 1.0f, 1.0f, 1.0f) // Белый цвет для правильного отображения текстуры
        glUniform1i(isMetallicLoc, 0)
        glDrawArrays(GL_TRIANGLES, 0, 36)

        // Стеклянная панель (как у вас было)
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

        val modelGlass = Matrix4f()
            .translate(0.0f, 0.0f, 0.0f)
            .scale(0.05f, 2.0f, 2.0f)

        glUniformMatrix4fv(glassModelLoc, false, modelGlass.get(FloatArray(16)))
        glDrawArrays(GL_TRIANGLES, 0, 36)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    // Освобождение ресурсов
    glDeleteVertexArrays(vao)
    glDeleteBuffers(vbo)
    if (textureId != -1) glDeleteTextures(textureId)
    glDeleteProgram(objectShader)
    glDeleteProgram(glassShader)
    glfwTerminate()
}

fun loadTexture(path: String): Int {
    MemoryStack.stackPush().use { stack ->
        val w = stack.mallocInt(1)
        val h = stack.mallocInt(1)
        val channels = stack.mallocInt(1)

        val image = stbi_load(path, w, h, channels, STBI_rgb_alpha)
            ?: throw RuntimeException("Failed to load texture: $path (${stbi_failure_reason()})")

        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)

        glTexImage2D(
            GL_TEXTURE_2D, 0, GL_RGBA,
            w.get(), h.get(), 0,
            GL_RGBA, GL_UNSIGNED_BYTE, image
        )
        glGenerateMipmap(GL_TEXTURE_2D)

        stbi_image_free(image)

        // Настройки текстуры
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        return texture
    }
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