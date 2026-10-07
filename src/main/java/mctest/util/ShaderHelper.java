package mctest.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class ShaderHelper {
    private static int shaderProgram = -1;
    private static boolean isLoaded = false;
    private static boolean hasFailed = false;

    // Our own copy of the frame, so we never read and write the same texture
    private static int copyTexture = -1;
    private static int copyW = 0, copyH = 0;

    public static void loadShader() {
        if (isLoaded || hasFailed) return;
        try {
            int vs = compile(GL20.GL_VERTEX_SHADER, loadSource("shaders/sanity.vsh"), "VERTEX");
            int fs = compile(GL20.GL_FRAGMENT_SHADER, loadSource("shaders/sanity.fsh"), "FRAGMENT");
            if (vs == 0 || fs == 0) { hasFailed = true; return; }

            shaderProgram = GL20.glCreateProgram();
            GL20.glAttachShader(shaderProgram, vs);
            GL20.glAttachShader(shaderProgram, fs);
            GL20.glLinkProgram(shaderProgram);

            if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                System.err.println("[SANITY] LINK ERROR: " + GL20.glGetProgramInfoLog(shaderProgram, 4096));
                hasFailed = true;
                return;
            }

            GL20.glDetachShader(shaderProgram, vs);
            GL20.glDeleteShader(vs);
            GL20.glDetachShader(shaderProgram, fs);
            GL20.glDeleteShader(fs);

            isLoaded = true;
            System.out.println("[SANITY] Shader loaded!");
        } catch (Exception e) {
            System.err.println("[SANITY] Fatal error: " + e.getMessage());
            e.printStackTrace();
            hasFailed = true;
        }
    }

    private static int compile(int type, String src, String name) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("[SANITY] " + name + " SHADER ERROR:\n" + GL20.glGetShaderInfoLog(id, 4096));
            return 0;
        }
        return id;
    }

    private static String loadSource(String path) throws Exception {
        ResourceLocation loc = new ResourceLocation("mctest", path);
        InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder source = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) source.append(line).append("\n");
        reader.close();
        return source.toString();
    }

    private static void ensureCopyTexture(int w, int h) {
        if (copyTexture == -1) copyTexture = GL11.glGenTextures();
        if (w != copyW || h != copyH) {
            GlStateManager.bindTexture(copyTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            copyW = w;
            copyH = h;
        }
    }

    /** Call this with the GUI projection active (e.g. RenderGameOverlayEvent.Pre, ElementType.ALL). */
    public static void renderShaderOverlay() {
        if (!isLoaded || hasFailed) return;

        Minecraft mc = Minecraft.getMinecraft();
        int w = mc.displayWidth;
        int h = mc.displayHeight;
        ScaledResolution sr = new ScaledResolution(mc);

        // 1. Copy the current frame from the bound framebuffer into our own texture
        ensureCopyTexture(w, h);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(copyTexture);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, w, h);

        // 2. GL state for a clean fullscreen quad
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        // 3. Shader + uniforms
        GL20.glUseProgram(shaderProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "screenTexture"), 0);
        GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "viewWidth"), w);
        GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "viewHeight"), h);
        GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "frameTimeCounter"),
                (float) ((System.currentTimeMillis() % 1000000L) / 1000.0));

        // 4. Quad in SCALED GUI coordinates (FBO textures are bottom-up, hence v flipped)
        double sw = sr.getScaledWidth_double();
        double sh = sr.getScaledHeight_double();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(0,  sh, 0).tex(0, 0).endVertex();
        buf.pos(sw, sh, 0).tex(1, 0).endVertex();
        buf.pos(sw, 0,  0).tex(1, 1).endVertex();
        buf.pos(0,  0,  0).tex(0, 1).endVertex();
        tess.draw();

        // 5. Cleanup
        GL20.glUseProgram(0);
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}