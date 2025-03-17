package reobf.proghatches.main.registration;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTUtility;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;
import thaumcraft.common.config.ConfigItems;

public class ProgHatchCreativeTab extends CreativeTabs {

    public ProgHatchCreativeTab(String lable) {
        super(lable);
        // TODO Auto-generated constructor stub
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getIconItemStack() {
        return ItemProgrammingCircuit.wrap(GTUtility.getIntegratedCircuit(0));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void displayAllReleventItems(List p_78018_1_) {
        p_78018_1_.addAll(Registration.items);

        p_78018_1_.add(ItemProgrammingCircuit.wrap(null));
        ProghatchesUtil.allCircuits()
            .stream()
            .map(ItemProgrammingCircuit::wrap)
            .forEach(p_78018_1_::add);
        p_78018_1_.add(new ItemStack(MyMod.fixer));
        p_78018_1_.add(new ItemStack(MyMod.toolkit));
        for (int i = 0; i < 15; i++) {
            p_78018_1_.add(new ItemStack(MyMod.smartarm, 1, i));
        }
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 4));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 0));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 1));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 100));
        // p_78018_1_.add(new ItemStack(MyMod.cover, 1, 2));
        p_78018_1_.add(new ItemStack(MyMod.oc_api, 1));
        p_78018_1_.add(new ItemStack(MyMod.oc_redstone, 1));
        p_78018_1_.add(new ItemStack(MyMod.iohub, 1));
        p_78018_1_.add(new ItemStack(MyMod.pitem, 1));
        p_78018_1_.add(new ItemStack(MyMod.pstation, 1));
        p_78018_1_.add(new ItemStack(MyMod.plunger, 1));
        p_78018_1_.add(new ItemStack(MyMod.plunger, 1, 1));
        p_78018_1_.add(new ItemStack(MyMod.upgrades, 1, 0));
        p_78018_1_.add(new ItemStack(MyMod.upgrades, 1, 1));
        p_78018_1_.add(new ItemStack(MyMod.upgrades, 1, 2));
        // p_78018_1_.add(new ItemStack(MyMod.alert, 1));
        p_78018_1_.add(new ItemStack(MyMod.lazer_p2p_part));
        p_78018_1_.add(new ItemStack(MyMod.ma_p2p_part));
        p_78018_1_.add(new ItemStack(ConfigItems.itemGolemCore, 1, 120));
        p_78018_1_.add(new ItemStack(MyMod.amountmaintainer));
        p_78018_1_.add(new ItemStack(MyMod.submitter));
        p_78018_1_.add(new ItemStack(MyMod.reader));
        p_78018_1_.add(new ItemStack(MyMod.reactorsyncer));
        p_78018_1_.add(new ItemStack(MyMod.partproxy, 1, 0));
        p_78018_1_.add(new ItemStack(MyMod.partproxy, 1, 1));
        p_78018_1_.add(new ItemStack(MyMod.partproxy, 1, 2));
        p_78018_1_.add(new ItemStack(MyMod.storageproxy, 1, 0));
        p_78018_1_.add(new ItemStack(MyMod.storageproxy, 1, 1));
        p_78018_1_.add(new ItemStack(MyMod.storageproxy, 1, 2));
        p_78018_1_.add(new ItemStack(MyMod.exciter, 1, 0));

        p_78018_1_.add(new ItemStack(MyMod.stockingexport, 1, 0));
        p_78018_1_.add(new ItemStack(MyMod.stockingexport, 1, 1));
        // p_78018_1_.add(new ItemStack(MyMod.storageproxy));
        // p_78018_1_.add(new ItemStack(MyMod.euupgrade, 1));

        p_78018_1_.addAll(Registration.items_eucrafting);
        // for(int i=0;i<=30;i++)
        // p_78018_1_.add(new ItemStack(MyMod.eu_source_part,1,i));
        // p_78018_1_.add(new ItemStack(MyMod.eu_source_part,1,0));
        // p_78018_1_.add(new ItemStack(MyMod.block_euinterface));

        // p_78018_1_.add(new ItemStack(MyMod.euinterface_p2p));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 3));

        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 32));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 33));
        // p_78018_1_.add(new ItemStack(MyMod.cover, 1, 34));
        // p_78018_1_.add(new ItemStack(MyMod.cover, 1, 35));
        // p_78018_1_.add(new ItemStack(MyMod.cover, 1, 36));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 37));
        p_78018_1_.add(new ItemStack(MyMod.ma_conduit));
        p_78018_1_.add(new ItemStack(MyMod.ma_iface));
        p_78018_1_.add(new ItemStack(MyMod.circuit_interceptor));
        p_78018_1_.add(new ItemStack(MyMod.circuit_interceptor, 1, 1));

        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 90));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 91));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 92));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 93));
        p_78018_1_.add(new ItemStack(MyMod.cover, 1, 94));
        p_78018_1_.add(new ItemStack(MyMod.emitterpattern));
        p_78018_1_.add(new ItemStack(MyMod.request_tunnel));
        p_78018_1_.add(new ItemStack(MyMod.orbswitcher));
        p_78018_1_.add(new ItemStack(MyMod.part_cow));
        p_78018_1_.add(new ItemStack(MyMod.chip));
    }

    @Override
    public Item getTabIconItem() {

        return MyMod.progcircuit;
    }

}
