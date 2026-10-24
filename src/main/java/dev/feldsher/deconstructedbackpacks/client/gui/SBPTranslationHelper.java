package dev.feldsher.deconstructedbackpacks.client.gui;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;

public class SBPTranslationHelper extends TranslationHelper {
	public static final SBPTranslationHelper INSTANCE = new SBPTranslationHelper();

	private SBPTranslationHelper() {
		super(DeconstructedBackpacks.MOD_ID);
	}
}
