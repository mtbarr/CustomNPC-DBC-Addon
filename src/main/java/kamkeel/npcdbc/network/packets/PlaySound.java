package kamkeel.npcdbc.network.packets;

import io.netty.buffer.ByteBuf;
import kamkeel.npcdbc.network.AbstractPacket;
import kamkeel.npcdbc.network.PacketHandler;
import kamkeel.npcdbc.util.ByteBufUtils;
import kamkeel.npcdbc.util.SoundHelper;
import kamkeel.npcdbc.util.Utility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public final class PlaySound extends AbstractPacket {
    public static final String packetName = "NPC|PlaySound";

    public SoundHelper.Sound sound;


    public PlaySound() {
    }

    public PlaySound(Entity entity, String soundDir) {
        sound = new SoundHelper.Sound(soundDir, entity);
    }

    public PlaySound(SoundHelper.Sound sound) {
        this.sound = sound;
    }


    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, sound.writeToNbt());


    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        sound = SoundHelper.Sound.createFromNBT(ByteBufUtils.readNBT(in));

        if (Utility.isServer())
            play(sound);
        else
            sound.play(false);


    }

    public static void play(SoundHelper.Sound sound) {
        if (sound == null || sound.entity == null)
            return;

        PacketHandler.Instance.sendToTrackingPlayers(sound.entity, new PlaySound(sound).generatePacket());

    }

    public static void play(Entity entity, String soundDir) {
        if (entity == null)
            return;
        SoundHelper.Sound sound = new SoundHelper.Sound(soundDir, entity);
        PacketHandler.Instance.sendToTrackingPlayers(sound.entity, new PlaySound(sound).generatePacket());

    }

}
