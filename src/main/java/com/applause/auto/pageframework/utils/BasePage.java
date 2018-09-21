package com.applause.auto.pageframework.utils;

import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.web.AbstractPage;
import com.applause.auto.framework.pageframework.web.AbstractUIData;
import com.applause.auto.pageframework.localization.LocaleMap;

import java.lang.invoke.MethodHandles;

@SuppressWarnings("unused")
public abstract class BasePage extends AbstractPage implements IPageFactoryObject {

	protected static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	/**
	 * Default constructor that allows you to set the page to use its own property context if needed.
	 * 
	 */
	protected BasePage() {
	}

	protected BasePage(boolean isJquery) {
		this.isJquery = isJquery;
	}

	/**
	 * Default constructor that allows you to set the page to use its own property context if needed.
	 * 
	 */
	protected BasePage(final int index) {
		super(index);
	}

	@Override
	public void waitUntilVisibleImpl() {
		// syncHelper.suspend(5000); // wait for page reloaded
		Helper.waitForPage();
		if (this.isJquery) {
			Helper.waitForLoad();
			Helper.ajaxWait();
		}
		waitUntilVisible();
	}

	public String getLocator(AbstractUIData view, String methodName) {
		String locatorStringValue = super.getLocator(view, methodName);

		// If locator have reference to locale key Ex. <<<LOCALE_NAME>>> we should
		// resolve reference by put locale value(from Locales file instead reference
		if (locatorStringValue.matches(".*<<<.*>>>.*")) {
			logger.debug("Found request to localization for selector: " + locatorStringValue);
			String pattern = locatorStringValue.substring(locatorStringValue.indexOf("<<<"),
					locatorStringValue.indexOf(">>>") + 3);
			String localeKey = pattern.substring(3, pattern.indexOf(">>>"));
			logger.debug("Required locale is: " + localeKey);
			String localeValue = LocaleMap.get(localeKey);
			logger.debug("Locale value is: " + localeValue);
			locatorStringValue = locatorStringValue.replace(pattern, localeValue);
			logger.debug("Result selector: " + locatorStringValue);
		}
		return locatorStringValue;
	}

	/**
	 * Must be implemented in page objects. This method should sync with the application under test by
	 * waiting for a locator signature to be found on the newly created page.
	 */
	protected abstract void waitUntilVisible();

	private boolean isJquery = true;
}
