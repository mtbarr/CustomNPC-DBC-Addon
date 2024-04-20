package kamkeel.npcdbc.network.packets;

import io.netty.buffer.ByteBuf;
import kamkeel.npcdbc.constants.enums.EnumNBTType;
import kamkeel.npcdbc.data.DBCData;
import kamkeel.npcdbc.network.AbstractPacket;
import kamkeel.npcdbc.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.constants.EnumPacketClient;

import java.io.IOException;

import static noppes.npcs.NoppesUtilServer.getPlayerByName;

public final class DBCSetValue extends AbstractPacket {
    public static final String packetName = "NPCDBC|SetValue";
    private EnumNBTType type;
    private EntityPlayer player;
    private String tag;
    private Object value;

    public DBCSetValue(EntityPlayer player, EnumNBTType type, String tag, Object value) {
        this.player = player;
        this.type = type;
        this.tag = tag;
        this.value = value;
    }

    public DBCSetValue() {}

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeUTF8String(out,this.player.getCommandSenderName());
        out.writeInt(this.type.ordinal());
        ByteBufUtils.writeUTF8String(out,this.tag);
        DBCData dbcData = DBCData.get(this.player);
        switch (this.type){
            case STRING:
                ByteBufUtils.writeUTF8String(out,(String) value);
                dbcData.getRawCompound().setString(tag, (String) value);
                break;
            case INT:
                out.writeInt((int) value);
                dbcData.getRawCompound().setInteger(tag, (int) value);
                break;
            case FLOAT:
                out.writeFloat((float) value);
                dbcData.getRawCompound().setFloat(tag, (float) value);
                break;
            case DOUBLE:
                out.writeDouble((double) value);
                dbcData.getRawCompound().setDouble(tag, (double) value);
                break;
            case BOOLEAN:
                out.writeBoolean((boolean) value);
                dbcData.getRawCompound().setBoolean(tag, (boolean) value);
                break;
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String playerName = ByteBufUtils.readUTF8String(in);
        EntityPlayer sendingPlayer = getPlayerByName(playerName);
        if (sendingPlayer == null)
            return;

        int enumOrdinal = in.readInt();
        EnumNBTType nbtType = EnumNBTType.values()[enumOrdinal];

        String tag = ByteBufUtils.readUTF8String(in);
        if(tag == null || tag.isEmpty())
            return;

        DBCData dbcData = DBCData.get(sendingPlayer);

        switch (nbtType){
            case STRING:
                String newString = ByteBufUtils.readUTF8String(in);
                dbcData.getRawCompound().setString(tag, newString);
                break;
            case INT:
                int intValue = in.readInt();
                dbcData.getRawCompound().setInteger(tag, intValue);
                break;
            case FLOAT:
                float floatValue = in.readFloat();
                dbcData.getRawCompound().setFloat(tag, floatValue);
                break;
            case DOUBLE:
                double doubleValue = in.readDouble();
                dbcData.getRawCompound().setDouble(tag, doubleValue);
                break;
            case BOOLEAN:
                boolean boolValue = in.readBoolean();
                dbcData.getRawCompound().setBoolean(tag, boolValue);
                break;
        }
    }
}
