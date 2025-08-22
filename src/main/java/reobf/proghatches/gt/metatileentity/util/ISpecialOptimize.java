package reobf.proghatches.gt.metatileentity.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.apache.commons.lang3.tuple.Pair;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IInterfaceViewable;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerOptimizePatterns;
import appeng.core.AELog;
import appeng.core.features.registries.InterfaceTerminalRegistry;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.crafting.v2.CraftingContext;
import appeng.crafting.v2.CraftingJobV2;
import appeng.crafting.v2.resolvers.CraftableItemResolver.CraftFromPatternTask;
import appeng.crafting.v2.resolvers.CraftingTask;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.cache.CraftingGridCache;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartPatternTerminalEx;
import appeng.parts.reporting.PartTerminal;
import appeng.tile.misc.TilePatternOptimizationMatrix;
import appeng.util.PatternMultiplierHelper;
import appeng.util.Platform;
import codechicken.nei.ItemStackMap;
import codechicken.nei.ItemStackSet;

public interface ISpecialOptimize {
public void optimize(ItemStackMap<Pair<Object, Integer>>map);

public void blacklist(ItemStackSet black);
}
