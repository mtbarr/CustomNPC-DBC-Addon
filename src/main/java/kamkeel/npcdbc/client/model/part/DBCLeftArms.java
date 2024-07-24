package kamkeel.npcdbc.client.model.part;

import kamkeel.npcdbc.client.ClientProxy;
import kamkeel.npcdbc.client.model.ModelDBCPartInterface;
import kamkeel.npcdbc.client.render.RenderEventHandler;
import kamkeel.npcdbc.data.form.Form;
import kamkeel.npcdbc.data.form.FormDisplay;
import kamkeel.npcdbc.data.npc.DBCDisplay;
import kamkeel.npcdbc.mixins.late.INPCDisplay;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.entity.data.ModelData;
import noppes.npcs.entity.data.ModelPartData;
import org.lwjgl.opengl.GL11;

import static kamkeel.npcdbc.client.render.RenderEventHandler.disableStencilWriting;

public class DBCLeftArms extends ModelDBCPartInterface {

    // Arm Spikes
    public ModelRenderer LeftArmSpike;
    public ModelRenderer leftArmSpikePart;

    // Shoulder Pads
    public ModelRenderer ArcoLeftShoulder;

    public DBCLeftArms(ModelMPM base) {
        super(base);
        textureWidth = 64;
        textureHeight = 32;

        this.ArcoLeftShoulder = new ModelRenderer(base, 38, 0);
        this.ArcoLeftShoulder.mirror = true;
        this.ArcoLeftShoulder.addBox(-1.0F, -3.0F, -3.0F, 7, 4, 6);
        this.ArcoLeftShoulder.setRotationPoint(0F, 0F, 0.0F);
        this.ArcoLeftShoulder.setTextureSize(128, 64);
        this.addChild(ArcoLeftShoulder);

        this.LeftArmSpike = new ModelRenderer(base, 0, 0);
        this.LeftArmSpike.addBox(-0.0F, -0.0F, -0.0F, 0, 0, 0, 0.02F);
        this.LeftArmSpike.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.leftArmSpikePart = new ModelRenderer(base, 0, 6);
        this.leftArmSpikePart.addBox(5.0F, 1.0F, -1.0F, 1, 5, 2);
        this.leftArmSpikePart.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.setRotation(this.leftArmSpikePart, 0.0F, 0.0F, (float) (Math.PI / 6));
        this.LeftArmSpike.addChild(leftArmSpikePart);
        this.addChild(LeftArmSpike);
    }

    @Override
    public void render(float par1) {
        DBCDisplay display = ((INPCDisplay) entity.display).getDBCDisplay();
        if (!display.enabled)
            return;

        if (!ClientProxy.renderingOutline && display.outlineID != -1)
            RenderEventHandler.enableStencilWriting((entity.getEntityId() + RenderEventHandler.TAIL_STENCIL_ID) % 256);

        if (ClientProxy.renderingOutline) {
            GL11.glTranslatef(-.02f,-0.016f,0);
            disableStencilWriting((entity.getEntityId() + RenderEventHandler.TAIL_STENCIL_ID) % 256, false);
        }

        this.ArcoLeftShoulder.rotateAngleY = base.bipedLeftArm.rotateAngleY;
        this.ArcoLeftShoulder.rotateAngleX = base.bipedLeftArm.rotateAngleX;
        this.ArcoLeftShoulder.rotateAngleZ = base.bipedLeftArm.rotateAngleZ;

        if(display.useSkin){;
            bodyCM = display.bodyCM;
            bodyC2 = display.bodyC2;
            //////////////////////////////////////////////////////
            //////////////////////////////////////////////////////
            //Forms
            Form form = display.getForm();
            if (form != null) {
                FormDisplay d = form.display;
                if (d.hasColor("bodycm"))
                    bodyCM = d.bodyCM;
                if (d.hasColor("bodyc2"))
                    bodyC2 = d.bodyC2;
            }
            //////////////////////////////////////////////////////
            //////////////////////////////////////////////////////

            location = new ResourceLocation("jinryuudragonbc:cc/arc/m/0B20.png");
            useColor = 0;
            super.render(par1);

            location = new ResourceLocation("jinryuudragonbc:cc/arc/m/2B20.png");
            useColor = 2;
            super.render(par1);

            useColor = 0;
        } else {
             super.render(par1);
        }
        if (!ClientProxy.renderingOutline && display.outlineID != -1)
            RenderEventHandler.enableStencilWriting(entity.getEntityId() % 256);

        if (ClientProxy.renderingOutline) {
            disableStencilWriting((entity.getEntityId()) % 256, false);
        }
    }

    @Override
    public void initData(ModelData modelData) {
        ModelPartData config = data.getPartData("dbcArms");
        if(config == null)
        {
            isHidden = true;
            return;
        }
        useColor = 0;
        bodyCM = config.color;
        isHidden = false;

        LeftArmSpike.isHidden = config.type != 1;
        ArcoLeftShoulder.isHidden = config.type != 2;
        if(!config.playerTexture){
            location = config.getResource();
        }
        else
            location = null;
    }
}
