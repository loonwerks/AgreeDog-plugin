package com.rockwellcollins.atc.agree.agreedog.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.rockwellcollins.atc.agree.agreedog.Activator;

public class AgreeDogPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(AgreeDogPreferenceConstants.PREF_OPEN_AI_KEY, "");
	}

}
