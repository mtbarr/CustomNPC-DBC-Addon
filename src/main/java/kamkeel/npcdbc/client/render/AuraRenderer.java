package kamkeel.npcdbc.client.render;

import JinRyuu.DragonBC.common.DBCClient;
import JinRyuu.DragonBC.common.Npcs.RenderDBC;
import JinRyuu.JRMCore.JRMCoreClient;
import JinRyuu.JRMCore.JRMCoreHDBC;
import JinRyuu.JRMCore.client.config.jrmc.JGConfigClientSettings;
import kamkeel.npcdbc.CustomNpcPlusDBC;
import kamkeel.npcdbc.client.model.ModelAura;
import kamkeel.npcdbc.client.shader.IShaderUniform;
import kamkeel.npcdbc.client.shader.ShaderHelper;
import kamkeel.npcdbc.client.shader.ShaderResources;
import kamkeel.npcdbc.client.sound.ClientSound;
import kamkeel.npcdbc.constants.DBCForm;
import kamkeel.npcdbc.constants.DBCRace;
import kamkeel.npcdbc.data.IAuraData;
import kamkeel.npcdbc.data.SoundSource;
import kamkeel.npcdbc.entity.EntityAura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import java.util.Random;

import static JinRyuu.DragonBC.common.Npcs.RenderAura2.cf;
import static JinRyuu.DragonBC.common.Npcs.RenderAura2.glColor4f;
import static org.lwjgl.opengl.GL11.*;

public class AuraRenderer extends RenderDBC {
    public static AuraRenderer Instance;
    private ModelAura model;
    String auraDir = "jinryuudragonbc:";
    int pulseAnimation;
    int pulseMax = 8;
    long animationStartTime;
    boolean throbOut;
    private float[][] lightVertRotation;
    private int lightVertN;


    public AuraRenderer() {
        super(new ModelAura(), 0.5F);
        model = (ModelAura) this.mainModel;
        this.shadowSize = 0.0F;
        this.lightVertRotation = new float[10][7];
        Instance = this;
    }

    public void animatePulsing() {
        if (!DBCClient.mc.isGamePaused()) {
            if (System.currentTimeMillis() - animationStartTime > 200 / 2 / pulseMax) {
                if (this.throbOut) {
                    if (pulseAnimation >= pulseMax)
                        this.throbOut = false;
                    else
                        ++pulseAnimation;

                } else if (pulseAnimation <= 0)
                    this.throbOut = true;
                else
                    --pulseAnimation;

                animationStartTime = System.currentTimeMillis();
            }
        }

    }

    public void renderAura(EntityAura aura, float partialTicks) {
        double interPosX = (aura.lastTickPosX + (aura.posX - aura.lastTickPosX) * (double) partialTicks) - RenderManager.renderPosX;
        double interPosY = (aura.lastTickPosY + (aura.posY - aura.lastTickPosY) * (double) partialTicks) - RenderManager.renderPosY;
        double interPosZ = (aura.lastTickPosZ + (aura.posZ - aura.lastTickPosZ) * (double) partialTicks) - RenderManager.renderPosZ;
        
        byte race = aura.auraData.getRace();
        byte state = aura.auraData.getState();
        int speed = aura.speed;
        int age = Math.max(1, aura.ticksExisted % speed);
        float release = Math.max(5, aura.auraData.getRelease());
        float alpha = aura.alpha;
        float alphaConfig = (float) JGConfigClientSettings.CLIENT_DA21 / 10.0F;
        boolean isFirstPerson = DBCClient.mc.thePlayer == aura.entity && DBCClient.mc.gameSettings.thirdPersonView == 0;
        alpha = (isFirstPerson ? aura.isKaioken ? 0.015f : 0.0125f : alpha) * alphaConfig;
        aura.setTexture(1, CustomNpcPlusDBC.ID + ":textures/aura/auraalpha.png");

        pulseMax = 0;
        if (pulseMax > 0)
            animatePulsing();
        else
            pulseAnimation = 0;

        Random rand = new Random();
        float pulsingSize = pulseAnimation * 0.03f;
        float kaiokenSize = 0;

        boolean isKaioken = aura.isKaioken || aura.aura.display.overrideDBCAura && aura.isInKaioken;
        if (isKaioken)
            kaiokenSize = 1f * aura.auraData.getState2();

        float stateSizeFactor = getStateSizeFactor(aura.auraData) + kaiokenSize;
        float sizeStateReleaseFactor = stateSizeFactor + (release / 100) * Math.max(stateSizeFactor * 0.75f, 2.5f); //aura gets 1.75x bigger at 100% release
        float size = aura.size + 0.1f * sizeStateReleaseFactor;
        aura.effectiveSize = size;


        double yOffset = aura.getYOffset(size);
        if (stateSizeFactor < 4)  //fixes bug in which offset is not// correct if size is too small
            yOffset -= 0.4 - (sizeStateReleaseFactor / 5) * 0.4;
        // glClear(GL_STENCIL_BUFFER_BIT);
        // glEnable(GL_STENCIL_TEST);
        GL11.glPushMatrix();
        if (Minecraft.getMinecraft().gameSettings.fancyGraphics)
            GL11.glShadeModel(GL11.GL_SMOOTH);
        glRotatef(180, 0, 0, 1);
        GL11.glTranslated(-interPosX, -interPosY - yOffset, interPosZ);

        
        //GL11.glTranslated(0,- yOffset, 0);
       // GL11.glRotatef(aura.ticksExisted % 360 * speed, 0.0F, 1.0F, 0.0F);
        // GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(3042);
        GL11.glDisable(2896);
        // glDisable(GL_LIGHTING);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.05F);
        // GL11.glEnable(GL11.GL_ALPHA_TEST);
        //  GL11.glAlphaFunc(GL11.GL_GREATER, 0.0005F);r
        float sizeFactor = 0.95f;

//        ////////////////////////////////////////
//        ////////////////////////////////////////
//        //Inner
//        //RenderEventHandler.disableStencilWriting(aura.entity.getEntityId(), false);
//        glPushMatrix();
//        sizeFactor = 0.9f;
//        glDepthMask(false);
//        GL11.glScalef((size + pulsingSize) * sizeFactor, size * sizeFactor, (size + pulsingSize) * sizeFactor);
//        glTranslatef(0, 0.5f, 0);
//        //glColorMask(false, false, false, true);
//        renderAura(aura, 0x5fffff, 0.05f, 1f);
//        glColorMask(true, true, true, true);
//        GL11.glDisable(GL11.GL_ALPHA_TEST);
//        glPopMatrix();

        //   RenderEventHandler.enableStencilWriting(aura.entity.getEntityId());
//        ////////////////////////////////////////
//        ////////////////////////////////////////
//        //Inner
//        GL11.glDepthMask(true);
//       // glTranslatef(0, 0.15f, 0);
//        glPushMatrix();
//       // GL11.glEnable(GL11.GL_ALPHA_TEST);
//       // GL11.glDisable(3042);
//        //GL11.glAlphaFunc(GL11.GL_GREATER, 0.000F);
//        sizeFactor = 0.95f;
//        GL11.glScalef((size + pulsingSize) * sizeFactor, size * sizeFactor, (size + pulsingSize) * sizeFactor);
//         glColorMask(false, false, false, true);
//        renderAura(aura, 0x00ffff, 0.1051f, 1f);
//        glColorMask(true, true, true, true);
//        GL11.glDisable(GL11.GL_ALPHA_TEST);
//        glPopMatrix();
//        //GL11.glEnable(3042);

        ////////////////////////////////////////
        ////////////////////////////////////////
        //Inner
        GL11.glDepthMask(false);
        glTranslatef(0, 0.15f, 0);
        glPushMatrix();

        // RenderEventHandler.enableStencilWriting(aura.entity.getEntityId() + 1);
        //RenderEventHandler.disableStencilWriting(aura.entity.getEntityId(), false);
        // glStencilFunc(GL_LEQUAL, aura.entity.getEntityId(), 0xFF);  // Always draw to the color buffer & pass the stencil test
        glStencilMask(0xFF);  // Write to stencil buffer
        // glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);  // Keep stencil value
        // GL11.glEnable(GL11.GL_ALPHA_TEST);
        //GL11.glDisable(3042);
        //GL11.glAlphaFunc(GL11.GL_GREATER, 0.000F);
        sizeFactor = 0.8f;
        // glRotatef(180, 0, 0, 1);
        GL11.glScalef((size + pulsingSize) * sizeFactor, size * sizeFactor, (size + pulsingSize) * sizeFactor);
        //glRotatef(180, 0, 0, 1);
        glTranslatef(0f, 0.8f, 0);
        // glColorMask(false, false, false, true);


       // renderAura(aura, 0x6e1188, 0.0515f, 3f);
        glColorMask(true, true, true, true);
       // GL11.glDisable(GL11.GL_ALPHA_TEST);
        glPopMatrix();
        //GL11.glEnable(3042);

        ////////////////////////////////////////
        ////////////////////////////////////////
        //Outer
        glPushMatrix();
        sizeFactor = 1.1f;
        GL11.glScalef((size + pulsingSize), size, (size + pulsingSize));
        glScalef(sizeFactor, sizeFactor * 1.1f, sizeFactor);
       // RenderEventHandler.disableStencilWriting(aura.entity.getEntityId() + 1, false);
        glScalef(1.2f, 1.14f, 1.2f);
        glTranslated(0, -0.95f, 0);
        GL11.glDepthMask(false);
        ////////////////////////////////////////
        ////////////////////////////////////////
        //Shader stuff
        float r = ((aura.color1 >> 16 & 255) / 255f);
        float g = ((aura.color1 >> 8 & 255) / 255f);
        float b = ((aura.color1 & 255) / 255f);

        ShaderHelper.loadTextureUnit(2, ShaderResources.AURA_NOISE);
        IShaderUniform uniforms = shader -> {
            int rgbaLocation = ARBShaderObjects.glGetUniformLocationARB(shader, "rgba");
            ARBShaderObjects.glUniform4fARB(rgbaLocation, r, g, b, 0.5f);

            int center = ARBShaderObjects.glGetUniformLocationARB(shader, "center");
            ARBShaderObjects.glUniform3fARB(center, (float) aura.entity.posX, (float) aura.entity.posY, (float) aura.entity.posZ);

            int textureLocation = ARBShaderObjects.glGetUniformLocationARB(shader, "noiseTexture");
            ARBShaderObjects.glUniform1iARB(textureLocation, 2);
        };

     ShaderHelper.useShader(ShaderHelper.aura, uniforms);
        renderAura(aura, 0x97b1f4, 1.1f, 1f);
        ShaderHelper.releaseShader();
        glPopMatrix();


        GL11.glAlphaFunc(516, 01F);
        GL11.glDisable(3042);
        GL11.glEnable(2896);
        GL11.glEnable(3553);
        GL11.glDepthMask(true);
        // GL11.glPopMatrix();
        //  float r = rand.nextInt(50);
        //if (aura.hasLightning && r < 10 && age < 10)
        //lightning(aura, interPosX, interPosY + aura.getYOffset(), interPosZ);

        if (Minecraft.getMinecraft().gameSettings.fancyGraphics)
            GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();


    }

    public void renderAura(EntityAura aura, int color, float alpha, float intensity) {
        byte race = aura.auraData.getRace();
        byte state = aura.auraData.getState();


        //   alpha = 0.8f;
        int maxLayers = 5;
        for (float i = 1; i < maxLayers + 1; ++i) {
            float layerPercent = i / maxLayers;
            float layerTemp = layerPercent * 20f;

            for (float j = 1; j < 2; j += 1f) {

                model.auraModel.offsetY = -(i / maxLayers) * aura.height;
                model.auraModel.offsetZ = layerTemp < 7F ? 0.2F - 1 * 0.075F : 0.35F + (1 - 7.0F) * 0.055F;
                model.auraModel.rotateAngleX = (0.9926646F - layerTemp * 0.01F) * (1 - i / maxLayers) * (1 - ((float) Math.pow(i / maxLayers, 2)));
                if (layerPercent > 0.99) //makes aura close in at top
                    model.auraModel.rotateAngleX = 100;

                model.auraModel.rotationPointY = 55.0F + (i / maxLayers) * 20;
                float r = new Random().nextInt(200);
              //  if (layerTemp > 3) //aura intensity
                  //  model.auraModel.offsetY += -r * 0.0015f * intensity * getStateIntensity(state, race);


                GL11.glPushMatrix();
                GL11.glRotatef(360 * j, 0.0F, 1.0F, 0.0F);
                if (layerPercent < 0.21) {
                    glColor4f(color, alpha);
                    this.renderManager.renderEngine.bindTexture(aura.text1);
                 //   model.auraModel.render(0.0625f);

                    if (aura.text2 != null) {
                        this.renderManager.renderEngine.bindTexture(aura.text2);
                        glColor4f(aura.color2, alpha);
                        model.auraModel.render(0.0625f);
                    }
                }
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glRotatef(360 * j + 45, 0F, 1F, 0F);
                this.renderManager.renderEngine.bindTexture(aura.text1);
                if (aura.color3 > -1 && j < 1)
                    cf(aura.color1, aura.color3, alpha);
                else
                    glColor4f(color, alpha);

                int numSegments = 5;
                for (int g = 0; g <= numSegments; g++) {
                    float angle = 2.0f * (float) Math.PI * g / numSegments;
                    float x = (float) (12.5 + (float) Math.cos(angle) * 2);
                    float y = 73 + (float) Math.sin(angle) * 2;

                    float t = (float) g / numSegments;
                    // GL11.glColor3f(0.0f, 0.5f * (1.0f - t), 1.0f - t);

                }
                model.auraModel.render(0.0625f);

                if (aura.text2 != null) {
                    GL11.glTranslatef(0.0F, 3F, 0.0F);
                    GL11.glPushMatrix();
                    GL11.glScalef(0.8F, 0.4F, 0.8F);

                    this.renderManager.renderEngine.bindTexture(aura.text2);
                    glColor4f(aura.color2, alpha);
                    model.auraModel.render(0.0625f);
                    GL11.glPopMatrix();
                }

                GL11.glPopMatrix();

                if (aura.color3 > -1 && aura.text3 != null) {
                    GL11.glPushMatrix();
                    GL11.glScalef(0.9F, 0.9F, 0.9F);
                    GL11.glTranslatef(0.0F, 0.5F, 0.0F);
                    GL11.glRotatef(360 * j + 45, 0.0F, 1.0F, 0.0F);

                    this.renderManager.renderEngine.bindTexture(aura.text3);
                    glColor4f(aura.color3, alpha);
                    model.auraModel.render(0.0625f);

                    GL11.glPopMatrix();
                }
            }

        }


    }

    private void lightning(EntityAura aura, double par2, double par4, double par6) {
        Random rand = new Random();

        if (aura.ticksExisted % 100 > 0 && rand.nextLong() < 1)
            return;

        this.lightVertRotation = new float[10][7];
        GL11.glPushMatrix();
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(3553);
        GL11.glDisable(2896);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1);
        GL11.glScalef(0.5f, 1f, 0.5f);

        double[] adouble = new double[8];
        double[] adouble1 = new double[8];
        double d3 = 0.0;
        double d4 = 0.0;
        GL11.glTranslated(par2, par4 +2.3, par6);
        int k1 = 0;
        int nu = (int) (Math.random() * 10.0) + 1;
        int nu2 = 3;
        if (!JRMCoreClient.mc.isGamePaused()) {
            this.lightVertN = (int) (rand.nextFloat() * 7.0);
        }

        // lightVertN = 3;
        for (int i = 0; i < this.lightVertN - 1; ++i) {
            if (!JRMCoreClient.mc.isGamePaused()) {
                this.lightVertRotation[i][0] = (float) (Math.random() * 1.0);
                this.lightVertRotation[i][1] = (float) (Math.random() * 1.0);
                this.lightVertRotation[i][2] = (float) (Math.random() * 1.0);
                this.lightVertRotation[i][3] = (float) (Math.random() * 1.2000000476837158) - 0.6F;
                this.lightVertRotation[i][4] = (float) (Math.random() * (double) aura.entity.height) - aura.entity.height / 2.0F;
                this.lightVertRotation[i][5] = (float) (Math.random() * 1.2000000476837158) - 0.6F;
                this.lightVertRotation[i][6] = (float) (Math.random() * 0.20000000298023224);
            }

            float sc = (0.05F + this.lightVertRotation[i][6]) * 0.75f;
            GL11.glRotatef(360.0F * this.lightVertRotation[i][0], 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(360.0F * this.lightVertRotation[i][1], 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(360.0F * this.lightVertRotation[i][2], 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(this.lightVertRotation[i][3], this.lightVertRotation[i][4], this.lightVertRotation[i][5]);


            for (int j = 0; j < nu2; ++j) {
                int k = 7;
                int l = 0;
                if (j > 0) {
                    k = 7 - j;
                }

                if (j > 0) {
                    l = k - 2;
                }

                double d5 = adouble[k] - d3;
                double d6 = adouble1[k] - d4;

                for (int i1 = k; i1 >= l; --i1) {
                    double d7 = d5;
                    double d8 = d6;
                    d5 += (double) (rand.nextInt(31) - 15) * 0.07000000029802322;
                    d6 += (double) (rand.nextInt(31) - 15) * 0.07000000029802322;
                    tessellator.startDrawing(5);
                    float f2 = 0.5F;
                    tessellator.setColorRGBA_I(aura.lightningColor, aura.lightningAlpha);


                    double d9 = 0.1 + (double) k1 * 0.2;
                    double d10 = 0.1 + (double) k1 * 0.2;

                    for (int j1 = 0; j1 < 5; ++j1) {
                        double d11 = 0.0 - d9;
                        double d12 = 0.0 - d9;
                        if (j1 == 1 || j1 == 2) {
                            d11 += d9 * 2.0 * (double) sc;
                        }

                        if (j1 == 2 || j1 == 3) {
                            d12 += d9 * 2.0 * (double) sc;
                        }

                        double d13 = 0.0 - d10;
                        double d14 = 0.0 - d10;
                        if (j1 == 1 || j1 == 2) {
                            d13 += d10 * 2.0 * (double) sc;
                        }

                        if (j1 == 2 || j1 == 3) {
                            d14 += d10 * 2.0 * (double) sc;
                        }

                        if (i1 < 8) {
                            tessellator.addVertex(d13 + d5 * (double) sc, -((double) (i1 * 1 - 7)) * (double) sc, d14 + d6 * (double) sc);
                            tessellator.addVertex(d11 + d7 * (double) sc, -((double) ((i1 + 1) * 1 - 7)) * (double) sc, d12 + d8 * (double) sc);
                        }
                    }

                    tessellator.draw();
                }
            }
        }
        if (rand.nextInt(15) < 2 && aura.ticksExisted % 5 == 0)
            new ClientSound(new SoundSource("jinryuudragonbc:1610.spark", aura.entity)).setVolume(0.1f).setPitch(0.90f + rand.nextInt(3) * 0.05f).play(false);
        GL11.glDisable(3042);
        GL11.glEnable(2896);
        GL11.glEnable(3553);
        GL11.glPopMatrix();

    }
    public static float getStateIntensity(int state, int race) {
        float intensityFactor = 150f; //the higher, the more intensely the aura moves in Y axis
        if (race == DBCRace.SAIYAN || race == DBCRace.HALFSAIYAN) {
            if (state > DBCForm.Base && state < DBCForm.SuperSaiyan2)
                intensityFactor = 40f;
            else if (state == DBCForm.SuperSaiyan2)
                intensityFactor = 32.5f;
            else if (state == DBCForm.SuperSaiyan3)
                intensityFactor = 35f;
            else if (state == DBCForm.SuperSaiyan4)
                intensityFactor = 10f;

            else if (state == DBCForm.SuperSaiyanGod)
                intensityFactor = 10f;
            else if (state == DBCForm.SuperSaiyanBlue)
                intensityFactor = 15f;
            else if (state == DBCForm.BlueEvo)
                intensityFactor = 15f;
        }

        if (state < 1)
            state = 1;

        return state * intensityFactor / 100;
    }

    public static float getStateSizeFactor(IAuraData data) {
        int state = data.getState();
        int race = data.getRace();


        float sizeFactor = state; //responsible for correctly scaling aura sizes
        if (race == DBCRace.SAIYAN || race == DBCRace.HALFSAIYAN) {
            if (state == DBCForm.Base)
                sizeFactor = 0.5f;
            else if (state > DBCForm.Base && state < DBCForm.SuperSaiyan2)
                sizeFactor = 4;
            else if (state == DBCForm.SuperSaiyan2)
                sizeFactor = 6f;
            else if (state == DBCForm.SuperSaiyan3)
                sizeFactor = 10;
            else if (state == DBCForm.SuperSaiyan4)
                sizeFactor = 12.5f;

            else if (state == DBCForm.SuperSaiyanGod)
                sizeFactor = 1;
            else if (state == DBCForm.SuperSaiyanBlue)
                sizeFactor = 4;
            else if (state == DBCForm.BlueEvo)
                sizeFactor = 6;

        }

        if (data.getFormID() > -1) {
            int release = data.getRelease();
            float size = JRMCoreHDBC.DBCsizeBasedOnRace2(race, state);
            float effectiveSize = size * ValueUtil.clamp(release, 15, 25) * 0.015f;
            float factor = effectiveSize / size * 10;
            return size * factor;
        }

        return sizeFactor;

    }

    public void doRender(Entity aura, double posX, double posY, double posZ, float yaw, float partialTickTime) {
      // this.renderAura((EntityAura) aura,partialTickTime);
    }
}
