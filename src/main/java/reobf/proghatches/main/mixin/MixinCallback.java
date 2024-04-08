package reobf.proghatches.main.mixin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.HashBasedItemList;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.main.MyMod;

public class MixinCallback {

	public static boolean encodingSpecialBehaviour = true;
	public static Function<Object ,Long> getter;
	public static BiConsumer<Object ,Long> setter;
	static{
		try {
			Field	n=Class.forName("appeng.me.cluster.implementations.CraftingCPUCluster$TaskProgress").getDeclaredField("value");
					n.setAccessible(true);
					setter=(s,b)->{
						
						try {
								 n.set(s,b);
						} catch (Exception e) {
							
							e.printStackTrace();
						}
						
						};
					getter=s->{
						
					try {
						return	(Long) n.get(s);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
					return null;
					};
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
	}
	public static void handleAddedToMachineList(IGregTechTileEntity aTileEntity, Object o) {
		GT_MetaTileEntity_MultiBlockBase thiz = (GT_MetaTileEntity_MultiBlockBase) o;
		try {
			if (aTileEntity == null)
				return;
			IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
			if (aMetaTileEntity != null && aMetaTileEntity instanceof DualInputHatch) {

				((DualInputHatch) aMetaTileEntity).setFilter(thiz.getRecipeMap());

			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}
	
}
