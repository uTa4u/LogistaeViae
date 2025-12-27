package su.uTa4u.logistaeviae.client.render;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.client.model.PipeQuad;
import su.uTa4u.logistaeviae.mixin.ActiveRenderInfoAccessor;
import su.uTa4u.logistaeviae.mixin.ContainerLocalRenderInformationAccessor;
import su.uTa4u.logistaeviae.mixin.RenderGlobalAccessor;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL42.glDrawElementsInstancedBaseInstance;

// Thanks tttsaurus for https://github.com/tttsaurus/Mc122RenderBook
public final class PipeInstancedRenderer {
    public static PipeInstancedRenderer instance;

    private final OpenGLSaver glsaver;

    private final FloatBuffer vertexBuffer;
    private final int program;
    private final int vao;
    private final int baseInstancevbo;
    private final int instvbo;
    private final int ebo;
    private final int projMatrixUniformLoc;
    private final int viewMatrixUniformLoc;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return;
        TextureMap textureMap = mc.getTextureMapBlocks();

        double partialTicks = event.getPartialTicks();
        ICamera camera = new Frustum();
        double cameraX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double cameraY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double cameraZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        camera.setPosition(cameraX, cameraY, cameraZ);

        RenderGlobalAccessor renderGlobalAccessor = ((RenderGlobalAccessor) event.getContext());

        Byte2ObjectMap<List<TileEntityPipe>> pipeByType = new Byte2ObjectArrayMap<>();
        for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobalAccessor.getRenderInfos()) {
            for (TileEntity te : ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().getCompiledChunk().getTileEntities()) {
                if (te instanceof TileEntityPipe && camera.isBoundingBoxInFrustum(te.getRenderBoundingBox())) {
                    TileEntityPipe pipe = (TileEntityPipe) te;
                    pipeByType.computeIfAbsent(pipe.packConnections(), k -> new ArrayList<>()).add(pipe);
                }
            }
        }
        if (pipeByType.isEmpty()) return;

        this.glsaver.storeCommonGlStates();
        this.glsaver.storeVertexObjects();
        this.glsaver.storeProgram();

        glUseProgram(this.program);

        renderGlobalAccessor.getRenderEngine().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        glUniformMatrix4(this.projMatrixUniformLoc, false, ActiveRenderInfoAccessor.getProjMatrix());
        glUniformMatrix4(this.viewMatrixUniformLoc, false, ActiveRenderInfoAccessor.getViewMatrix());

        GlStateManager.disableCull();

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);

        for (Byte2ObjectMap.Entry<List<TileEntityPipe>> entry : pipeByType.byte2ObjectEntrySet()) {
            List<TileEntityPipe> pipes = entry.getValue();
            if (pipes.isEmpty()) continue;
            byte packedConnections = entry.getByteKey();

            this.vertexBuffer.clear();
            for (TileEntityPipe pipe : pipes) {
                TextureAtlasSprite tex = textureMap.getAtlasSprite(PipeModelManager.getTextureLoc(pipe));
                BlockPos pos = pipe.getPos();
                this.vertexBuffer.put((float) (pos.getX() - cameraX));
                this.vertexBuffer.put((float) (pos.getY() - cameraY));
                this.vertexBuffer.put((float) (pos.getZ() - cameraZ));
                this.vertexBuffer.put(tex.getMinU());
                this.vertexBuffer.put(tex.getMinV());
                this.vertexBuffer.put(tex.getMaxU());
                this.vertexBuffer.put(tex.getMaxV());
            }
            this.vertexBuffer.flip();
            glBindBuffer(GL_ARRAY_BUFFER, this.instvbo);
            glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_DYNAMIC_DRAW);

            final int indicesPerPipe = PipeQuad.INDICES.length * PipeModelManager.QUAD_COUNT;

            glDrawElementsInstancedBaseInstance(
                    GL_TRIANGLES,
                    indicesPerPipe,
                    GL_UNSIGNED_INT,
                    (long) packedConnections * indicesPerPipe * Integer.BYTES,
                    pipes.size(),
                    0
            );
        }

        this.glsaver.restoreProgram();
        this.glsaver.restoreVertexObjects();
        this.glsaver.restoreCommonGlStates();
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
        this.glsaver = new OpenGLSaver();
        // FIXME: batch rendering to always fit inside the buffer
        // TODO: try indirect rendering, watch vid by that lady about it first
        this.vertexBuffer = BufferUtils.createFloatBuffer(20480 * (PipeQuad.POS_COUNT + 4)); // 4 for uv bounds

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

        this.glsaver.storeVertexObjects();

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        int quadCount = PipeModelManager.BASE_INSTANCE_COUNT * PipeModelManager.QUAD_COUNT;
        ByteBuffer baseInstances = BufferUtils.createByteBuffer(quadCount * PipeQuad.VERTEX_COUNT * (1 + PipeQuad.POS_COUNT * Float.BYTES));
        for (byte i = 0; i < 64; i++) {
            for (Map.Entry<EnumFacing, PipeQuad> entry : PipeModelManager.getQuadsForPipe(i).entrySet()) {
                byte faceIndex = (byte) entry.getKey().getIndex();
                PipeQuad quad = entry.getValue();
                for (int j = 0; j < 4; j++) {
                    baseInstances.putFloat(quad.xs[j]);
                    baseInstances.putFloat(quad.ys[j]);
                    baseInstances.putFloat(quad.zs[j]);
                    baseInstances.put(faceIndex);
                }
            }
        }
        baseInstances.flip();
        this.baseInstancevbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.baseInstancevbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 1 + 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(1, 1, GL_BYTE, 1 + 3 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBufferData(GL_ARRAY_BUFFER, baseInstances, GL_STATIC_DRAW);

        this.instvbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.instvbo);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 7 * Float.BYTES, 0);
        glEnableVertexAttribArray(2);
        glVertexAttribDivisor(2, 1);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(3);
        glVertexAttribDivisor(3, 1);

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(quadCount * PipeQuad.INDICES.length);
        for (int quadIndex = 0; quadIndex < quadCount; quadIndex++) {
            int baseVertex = quadIndex * PipeQuad.VERTEX_COUNT;
            indexBuffer.put(baseVertex);
            indexBuffer.put(baseVertex + 1);
            indexBuffer.put(baseVertex + 2);
            indexBuffer.put(baseVertex);
            indexBuffer.put(baseVertex + 2);
            indexBuffer.put(baseVertex + 3);
        }
        indexBuffer.flip();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        this.glsaver.restoreVertexObjects();
    }

    public static void initInstance() {
        if (instance == null) {
            instance = new PipeInstancedRenderer();
        }
    }

}
