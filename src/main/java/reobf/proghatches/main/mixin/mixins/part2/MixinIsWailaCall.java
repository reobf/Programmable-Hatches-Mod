package reobf.proghatches.main.mixin.mixins.part2;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.crossmod.waila.GregtechTEWailaDataProvider;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.eucrafting.AECover;

@Mixin(
		
		
		//value = GregtechTEWailaDataProvider.class
targets={
		"gregtech.crossmod.waila.GregtechTEWailaDataProvider",
		"gregtech.crossmod.waila.GregtechWailaDataProvider"//喜欢改名字?
		
}

, remap = false)
public class MixinIsWailaCall {

    static {
        AECover.mixinReady = true;

    }

    @Inject(require = 1, at = { @At("HEAD") }, method = "getNBTData", remap = false)
    public void getNBTData(final EntityPlayerMP player, final TileEntity tile, final NBTTagCompound tag,
        final World world, int x, int y, int z, CallbackInfoReturnable r) {
        AECover.getNBTData = true;
    }

    @Inject(require = 1, at = { @At("RETURN") }, method = "getNBTData", remap = false)
    public void getNBTData1(final EntityPlayerMP player, final TileEntity tile, final NBTTagCompound tag,
        final World world, int x, int y, int z, CallbackInfoReturnable r) {
        AECover.getNBTData = false;
    }

    @Inject(require = 1, at = { @At("HEAD") }, method = "getWailaBody", remap = false)
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config, CallbackInfoReturnable r) {
        AECover.getWailaBody = true;
    }

    @Inject(require = 1, at = { @At("RETURN") }, method = "getWailaBody", remap = false)
    public void getWailaBody1(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config, CallbackInfoReturnable r) {
        AECover.getWailaBody = false;
    }
}
