package reobf.proghatches.gt.metatileentity.util.mui2;

import static gregtech.api.enums.Mods.GregTech;

import com.cleanroommc.modularui.drawable.UITexture;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;

import gregtech.api.modularui2.GTTextureIds;

public interface IMUITexture {
	 public static final UITexture OVERLAY_BUTTON_PLUS_LARGE = UITexture.builder()
		        .location(GregTech.ID, "gui/overlay_button/plus_large")
		        .imageSize(18, 18)
		        .adaptable(1)
		        .canApplyTheme()
		       
		        .build();
	// public static final AdaptableUITexture BACKGROUND_SINGLEBLOCK_DEFAULT = UITexture.builder()
	//	        .location(GregTech.ID, "gui/background/singleblock_default")
	//	      .

}
