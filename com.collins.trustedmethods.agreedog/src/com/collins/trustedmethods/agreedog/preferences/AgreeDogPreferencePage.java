package com.collins.trustedmethods.agreedog.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.collins.trustedmethods.agreedog.Activator;

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
