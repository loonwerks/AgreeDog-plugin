package com.rockwellcollins.atc.agree.agreedog.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.rockwellcollins.atc.agree.agreedog.Activator;

public class AgreeDogPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public AgreeDogPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("AgreeDog Settings");
	}

	@Override
	public void init(IWorkbench workbench) {


	}

	@Override
	protected void createFieldEditors() {

		addField(new StringFieldEditor(AgreeDogPreferenceConstants.PREF_OPEN_AI_KEY, "OpenAI Key",
				getFieldEditorParent()));

	}

}
