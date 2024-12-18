package reobf.proghatches.keybinding;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.net.SwitchModeMessage;

public class KeyBindings {

	@SideOnly(Side.CLIENT)
	public  KeyBinding key;

	interface Run {
		public default void run() {
		}
	}

	 {

		((Run) new Run() {
			@SideOnly(Side.CLIENT)
			@Override
			public void run() {
				key = new KeyBinding("proghatch.keybinding.kit.switch.desc", 0, "itemGroup.proghatches");
				 ClientRegistry.registerKeyBinding(key);
				 FMLCommonHandler.instance().bus().register(KeyBindings.this);
			}
		}).run();

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public  void tick(final TickEvent.ClientTickEvent event) {
		if (key.isPressed()) {

			MyMod.net.sendToServer(new SwitchModeMessage());

		}
		;
	}

}
