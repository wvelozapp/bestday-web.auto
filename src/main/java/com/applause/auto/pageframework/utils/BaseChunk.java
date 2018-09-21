package com.applause.auto.pageframework.utils;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.web.AbstractPageChunk;
import com.applause.auto.framework.pageframework.web.AbstractUIData;
import com.applause.auto.pageframework.localization.LocaleMap;

@SuppressWarnings("unused")
public abstract class BaseChunk extends AbstractPageChunk implements IPageFactoryObject {

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent element
	 * @param selector
	 *            the selector of the chunk
	 */
	protected BaseChunk(final UIData parent, final String selector) {
		super(parent, selector);
		logger.debug("----> Absolute parent selector " + parent.getSelector());
		logger.debug("----> Absolute this selector " + selector);
	}

	/**
	 * Constructor with index.
	 * 
	 * @param parent
	 *            the parent element
	 * @param selector
	 *            the selector of the chunk
	 * @param index
	 */
	protected BaseChunk(final UIData parent, final String selector, final int index) {
		super(parent, selector, index);
	}

	@Override
	public void waitUntilVisibleImpl() {
		logger.debug("Chunk wait until becomes visible");
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
}
