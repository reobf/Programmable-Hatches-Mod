package reobf.proghatches.main.mixin.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class, remap = true)
public class MixinCircuitMigration {

    @Inject(method = "readFromNBT", at = { @At("RETURN") })
    public void fix(NBTTagCompound tag, CallbackInfo X) {
        /*
         * if(!Config.fixCircuit){return;}
         * if(getItem()==MyMod.progcircuit){
         * if( MixinCallback.fixCircuitTag(stackTagCompound)){}
         * }
         */

    }

    @Shadow
    public NBTTagCompound stackTagCompound;

    @Shadow
    public Item getItem() {
        return null;
    }

}
