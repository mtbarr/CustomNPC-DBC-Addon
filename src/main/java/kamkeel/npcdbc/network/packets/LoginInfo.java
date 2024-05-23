package kamkeel.npcdbc.network.packets;

import JinRyuu.DragonBC.common.DBCConfig;
import io.netty.buffer.ByteBuf;
import kamkeel.npcdbc.client.ClientCache;
import kamkeel.npcdbc.config.ConfigDBCGameplay;
import kamkeel.npcdbc.constants.DBCClass;
import kamkeel.npcdbc.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public final class LoginInfo extends AbstractPacket {
    public static final String packetName = "NPC|Login";
    private final float ma;
    private final float spi;
    private final float war;
    private final boolean chargeDex;
    private final boolean transformBypass;
    private final double kiProtectionValue;
    private final double kiFistValue;
    private final boolean preciseCharge;

    public LoginInfo(){
        this.chargeDex = ConfigDBCGameplay.EnableChargingDex;
        this.ma = ConfigDBCGameplay.MartialArtistCharge;
        this.spi = ConfigDBCGameplay.SpiritualistCharge;
        this.war = ConfigDBCGameplay.WarriorCharge;
        this.transformBypass = ConfigDBCGameplay.InstantTransform;
        this.kiProtectionValue = DBCConfig.ccnfKDd;
        this.kiFistValue = DBCConfig.ccnfKFd;
        this.preciseCharge = ConfigDBCGameplay.PreciseKiCharging;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeBoolean(this.chargeDex);
        out.writeFloat(this.ma);
        out.writeFloat(this.spi);
        out.writeFloat(this.war);

        out.writeBoolean(this.transformBypass);

        out.writeDouble(this.kiProtectionValue);
        out.writeDouble(this.kiFistValue);

        out.writeBoolean(this.preciseCharge);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if(player.worldObj.isRemote){
            ClientCache.hasChargingDex = in.readBoolean();
            float martialArtist = in.readFloat();
            float spiritualist = in.readFloat();
            float warrior = in.readFloat();

            ClientCache.chargingDexValues.put(DBCClass.MartialArtist, martialArtist);
            ClientCache.chargingDexValues.put(DBCClass.Spiritualist, spiritualist);
            ClientCache.chargingDexValues.put(DBCClass.Warrior, warrior);

            ClientCache.allowTransformBypass = in.readBoolean();

            DBCConfig.cnfKDd = in.readDouble();
            DBCConfig.cnfKFd = in.readDouble();

            ClientCache.preciseCharging = in.readBoolean();
        }
    }
}
