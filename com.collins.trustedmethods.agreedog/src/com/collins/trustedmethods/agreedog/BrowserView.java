package com.collins.trustedmethods.agreedog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.collins.trustedmethods.agreedog.preferences.AgreeDogPreferenceConstants;

public class BrowserView extends ViewPart {

	public final static String BROWSER_VIEW_ID = "com.collins.trustedmethods.agreedog.BrowserView";
	private final static String AGREE_DOG_SERVER_ADDR = "http://127.0.0.1";

	@Override
	public void createPartControl(Composite parent) {
		try {
			final Browser browser = new Browser(parent, SWT.NONE);
			final String port = Activator.getDefault()
					.getPreferenceStore()
					.getString(AgreeDogPreferenceConstants.PREF_PORT);
			browser.setUrl(AGREE_DOG_SERVER_ADDR + ":" + port);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setFocus() {

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		setPartName("AgreeDog");
	}

}
