package reobf.proghatches.main.mixin.mixins.part2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.container.ContainerNull;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import reobf.proghatches.ae.ICondenser;
import reobf.proghatches.eucrafting.IInstantCompletable;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.main.mixin.MixinCallback;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinMultiPattern<T extends ICraftingMedium> {
	@Unique
	boolean isMulti;
	@Unique
	T medium;

	@ModifyVariable(method = "executeCrafting", at = @At(value = "INVOKE", target = "pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public ICraftingMedium b(ICraftingMedium a) {
		isMulti = a instanceof IMultiplePatternPushable;

		medium = (T) a;
		return a;
	}

	@Unique
	InventoryCrafting inv;

	@ModifyArg(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public InventoryCrafting a(InventoryCrafting a) {
		
			inv = a;
		return a;
	}

	@Unique
	ICraftingPatternDetails detail;

	@ModifyArg(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public ICraftingPatternDetails b(ICraftingPatternDetails a) {
		
			detail = a;
		return a;
	}

	@Unique
	java.util.Map.Entry e;

	@ModifyVariable(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))
	public java.util.Map.Entry b(java.util.Map.Entry a) {
		
			e = a;
		return a;
	}

	@Shadow
	private MachineSource machineSrc;
	@Shadow
	private IItemList<IAEItemStack> waitingFor;

	@Shadow
	private void postChange(final IAEItemStack diff, final BaseActionSource src) {
	};

	@Shadow
	private void postCraftingStatusChange(final IAEItemStack diff) {
	};

	@Shadow
	private MECraftingInventory inventory;
	private static final IAEItemStack[] EMPTY = new IAEItemStack[0];

	@Inject(at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "markDirty"), method = "executeCrafting")
	public void b(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci) {

		if (isMulti) {
			int used = 0;

			LinkedList<Object> is = new LinkedList<>();
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				if (inv.getStackInSlot(i) != null) {
					is.addLast(inv.getStackInSlot(i));
				}
			}
			ListIterator<Object> itr = is.listIterator();
			while (itr.hasNext()) {
				Object o = itr.next();
				if (o == null) {
					itr.remove();
				}
				if (((ItemStack) o).getItem() instanceof ItemFluidPacket) {
					o = (ItemFluidDrop.newStack(ItemFluidPacket.getFluidStack((ItemStack) o)));
				}
				itr.set(AEItemStack.create((ItemStack) o));
			}

			IAEItemStack[] input = is.toArray(EMPTY);

			int[] nums = new int[input.length];
			for (int x = 0; x < input.length; x++) {
				IAEItemStack tmp = input[x].copy().setStackSize(Integer.MAX_VALUE);
				final IAEItemStack ais = this.inventory.extractItems(tmp, Actionable.MODULATE, this.machineSrc);
				if (ais != null)
					nums[x] = (int) ais.getStackSize();
			}
			try {

				int best = Integer.MAX_VALUE;
				boolean any = false;
				for (int x = 0; x < input.length; x++) {
					if (input[x].getStackSize() > 0) {
						int num = (int) (nums[x] / input[x].getStackSize());
						if (num < best)
							best = num;
						any = true;
					}
				}
				if (any == false) {
					return;
				}

				long num = MixinCallback.getter.apply(e.getValue());

				int maxtry = Math.min(Math.min(
						(int) (num > (Integer.MAX_VALUE - 1) ? (Integer.MAX_VALUE - 1) : num) - 1, remainingOperations),

						best);

				if (maxtry <= 0) {
					return;
				}
				used = ((IMultiplePatternPushable) medium).pushPatternMulti(detail, inv, maxtry);

				MixinCallback.setter.accept(e.getValue(), num - used);

				if (used > 0) {
					for (IAEItemStack out : detail.getCondensedOutputs()) {
						out = out.copy().setStackSize(used * out.getStackSize());
						this.postChange(out, this.machineSrc);
						this.waitingFor.add(out.copy());
						this.postCraftingStatusChange(out.copy());
					}
				}

			} finally {
				// return all unused
				for (int x = 0; x < input.length; x++) {
					this.inventory.injectItems(input[x].copy().setStackSize(
							/* all availavle - pushed recipes*per recipe */
							nums[x] - used * input[x].getStackSize()), Actionable.MODULATE, this.machineSrc);

				}
			}

		} else {
			if(getMaxSkips()<=0)return;
			//int now = temp1.getOrDefault(detail, 0);
			final long max = getMaxSkips();
			for (int i = 0; i < max; i=(i<Integer.MAX_VALUE-10)?(i+1):i) {
				
				if(medium.isBusy()){break;}
				
				if (detail.isCraftable()) {
					continue;// that's impossible to be done in same tick
				}
				InventoryCrafting ic = detail.isCraftable() ? new InventoryCrafting(new ContainerNull(), 3, 3)
						: new InventoryCrafting(new ContainerNull(), detail.getInputs().length, 1);
				final IAEItemStack[] input = detail.getInputs();
				boolean found = false;
				for (int x = 0; x < input.length; x++) {
					if (input[x] != null) {
						found = false;
						for (IAEItemStack ias : getExtractItems(input[x], detail)) {

							final IAEItemStack ais = this.inventory.extractItems(ias, Actionable.MODULATE,
									this.machineSrc);
							final ItemStack is = ais == null ? null : ais.getItemStack();
							if (is == null)
								continue;
							found = true;
							ic.setInventorySlotContents(x, is);
							if (!detail.canBeSubstitute() && is.stackSize == input[x].getStackSize()) {
								this.postChange(input[x], this.machineSrc);
								break;
							} else {
								this.postChange(AEItemStack.create(is), this.machineSrc);
							}
						}
						if (!found) {
							break;
						}
					}
				}

				if (!found) {
					// put stuff back..
					for (int x = 0; x < ic.getSizeInventory(); x++) {
						final ItemStack is = ic.getStackInSlot(x);
						if (is != null) {
							this.inventory.injectItems(AEItemStack.create(is), Actionable.MODULATE, this.machineSrc);
						}
					}
					ic = null;
					break;
				}
				if (medium.pushPattern(detail, ic)) {
					MixinCallback.setter.accept(e.getValue(), MixinCallback.getter.apply(e.getValue()) - 1);
					for (IAEItemStack out : detail.getCondensedOutputs()) {

						this.postChange(out, this.machineSrc);
						this.waitingFor.add(out.copy());
						this.postCraftingStatusChange(out.copy());
					}
				}

			}

		}

	}

	@Shadow
	private ArrayList<IAEItemStack> getExtractItems(IAEItemStack ingredient, ICraftingPatternDetails patternDetails) {
		return null;
	};

	@Shadow
	int remainingOperations;

	@Inject(at = @At(value = "RETURN"), method = "executeCrafting")
	public void ret(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci) {
		detail = null;
		e = null;
		inv = null;
		medium = null;
		if (getMaxSkips() <= 0)
			return;
		temp1.clear();
	}

	////////// xxxxxxxxxx
	// @Shadow
	// private int remainingOperations;
	// @Unique
	// boolean skip;
	@Unique
	Reference2IntOpenHashMap<ICraftingPatternDetails> temp1 = new Reference2IntOpenHashMap<ICraftingPatternDetails>();

	private long maxSkips;
	/*
	 * @Inject(at = @At(value = "RETURN"), method = "updateCraftingLogic")
	 * public void a(final IGrid grid, final IEnergyGrid eg, final
	 * CraftingGridCache cc, CallbackInfo it) {
	 * 
	 * 
	 * }
	 */

	/*
	 * @ModifyVariable(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
	 * target =
	 * "Lappeng/me/cluster/implementations/CraftingCPUCluster;remainingOperations"
	 * ), method = "executeCrafting")
	 * 
	 * public ICraftingPatternDetails a(ICraftingPatternDetails m) {
	 * if(getMaxSkips()<=0)return m; int now= temp1 .getOrDefault(m, 0); skip
	 * =getMaxSkips()>=now;
	 * 
	 * if(now<Integer.MAX_VALUE-10) temp1.put(m,now+1);
	 * 
	 * 
	 * return m; }
	 */

	/*
	 * @WrapWithCondition(remap = false, at = {
	 * 
	 * @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target =
	 * "Lappeng/me/cluster/implementations/CraftingCPUCluster;remainingOperations")
	 * }, method = { "executeCrafting" }) private boolean b(CraftingCPUCluster
	 * thiz,int neo) { if(getMaxSkips()<=0)return true;
	 * 
	 * 
	 * return !skip; }
	 */
	private long getMaxSkips() {
		return maxSkips;
	}

	@Inject(at = @At(value = "RETURN"), method = "addTile")
	public void addTile(TileCraftingTile te, CallbackInfo it) {
		if (te instanceof ICondenser) {
			ICondenser con = (ICondenser) te;
			if (con.isinf()) {
				maxSkips = Integer.MAX_VALUE;
			} else {

				maxSkips = Long.max(maxSkips, maxSkips + con.getSkips());
			}

		}
	}

}
