package dev.mahiro.trollhack.gui.clickgui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.mahiro.trollhack.nanovg.util.NanoVGHelper;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.NVG_IMAGE_FLIPY;
import static org.lwjgl.opengl.GL33C.*;

final class FramebufferBlur {
    static final FramebufferBlur INSTANCE = new FramebufferBlur();

    private int fbWidth;
    private int fbHeight;

    private int srcTex;
    private int pingTex;
    private int pongTex;
    private int pingFbo;
    private int pongFbo;

    private int vao;
    private int vbo;

    private int program;
    private int uTexture;
    private int uTexelSize;
    private int uDirection;
    private int uRadius;

    private int nvgImageId;
    private int nvgImageTexId;

    private FramebufferBlur() {
    }

    void ensureUpdated(long vg, int framebufferWidth, int framebufferHeight, int iterations, float radius) {
        RenderSystem.assertOnRenderThread();

        if (iterations <= 0 || radius <= 0.0f) return;
        if (framebufferWidth <= 0 || framebufferHeight <= 0) return;

        ensureResources(framebufferWidth, framebufferHeight);
        captureFramebufferToTexture();
        blurToPong(iterations, radius);
        ensureNvGImage(vg);
    }

    void drawFullScreen(float x, float y, float w, float h, float alpha) {
        if (nvgImageId == 0) return;
        if (alpha <= 0.0f) return;
        NanoVGHelper.drawImage(nvgImageId, x, y, w, h, alpha);
    }

    void drawRectScissored(float x, float y, float w, float h, float alpha, float trollWidth, float trollHeight) {
        if (nvgImageId == 0) return;
        if (alpha <= 0.0f) return;
        NanoVGHelper.save();
        NanoVGHelper.scissor(x, y, w, h);
        NanoVGHelper.drawImage(nvgImageId, 0.0f, 0.0f, trollWidth, trollHeight, alpha);
        NanoVGHelper.restore();
    }

    private void ensureResources(int width, int height) {
        if (width == fbWidth && height == fbHeight && program != 0 && vao != 0 && srcTex != 0 && pingTex != 0 && pongTex != 0) {
            return;
        }

        deleteResources();

        fbWidth = width;
        fbHeight = height;

        srcTex = createTexture(width, height);
        pingTex = createTexture(width, height);
        pongTex = createTexture(width, height);

        pingFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, pingFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, pingTex, 0);

        pongFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, pongFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, pongTex, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        float[] vertices = {
                -1.0f, -1.0f, 0.0f, 0.0f,
                1.0f, -1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 0.0f, 1.0f
        };
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        program = createProgram(VERT, FRAG);
        uTexture = glGetUniformLocation(program, "uTexture");
        uTexelSize = glGetUniformLocation(program, "uTexelSize");
        uDirection = glGetUniformLocation(program, "uDirection");
        uRadius = glGetUniformLocation(program, "uRadius");
    }

    private void ensureNvGImage(long vg) {
        if (nvgImageId != 0 && nvgImageTexId == pongTex) return;
        if (nvgImageId != 0) {
            NanoVG.nvgDeleteImage(vg, nvgImageId);
            nvgImageId = 0;
        }
        nvgImageId = NanoVGHelper.createImageFromTexture(pongTex, fbWidth, fbHeight, NVG_IMAGE_FLIPY);
        nvgImageTexId = pongTex;
    }

    private void captureFramebufferToTexture() {
        int prevTex = glGetInteger(GL_TEXTURE_BINDING_2D);
        glBindTexture(GL_TEXTURE_2D, srcTex);
        glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, fbWidth, fbHeight);
        glBindTexture(GL_TEXTURE_2D, prevTex);
    }

    private void blurToPong(int iterations, float radius) {
        int prevProgram = glGetInteger(GL_CURRENT_PROGRAM);
        int prevVao = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        int prevFbo = glGetInteger(GL_FRAMEBUFFER_BINDING);
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        boolean prevBlend = glIsEnabled(GL_BLEND);
        boolean prevDepth = glIsEnabled(GL_DEPTH_TEST);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        glUseProgram(program);
        glUniform1i(uTexture, 0);
        glUniform2f(uTexelSize, 1.0f / fbWidth, 1.0f / fbHeight);

        int inputTex = srcTex;
        for (int i = 0; i < iterations; i++) {
            float r = radius + i;

            glBindFramebuffer(GL_FRAMEBUFFER, pingFbo);
            glViewport(0, 0, fbWidth, fbHeight);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, inputTex);
            glUniform2f(uDirection, 1.0f, 0.0f);
            glUniform1f(uRadius, r);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 6);

            glBindFramebuffer(GL_FRAMEBUFFER, pongFbo);
            glViewport(0, 0, fbWidth, fbHeight);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, pingTex);
            glUniform2f(uDirection, 0.0f, 1.0f);
            glUniform1f(uRadius, r);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 6);

            inputTex = pongTex;
        }

        glBindVertexArray(prevVao);
        glUseProgram(prevProgram);
        glBindFramebuffer(GL_FRAMEBUFFER, prevFbo);
        glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        if (prevBlend) glEnable(GL_BLEND);
        if (prevDepth) glEnable(GL_DEPTH_TEST);
    }

    private int createTexture(int width, int height) {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0L);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        return tex;
    }

    private int createProgram(String vertSrc, String fragSrc) {
        int vs = compileShader(GL_VERTEX_SHADER, vertSrc);
        int fs = compileShader(GL_FRAGMENT_SHADER, fragSrc);
        int program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        glDeleteShader(vs);
        glDeleteShader(fs);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            glDeleteProgram(program);
            throw new IllegalStateException("Failed to link blur program: " + log);
        }
        return program;
    }

    private int compileShader(int type, String src) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException("Failed to compile blur shader: " + log);
        }
        return shader;
    }

    private void deleteResources() {
        if (program != 0) {
            glDeleteProgram(program);
            program = 0;
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
            vbo = 0;
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
            vao = 0;
        }
        if (pingFbo != 0) {
            glDeleteFramebuffers(pingFbo);
            pingFbo = 0;
        }
        if (pongFbo != 0) {
            glDeleteFramebuffers(pongFbo);
            pongFbo = 0;
        }
        if (srcTex != 0) {
            glDeleteTextures(srcTex);
            srcTex = 0;
        }
        if (pingTex != 0) {
            glDeleteTextures(pingTex);
            pingTex = 0;
        }
        if (pongTex != 0) {
            glDeleteTextures(pongTex);
            pongTex = 0;
        }
        fbWidth = 0;
        fbHeight = 0;
        nvgImageTexId = 0;
    }

    private static final String VERT = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            layout (location = 1) in vec2 aUv;
            out vec2 vUv;
            void main() {
              vUv = aUv;
              gl_Position = vec4(aPos, 0.0, 1.0);
            }
            """;

    private static final String FRAG = """
            #version 330 core
            in vec2 vUv;
            out vec4 FragColor;
            uniform sampler2D uTexture;
            uniform vec2 uTexelSize;
            uniform vec2 uDirection;
            uniform float uRadius;
            
            float weight(float x, float sigma) {
              return exp(-(x * x) / (2.0 * sigma * sigma));
            }
            
            void main() {
              float sigma = max(uRadius, 0.5);
              vec4 color = vec4(0.0);
              float sum = 0.0;
              int radius = int(clamp(uRadius, 1.0, 24.0));
              for (int i = -radius; i <= radius; i++) {
                float w = weight(float(i), sigma);
                vec2 uv = vUv + uDirection * uTexelSize * float(i);
                color += texture(uTexture, uv) * w;
                sum += w;
              }
              FragColor = color / max(sum, 0.00001);
            }
            """;
}
