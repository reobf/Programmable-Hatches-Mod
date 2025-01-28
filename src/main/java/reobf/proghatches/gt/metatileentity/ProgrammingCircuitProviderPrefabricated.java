package reobf.proghatches.gt.metatileentity;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;

import static gregtech.api.enums.ItemList.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import bartworks.system.material.Werkstoff;
import bartworks.system.material.WerkstoffLoader;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTOreDictUnificator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.gt.metatileentity.util.ICircuitProvider;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class ProgrammingCircuitProviderPrefabricated extends MTEHatch implements ICircuitProvider{
	public ProgrammingCircuitProviderPrefabricated(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount,int i) {
		super(aID, aName, aNameRegional, aTier, aInvSlotCount,
				reobf.proghatches.main.Config.get("PCPP", ImmutableMap.of(
						"type",
						StatCollector.translateToLocal("circuitprovider.prefab."+i+".type.name")
						
						
						))

		);
		Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
		this.index=i;
	}

	
	@Override
	public boolean isFacingValid(ForgeDirection facing) {

		return true;
	}

	

	public ProgrammingCircuitProviderPrefabricated(String aName, int aTier, int aInvSlotCount, String[] aDescription,
			ITexture[][][] aTextures,int i) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);
this.index=i;
	}


	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
	
		return new ProgrammingCircuitProviderPrefabricated(mName, mTier, mInventory.length, mDescriptionArray, mTextures,index);
		
	}


	@Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
			int colorIndex, boolean aActive, boolean redstoneLevel) {

		return super.getTexture(aBaseMetaTileEntity, side, aFacing, colorIndex, aActive, redstoneLevel);

	}

	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {

		return new ITexture[] { aBaseTexture, TextureFactory.builder()
				.setFromBlock(MyMod.iohub, BlockIOHub.magicNO_provider_in_active_overlay).glow().build() };
	}

	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {

		return new ITexture[] { aBaseTexture, TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_provider_in_overlay) };
	}


	@Override
	public void clearDirty() {
		
		
	}


	@Override
	public boolean patternDirty() {
		
		return false;
	}

int index;
public static  Map<Integer,Collection<ItemStack>> prefab=new HashMap<>();
public static boolean   init;
static void init(){
	if(init==true)return;
	init=true;
	System.out.println("init!!!!");
	reg(0,GregTechAPI.getConfigurationCircuitList(100));
	   ItemList[][] all = new ItemList[][]{{
	    Shape_Mold_Bottle,
	    Shape_Mold_Plate,
	    Shape_Mold_Ingot,
	    Shape_Mold_Casing,
	    Shape_Mold_Gear,
	    Shape_Mold_Gear_Small,
	    Shape_Mold_Credit,
	    Shape_Mold_Nugget,
	    Shape_Mold_Block,
	    Shape_Mold_Ball,
	    Shape_Mold_Bun,
	    Shape_Mold_Bread,
	    Shape_Mold_Baguette,
	    Shape_Mold_Cylinder,
	    Shape_Mold_Anvil,
	    Shape_Mold_Arrow,
	    Shape_Mold_Name,
	    Shape_Mold_Rod,
	    Shape_Mold_Bolt,
	    Shape_Mold_Round,
	    Shape_Mold_Screw,
	    Shape_Mold_Ring,
	    Shape_Mold_Rod_Long,
	    Shape_Mold_Rotor,
	    Shape_Mold_Turbine_Blade,
	    Shape_Mold_Pipe_Tiny,
	    Shape_Mold_Pipe_Small,
	    Shape_Mold_Pipe_Medium,
	    Shape_Mold_Pipe_Large,
	    Shape_Mold_Pipe_Huge,
	    Shape_Mold_ToolHeadDrill,},
		   { Shape_Slicer_Flat,
	    Shape_Slicer_Stripes,},{
	    Shape_Extruder_Bottle,
	    Shape_Extruder_Plate,
	    Shape_Extruder_Cell,
	    Shape_Extruder_Ring,
	    Shape_Extruder_Rod,
	    Shape_Extruder_Bolt,
	    Shape_Extruder_Ingot,
	    Shape_Extruder_Wire,
	    Shape_Extruder_Casing,
	    Shape_Extruder_Pipe_Tiny,
	    Shape_Extruder_Pipe_Small,
	    Shape_Extruder_Pipe_Medium,
	    Shape_Extruder_Pipe_Large,
	    Shape_Extruder_Pipe_Huge,
	    Shape_Extruder_Block,
	    Shape_Extruder_Sword,
	    Shape_Extruder_Pickaxe,
	    Shape_Extruder_Shovel,
	    Shape_Extruder_Axe,
	    Shape_Extruder_Hoe,
	    Shape_Extruder_Hammer,
	    Shape_Extruder_File,
	    Shape_Extruder_Saw,
	    Shape_Extruder_Gear,
	    Shape_Extruder_Rotor,
	    Shape_Extruder_Turbine_Blade,
	    Shape_Extruder_Small_Gear,
	    Shape_Extruder_ToolHeadDrill,
	    },{
	    White_Dwarf_Shape_Extruder_Bottle,
	    White_Dwarf_Shape_Extruder_Plate,
	    White_Dwarf_Shape_Extruder_Cell,
	    White_Dwarf_Shape_Extruder_Ring,
	    White_Dwarf_Shape_Extruder_Rod,
	    White_Dwarf_Shape_Extruder_Bolt,
	    White_Dwarf_Shape_Extruder_Ingot,
	    White_Dwarf_Shape_Extruder_Wire,
	    White_Dwarf_Shape_Extruder_Casing,
	    White_Dwarf_Shape_Extruder_Pipe_Tiny,
	    White_Dwarf_Shape_Extruder_Pipe_Small,
	    White_Dwarf_Shape_Extruder_Pipe_Medium,
	    White_Dwarf_Shape_Extruder_Pipe_Large,
	    White_Dwarf_Shape_Extruder_Pipe_Huge,
	    White_Dwarf_Shape_Extruder_Block,
	    White_Dwarf_Shape_Extruder_Sword,
	    White_Dwarf_Shape_Extruder_Pickaxe,
	    White_Dwarf_Shape_Extruder_Shovel,
	    White_Dwarf_Shape_Extruder_Axe,
	    White_Dwarf_Shape_Extruder_Hoe,
	    White_Dwarf_Shape_Extruder_Hammer,
	    White_Dwarf_Shape_Extruder_File,
	    White_Dwarf_Shape_Extruder_Saw,
	    White_Dwarf_Shape_Extruder_Gear,
	    White_Dwarf_Shape_Extruder_Rotor,
	    White_Dwarf_Shape_Extruder_Turbine_Blade,
	    White_Dwarf_Shape_Extruder_Small_Gear,
	    White_Dwarf_Shape_Extruder_ToolHeadDrill}};
	    
	    int i=1;
	    for(ItemList[] t:all){
	    	reg(i++,Arrays.stream(t)
	    	.map(s->s.get(1)).collect(Collectors.toList()))
	    	;
	    	
	    	
	    }
	    
	    ArrayList<ItemStack> is= new ArrayList<>();
	 for(Materials mat:  Materials.values()){
		ItemStack opt= GTOreDictUnificator.get(OrePrefixes.lens.get(mat),null, 1,false,false);
	if(opt!=null)is.add(opt);
	//
	
	 }
	 for(Werkstoff mat:Werkstoff.werkstoffHashSet){
		 ItemStack opt=  WerkstoffLoader.getCorrespondingItemStackUnsafe(OrePrefixes.lens, mat, i);
		 if(opt!=null)is.add(opt);
	 }
	 System.out.println(is);System.out.println("TTTTTTTTTTTTT");
	 reg(5,is);
	 
	 
	 
	    prefab.values().forEach(s->s.removeIf(isy->{
	    	if(isy==null)System.out.println("Null:"+s);
	    	return isy==null;}));
	 
	
	    
	    
}
static private void reg(int a,Collection<ItemStack> raw){
	prefab.put(a,raw.stream().map(ItemProgrammingCircuit::wrap)
	.collect(Collectors.toList()));
}

	@Override
	public Collection<ItemStack> getCircuit() {
		init();
		return prefab.get(index);
	}
}
