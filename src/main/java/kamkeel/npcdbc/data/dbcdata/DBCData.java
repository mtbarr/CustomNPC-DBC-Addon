package kamkeel.npcdbc.data.dbcdata;


import JinRyuu.JRMCore.JRMCoreH;
import cpw.mods.fml.relauncher.Side;
import kamkeel.npcdbc.constants.DBCForm;
import kamkeel.npcdbc.controllers.AuraController;
import kamkeel.npcdbc.controllers.BonusController;
import kamkeel.npcdbc.controllers.FormController;
import kamkeel.npcdbc.controllers.StatusEffectController;
import kamkeel.npcdbc.data.PlayerBonus;
import kamkeel.npcdbc.data.PlayerDBCInfo;
import kamkeel.npcdbc.data.aura.Aura;
import kamkeel.npcdbc.data.form.Form;
import kamkeel.npcdbc.data.statuseffect.PlayerEffect;
import kamkeel.npcdbc.network.PacketHandler;
import kamkeel.npcdbc.network.packets.PingPacket;
import kamkeel.npcdbc.util.PlayerDataUtil;
import kamkeel.npcdbc.util.Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashMap;

public class DBCData extends DBCDataUniversal {

    public static String DBCPersisted = "PlayerPersisted";
    public final Side side;
    public EntityPlayer player;

    // Original DBC
    public int STR, DEX, CON, WIL, MND, SPI, TP, Body, Ki, Stamina, KOforXSeconds, Rage, Heat, Pain, AuraColor, ArcReserve;
    public byte Class, Race, Powertype, Accept, State, State2, Release;
    public boolean Alive, isKO;
    public String Skills = "", RacialSkills = "", StatusEffects = "", Settings = "", FormMasteryRacial = "", FormMasteryNR = "", DNS = "", DNSHair = "", MajinAbsorptionData = "", Fusion = "";

    // Custom Form / Custom Aura
    public int addonFormID, auraID;
    public float addonFormLevel, addonCurrentHeat;
    public HashMap<Integer, PlayerEffect> currentEffects = new HashMap<>();
    public HashMap<String, PlayerBonus> currentBonuses = new HashMap<>();

    public DBCDataStats stats = new DBCDataStats(this);
    public DBCDataBonus bonus = new DBCDataBonus(this);

    public DBCData() {
        this.side = Side.SERVER;
    }

    public DBCData(EntityPlayer player) {
        this.player = player;
        this.side = player.worldObj.isRemote ? Side.CLIENT : Side.SERVER;

        if (side == Side.SERVER)
            loadNBTData(true);
    }


    public NBTTagCompound saveFromNBT(NBTTagCompound comp) {
        comp.setInteger("jrmcStrI", STR);
        comp.setInteger("jrmcDexI", DEX);
        comp.setInteger("jrmcCnsI", CON);
        comp.setInteger("jrmcWilI", WIL);
        comp.setInteger("jrmcIntI", MND);
        comp.setInteger("jrmcCncI", SPI);
        comp.setInteger("jrmcEnrgy", Ki);
        comp.setInteger("jrmcStamina", Stamina);
        comp.setInteger("jrmcBdy", Body);
        comp.setInteger("jrmcHar4va", KOforXSeconds);
        comp.setInteger("jrmcSaiRg", Rage);
        comp.setInteger("jrmcEf8slc", Heat);
        comp.setInteger("jrmcGyJ7dp", Pain);
        comp.setInteger("jrmcAuraColor", AuraColor);
        comp.setInteger("jrmcArcRsrv", ArcReserve);


        comp.setByte("jrmcState", State);
        comp.setByte("jrmcState2", State2);
        comp.setByte("jrmcRelease", Release);
        comp.setByte("jrmcPwrtyp", Powertype);
        comp.setByte("jrmcRace", Race);
        comp.setByte("jrmcClass", Class);
        comp.setByte("jrmcAccept", Accept);

        comp.setString("jrmcStatusEff", StatusEffects);
        comp.setString("jrmcSSltX", RacialSkills);
        comp.setString("jrmcSSlts", Skills);
        comp.setString("jrmcSettings", Settings);
        comp.setString("jrmcFormMasteryRacial_" + JRMCoreH.Races[Race], FormMasteryRacial);
        comp.setString("jrmcFormMasteryNonRacial", FormMasteryNR);
        comp.setString("jrmcDNS", DNS);
        comp.setString("jrmcDNSH", DNSHair);
        comp.setString("jrmcMajinAbsorptionData", MajinAbsorptionData);
        comp.setString("jrmcFuzion", Fusion);
        // DBC Addon
        comp.setInteger("addonFormID", addonFormID);
        comp.setInteger("auraID", auraID);
        comp.setFloat("addonFormLevel", addonFormLevel);
        comp.setFloat("addonCurrentHeat", addonCurrentHeat);
        stats.saveEffectsNBT(comp);
        bonus.saveBonusNBT(comp);
        return comp;
    }

    public void loadFromNBT(NBTTagCompound c) {
        STR = c.getInteger("jrmcStrI");
        DEX = c.getInteger("jrmcDexI");
        CON = c.getInteger("jrmcCnsI");
        WIL = c.getInteger("jrmcWilI");
        MND = c.getInteger("jrmcIntI");
        SPI = c.getInteger("jrmcCncI");
        Ki = c.getInteger("jrmcEnrgy");
        Stamina = c.getInteger("jrmcStamina");
        Body = c.getInteger("jrmcBdy");
        KOforXSeconds = c.getInteger("jrmcHar4va");
        Rage = c.getInteger("jrmcSaiRg");
        Heat = c.getInteger("jrmcEf8slc");
        Pain = c.getInteger("jrmcGyJ7dp");
        isKO = c.getInteger("jrmcHar4va") > 0;
        AuraColor = c.getInteger("jrmcAuraColor");
        ArcReserve = c.getInteger("jrmcArcRsrv");

        State = c.getByte("jrmcState");
        State2 = c.getByte("jrmcState2");
        Release = c.getByte("jrmcRelease");
        Powertype = c.getByte("jrmcPwrtyp");
        Race = c.getByte("jrmcRace");
        Class = c.getByte("jrmcClass");
        Accept = c.getByte("jrmcAccept");

        StatusEffects = c.getString("jrmcStatusEff");
        RacialSkills = c.getString("jrmcSSltX");
        Skills = c.getString("jrmcSSlts");
        Settings = c.getString("jrmcSettings");
        FormMasteryRacial = c.getString("jrmcFormMasteryRacial_" + JRMCoreH.Races[Race]);
        FormMasteryNR = c.getString("jrmcFormMasteryNonRacial");
        DNS = c.getString("jrmcDNS");
        DNSHair = c.getString("jrmcDNSH");
        MajinAbsorptionData = c.getString("jrmcMajinAbsorptionData");
        Fusion = c.getString("jrmcFuzion");

        // DBC Addon
        addonFormID = c.getInteger("addonFormID");
        addonFormLevel = c.getFloat("addonFormLevel");
        addonCurrentHeat = c.getFloat("addonCurrentHeat");
        auraID = c.getInteger("auraID");

        this.currentEffects.clear();
        if (c.hasKey("addonActiveEffects", 9)) {
            NBTTagList nbttaglist = c.getTagList("addonActiveEffects", 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                PlayerEffect playerEffect = PlayerEffect.readEffectData(nbttagcompound1);
                if (playerEffect != null) {
                    this.currentEffects.put(playerEffect.id, playerEffect);
                }
            }
        }

        this.currentBonuses.clear();
        if (c.hasKey("addonBonus", 9)) {
            NBTTagList nbttaglist = c.getTagList("addonBonus", 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                PlayerBonus bonus = PlayerBonus.readBonusData(nbttagcompound1);
                this.currentBonuses.put(bonus.name, bonus);
            }
        }
    }

    public void saveNBTData(boolean syncTracking) {
        NBTTagCompound nbt = this.saveFromNBT(this.player.getEntityData().getCompoundTag(DBCPersisted));

        PlayerDBCInfo formData = PlayerDataUtil.getDBCInfo(player);
        addonFormID = formData.currentForm;
        addonFormLevel = formData.getCurrentLevel();
        auraID = formData.currentAura;
        stats.setCurrentEffects(StatusEffectController.Instance.playerEffects.get(Utility.getUUID(player)));
        bonus.setCurrentBonuses(BonusController.Instance.playerBonus.get(Utility.getUUID(player)));
        nbt.setInteger("addonFormID", addonFormID);
        nbt.setFloat("addonFormLevel", addonFormLevel);
        nbt.setInteger("auraID", auraID);
        stats.saveEffectsNBT(nbt);
        bonus.saveBonusNBT(nbt);
        this.player.getEntityData().setTag(DBCPersisted, nbt);

        // Send to Tracking Only
        if (syncTracking)
            syncTracking();
    }

    public void loadNBTData(boolean syncALL) {
        NBTTagCompound dbc = this.player.getEntityData().getCompoundTag(DBCPersisted);

        // Save the DBC Addon tags to PlayerPersisted before loading it to fields
        PlayerDBCInfo formData = PlayerDataUtil.getDBCInfo(player);
        dbc.setInteger("addonFormID", formData.currentForm);
        dbc.setInteger("auraID", formData.currentAura);
        dbc.setFloat("addonFormLevel", formData.getCurrentLevel());
        stats.saveEffectsNBT(dbc);
        bonus.saveBonusNBT(dbc);
        loadFromNBT(dbc);
        if (syncALL)
            syncTracking();
    }

    public void syncTracking() {
        PacketHandler.Instance.sendToTrackingPlayers(new PingPacket(this).generatePacket(), player);
    }

    public NBTTagCompound getRawCompound() {
        return this.player.getEntityData().getCompoundTag(DBCPersisted);
    }


    public boolean isForm(int dbcForm) {
        switch (dbcForm) {
            case DBCForm.Kaioken:
                return State2 > 0 && JRMCoreH.StusEfcts(5, StatusEffects);
            case DBCForm.UltraInstinct:
                return State2 > 0 && JRMCoreH.StusEfcts(19, StatusEffects);
            case DBCForm.GodOfDestruction:
                return JRMCoreH.StusEfcts(20, StatusEffects);
            case DBCForm.Mystic:
                return JRMCoreH.StusEfcts(13, StatusEffects);
            //the following doesn't count as "forms" but they can be checked from this method as well
            case DBCForm.Legendary:
                return JRMCoreH.StusEfcts(14, StatusEffects);
            case DBCForm.Divine:
                return JRMCoreH.StusEfcts(17, StatusEffects);
            case DBCForm.Majin:
                return JRMCoreH.StusEfcts(12, StatusEffects);
            default:
                return false;
        }
    }


    public boolean containsSE(int id) {
        return JRMCoreH.StusEfcts(id, StatusEffects);
    }

    public void setSE(int id, boolean bo) {
        JRMCoreH.StusEfcts(id, StatusEffects, player, bo);
    }

    public void setForm(int dbcForm, boolean on) {
        switch (dbcForm) {
            case DBCForm.Kaioken:
                setSE(5, on);
                if (on)
                    State2 = 1;
                break;
            case DBCForm.UltraInstinct:
                setSE(19, on);
                if (on)
                    State2 = 1;
                break;
            case DBCForm.GodOfDestruction:
                setSE(20, on);
                break;
            case DBCForm.Mystic:
                setSE(13, on);
                break;
            case DBCForm.Legendary:
                setSE(14, on);
                break;
            case DBCForm.Divine:
                setSE(17, on);
                break;
            case DBCForm.Majin:
                setSE(12, on);
                break;
        }
    }

    public boolean settingOn(int id) {
        return Utility.isServer(player) ? JRMCoreH.PlyrSettingsB(player, id) : JRMCoreH.PlyrSettingsB(id);
    }

    public boolean formSettingOn(int dbcForm) {
        switch (dbcForm) {
            case DBCForm.Kaioken:
                return settingOn(0);
            case DBCForm.UltraInstinct:
                return settingOn(11);
            case DBCForm.GodOfDestruction:
                return settingOn(16);
            case DBCForm.Mystic:
                return settingOn(6);
            default:
                return false;
        }
    }


    public Aura getAura() {
        if (player == null)
            return null;
        DBCData dbcData = DBCData.get(player);
        Form form = (Form) FormController.getInstance().get(dbcData.addonFormID);

        if (form != null && form.display.hasAura())
            return form.display.getAur();
        else if (auraID > -1)
            return (Aura) AuraController.Instance.get(auraID);

        return null;
    }

    public Form getForm() {
        if (player == null)
            return null;

        if (addonFormID == -1)
            return null;

        return (Form) FormController.getInstance().get(addonFormID);
    }


}
