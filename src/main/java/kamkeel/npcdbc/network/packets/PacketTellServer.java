package kamkeel.npcdbc.network.packets;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import kamkeel.npcdbc.api.ICustomForm;
import kamkeel.npcdbc.network.AbstractMessage.AbstractServerMessage;
import kamkeel.npcdbc.scripted.DBCAPI;
import kamkeel.npcdbc.controllers.TransformController;
import kamkeel.npcdbc.util.DBCUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class PacketTellServer extends AbstractServerMessage<PacketTellServer> {
    private String packet_id;

    public PacketTellServer() {
    }

    public PacketTellServer(String info) {
        packet_id = info;
    }

    @Override
    public void process(EntityPlayer p, Side side) {
        NBTTagCompound nbt = p.getEntityData().getCompoundTag("PlayerPersisted");
        goThroughStringIDs(packet_id, nbt, (EntityPlayerMP) p);


    }

    public void goThroughStringIDs(String idString, NBTTagCompound nbt, EntityPlayerMP p) {
        if (idString.startsWith("createForm")) {

            ICustomForm f = DBCAPI.Instance().createCustomForm("Super Saiyan Green");
            ICustomForm f1 = DBCAPI.Instance().createCustomForm("Super Saiyan Yellow");

            DBCUtils.getFullAttribute(p, 0);
            f.assignToPlayer(p);
            //f.removeFromPlayer(p);
            f.save();
        }

        if (idString.startsWith("Transform:"))
            TransformController.handleCustomFormAscend(p, Integer.parseInt(idString.split(":")[1]));
        if (idString.contains("Descend"))
            TransformController.handleCustomFormDescend(p);


    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        packet_id = ByteBufUtils.readUTF8String(buffer);

    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        ByteBufUtils.writeUTF8String(buffer, packet_id);
    }

}
