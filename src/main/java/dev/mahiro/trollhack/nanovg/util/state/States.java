package dev.mahiro.trollhack.nanovg.util.state;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

public final class States {
    public static final States INSTANCE = new States();

    private final State state;

    private States() {
        this.state = new State(getGLVersion());
    }

    public void push() {
        state.push();
    }

    public void pop() {
        state.pop();
    }

    private static int getGLVersion() {
        GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL33) return 330;
        if (caps.OpenGL32) return 320;
        if (caps.OpenGL31) return 310;
        if (caps.OpenGL30) return 300;
        if (caps.OpenGL21) return 210;
        if (caps.OpenGL20) return 200;
        return 110;
    }
}

