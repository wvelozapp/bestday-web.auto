package com.applause.auto.pageframework.utils;

interface IPageFactoryObject {

	/**
	 * Must be implemented in page objects. This method should sync with the application under test by
	 * waiting for a locator signature to be found on the newly created page.
	 */
	void waitUntilVisibleImpl();
}
