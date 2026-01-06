package reobf.proghatches.main.mixin.mixins;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.vfyjxf.nee.processor.GregTech5RecipeProcessor;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import codechicken.nei.PositionedStack;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.main.mixin.MixinCallback;

@Pseudo
@Mixin(value = GregTech5RecipeProcessor.class, remap = false, priority = 1)
public class MixinPatternEncodingCiruitSpecialTreatment2 {

   /* @Inject(
        method = "getRecipeInput",

        at = @At(value = "INVOKE", target = "addAll(Ljava/util/Collection;)Z", shift = Shift.AFTER),

        require = 1,
        cancellable = false)

    private void packProcessRecipe(CallbackInfoReturnable<Object> x,

        @Local(name = "recipeInputs") LocalRef<List<PositionedStack>> inputs) {

        //if (GuiUtils.isFluidCraftPatternTermEx(Minecraft.getMinecraft().currentScreen)) {
            System.out.println("abc");
            inputs.set(process(inputs.get()));
       // }

    }*/

    private static List process(List<PositionedStack> c) {
c.stream().forEach(s->{
	
	/*System.out.println(s.item);
	System.out.println(s.relx);
	System.out.println(s.rely);*/
});
        AtomicBoolean circuit = new AtomicBoolean(false);
        if (ItemProgrammingToolkit.holding() == false) {
            return c;
        }
        if (MixinCallback.encodingSpecialBehaviour == false) return c;
        // AtomicInteger i = new AtomicInteger(0);
        List<PositionedStack> spec = new ArrayList<>();
        List<int[]> order = new ArrayList<>();

        List<PositionedStack> ret = c.stream()
            .filter(Objects::nonNull)

            .filter(s -> s.item != null /* && s.item.getItem() != ItemList.Display_Fluid.getItem() */)
            .map(s -> s.copy())
            .filter(orderStack -> {
                boolean regular = !(orderStack.item != null && orderStack.item instanceof ItemStack
                    && ((ItemStack) orderStack.item).stackSize == 0);
                order.add(new int[] { orderStack.relx, orderStack.rely });
                if (regular == false) {
                    circuit.set(true);
                    spec.add(
                        new PositionedStack(
                            ItemProgrammingCircuit.wrap(((ItemStack) orderStack.item)),
                            orderStack.relx,
                            orderStack.rely));
                    return false;
                }

                return true;
            })
            .collect(Collectors.toList());

        if (circuit.get() == false && ItemProgrammingToolkit.addEmptyProgCiruit()) {
            spec.add(0, new PositionedStack(ItemProgrammingCircuit.wrap(null), 0, 0));
        }
        spec.addAll(ret);
        AtomicInteger cs = new AtomicInteger();
        // Iterator<int[]> itr = order.iterator();
        for (int i = 0; i < spec.size(); i++) {
            PositionedStack tocopy = spec.get(i);
            boolean p[] = new boolean[1];
            try {
                Field f;
                f = PositionedStack.class.getDeclaredField("permutated");
                f.setAccessible(true);
                p[0] = (boolean) f.get(tocopy);
            } catch (Exception e) {}
            spec.set(i, new PositionedStack(tocopy.items, 0, 0, p[0]));

        }

        spec.forEach(s -> {
            // int[] ii = itr.next();
            int[] ii = new int[] { cs.get() % 3, cs.getAndAdd(1) / 3 };
            s.relx = ii[0];
            s.rely = ii[1];
        });
        // System.out.println(spec);
        return spec;

    }

    @Inject(
        require = 1,
        method = "getRecipeInput",
        at = @At(value = "INVOKE", target = "removeIf(Ljava/util/function/Predicate;)Z", shift = Shift.BY, by = -2))
    private void getRecipeInput(CallbackInfoReturnable<Object> c,

        @Local(name = "recipeInputs") LocalRef<List<PositionedStack>> inputs) {
        inputs.set(process(inputs.get()));
    }

}
