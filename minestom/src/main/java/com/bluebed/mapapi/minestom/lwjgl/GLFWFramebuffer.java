package com.bluebed.mapapi.minestom.lwjgl;

import net.minestom.server.map.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;

public class GLFWFramebuffer extends GLFWCapableBuffer implements Framebuffer {

    private final byte[] colors = new byte[WIDTH*HEIGHT];
    private final ByteBuffer pixels = BufferUtils.createByteBuffer(WIDTH*HEIGHT*4);

    public GLFWFramebuffer() {
        this(GLFW_NATIVE_CONTEXT_API, GLFW_OPENGL_API);
    }

    /**
     * Creates the framebuffer and initializes a new context
     */
    public GLFWFramebuffer(int apiContext, int clientAPI) {
        super(WIDTH, HEIGHT, apiContext, clientAPI);
    }

    @Override
    public byte[] toMapColors() {
        return colors;
    }
}