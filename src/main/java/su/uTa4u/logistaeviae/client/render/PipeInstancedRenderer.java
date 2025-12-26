package su.uTa4u.logistaeviae.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.client.model.Quad;
import su.uTa4u.logistaeviae.mixin.ActiveRenderInfoAccessor;
import su.uTa4u.logistaeviae.mixin.ContainerLocalRenderInformationAccessor;
import su.uTa4u.logistaeviae.mixin.RenderGlobalAccessor;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

// Thanks tttsaurus for https://github.com/tttsaurus/Mc122RenderBook
public final class PipeInstancedRenderer {
    public static PipeInstancedRenderer instance;
    // Taken from org.lwjgl.opengl.APIUtil
    private static final int BUFFERS_SIZE = 32;
    private final IntBuffer intBuffer;
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

    private final FloatBuffer vertexBuffer;
    private final int program;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int projMatrixUniformLoc;
    private final int viewMatrixUniformLoc;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return;

        double partialTicks = event.getPartialTicks();
        ICamera camera = new Frustum();
        double cameraX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double cameraY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double cameraZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        camera.setPosition(cameraX, cameraY, cameraZ);

        RenderGlobalAccessor renderGlobalAccessor = ((RenderGlobalAccessor) event.getContext());

        List<TileEntityPipe> toRender = new ArrayList<>();
        for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobalAccessor.getRenderInfos()) {
            for (TileEntity te : ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().getCompiledChunk().getTileEntities()) {
                if (te instanceof TileEntityPipe && camera.isBoundingBoxInFrustum(te.getRenderBoundingBox())) {
                    toRender.add((TileEntityPipe) te);
                }
            }
        }
        if (toRender.isEmpty()) return;

        storeCommonGlStates();
        storeVertexObjects();
        storeProgram();

        glUseProgram(this.program);

        renderGlobalAccessor.getRenderEngine().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        glUniformMatrix4(this.projMatrixUniformLoc, false, ActiveRenderInfoAccessor.getProjMatrix());
        glUniformMatrix4(this.viewMatrixUniformLoc, false, ActiveRenderInfoAccessor.getViewMatrix());

        GlStateManager.disableCull();

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);

        for (TileEntityPipe te : toRender) {
            for (Quad quad : PipeModelManager.getQuadsForPipe(te).values()) {
                this.vertexBuffer.put(quad.pack(te.getPos(), (float) cameraX, (float) cameraY, (float) cameraZ)).flip();
                glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_DYNAMIC_DRAW);
                glDrawElements(GL_TRIANGLES, Quad.INDICES.length, GL_UNSIGNED_INT, 0);
                this.vertexBuffer.clear();
            }
        }

        restoreProgram();
        restoreVertexObjects();
        restoreCommonGlStates();
    }

    private void storeCommonGlStates() {
        glGetInteger(GL_TEXTURE_BINDING_2D, this.intBuffer);
        this.textureID = this.intBuffer.get(0);

        glGetFloat(GL_CURRENT_COLOR, this.floatBuffer);
        this.r = this.floatBuffer.get(0);
        this.g = this.floatBuffer.get(1);
        this.b = this.floatBuffer.get(2);
        this.a = this.floatBuffer.get(3);

        glGetInteger(GL_SHADE_MODEL, this.intBuffer);
        this.shadeModel = this.intBuffer.get(0);

        this.blend = glIsEnabled(GL_BLEND);
        this.lighting = glIsEnabled(GL_LIGHTING);
        this.texture2D = glIsEnabled(GL_TEXTURE_2D);
        this.alphaTest = glIsEnabled(GL_ALPHA_TEST);
        this.depthTest = glIsEnabled(GL_DEPTH_TEST);
        this.cullFace = glIsEnabled(GL_CULL_FACE);
    }

    private void restoreCommonGlStates() {
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

    private void storeVertexObjects() {
        this.prevVao = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        this.prevVbo = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        this.prevEbo = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
    }

    private void restoreVertexObjects() {
        glBindVertexArray(this.prevVao);
        glBindBuffer(GL_ARRAY_BUFFER, this.prevVbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.prevEbo);
    }

    private void storeProgram() {
        this.prevProgram = glGetInteger(GL_CURRENT_PROGRAM);
    }

    private void restoreProgram() {
        glUseProgram(this.prevProgram);
    }

    private String getShaderSource(String name) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new IOException();
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load shader file: " + name, e);
        }
    }

    private PipeInstancedRenderer() {
        this.intBuffer = BufferUtils.createIntBuffer(BUFFERS_SIZE);
        this.floatBuffer = BufferUtils.createFloatBuffer(BUFFERS_SIZE);
        this.vertexBuffer = BufferUtils.createFloatBuffer(Quad.VERTEX_COUNT * Quad.VERTEX_LENGHT);

        this.program = glCreateProgram();
        if (this.program == 0) {
            throw new RuntimeException("Could not create shader program");
        }

        int vertShaderID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShaderID, getShaderSource("assets/" + Tags.MOD_ID + "/shaders/pipe.vert"));
        glCompileShader(vertShaderID);

        if (glGetShaderi(vertShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Could not compile vertex shader: " + glGetShaderInfoLog(vertShaderID, 1024));
        }
        glAttachShader(this.program, vertShaderID);

        int fragShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShaderID, getShaderSource("assets/" + Tags.MOD_ID + "/shaders/pipe.frag"));
        glCompileShader(fragShaderID);

        if (glGetShaderi(fragShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Could not compile fragment shader: " + glGetShaderInfoLog(fragShaderID, 1024));
        }
        glAttachShader(this.program, fragShaderID);

        glLinkProgram(this.program);
        if (glGetProgrami(this.program, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Could not link shader program: " + glGetProgramInfoLog(this.program, 1024));
        }

        glDetachShader(this.program, vertShaderID);
        glDeleteShader(vertShaderID);
        glDetachShader(this.program, fragShaderID);
        glDeleteShader(fragShaderID);

        final String projMatrixUniformName = "projMatrix";
        this.projMatrixUniformLoc = glGetUniformLocation(this.program, projMatrixUniformName);
        if (this.projMatrixUniformLoc < 0) {
            throw new RuntimeException("Could not create uniform: " + projMatrixUniformName + " in shader program " + this.program);
        }

        final String viewMatrixUniformName = "viewMatrix";
        this.viewMatrixUniformLoc = glGetUniformLocation(this.program, viewMatrixUniformName);
        if (this.viewMatrixUniformLoc < 0) {
            throw new RuntimeException("Could not create uniform: " + viewMatrixUniformName + " in shader program " + this.program);
        }

        storeVertexObjects();

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(
                GL_ELEMENT_ARRAY_BUFFER,
                (IntBuffer) BufferUtils.createIntBuffer(Quad.INDICES.length).put(Quad.INDICES).flip(),
                GL_STATIC_DRAW
        );

        restoreVertexObjects();
    }

    public static void initInstance() {
        if (instance == null) {
            instance = new PipeInstancedRenderer();
        }
    }

}
