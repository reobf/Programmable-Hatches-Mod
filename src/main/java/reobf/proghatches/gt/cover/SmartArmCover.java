package reobf.proghatches.gt.cover;

import static gregtech.api.enums.Mods.GregTech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataInput;
import com.google.gson.Gson;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.api.util.ISerializableObject;
import gregtech.common.covers.redstone.GT_Cover_AdvancedRedstoneReceiverBase.GateMode;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.gt.cover.parser.SimpleParser;
import reobf.proghatches.gt.cover.parser.SimpleParser.Context;
import reobf.proghatches.gt.cover.parser.SimpleParser.Expression;
import reobf.proghatches.gt.cover.parser.SimpleParser.Rational;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.util.ProghatchesUtil;

public class SmartArmCover extends GT_CoverBehaviorBase<SmartArmCover.Data> {

	public SmartArmCover(int tier) {
		super(Data.class);
		this.mtier = tier;
	}

	int mtier;

	static public class Data implements ISerializableObject {

		boolean io;
		int mode;
		int probe;

		public boolean dyn;
		String formula = "";
		String formulaprev = "";
		// public boolean update;
		String detail = "idle";

		public int state;// 0:idle 1:parsing 2:gen
		public int cslot;
		public int key[] = new int[0];
		public int value[] = new int[0];
		SimpleParser.Expression e;

		@Override
		public ISerializableObject copy() {
			Data d = new Data();
			d.io = io;
			d.formula = formula;
			d.formulaprev = formulaprev;
			d.mode = mode;
			d.detail = detail;
			d.state = state;
			d.cslot = cslot;
			d.key = key;
			d.value = value;
			d.probe = probe;
			return d;
		}

		@Override
		public NBTBase saveDataToNBT() {
			NBTTagCompound t = new NBTTagCompound();
			t.setInteger("probe", probe);
			t.setBoolean("io", io);
			t.setString("formula", Optional.ofNullable(formula).orElse(""));
			t.setString("formulaprev", Optional.ofNullable(formulaprev).orElse(""));
			t.setInteger("mode", mode);

			t.setString("detail", Optional.ofNullable(detail).orElse(""));
			t.setInteger("state", state);
			t.setInteger("cslot", cslot);
			t.setIntArray("in", key);
			t.setIntArray("out", value);
			Optional.ofNullable(e).map(json::toJson).ifPresent(s -> t.setByteArray("expression", s.getBytes()));
			// json.toJson(e);
			t.setBoolean("dyn", dyn);
			return t;
		}

		static Gson json = new Gson();

		private int[] cv(byte[] b) {
			int i[] = new int[b.length / 4];
			for (int ii = 0; ii < i.length; ii++) {
				i[ii] = (b[ii * 4] << 0) | (b[ii * 4 + 1] << 8) | (b[ii * 4 + 2] << 16) | (b[ii * 4 + 3] << 24);
			}
			return i;
		}

		private byte[] cv(int[] b) {
			byte i[] = new byte[b.length * 4];
			for (int ii = 0; ii < b.length; ii++) {
				i[ii] = (byte) (Byte.MAX_VALUE & (b[ii] >> 0));
				i[ii + 1] = (byte) (Byte.MAX_VALUE & (b[ii] >> 8));
				i[ii + 2] = (byte) (Byte.MAX_VALUE & (b[ii] >> 16));
				i[ii + 3] = (byte) (Byte.MAX_VALUE & (b[ii] >> 24));
			}
			return i;
		}

		@Override
		public void writeToByteBuf(ByteBuf aBuf) {
			aBuf.writeInt(probe);
			aBuf.writeBoolean(io);
			aBuf.writeInt(formula.getBytes().length);
			aBuf.writeBytes(formula.getBytes());
			aBuf.writeInt(formulaprev.getBytes().length);
			aBuf.writeBytes(formulaprev.getBytes());
			aBuf.writeInt(mode);

			aBuf.writeInt(detail.getBytes().length);
			aBuf.writeBytes(detail.getBytes());
			aBuf.writeInt(state);
			aBuf.writeInt(cslot);
			byte[] ain = cv(key);
			aBuf.writeInt(ain.length);
			aBuf.writeBytes(ain);

			byte[] aout = cv(key);
			aBuf.writeInt(aout.length);
			aBuf.writeBytes(aout);
			Optional.ofNullable(e).map(json::toJson).ifPresent(s -> {
				byte[] b = s.getBytes();
				aBuf.writeInt(b.length);
				aBuf.writeBytes(b);

			});
			if (e == null) {
				aBuf.writeInt(0);
			}
			aBuf.writeBoolean(dyn);
		}

		@Override
		public void loadDataFromNBT(NBTBase aNBT) {
			probe = ((NBTTagCompound) aNBT).getInteger("probe");

			io = ((NBTTagCompound) aNBT).getBoolean("io");
			formula = ((NBTTagCompound) aNBT).getString("formula");
			formulaprev = ((NBTTagCompound) aNBT).getString("formulaprev");
			mode = ((NBTTagCompound) aNBT).getInteger("mode");

			detail = ((NBTTagCompound) aNBT).getString("detail");
			state = ((NBTTagCompound) aNBT).getInteger("state");
			cslot = ((NBTTagCompound) aNBT).getInteger("cslot");
			key = ((NBTTagCompound) aNBT).getIntArray("in");
			value = ((NBTTagCompound) aNBT).getIntArray("out");

			Optional.ofNullable(((NBTTagCompound) aNBT).getByteArray("expression"))
					.map(s -> json.fromJson(new String(s), Expression.class)).ifPresent(s -> e = s);

			;
			dyn = ((NBTTagCompound) aNBT).getBoolean("dyn");
			// System.err.println(e);

		}

		@Override
		public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
			probe = aBuf.readInt();
			io = aBuf.readBoolean();
			byte[] b = new byte[aBuf.readInt()];
			aBuf.readFully(b);
			formula = new String(b);
			b = new byte[aBuf.readInt()];
			aBuf.readFully(b);

			formulaprev = new String(b);

			mode = aBuf.readInt();

			b = new byte[aBuf.readInt()];
			aBuf.readFully(b);
			detail = new String(b);
			state = aBuf.readInt();
			cslot = aBuf.readInt();

			b = new byte[aBuf.readInt()];
			aBuf.readFully(b);
			key = cv(b);
			b = new byte[aBuf.readInt()];
			aBuf.readFully(b);
			value = cv(b);
			int size = aBuf.readInt();
			if (size > 0) {

				byte[] bt = new byte[size];
				aBuf.readFully(bt);
				e = json.fromJson(new String(bt), Expression.class);
			}

			dyn = aBuf.readBoolean();
			return this;
		}

	}

	public static final UITexture PROBE = UITexture.fullImage(GregTech.ID, "items/gt.metaitem.01/762");

	private class ArmUIFactory extends UIFactory {

		protected ModularWindow createWindow(final EntityPlayer player) {

			final int WIDTH = 16 * 8;
			final int HEIGHT = 16 * 2;

			ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
			builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
			builder.setGuiTint(this.getUIBuildContext().getGuiColorization());
			builder.setDraggable(true);
			builder.widget((new com.gtnewhorizons.modularui.common.widget.ButtonWidget()).setOnClick((s, b) -> {
				if (getCoverData().state != 0)
					return;
				if (s.mouseButton == 0)
					getCoverData().probe++;
				else
					getCoverData().probe--;
				int len = getCoverData().key.length;
				if (len > 0)
					if (getCoverData().probe >= len) {
						getCoverData().probe = getCoverData().probe % len;
					}
				if (getCoverData().probe < 0) {
					getCoverData().probe = getCoverData().probe + len;
				}

			}).setBackground(GT_UITextures.BUTTON_STANDARD,
					UITexture.fullImage(GregTech.ID, "blocks/iconsets/OVERLAY_PIPELINE_ITEM_SIDE_UP_DOWN")
			// BlockIcons.OVERLAY_PIPELINE_ITEM_SIDE_UP_DOWN,
			// GT_UITextures.OVERLAY_BUTTON_ARROW_GREEN_UP
			).setSize(16, 16)

					.addTooltip(LangManager.translateToLocal("programmable_hatches.cover.smart.probe.move"))
					.setPos(8, 8)

			);
			builder.widget(TextWidget.dynamicString(() ->

			String.format("§0f(§4%s§0)=%s", getCoverData().probe, probe(getCoverData(), getUIBuildContext().getTile()))

			).setSynced(true).setMaxWidth(999).setPos(8 + 16 * 2, 16));

			return builder.build();
		}

		

		private static final int startX = 10;
		private static final int startY = 25;
		private static final int spaceX = 18;
		private static final int spaceY = 18;

		//private int maxSlot;

		protected ArmUIFactory(GT_CoverUIBuildContext buildContext) {
			super(buildContext);
		}

		
		@Override
		protected void addUIWidgets(ModularWindow.Builder builder) {
			//maxSlot = getMaxSlot();
			// CoverDataControllerWidget<Data> o = new
			// CoverDataControllerWidget<>(this::getCoverData,
			// this::setCoverData, SmartRobotArmCover.this);

			if (getUIBuildContext().isAnotherWindow() == false) {
				getUIBuildContext().addSyncedWindow(77, (s) -> createWindow(s));

				builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
					if (clickData.mouseButton == 0) {
						if (!widget.isClient())

							widget.getContext().openSyncedWindow(77);
					}
				}).setPlayClickSound(true).setBackground(GT_UITextures.BUTTON_STANDARD, PROBE)
						.addTooltips(ImmutableList
								.of(LangManager.translateToLocalFormatted("programmable_hatches.cover.smart.probe")))
						.setSize(16, 16).setPos(startX + spaceX * 6, startY + spaceY * 2));

			}

			builder.widget(new FakeSyncWidget.StringSyncer(() -> {
				return getCoverData().detail;

			}, s -> {
				getCoverData().detail = s;
			}

			));
			builder.widget(new FakeSyncWidget.IntegerSyncer(() -> {
				return getCoverData().probe;

			}, s -> {
				getCoverData().probe = s;
			}

			));
			builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
					.setGetter(() -> getCoverData().io ? 1 : 0).setSetter(s -> getCoverData().io = s == 1).setLength(2)
					.setTextureGetter(i -> {
						if (i == 1)
							return GT_UITextures.OVERLAY_BUTTON_EXPORT;
						return GT_UITextures.OVERLAY_BUTTON_IMPORT;
					})

					.addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.smart.io.false"))
					.addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.smart.io.true"))
					.setPos(startX, startY)

			);
			builder.widget((new com.gtnewhorizons.modularui.common.widget.ButtonWidget()).setOnClick((s, b) -> {
				getCoverData().formulaprev = "\0\0\0\0";
			}).setBackground(GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_SLOT_RECYCLE)

					.addTooltip(LangManager.translateToLocal("programmable_hatches.cover.smart.reset"))
					.setPos(startX + spaceX * 6, startY)

			);

			builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
					.setGetter(() -> getCoverData().mode).setSetter(s -> {
						getCoverData().mode = s;
						getCoverData().formulaprev = "\0\0\0\0";
					}).setLength(2).setTextureGetter(i -> {
						if (i == 1)
							return GT_UITextures.OVERLAY_BUTTON_EXPORT;
						return GT_UITextures.OVERLAY_BUTTON_IMPORT;
					}

					).addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.smart.mode.0")).addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.smart.mode.1")).setPos(startX, startY + spaceY));

			CycleButtonWidget c;
			builder.widget((c = (CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
					.setGetter(() -> getCoverData().dyn ? 1 : 0).setSetter(s -> {
						c.notifyTooltipChange();
						getCoverData().dyn = s == 1;
						getCoverData().formulaprev = "~~~~~";
					}).setLength(2).setTextureGetter(i -> {
						if (i == 1)
							return GT_UITextures.OVERLAY_BUTTON_CHECKMARK;
						return GT_UITextures.OVERLAY_BUTTON_CROSS;
					}

					).dynamicTooltip(() -> c.getState() == 0 ? Arrays.asList(LangManager.translateToLocal("programmable_hatches.cover.smart.dyn.0"), StatCollector.translateToLocal("programmable_hatches.cover.smart.dyn.shift.for.detail")) : Arrays.asList(LangManager.translateToLocal("programmable_hatches.cover.smart.dyn.1"), StatCollector.translateToLocal("programmable_hatches.cover.smart.dyn.shift.for.detail"))

					)

					// .addTooltip(0,LangManager.translateToLocal("programmable_hatches.cover.smart.dyn.0"))
					// .addTooltip(0,LangManager.translateToLocal("programmable_hatches.cover.smart.dyn.shift.for.detail"))
					// .addTooltip(1,LangManager.translateToLocal("programmable_hatches.cover.smart.dyn.1"))
					// .addTooltip(1,LangManager.translateToLocal("programmable_hatches.cover.smart.dyn.shift.for.detail"))
					.dynamicTooltipShift(() -> Arrays.asList(rangeOf(Integer.valueOf(
							LangManager.translateToLocal("programmable_hatches.cover.smart.dyn.shift." + c.getState())))
									.map(s -> LangManager.translateToLocal(
											"programmable_hatches.cover.smart.dyn.shift." + c.getState() + "." + s))
									.toArray(String[]::new))

					).setPos(startX, startY + spaceY * 3));

			builder.widget(Optional.of(new TextFieldWidget()).filter(s -> {
				s.setText((getCoverData().formula));
				return true;
			}).get().setValidator(val -> {
				if (val == null) {
					val = "";
				}
				return val;
			}).setSynced(false, true).setGetter(() -> {
				return getCoverData().formula;
			}).setSetter(s -> {
				getCoverData().formula = s;
			}).setPattern(BaseTextFieldWidget.ANY).setMaxLength(150).setScrollBar()

					.setFocusOnGuiOpen(true).setTextColor(Color.WHITE.dark(1))

					.setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
					.setPos(startX + spaceX, startY + spaceY * 2).setSize(spaceX * 3, 12)

			)

			;

			/*
			 * o.addFollower( new CoverDataFollower_CycleButtonWidget<Data>(),
			 * (s)->{return (s.io)?1:0;}, (b,s)->{ b.io=s==1; return b;}, widget
			 * ->{ widget .setLength(2) .setTextureGetter( i->{ if(i==1) return
			 * GT_UITextures.OVERLAY__EXPORT; return
			 * GT_UITextures.OVERLAY_BUTTON_IMPORT; } ).addTooltip(0,
			 * LangManager.translateToLocal(
			 * "programmable_hatches.cover.smart.io.false"))
			 * .addTooltip(1,LangManager.translateToLocal(
			 * "programmable_hatches.cover.smart.io.true")) .setPos(startX ,
			 * startY ); } ); o.addFollower( new
			 * CoverDataFollower_CycleButtonWidget<Data>(), (s)->{return
			 * s.mode;}, (b,s)->{ b.mode=s; return b;}, widget ->{ widget
			 * .setLength(2) .setTextureGetter( i->{ if(i==1) return
			 * GT_UITextures.OVERLAY_BUTTON_EXPORT; return
			 * GT_UITextures.OVERLAY_BUTTON_IMPORT; }
			 * ).addTooltip(0,LangManager.translateToLocal(
			 * "programmable_hatches.cover.smart.mode.0"))
			 * .addTooltip(1,LangManager.translateToLocal(
			 * "programmable_hatches.cover.smart.mode.1")) .setPos(startX ,
			 * startY +spaceY); } ); o.addFollower( new
			 * CoverDataFollower_TextFieldWidget<>(), w->w.formula, (w,a)->{
			 * w.formula=a; return w;} , widget -> widget.setOnScrollText()
			 * .setValidator(val -> { if(val==null){val="";} return val; })
			 * .setPattern(BaseTextFieldWidget.ANY)
			 * .setMaxLength(50).setScrollBar() .setFocusOnGuiOpen(true)
			 * .setPos(startX+ spaceX , startY+spaceY*2) .setSize(spaceX * 2 +
			 * 5, 12) ); builder.widget(o)
			 */

			builder.widget(new DrawableWidget().setDrawable(() ->

			UITexture.fullImage(new ResourceLocation("proghatches", "textures/formula" + getCoverData().mode + ".png"))

			).addTooltip(LangManager.translateToLocal("programmable_hatches.cover.smart.tips.0"))
					.addTooltip(LangManager.translateToLocal("programmable_hatches.cover.smart.tips.1"))

					.setPos(startX + spaceX, startY + spaceY).setSize(14 * 4, 14))

					.widget(TextWidget.dynamicString(() -> {
						return StatCollector
								.translateToLocal("programmable_hatches.cover.smart.io." + getCoverData().io);
					}).setSynced(false).setTextAlignment(Alignment.CenterLeft).setDefaultColor(COLOR_TEXT_GRAY.get())
							.setPos(startX + spaceX, 4 + startY + spaceY * 0))

					.widget(new TextWidget("f =").setDefaultColor(COLOR_TEXT_GRAY.get()).setPos(startX,
							startY + spaceY * 2))

					.widget(TextWidget.dynamicString(() -> {
						return getCoverData().detail;
					}).setSynced(false).setTextAlignment(Alignment.CenterLeft).setDefaultColor(COLOR_TEXT_GRAY.get())
							.setPos(startX + spaceX, 4 + startY + spaceY * 3))

			;

			/*
			 * .widget( new TextWidget( LangManager.translateToLocal(
			 * "programmable_hatches.cover.smart.formula")).setDefaultColor(
			 * COLOR_TEXT_GRAY.get()) .setPos(startX + spaceX * 3, 4 + startY +
			 * spaceY * 1)) .widget( new
			 * TextWidget(LangManager.translateToLocal(
			 * "programmable_hatches.cover.smart.formula.desc")).
			 * setDefaultColor(COLOR_TEXT_GRAY.get()) .setPos(startX + spaceX *
			 * 3, 4 + startY + spaceY * 2));
			 */ }

		

	}

	@Override
	public boolean hasCoverGUI() {
		return true;
	}

	@Override
	public boolean useModularUI() {
		return true;
	}

	@Override
	public ModularWindow createWindow(GT_CoverUIBuildContext buildContext) {
		return new ArmUIFactory(buildContext).createWindow();
	}

	
	@Override
	protected boolean onCoverRightClickImpl(ForgeDirection side, int aCoverID, Data d, ICoverable aTileEntity,
			EntityPlayer aPlayer, float aX, float aY, float aZ) {

		//if (1 > 0)
			return super.onCoverRightClickImpl(side, aCoverID, d, aTileEntity, aPlayer, aX, aY, aZ);
		// not used
		/*
		if (d.dyn) {
			GT_Utility.sendChatToPlayer(aPlayer, info("print.dyn"));
			return true;
		}
		GT_Utility.sendChatToPlayer(aPlayer, info("mode." + d.mode));

		int maxslot = Integer.MAX_VALUE;
		TileEntity te = aTileEntity.getTileEntityAtSide(side);
		if (d.mode == 1 && te != null && te instanceof IInventory) {
			maxslot = ((IInventory) (te)).getSizeInventory();
		}
		if (d.mode == 0) {
			maxslot = aTileEntity.getSizeInventory();
		}
		String dir = d.io == (d.mode == 1) ? "========>" : "<========";
		for (int i = 0; i < d.key.length; i++) {

			if (d.key[i] == Integer.MIN_VALUE) {
				GT_Utility.sendChatToPlayer(aPlayer, info("nyc"));
			} else if (d.value[i] == Integer.MIN_VALUE) {
				GT_Utility.sendChatToPlayer(aPlayer, d.key[i] + dir + "" + info("ftc"));

			} else

			// TileEntity te = aTileEntity.getTileEntityAtSide(side);

			if (d.value[i] >= maxslot || d.value[i] < 0) {
				GT_Utility.sendChatToPlayer(aPlayer, d.key[i] + dir + "" + d.value[i] + info("oob"));

			}

			else {

				GT_Utility.sendChatToPlayer(aPlayer, d.key[i] + dir + d.value[i]);
			}
		}

		return true;*/
	}

	@Override
	public Data createDataObject(int aLegacyData) {

		throw new RuntimeException("not legacy");
	}

	@Override
	public Data createDataObject() {

		return new Data();
	}

	@Override
	protected int getTickRateImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {

		return Math.max(tier[mtier][0], aCoverVariable.dyn ? 5 : 1);
	}

	@Override
	public boolean allowsTickRateAddition() {

		return true;
	}

	static final int[] empty = new int[0];
	public static final int[][] tier = new int[][] { { 1600, 1 }, { 400, 1 }, { 100, 1 }, { 20, 1 }, { 4, 1 }, { 1, 2 },
			{ 1, 4 }, { 1, 8 }, { 1, 16 }, { 1, 32 }, { 1, 64 }, { 1, 64 }, { 1, 64 }, { 1, 64 }, { 1, 64 },

	};

	@Override
	protected Data doCoverThingsImpl(ForgeDirection side, byte aInputRedstone, int aCoverID, Data d,
			ICoverable aTileEntity, long aTimer) {
		aTileEntity.markDirty();
		if (aTileEntity.getWorld().isRemote == false)
			stop: {
				if (d.formula.isEmpty()) {
					d.detail = info("idle");
					d.value = d.key = empty;
					return d;
				}
				boolean diff = (Objects.equals(d.formulaprev, d.formula) == false);

				// d.detail="sd";
				// if(true)break stop;

				if (diff) {
					// System.out.println(d.formulaprev+" "+d.formula);
					d.value = d.key = empty;
					d.formulaprev = d.formula;

					// d.update=false;
					d.e = new SimpleParser.Expression(d.formula);
					d.state = 1;
					break stop;
				}
				int argsize = 0;

				if (d.mode == 0) {
					try {
						argsize = ((IInventory) aTileEntity.getTileEntityAtSide(side)).getSizeInventory();
					} catch (RuntimeException e) {
						d.state = -1;
						d.detail = info("not.inventory");
					}
				} else {
					argsize = aTileEntity.getSizeInventory();

				}

				if (d.state == 1) {
					try {
						if (d.e.parse()) {
							d.detail = info("parsing");// +"\n"+d.e;
							// d.displayexp=d.e;
						} else {

							d.detail = info("validating");// +"\n"+d.e;
							// d.displayexp=d.e;
							d.cslot = 0;
							String res = d.e.checkCompleteOpt();
							if (res == "false") {
								d.state = -1;
								d.detail = info("syntax.error");
								break stop;
							}
							if (res == "changed") {
								break stop;
							}
							d.state = 2;
							d.key = new int[argsize];
							if (d.dyn) {
								for (int i = 0; i < argsize; i++) {
									d.key[i] = i;
								}
								d.state = 0;
								d.detail = info("done");
								break stop;
							}

							d.value = new int[argsize];
							Arrays.fill(d.key, Integer.MIN_VALUE);
							Arrays.fill(d.value, Integer.MIN_VALUE);
							break stop;
						}
					} catch (Exception e) {
						d.state = -1;
						d.detail = info("syntax.error");
						break stop;
					}

				}

				if (d.state == 2) {
					d.detail = info("gmt") + (d.cslot + 1) + "/" + argsize;
					if (d.cslot < argsize) {
						int from = d.cslot;
						int to = Integer.MIN_VALUE;
						try {
							// System.out.println(
							to = (int) Math.round(((Number) d.e
									.evaluate(new Context(publicContext).add(d.mode == 1 ? "i" : "o", d.cslot)))
											.doubleValue());
						} catch (Exception e) {

							// System.out.println(e.getMessage());

						}

						try {
							d.key[d.cslot] = from;
							d.value[d.cslot] = to;
						} catch (ArrayIndexOutOfBoundsException e) {
							d.state = -1;
							d.detail = info("target.changed");
						}

						d.cslot++;
					} else {
						d.state = 0;
						d.detail = info("done");// "all done";
						break stop;
					}

					break stop;
				}

				// move item
				int remain = tier[mtier][1];
				abort: {
					if (d.state != 0) {
						break abort;
					}
					IInventory thiz = aTileEntity;
					IInventory that = aTileEntity.getIInventoryAtSide(side);
					if (that == null)
						break abort;
					if (d.dyn == false) {
						int[] out = d.mode == 0 ? d.key : (d.value);
						int[] in = d.mode == 1 ? d.key : (d.value);

						if (d.io) {// I->O
							for (int i = 0; i < out.length && remain > 0; i++)
								/* if(thiz.getStackInSlot(in[i])!=null) */remain -= 0 < ProghatchesUtil
										.moveFromSlotToSlotSafe(thiz, that, in[i], out[i], null, false, (byte) 64,
												(byte) 1, (byte) 64, (byte) 1) ? 1 : 0;

						} else {
							for (int i = 0; i < out.length && remain > 0; i++)
								/* if(that.getStackInSlot(out[i])!=null) */remain -= 0 < ProghatchesUtil
										.moveFromSlotToSlotSafe(that, thiz, out[i], in[i], null, false, (byte) 64,
												(byte) 1, (byte) 64, (byte) 1) ? 1 : 0;

						}
					} else if (d.dyn == true) {

						IntUnaryOperator mapper = (ii) -> {
							try {
								return (int) Math.round(((Number) d.e.evaluate(addDyn(
										new Context(publicContext).add(d.mode == 1 ? "i" : "o", ii), d, aTileEntity)))
												.doubleValue());
							} catch (Exception e) {
								return Integer.MIN_VALUE;
							}
						};
						IntUnaryOperator out = d.mode == 0 ? s -> d.key[s] : mapper;
						IntUnaryOperator in = d.mode == 1 ? s -> d.key[s] : mapper;

						if (d.io) {// I->O
							for (int i = 0; i < d.key.length && remain > 0; i++)
								/*
								 * if(thiz.getStackInSlot(in.applyAsInt(i))!=
								 * null)
								 */remain -= 0 < ProghatchesUtil.moveFromSlotToSlotSafe(thiz, that, in.applyAsInt(i),
										out.applyAsInt(i), null, false, (byte) 64, (byte) 1, (byte) 64, (byte) 1) ? 1
												: 0;

						} else {
							for (int i = 0; i < d.key.length && remain > 0; i++)
								/*
								 * if(that.getStackInSlot(out.applyAsInt(i))!=
								 * null)
								 */remain -= 0 < ProghatchesUtil.moveFromSlotToSlotSafe(that, thiz, out.applyAsInt(i),
										in.applyAsInt(i), null, false, (byte) 64, (byte) 1, (byte) 64, (byte) 1) ? 1
												: 0;

						}

					}

				} // end moveitem
			}

		return super.doCoverThingsImpl(side, aInputRedstone, aCoverID, d, aTileEntity, aTimer);
	}

	private Context addDyn(Context add, Data d, ICoverable aTileEntity) {
		add.add("enabled", (s) -> {
			if (aTileEntity instanceof IGregTechTileEntity) {
				return new Rational(((IGregTechTileEntity) aTileEntity).isAllowedToWork() ? 1 : 0, 1);
			}
			return new Rational(0, 1);
		});

		add.add("redstone", (sp) -> {
			int freq = ((Number) sp.get(0)).intValue();
			int extra = sp.size() >= 2 ? ((Number) sp.get(1)).intValue() : 0;
			String extra2 = sp.size() >= 3 ? ((String) sp.get(2)) : "AND";

			byte b = ProghatchesUtil.getSignalAt(extra == 1 ? ((IGregTechTileEntity) aTileEntity).getOwnerUuid() : null,
					freq, GateMode.valueOf(extra2));
			return new Rational(b, 1);
		});

		return add;
	}

	private static String info(String s) {
		return LangManager.translateToLocal("programmable_hatches.cover.smart.info." + s);

	}

	static Context publicContext = new Context();
	{/*
		 * publicContext.add("pow", s->{ Number a=(Number) s.get(0); Number
		 * b=(Number) s.get(1); fail:if(a instanceof SimpleParser.Rational&&a
		 * instanceof SimpleParser.Rational){ SimpleParser.Rational
		 * aa=(Rational) a; SimpleParser.Rational bb=(Rational) b;
		 * if(aa.num%aa.den!=0)break fail;if(bb.num%bb.den!=0)break fail; }
		 * return Math.pow(a.doubleValue(), b.doubleValue()); });
		 */
		publicContext.add("max", s -> s.stream().reduce((a, b) -> {
			if (a instanceof SimpleParser.Rational && b instanceof SimpleParser.Rational) {
				SimpleParser.Rational aa = (Rational) a;
				SimpleParser.Rational bb = (Rational) b;
				boolean f1 = aa.num * bb.den > aa.den * bb.num;
				boolean f2 = (bb.den > 0) ^ (aa.den > 0);
				return (f1 ^ f2 ? aa : bb).clone();
			}
			return Math.max(((Number) a).doubleValue(), ((Number) b).doubleValue());
		}).get());
		publicContext.add("min", s -> s.stream().reduce((a, b) -> {
			if (a instanceof SimpleParser.Rational && a instanceof SimpleParser.Rational) {
				SimpleParser.Rational aa = (Rational) a;
				SimpleParser.Rational bb = (Rational) b;
				boolean f1 = aa.num * bb.den < aa.den * bb.num;
				boolean f2 = (bb.den > 0) ^ (aa.den > 0);
				return (f1 ^ f2 ? aa : bb).clone();
			}
			return Math.min(((Number) a).doubleValue(), ((Number) b).doubleValue());
		}).get());

	}

	static Stream<Integer> rangeOf(int i) {

		ArrayList<Integer> d = new ArrayList<>();
		for (int o = 0; o < i; o++) {
			d.add(o);
		}
		return d.stream();

	}

	public String probe(Data d, ICoverable c) {
		if (d.state != 0 || d.formula.isEmpty())
			return "";
		try {
			if (d.dyn) {

				String s = "";
				try {
					s = "" + (int) Math.round(((Number) d.e
							.evaluate(addDyn(new Context(publicContext).add(d.mode == 1 ? "i" : "o", d.probe), d, c)))
									.doubleValue());
				} catch (Exception e) {
					// e.printStackTrace();
					s = "error:" + e.getMessage();
				}
				;

				return s;

			} else {

				String s = "" + d.value[d.probe];
				if (d.value[d.probe] == Integer.MIN_VALUE) {

					s = "error";
				}
				return s;
			}
		} catch (Exception e) {

			/* e.printStackTrace(); */return "error";
		}

	}

}
