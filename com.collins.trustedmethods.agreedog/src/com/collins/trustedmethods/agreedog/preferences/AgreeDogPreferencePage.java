package com.collins.trustedmethods.agreedog.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.collins.trustedmethods.agreedog.Activator;

public class AgreeDogPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private FileFieldEditor sysReqFileFieldEditor;

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

		sysReqFileFieldEditor = new FileFieldEditor(AgreeDogPreferenceConstants.PREF_SYS_REQ_FILENAME,
				"System Requirements filename:", true, getFieldEditorParent()) {

			@Override
			protected String changePressed() {

				FileDialog dlgSelectFile = new FileDialog(getShell(), SWT.OPEN | SWT.SHEET);
				dlgSelectFile.setText("System requirements file");
				if (!getTextControl().getText().isEmpty()) {
					dlgSelectFile.setFileName(getTextControl().getText());
				}
				dlgSelectFile.setFilterExtensions(new String[] { "*.txt", "*.*" });
				String fileName = dlgSelectFile.open();
				if (fileName == null) {
					return null;
				} else {
					fileName = fileName.trim();
				}

				return fileName;
			}

		};
		addField(sysReqFileFieldEditor);
		addField(new StringFieldEditor(AgreeDogPreferenceConstants.PREF_OPEN_AI_KEY, "OpenAI Key",
				getFieldEditorParent()));
		addField(
				new StringFieldEditor(AgreeDogPreferenceConstants.PREF_PORT, "Localhost Port", getFieldEditorParent()));

	}

}
