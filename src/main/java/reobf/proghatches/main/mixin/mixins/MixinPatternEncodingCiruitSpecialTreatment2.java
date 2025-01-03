package reobf.proghatches.main.mixin.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import codechicken.nei.PositionedStack;
import gregtech.api.enums.ItemList;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.main.mixin.MixinCallback;

@Pseudo
@Mixin(targets = "com.github.vfyjxf.nee.processor.GregTech5RecipeProcessor", remap = false, priority = 1)
public class MixinPatternEncodingCiruitSpecialTreatment2 {

	@ModifyVariable(require = 1, method = "getRecipeInput", at = @At(value = "INVOKE", target = "removeIf(Ljava/util/function/Predicate;)Z"))
	private List getRecipeInput(List<PositionedStack> c) {
		AtomicBoolean circuit = new AtomicBoolean(false);
		if (ItemProgrammingToolkit.holding() == false) {
			return c;
		}
		if (MixinCallback.encodingSpecialBehaviour == false)
			return c;
		// AtomicInteger i = new AtomicInteger(0);
		List<PositionedStack> spec = new ArrayList<>();
		List<int[]> order = new ArrayList<>();

		List<PositionedStack> ret = c.stream().filter(Objects::nonNull)
				
				.filter(s -> s.item != null && s.item.getItem() != ItemList.Display_Fluid.getItem()).map(s -> s.copy())
				.filter(orderStack -> {
					boolean regular = !(orderStack.item != null && orderStack.item instanceof ItemStack
							&& ((ItemStack) orderStack.item).stackSize == 0);
					order.add(new int[] { orderStack.relx, orderStack.rely });
					if (regular == false) {
						circuit.set(true);
						spec.add(new PositionedStack(ItemProgrammingCircuit.wrap(((ItemStack) orderStack.item)),
								orderStack.relx, orderStack.rely));
						return false;
					}

					return true;
				}).collect(Collectors.toList());

		if (circuit.get() == false && ItemProgrammingToolkit.addEmptyProgCiruit()) {
			spec.add(0, new PositionedStack(ItemProgrammingCircuit.wrap(null), 0, 0));
		}
		spec.addAll(ret);
		AtomicInteger cs = new AtomicInteger();
		// Iterator<int[]> itr = order.iterator();
		spec.forEach(s -> {
			// int[] ii = itr.next();
			int[] ii = new int[] { cs.get() % 3, cs.getAndAdd(1) / 3 };
			s.relx = ii[0];
			s.rely = ii[1];
		});
		// System.out.println(spec);
		return spec;

	}

	//spotless:off
    /*
     * @Inject(
     * require=1,
     * method = "lambda$getRecipeInput$2",at = @At(value = "HEAD"))
     * private static void g(PositionedStack orderStack,CallbackInfoReturnable c){
     * if(orderStack.item.stackSize==0){
     * orderStack.item=ItemProgrammingCircuit.wrap( ( orderStack).item);
     * orderStack.items[0]=ItemProgrammingCircuit.wrap( orderStack.items[0]);
     * }
     * }
     */

    /*
     * @Inject(locals=LocalCapture.CAPTURE_FAILHARD,
     * require=1,
     * method = "getRecipeInput",at = @At(value = "INVOKE",target="removeIf(Ljava/util/function/Predicate;)Z"))
     * private void g(IRecipeHandler recipe, int recipeIndex, String identifier,CallbackInfoReturnable c,List cap,int i,
     * Iterator ii, PositionedStack iii){
     * }
     *  
     * cpw.mods.fml.common.LoaderException: java.lang.VerifyError: Bad local variable type
     * Exception Details:
     * Location:
     * com/github/vfyjxf/nee/processor/GregTech5RecipeProcessor.getRecipeInput(Lcodechicken/nei/recipe/IRecipeHandler;
     * ILjava/lang/String;)Ljava/util/List; @231: iload
     * Reason:
     * Type top (current frame, locals[5]) is not assignable to integer
     * Current Frame:
     * bci: @231
     * flags: { }
     * locals: { 'com/github/vfyjxf/nee/processor/GregTech5RecipeProcessor', 'codechicken/nei/recipe/IRecipeHandler',
     * integer, 'java/lang/String', 'java/util/ArrayList' }
     * stack: { 'java/util/ArrayList', 'java/util/function/Predicate',
     * 'com/github/vfyjxf/nee/processor/GregTech5RecipeProcessor', 'codechicken/nei/recipe/IRecipeHandler', integer,
     * 'java/lang/String', null, 'java/util/ArrayList' }
     * Bytecode:
     * 0x0000000: bb00 f459 b700 f53a 042a 2bb7 00f9 9901
     * 0x0000010: 63b8 00ff b401 03b8 0109 9900 b0b8 00ff
     * 0x0000020: b401 03c0 010b b401 0fb4 0114 3605 1505
     * 0x0000030: 9900 882b 1cb9 011a 0200 b901 1d01 003a
     * 0x0000040: 0619 06b9 00ca 0100 9900 2c19 06b9 00ce
     * 0x0000050: 0100 c001 1f3a 0719 07c6 0018 1907 b401
     * 0x0000060: 22b8 0124 c600 0d19 0419 07b9 0125 0200
     * 0x0000070: 57a7 ffd0 2b1c b901 1a02 00b9 011d 0100
     * 0x0000080: 3a06 1906 b900 ca01 0099 002c 1906 b900
     * 0x0000090: ce01 00c0 011f 3a07 1907 c600 1819 07b4
     * 0x00000a0: 0122 b801 24c7 000d 1904 1907 b901 2502
     * 0x00000b0: 0057 a7ff d0a7 0012 1904 2b1c b901 1a02
     * 0x00000c0: 00b9 0129 0200 57a7 002f 1904 2b1c b901
     * 0x00000d0: 1a02 00b9 0129 0200 5719 04ba 0130 0000
     * 0x00000e0: 2a2b 1c2d 0119 0415 0519 0619 07b7 0134
     * 0x00000f0: b901 3802 0057 1904 b901 3b01 009a 0071
     * 0x0000100: 1904 1904 b901 3f01 0004 64b9 0142 0200
     * 0x0000110: c001 1fb4 0146 0332 3a05 1905 b201 490a
     * 0x0000120: 03bd 0005 b601 4cb6 0150 9a00 3319 05b2
     * 0x0000130: 0153 0a03 bd00 05b6 014c b601 5099 0031
     * 0x0000140: 2bb9 0156 0100 1301 58b6 015b 9a00 112b
     * 0x0000150: b901 5601 0012 dcb6 015b 9900 1419 0419
     * 0x0000160: 04b9 013f 0100 0464 b901 5e02 0057 1904
     * 0x0000170: b019 04b0
     * Stackmap Table:
     * append_frame(@65,Object[#244],Integer,Object[#199])
     * append_frame(@113,Object[#287])
     * chop_frame(@116,1)
     * same_frame(@130)
     * append_frame(@178,Object[#287])
     * chop_frame(@181,1)
     * chop_frame(@184,1)
     * same_frame(@199)
     * chop_frame(@202,1)
     * same_frame(@246)
     * append_frame(@349,Object[#41])
     * chop_frame(@366,1)
     * same_frame(@369)
     * at cpw.mods.fml.common.LoadController.transition(LoadController.java:163)
     * at cpw.mods.fml.common.Loader.initializeMods(Loader.java:744)
     * at cpw.mods.fml.client.FMLClientHandler.finishMinecraftLoading(FMLClientHandler.java:311)
     * at net.minecraft.client.Minecraft.startGame(Minecraft.java:597)
     * at net.minecraft.client.Minecraft.run(Minecraft.java:942)
     * at net.minecraft.client.main.Main.main(Main.java:164)
     * at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     * at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     * at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     * at java.lang.reflect.Method.invoke(Method.java:498)
     * at net.minecraft.launchwrapper.Launch.launch(Launch.java:135)
     * at net.minecraft.launchwrapper.Launch.main(Launch.java:28)
     * at net.minecraftforge.gradle.GradleStartCommon.launch(GradleStartCommon.java:97)
     * at GradleStart.main(GradleStart.java:40)
     * Caused by: java.lang.VerifyError: Bad local variable type
     * Exception Details:
     * Location:
     * com/github/vfyjxf/nee/processor/GregTech5RecipeProcessor.getRecipeInput(Lcodechicken/nei/recipe/IRecipeHandler;
     * ILjava/lang/String;)Ljava/util/List; @231: iload
     * Reason:
     * Type top (current frame, locals[5]) is not assignable to integer
     * Current Frame:
     * bci: @231
     * flags: { }
     * locals: { 'com/github/vfyjxf/nee/processor/GregTech5RecipeProcessor', 'codechicken/nei/recipe/IRecipeHandler',
     * integer, 'java/lang/String', 'java/util/ArrayList' }
     * stack: { 'java/util/ArrayList', 'java/util/function/Predicate',
     * 'com/github/vfyjxf/nee/processor/GregTech5RecipeProcessor', 'codechicken/nei/recipe/IRecipeHandler', integer,
     * 'java/lang/String', null, 'java/util/ArrayList' }
     * Bytecode:
     * 0x0000000: bb00 f459 b700 f53a 042a 2bb7 00f9 9901
     * 0x0000010: 63b8 00ff b401 03b8 0109 9900 b0b8 00ff
     * 0x0000020: b401 03c0 010b b401 0fb4 0114 3605 1505
     * 0x0000030: 9900 882b 1cb9 011a 0200 b901 1d01 003a
     * 0x0000040: 0619 06b9 00ca 0100 9900 2c19 06b9 00ce
     * 0x0000050: 0100 c001 1f3a 0719 07c6 0018 1907 b401
     * 0x0000060: 22b8 0124 c600 0d19 0419 07b9 0125 0200
     * 0x0000070: 57a7 ffd0 2b1c b901 1a02 00b9 011d 0100
     * 0x0000080: 3a06 1906 b900 ca01 0099 002c 1906 b900
     * 0x0000090: ce01 00c0 011f 3a07 1907 c600 1819 07b4
     * 0x00000a0: 0122 b801 24c7 000d 1904 1907 b901 2502
     * 0x00000b0: 0057 a7ff d0a7 0012 1904 2b1c b901 1a02
     * 0x00000c0: 00b9 0129 0200 57a7 002f 1904 2b1c b901
     * 0x00000d0: 1a02 00b9 0129 0200 5719 04ba 0130 0000
     * 0x00000e0: 2a2b 1c2d 0119 0415 0519 0619 07b7 0134
     * 0x00000f0: b901 3802 0057 1904 b901 3b01 009a 0071
     * 0x0000100: 1904 1904 b901 3f01 0004 64b9 0142 0200
     * 0x0000110: c001 1fb4 0146 0332 3a05 1905 b201 490a
     * 0x0000120: 03bd 0005 b601 4cb6 0150 9a00 3319 05b2
     * 0x0000130: 0153 0a03 bd00 05b6 014c b601 5099 0031
     * 0x0000140: 2bb9 0156 0100 1301 58b6 015b 9a00 112b
     * 0x0000150: b901 5601 0012 dcb6 015b 9900 1419 0419
     * 0x0000160: 04b9 013f 0100 0464 b901 5e02 0057 1904
     * 0x0000170: b019 04b0
     * Stackmap Table:
     * append_frame(@65,Object[#244],Integer,Object[#199])
     * append_frame(@113,Object[#287])
     * chop_frame(@116,1)
     * same_frame(@130)
     * append_frame(@178,Object[#287])
     * chop_frame(@181,1)
     * chop_frame(@184,1)
     * same_frame(@199)
     * chop_frame(@202,1)
     * same_frame(@246)
     * append_frame(@349,Object[#41])
     * chop_frame(@366,1)
     * same_frame(@369)
     */
    /*
     * for(ItemStack[] i:recipe){
     * if(i!=null){
     * if(i.length>0&&i[0].stackSize==0)
     * i[0]=ItemProgrammingCircuit.wrap(i[0]);
     * }
     */

}

// if(is.stackSize==0) ItemProgrammingCircuit.wrap(is);

// return (is);
