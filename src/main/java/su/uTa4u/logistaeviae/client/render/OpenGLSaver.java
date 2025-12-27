package su.uTa4u.logistaeviae.client.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public final class OpenGLSaver {

    private final FloatBuffer floatBuffer;
    private int textureID;
    private int shadeModel;
    private float r;
    private float g;
    private float b;
    private float a;
    private boolean blend;
    private boolean lighting;
    private boolean texture2D;
    private boolean alphaTest;
    private boolean depthTest;
    private boolean cullFace;

    private int prevProgram;
    private int prevVao;
    private int prevVbo;
    private int prevEbo;

    public OpenGLSaver() {
        this.floatBuffer = BufferUtils.createFloatBuffer(16);
    }

    public void storeCommonGlStates() {
        this.textureID = glGetInteger(GL_TEXTURE_BINDING_2D);

        glGetFloat(GL_CURRENT_COLOR, this.floatBuffer);
        this.r = this.floatBuffer.get(0);
        this.g = this.floatBuffer.get(1);
        this.b = this.floatBuffer.get(2);
        this.a = this.floatBuffer.get(3);

        this.shadeModel = glGetInteger(GL_SHADE_MODEL);

        this.blend = glIsEnabled(GL_BLEND);
        this.lighting = glIsEnabled(GL_LIGHTING);
        this.texture2D = glIsEnabled(GL_TEXTURE_2D);
        this.alphaTest = glIsEnabled(GL_ALPHA_TEST);
        this.depthTest = glIsEnabled(GL_DEPTH_TEST);
        this.cullFace = glIsEnabled(GL_CULL_FACE);
    }

    public void restoreCommonGlStates() {
        GlStateManager.bindTexture(this.textureID);
        GlStateManager.color(this.r, this.g, this.b, this.a);
        GlStateManager.shadeModel(this.shadeModel);

        if (this.blend) GlStateManager.enableBlend();
        else GlStateManager.disableBlend();

        if (this.lighting) GlStateManager.enableLighting();
        else GlStateManager.disableLighting();

        if (this.texture2D) GlStateManager.enableTexture2D();
        else GlStateManager.disableTexture2D();

        if (this.alphaTest) GlStateManager.enableAlpha();
        else GlStateManager.disableAlpha();

        if (this.depthTest) GlStateManager.enableDepth();
        else GlStateManager.disableDepth();

        if (this.cullFace) GlStateManager.enableCull();
        else GlStateManager.disableCull();
    }

    public void storeVertexObjects() {
        this.prevVao = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        this.prevVbo = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        this.prevEbo = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
    }

    public void restoreVertexObjects() {
        glBindVertexArray(this.prevVao);
        glBindBuffer(GL_ARRAY_BUFFER, this.prevVbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.prevEbo);
    }

    public void storeProgram() {
        this.prevProgram = glGetInteger(GL_CURRENT_PROGRAM);
    }

    public void restoreProgram() {
        glUseProgram(this.prevProgram);
    }

}
