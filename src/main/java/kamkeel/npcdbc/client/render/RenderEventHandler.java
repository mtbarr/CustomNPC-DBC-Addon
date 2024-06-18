package kamkeel.npcdbc.client.render;

import JinRyuu.JBRA.RenderPlayerJBRA;
import JinRyuu.JRMCore.entity.EntityCusPar;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import kamkeel.npcdbc.CustomNpcPlusDBC;
import kamkeel.npcdbc.client.shader.PostProcessing;
import kamkeel.npcdbc.client.shader.ShaderHelper;
import kamkeel.npcdbc.config.ConfigDBCClient;
import kamkeel.npcdbc.data.dbcdata.DBCData;
import kamkeel.npcdbc.data.npc.DBCDisplay;
import kamkeel.npcdbc.entity.EntityAura;
import kamkeel.npcdbc.mixins.early.IEntityMC;
import kamkeel.npcdbc.mixins.late.INPCDisplay;
import kamkeel.npcdbc.mixins.late.IRenderCusPar;
import kamkeel.npcdbc.scripted.DBCPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.Sphere;

import java.nio.FloatBuffer;
import java.util.Iterator;

import static kamkeel.npcdbc.client.shader.PostProcessing.processBloom;
import static org.lwjgl.opengl.GL11.*;

public class RenderEventHandler {
    public static final int TAIL_STENCIL_ID = 2;
    public static FloatBuffer PRE_RENDER_MODELVIEW = BufferUtils.createFloatBuffer(16);

    @SubscribeEvent
    public void enablePlayerStencil(RenderPlayerEvent.Pre e) {
        glClear(GL_STENCIL_BUFFER_BIT); //TODO: needs to be put somewhere else i.e RenderWorldLastEvent, but for some reason doesn't work when put there
        glEnable(GL_STENCIL_TEST);
        enableStencilWriting(e.entity.getEntityId());
    }

    @SubscribeEvent
    public void enableHandStencil(DBCPlayerEvent.RenderArmEvent.Pre e) {
        glClear(GL_STENCIL_BUFFER_BIT); //TODO: needs to be put somewhere else i.e RenderWorldLastEvent, but for some reason doesn't work when put there
        glEnable(GL_STENCIL_TEST);
        enableStencilWriting(e.entity.getEntityId());
    }

    @SubscribeEvent
    public void enableEntityStencil(RenderLivingEvent.Pre e) {
        if ((e.entity instanceof EntityPlayer)) {
            //IMPORTANT, SAVES THE MODEL VIEW MATRIX PRE ENTITYLIVING TRANSFORMATIONS
            glGetFloat(GL_MODELVIEW_MATRIX, PRE_RENDER_MODELVIEW);
        } else if ((e.entity instanceof EntityNPCInterface)) {
            glGetFloat(GL_MODELVIEW_MATRIX, PRE_RENDER_MODELVIEW);
            glClear(GL_STENCIL_BUFFER_BIT); //TODO: needs to be put somewhere else i.e RenderWorldLastEvent, but for some reason doesn't work when put there
            glEnable(GL_STENCIL_TEST);
            enableStencilWriting(e.entity.getEntityId());
            Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
        }
        glDepthMask(true); //fixes a native MC RP1 entity bug in which the depth test is disabled
    }

    @SubscribeEvent
    public void renderNPC(RenderLivingEvent.Post e) {
        if (!(e.entity instanceof EntityNPCInterface))
            return;

        EntityNPCInterface entity = (EntityNPCInterface) e.entity;
        RenderCustomNpc r = (RenderCustomNpc) e.renderer;
        float partialTicks = Minecraft.getMinecraft().timer.renderPartialTicks;

        disableStencilWriting(entity.getEntityId(), false);
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
        DBCDisplay display = ((INPCDisplay) entity.display).getDBCDisplay();


        ////////////////////////////////////////
        ////////////////////////////////////////
        //Aura
        EntityAura aura = display.auraEntity;
        if (aura != null && aura.shouldRender()) {
            glPushMatrix();
            glLoadMatrix(PRE_RENDER_MODELVIEW); //RESETS TRANSFORMATIONS DONE TO CURRENT MATRIX TO PRE-ENTITY RENDERING STATE
            AuraRenderer.Instance.renderAura(aura, partialTicks);
            glPopMatrix();
        }


        ////////////////////////////////////////
        ////////////////////////////////////////
        //Custom Particles
        glPushMatrix();
        glLoadMatrix(PRE_RENDER_MODELVIEW); //IMPORTANT, PARTICLES WONT ROTATE PROPERLY WITHOUT THIS
        IRenderCusPar particleRender = null;
        for (Iterator<EntityCusPar> iter = display.particleRenderQueue.iterator(); iter.hasNext(); ) {
            EntityCusPar particle = iter.next();
            if (particleRender == null)
                particleRender = (IRenderCusPar) RenderManager.instance.getEntityRenderObject(particle);

            particleRender.renderParticle(particle, partialTicks);
            if (particle.isDead)
                iter.remove();
        }
        glPopMatrix();


        ////////////////////////////////////////
        ////////////////////////////////////////
        //Outline

        ////////////////////////////////////////
        ////////////////////////////////////////
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
        enableStencilWriting(e.entity.getEntityId());
        // postStencilRendering();//LETS YOU DRAW TO THE COLOR BUFFER AGAIN
        glClear(GL_STENCIL_BUFFER_BIT); //TODO: needs to be put somewhere else i.e RenderWorldLastEvent, but for some reason doesn't work when put there
        glDisable(GL_STENCIL_TEST);

    }


    public void renderPlayer(EntityPlayer player, Render renderer, float partialTicks, boolean isArm) {

        ShaderHelper.releaseShader();
        RenderPlayerJBRA render = (RenderPlayerJBRA) renderer;
        DBCData data = DBCData.get(player);

        disableStencilWriting(player.getEntityId(), false);
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
        EntityAura aura = data.auraEntity;
        if (ConfigDBCClient.EnableBloom) {
            PostProcessing.drawToBuffers(0, 2);
            processBloom = true;
        }

        glPushMatrix(); glDepthMask(false);
        ClientProxy.bindTexture(new ResourceLocation(CustomNpcPlusDBC.ID + ":textures/gui/aura.png"));
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE,GL_ONE_MINUS_DST_COLOR);
        glColor4f(1, 1f, 1.0f, 1f);
        glTranslatef(3.95F, -1, 0);
        glScalef(3f, 3f, 3f);
        glRotatef(180, 0, 0, 1);
        glRotatef(180, 0, 1, 0);
        PostProcessing.renderQuad(-1, -2, -1, 2, 1);
        glPopMatrix();
        glPushMatrix();
        ClientProxy.bindTexture(new ResourceLocation(CustomNpcPlusDBC.ID + ":textures/gui/aura.png"));
        glColor4f(0f, 0f, 0f, 1.0f);
      //  glDepthMask(false);
         glTranslatef(-0.025f,-0.65f,0);
        glScalef(5f, 2.5f, 5f);
        glRotatef(180, 0, 0, 1);
        glRotatef(180, 0, 1, 0);
     //   PostProcessing.renderQuad(-1, -1, -1, 1, 1);
       glDepthMask(true);
        glPopMatrix();

        ////////////////////////////////////////
        ////////////////////////////////////////
        //Aura
        if (aura != null && aura.shouldRender()) {
            glPushMatrix();
            glLoadMatrix(PRE_RENDER_MODELVIEW); //RESETS TRANSFORMATIONS DONE TO CURRENT MATRIX TO PRE-ENTITY RENDERING STATE
            AuraRenderer.Instance.renderAura(aura, partialTicks);
            glPopMatrix();
        }


        ////////////////////////////////////////
        ////////////////////////////////////////
        //Custom Particles
        glPushMatrix();

        glLoadMatrix(PRE_RENDER_MODELVIEW); //IMPORTANT, PARTICLES WONT ROTATE PROPERLY WITHOUT THIS
        IRenderCusPar particleRender = null;
        for (Iterator<EntityCusPar> iter = data.particleRenderQueue.iterator(); iter.hasNext(); ) {
            EntityCusPar particle = iter.next();
            if (particleRender == null)
                particleRender = (IRenderCusPar) RenderManager.instance.getEntityRenderObject(particle);

            particleRender.renderParticle(particle, partialTicks);
            if (particle.isDead)
                iter.remove();
        }
        glPopMatrix();


        ////////////////////////////////////////
        ////////////////////////////////////////
        //Outline
        data.outline = new PlayerOutline(0x00ffff, 0xffffff);
        //  data.outline = null;
        if (data.outline != null) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            Sphere s = new Sphere();
            int sphereTrans = 10;
            glTranslatef(0, 0, sphereTrans);
            //    s.draw(6, 36, 18);
            glTranslatef(0, 0, -sphereTrans);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            PlayerOutline.renderOutline(render, player, partialTicks, isArm);
        } else if (aura == null && ((IEntityMC) player).getRenderPassTampered()) {
            ((IEntityMC) player).setRenderPass(0);
        }


        ////////////////////////////////////////
        ////////////////////////////////////////
        if (processBloom)
            PostProcessing.resetDrawBuffer();
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
        //  postStencilRendering();//LETS YOU DRAW TO THE COLOR BUFFER AGAIN
        enableStencilWriting(player.getEntityId());

    }

    @SubscribeEvent
    public void renderPlayer(DBCPlayerEvent.RenderEvent.Pre e) {
        EntityAura aura = DBCData.get(e.entityPlayer).auraEntity;
        if ((aura != null && aura.shouldRender()) || DBCData.get(e.entityPlayer).outline != null)
            Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
    }

    @SubscribeEvent
    public void renderPlayer(DBCPlayerEvent.RenderEvent.Post e) {
        renderPlayer(e.entityPlayer, e.renderer, e.partialRenderTick, false);
    }

    @SubscribeEvent
    public void renderHand(DBCPlayerEvent.RenderArmEvent.Post e) {
        renderPlayer(e.entityPlayer, e.renderer, e.partialRenderTick, true);
    }

    public static void newerAuraTemp(EntityAura aura, float partialTicks) {
        double interPosX = (aura.lastTickPosX + (aura.posX - aura.lastTickPosX) * (double) partialTicks) - RenderManager.renderPosX;
        double interPosY = (aura.lastTickPosY + (aura.posY - aura.lastTickPosY) * (double) partialTicks) - RenderManager.renderPosY;
        double interPosZ = (aura.lastTickPosZ + (aura.posZ - aura.lastTickPosZ) * (double) partialTicks) - RenderManager.renderPosZ;
        ImageData tex = new ImageData(CustomNpcPlusDBC.ID + ":textures/aura/aura.png");
        float scale = 2.00f;

        glPushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_LIGHTING);
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);

        glScalef(scale, scale, scale);
        glTranslated(interPosX, interPosY - 0.65f, interPosZ - 0.025f);
        glTranslatef(0f, 0, -0.35f);
        glRotatef(180, 0, 0, 1);
        glRotatef(315, 1, 0, 0);

        for (float j = 1; j < 2; j += 1) {
            glPushMatrix();
            glRotatef(360 * j, 0F, 0F, 1F);
            renderImage(tex, aura.color1, 0.2f);
            glPopMatrix();
        }

        // Reset OpenGL states
        glDisable(GL_BLEND);
        glEnable(GL_LIGHTING);
        glPopMatrix();
    }

    public static void renderImage(ImageData imageData, int color, float alpha) {
        if (!imageData.imageLoaded())
            return;

        glPushMatrix();
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;
        glColor4f(red, green, blue, alpha);

        for (int i = 0; i < 2; i++) {
            boolean front = i == 1;
            Tessellator tessellator = Tessellator.instance;
            if (front) {
                glRotatef(180, 0, 0, 1.0f);
            }

            imageData.bindTexture();

            int totalWidth = imageData.getTotalWidth();
            int totalHeight = imageData.getTotalHeight();
            float u1 = 0.0F;
            float u2 = 1.0F;
            float v1 = 0.0F;
            float v2 = 1.0F;

            float textureXScale = 1.0F, textureYScale = 1.0F;
            if (totalWidth > totalHeight) {
                textureYScale = (float) totalHeight / totalWidth;
                glScalef(1 / textureYScale / 2, 1 / textureYScale / 2, 1 / textureYScale / 2);
            } else if (totalHeight > totalWidth) {
                textureXScale = (float) totalWidth / totalHeight;
                glScalef(1 / textureXScale / 2, 1 / textureXScale / 2, 1 / textureXScale / 2);
            }

            tessellator.startDrawingQuads();
            tessellator.setBrightness(240);
            tessellator.setColorOpaque_F(1, 1, 1);
            tessellator.setColorRGBA_F(red, green, blue, alpha);
            tessellator.addVertexWithUV(textureXScale * (u2 - u1) / 2, 0, textureYScale * (v2 - v1) / 2, u2, v2);
            tessellator.addVertexWithUV(textureXScale * (u2 - u1) / 2, 0, textureYScale * -(v2 - v1) / 2, u2, v1);
            tessellator.addVertexWithUV(textureXScale * -(u2 - u1) / 2, 0, textureYScale * -(v2 - v1) / 2, u1, v1);
            tessellator.addVertexWithUV(textureXScale * -(u2 - u1) / 2, 0, textureYScale * (v2 - v1) / 2, u1, v2);
            tessellator.draw();
        }
        glPopMatrix();
    }

    public static void enableStencilWriting(int id) {
        glStencilFunc(GL_ALWAYS, id, 0xFF);  // Always draw to the color buffer & pass the stencil test
        glStencilMask(0xFF);  // Write to stencil buffer
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);  // Keep stencil value
    }

    public static void disableStencilWriting(int id, boolean invert) {
        glStencilFunc(invert ? GL_EQUAL : GL_NOTEQUAL, id, 0xFF);  // Test stencil value
        glStencilMask(0x00);  // Do not write to stencil buffer
    }

    public static void postStencilRendering() {
        glStencilFunc(GL_ALWAYS, 0, 0xFF);
    }
}
