package kamkeel.npcdbc.mixins.late;

import JinRyuu.DragonBC.common.Npcs.EntityAura2;
import kamkeel.npcdbc.constants.enums.EnumAuraTypes2D;
import org.spongepowered.asm.mixin.Unique;

public interface IEntityAura {
    float getState();

    void setState(float ok);

    boolean hasLightning();

    @Unique
    void setType2D(EnumAuraTypes2D types2D);

    @Unique
    EnumAuraTypes2D getType2D();

    void setHasLightning(boolean hasLightning);

    int getLightningColor();

    void setLightningColor(int lightningColor);

    @Unique
    int getLightningAlpha();

    @Unique
    void setLightningAlpha(int lightningAlpha);

    @Unique
    int getLightningSpeed();

    @Unique
    int getLightningIntensity();

    @Unique
    void setLightningSpeed(int lightningSpeed);

    @Unique
    void setLightningIntensity(int lightningIntensity);

    float getSize();

    void setSize(float getSize);

    @Unique
    EntityAura2 getParent();

    @Unique
    void setParent(EntityAura2 parent);

    @Unique
    boolean hasParent();

    @Unique
    void setIsKaioken(boolean is);

    @Unique
    boolean isKaioken();


}
