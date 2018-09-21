package com.applause.auto.pageframework.utils;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.util.TestHelper;
import com.applause.auto.framework.pageframework.util.actions.NativeBrowserAction;
import com.applause.auto.framework.pageframework.util.api.RestfulApiUtil;
import com.applause.auto.framework.pageframework.util.drivers.BrowserType;
import com.applause.auto.framework.pageframework.util.drivers.DriverWrapperManager;
import com.applause.auto.framework.pageframework.util.environment.EnvironmentUtil;
import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.util.logger.testrail.TestRailStatus;
import com.applause.auto.framework.pageframework.util.queryhelpers.WebElementQueryHelper;
import com.applause.auto.framework.pageframework.util.synchronization.WebSyncHelper;
import com.applause.auto.framework.pageframework.web.AbstractPageChunk;
import com.applause.auto.framework.pageframework.webcontrols.BaseHtmlElement;
import com.applause.auto.pageframework.controls.CustomAction;
import com.applause.auto.pageframework.pages.RootBasePage;
import com.applause.auto.pageframework.testdata.TestConstants;
import com.applause.auto.pageframework.testrail.TestRailLogger;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The type Helper.
 */
@SuppressWarnings("UtilityClass")
public final class Helper {

	/**
	 * The constant logger.
	 */
	protected static final LogController logger = new LogController(Helper.class);

	private Helper() {
	}

	/**
	 * Assert that elements are not shown
	 *
	 * @param element
	 *            the element
	 * @param assertMessage
	 *            the assert message
	 */
	public static void assertElementNotDisplayed(BaseChunk element, String assertMessage) {
		WebSyncHelper syncHelper = new WebSyncHelper(getDriver());
		try {
			syncHelper.waitForElementToAppear(element.getAbsoluteSelector());
			logger.debug("Element: " + element.getAbsoluteSelector() + " visible");
			Assert.fail(assertMessage);
		} catch (Throwable ex) {
			logger.debug("Element: " + element.getAbsoluteSelector() + " invisible");
		}
	}

	/**
	 * Click an element
	 *
	 * @param webElement
	 *            - The element to click
	 */
	public static void jsClick(final WebElement webElement) {
		final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		executor.executeScript("arguments[0].click();", webElement);
	}

	/**
	 * Maximizes the browser according to screen size
	 *
	 * @param driver
	 *            the WebDriver
	 */
	public static void maximizeBrowser(final WebDriver driver) {
		// Nothing to do for mobile.
		EnvironmentUtil env = EnvironmentUtil.getInstance();
		if (!env.getIsMobileWebTest()) {
			driver.manage().window().maximize();
			if (getBrowserType().equals("IE")) {
				// Edge cause failures if commands sent before maximize rendering completed
				ajaxWait(4000);
			}
		}
	}

	/**
	 * Scroll into view.
	 *
	 * @param element
	 *            the element
	 * @param shiftToffset
	 *            the shift toffset
	 */
	public static void scrollIntoView(final BaseHtmlElement element, final int shiftToffset) {
		((JavascriptExecutor) getDriver())
				.executeScript("$(document).ready(function(){" + "    $(window).scrollTop(0);" + "});");
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element.getWebElement());
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		jsShiftWindow(shiftToffset);
	}

	/**
	 * Scroll into view.
	 *
	 * @param element
	 *            the element
	 * @param shiftToffset
	 *            the shift toffset
	 */
	public static void scrollIntoView(final WebElement element, final int shiftToffset) {
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		jsShiftWindow(shiftToffset);
	}

	/**
	 * Scrolls to an element located by xpath
	 *
	 * @param locator
	 *            the locator of the element to scroll into view
	 * @param type
	 *            the kind of locator described by the locator param
	 */
	public static void scrollIntoView(final String locator, final LOCATOR_TYPE type) {
		WebElement element = null;
		final WebDriver driver = getDriver();

		switch (type) {
		case ID:
			element = driver.findElement(By.id(locator));
			break;
		case CSS:
			element = driver.findElement(By.cssSelector(locator));
			break;
		case XPATH:
			element = driver.findElement(By.xpath(locator));
			break;
		default:
			break;
		}

		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
		try {
			Thread.sleep(500L);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static WebDriver getDriver() {
		return (WebDriver) DriverWrapperManager.getInstance().getPrimaryDriverWrapper().getDriver();
	}

	private static void jsShiftWindow(final int yOffset) {
		final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		executor.executeScript(String.format("window.scrollBy(0, %s)", yOffset), "");
	}

	/**
	 * Safe focus.
	 *
	 * @param element
	 *            the element
	 */
	public static void safeFocus(BaseHtmlElement element) {
		Helper.waitForElementToAppear(element.getAbsoluteSelector());
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].focus();", element.getWebElement());
		ajaxWait();
	}

	/**
	 * Safe focus on webElement
	 *
	 * @param element
	 *            the element
	 */
	public static void safeFocus(WebElement element) {
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].focus();", element);
		ajaxWait();
	}

	/**
	 * Ajax wait.
	 */
	public static void ajaxWait() {
		ajaxWait(2000);
	}

	/**
	 * Ajax wait.
	 *
	 * @param timeout
	 *            the timeout
	 */
	public static void ajaxWait(int timeout) {
		long startTime = TestHelper.getCurrentGMT6Time();
		logger.debug("==> Staring ajax wait");
		logger.debug("==> Waiting for ajax completed with initial delay: " + timeout);
		Helper.waitForElementToDisappear(".ajax_overlay", timeout, 10 * 1000);
		logger.debug("==> Exiting ajax wait. Completed in: " + (TestHelper.getCurrentGMT6Time() - startTime));

	}

	/**
	 * Wait for element to disappear.
	 *
	 * @param selector
	 *            the selector
	 */
	public static void waitForElementToDisappear(String selector) {
		waitForElementToDisappear(selector, 500, 10 * 1000);
	}

	/**
	 * Wait up to 20 minutes until for BS remote page become accessible with selenium
	 */
	public static void waitForPage() {
		try (AutoTimer ignored = AutoTimer.start("Helper.waitForPage")) {
			logger.debug("=> Wait for page accessibility");
			long startTime = TestHelper.getCurrentGMT6Time();
			WebSyncHelper syncHelper = new WebSyncHelper(getDriver());
			long waitForPageDeadline = TestHelper.getCurrentGMT6Time()
					+ AbstractPageChunk.getDefaultTimeOutSeconds() * 1000 * 20;
			while ((TestHelper.getCurrentGMT6Time() < waitForPageDeadline) && !isPageAlive()) {
				syncHelper.suspend(1000);
			}
			if (!isPageAlive()) {
				logger.info("Page load timeout exception. Trying to work with incomplete load....");
			}
			logger.debug("=> Wait for page completed in: " + (TestHelper.getCurrentGMT6Time() - startTime));
		}
	}

	/**
	 * Is page alive boolean.
	 *
	 * @return the boolean
	 */
	private static boolean isPageAlive() {
		try (AutoTimer ignored = AutoTimer.start("Helper.isPageAlive")) {
			if (getDriver().findElements(By.cssSelector("html")).size() != 0) {
				logger.debug("Remote browser response OK");
				return true;
			}
		} catch (Throwable throwable) {
			logger.info(throwable.getMessage());
			throw new RuntimeException("Webdriver died!!! " + throwable.getMessage());
		}
		logger.debug("Remote browser response BAD");
		return false;
	}

	/**
	 * Waits for the number of window handles to change to the desired number
	 *
	 * @param numDesiredWindows
	 *            the num desired windows
	 */
	public static void waitForWindowHandleChanged(int numDesiredWindows) {
		WebSyncHelper syncHelper = new WebSyncHelper(getDriver());
		for (int i = 0; i < 5; i++) {
			Set<String> handles = getDriver().getWindowHandles();
			if (handles.size() == numDesiredWindows) {
				return;
			} else {
				syncHelper.suspend(100);
			}
		}
	}

	/**
	 * Wait for page load completed.
	 */
	public static void waitForLoad() {
		logger.debug("======> ");
		logger.debug("======> Wait for page load");
		logger.debug("======> ");
		long startTime = TestHelper.getCurrentGMT6Time();

		logger.debug("Waiting for document.ready state completed");
		try {
			waitForDocument(10);
			waitForJquery(10);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
			logger.warn("Page not loaded properly. Continue....");
		}
		logger.debug("======> ");
		logger.debug("======> Wait for page load comleted in: " + (TestHelper.getCurrentGMT6Time() - startTime));
		logger.debug("======> ");

	}

	/**
	 * Wait for element to appear boolean.
	 *
	 * @param selector
	 *            the selector
	 * @return the boolean
	 */
	public static boolean waitForElementToAppear(String selector) {
		return waitForElementToAppear(selector, 10 * 1000);
	}

	/**
	 * Wait for element to appear boolean.
	 *
	 * @param selector
	 *            the selector
	 * @param timeToWaitForAppearanceInMilliseconds
	 *            the time to wait for element appearance
	 * @return the boolean
	 */
	public static boolean waitForElementToAppear(String selector, long timeToWaitForAppearanceInMilliseconds) {
		try (AutoTimer ignored = AutoTimer.start("Helper.waitForElementToAppear:" + selector)) {
			if (getBrowserType().equals(BrowserType.SAFARI.toString())) {
				// This value must be larger than the implicit wait value above or else
				// the subsequent findElement call hangs.
				new WebSyncHelper(getDriver()).suspend(500);
			}
			getDriver().manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
			if (getBrowserType().equals(BrowserType.SAFARI.toString())) {
				// This value must be larger than the implicit wait value above or else
				// the subsequent findElement call hangs.
				new WebSyncHelper(getDriver()).suspend(2000);
			}
			WebElementQueryHelper webElementQueryHelper = new WebElementQueryHelper(getDriver());
			try (AutoTimer ignored2 = AutoTimer.start("Helper.waitForElementToAppear.quickOut:" + selector)) {
				WebElement element = webElementQueryHelper.findElementByExtendedCss(selector);
				// Do a quick out in case it is visible.
				if (element != null && element.isDisplayed()) {
					logger.debug("waitForElementToAppear->quick out for: " + selector);
					return true;
				}
			} catch (Throwable i) {
				logger.debug("waitForElementToAppear->NO quick out for: " + selector);
			}

			logger.debug("======> Wait for Element to appear");
			long startTime = System.currentTimeMillis();
			// wait for page
			long timeToAppear = startTime + timeToWaitForAppearanceInMilliseconds;
			boolean elementIsVisible = false;
			final boolean isIEorSafari = (getBrowserType().equals(BrowserType.IE.toString())
					|| getBrowserType().equals(BrowserType.SAFARI.toString()));
			while ((System.currentTimeMillis() < timeToAppear) && !elementIsVisible) {
				logger.debug("======> Current time: " + System.currentTimeMillis());
				logger.debug("======> Exit time: " + timeToAppear);
				try {
					List<WebElement> elements;
					try (AutoTimer i = AutoTimer
							.start("Helper.waitforElementToAppear.findElementsByExtendedCss:" + selector)) {
						elements = webElementQueryHelper.findElementsByExtendedCss(selector);
					}
					logger.debug(
							"======> wait for element to appear found " + selector + " " + elements.size() + " items");
					for (WebElement elem : elements) {
						if (elem != null) {
							if (isIEorSafari) {
								String builder = "return !!( arguments[0].offsetWidth || arguments[0].offsetHeight || arguments[0].getClientRects().length );";
								if ((boolean) ((JavascriptExecutor) getDriver()).executeScript(builder,
										new Object[] { elem })) {
									logger.debug("======> Element " + selector + " found and it is visible");
									elementIsVisible = true;
									break;
								}
							} else {
								try (AutoTimer i = AutoTimer.start("waitforElementToAppear.isDisplayed")) {
									if (elem.isDisplayed()) {
										logger.debug("======> Element " + selector + " found and it is visible");
										elementIsVisible = true;
										break;
									}
								}
							}
							logger.info("======> Waiting for " + selector + " to vanish");
						}
					}
				} catch (ElementNotVisibleException var5) {
					logger.debug("======> ElementNotVisibleException thrown waiting for: " + selector);
				} catch (WebDriverException var5) {
					logger.info("======> WebDriverException thrown waiting for: " + selector);
					logger.info(var5.getMessage());
				} catch (AssertionError var5) {
					logger.info("======> AssertionError thrown waiting for: " + var5.getMessage());
				} catch (Throwable th) {
					logger.info("======> Other exception thrown waiting for: " + th.getMessage());
				}
			}
			// // Check if queried page remains
			// if (!isPageAlive()) {
			// logger.debug("======> Page seems not ready. Retrying.");
			// waitForPage(); // wait for page became responsive
			// // retry after page became responsive
			// stopIterateFlag = waitForElementToAppear(selector, timeToWaitForElementAppearence);
			// }
			logger.debug("======> Exiting Wait for Element to appear. Completed in: "
					+ (System.currentTimeMillis() - startTime));

			logger.info(String.format("waitForElementToAppear returned %s for [%s]",
					elementIsVisible ? "true" : "false", selector));
			return elementIsVisible;
		} finally {
			getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			if (getBrowserType().equals(BrowserType.SAFARI.toString())) {
				// This value must be larger than the implicit wait value above or else
				// the subsequent findElement call hangs.
				new WebSyncHelper(getDriver()).suspend(2000);
			}
		}
	}

	/**
	 * Wait for element to disappear.
	 *
	 * @param selector
	 *            the selector the selector
	 * @param timeToWaitForAppearanceInMilliseconds
	 *            the time to wait for element appearance
	 * @param timeToWaitForDisappearanceInMilliseconds
	 *            the time to wait for disappearance in milliseconds
	 */
	public static void waitForElementToDisappear(String selector, long timeToWaitForAppearanceInMilliseconds,
			long timeToWaitForDisappearanceInMilliseconds) {
		try (AutoTimer ignored = AutoTimer.start("Helper.waitForElementToDisappear:" + selector)) {
			logger.debug("====> Wait for Element to disappear");
			boolean elementIsVisible;
			long startTime = System.currentTimeMillis();
			if (waitForElementToAppear(selector, timeToWaitForAppearanceInMilliseconds)) {
				logger.debug("====> Element was found during appearance time: " + selector);
				elementIsVisible = true;
				long end = startTime + timeToWaitForDisappearanceInMilliseconds;
				logger.info("----------->start time: " + startTime);
				logger.info("----------->end time: " + end);
				while ((System.currentTimeMillis() < end) && elementIsVisible) {
					elementIsVisible = waitForElementToAppear(selector, 100);
				}
			} else {
				logger.debug("====> Element was not found during appearance time");
				elementIsVisible = false;
			}
			if (elementIsVisible) {
				throw new RuntimeException("Element [" + selector + "] has not disappeared after "
						+ timeToWaitForDisappearanceInMilliseconds + " ms.");
			} else {
				logger.debug("====> Element not found on the screen now: " + selector);
			}
			logger.debug("====> Exiting Wait for Element to disappear. Completed in: "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * Set attribute value.
	 *
	 * @param elem
	 *            the elem
	 * @param attributeName
	 *            the attribute name
	 * @param value
	 *            the value
	 */
	public static void setAttributeValue(BaseHtmlElement elem, String attributeName, String value) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		String scriptSetAttrValue = "arguments[0].setAttribute(arguments[1],arguments[2])";
		js.executeScript(scriptSetAttrValue, elem.getWebElement(), attributeName, value);
	}

	/**
	 * Bs hartbeat.
	 *
	 * @param timeout
	 *            the timeout
	 */
	static void bsHartbeat(long timeout) {
		try (AutoTimer ignored = AutoTimer.start("Helper.bsHartbeat")) {
			logger.info("Suspending with heartbeat for " + timeout / 1000 + " seconds");
			long end = System.currentTimeMillis() + timeout;
			long remaining = timeout;
			try {
				// Ensure the heartbeat is sent at least once, for the 0 ms case.
				getDriver().findElement(By.cssSelector("body"));

				WebSyncHelper syncHelper = new WebSyncHelper(getDriver());
				while (remaining > 0) {
					logger.debug("Browser stack heart beat...");
					// ping BS every 15 seconds or less to keep the session alive.
					getDriver().findElement(By.cssSelector("body"));
					syncHelper.suspend((int) Math.min(remaining, 15000));
					remaining = end - System.currentTimeMillis();
				}
			} catch (Throwable e) {
				logger.warn("Exception thrown during suspendWithHeartbeat: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the browser type string, such as "CHROME" or "FIREFOX".
	 *
	 * @return A BrowserType object.
	 */
	public static String getBrowserType() {
		BrowserType type = EnvironmentUtil.getInstance().getBrowserType();
		return type != null ? type.toString() : "UNKNOWN";
	}

	/**
	 * Enum structure to manage locator types
	 */
	public enum LOCATOR_TYPE {
		/**
		 * Id locator type.
		 */
		ID,
		/**
		 * Css locator type.
		 */
		CSS,
		/**
		 * Xpath locator type.
		 */
		XPATH
	}

	/**
	 * Move to element.
	 *
	 * @param driver
	 *            the driver
	 * @param element
	 *            the element
	 */
	public static void moveToElement(WebDriver driver, WebElement element) {
		logger.debug("Using JS for firefox Hovering");
		NativeBrowserAction nativeBrowserAction = new NativeBrowserAction(driver);
		nativeBrowserAction.hoverOverElement(element);
	}

	/**
	 * Move to element native.
	 *
	 * @param driver
	 *            the driver
	 * @param element
	 *            the element
	 */
	public static void moveToElementNative(WebDriver driver, WebElement element) {
		logger.debug("Using Action buildrt");
		Actions builder = new Actions(driver);
		builder.moveToElement(element).perform();
	}

	/**
	 * Click.
	 *
	 * @param element
	 *            the element
	 */
	public static void click(BaseHtmlElement element) {
		if (getBrowserType().equals(BrowserType.IE.toString())
				|| getBrowserType().equals(BrowserType.SAFARI.toString())) {
			if (Helper.waitForElementToAppear(element.getAbsoluteSelector())) {
				logger.debug("Clicking element with selector: " + element.getAbsoluteSelector());
				try {
					element.getWebElement().click();
				} catch (ElementNotVisibleException ex) {
					logger.warn("IE driver seems have issues with this type of elements, trying with jsClick");
					jsClick(element.getWebElement());
				} catch (WebDriverException ex) {
					logger.info("Exception in Helper.click: " + ex.getMessage());
				}
			} else {
				throw new ElementNotVisibleException("Element not visible");
			}
		} else {
			logger.debug("Clicking by core");
			((CustomAction) element).clickCore();
		}
	}

	/**
	 * Is displayed boolean.
	 *
	 * @param element
	 *            the element
	 * @return the boolean
	 */
	public static boolean isDisplayed(BaseHtmlElement element) {
		try (AutoTimer ignored = AutoTimer.start("Helper.isDisplayed")) {
			boolean result = false;
			WebElementQueryHelper webElementQueryHelper = new WebElementQueryHelper(getDriver());
			List<WebElement> elements = webElementQueryHelper.findElementsByExtendedCss(element.getAbsoluteSelector());
			logger.debug("======> wait for element to appear found " + element.getAbsoluteSelector() + " "
					+ elements.size() + " items");
			for (WebElement elem : elements) {
				if (elem != null) {
					if (!(getBrowserType().equals(BrowserType.IE.toString())
							|| getBrowserType().equals(BrowserType.SAFARI.toString()))) {
						if (elem.isDisplayed()) {
							logger.debug(
									"======> Element " + element.getAbsoluteSelector() + " found and it is visible");
							result = true;
							break;
						}
					} else {
						String builder = "return !!( arguments[0].offsetWidth || arguments[0].offsetHeight || arguments[0].getClientRects().length );";
						if ((boolean) ((JavascriptExecutor) getDriver()).executeScript(builder,
								new Object[] { elem })) {
							logger.debug(
									"======> Element " + element.getAbsoluteSelector() + " found and it is visible");
							result = true;
							break;
						}
					}
					logger.info("======> Waiting for " + element.getAbsoluteSelector() + " to vanish");
				}

			}
			return result;
		}
	}

	/**
	 * Wait for jquery.
	 *
	 * @param seconds
	 *            the seconds
	 */
	private static void waitForJquery(int seconds) {
		try (AutoTimer ignored = AutoTimer.start("Helper.waitForJquery")) {
			logger.info("jQuery synchronization wait for " + seconds + " seconds");
			long start = System.currentTimeMillis();
			long end = start + seconds * 1000;
			boolean exitCond = false;
			while ((System.currentTimeMillis() < end) && !exitCond) {
				try {
					exitCond = (boolean) ((JavascriptExecutor) getDriver())
							.executeScript("return !!window.$ && window.$.active == 0", new Object[0]);
					logger.info("waitForJquery script returned " + exitCond);
				} catch (WebDriverException we) {
					logger.info("jQuery throw exception. Status undefined: " + we.getMessage());
				}
			}
			if (!exitCond) {
				logger.info("waitForDocument timed out.");
			}
		}
	}

	/**
	 * Wait for document.
	 *
	 * @param seconds
	 *            the seconds
	 */
	private static void waitForDocument(int seconds) {
		try (AutoTimer ignored = AutoTimer.start("Helper.waitForDocument")) {
			logger.info("Page load synchronization wait for " + seconds + " seconds");
			long start = System.currentTimeMillis();
			long end = start + seconds * 1000;
			boolean exitCond = false;
			while ((System.currentTimeMillis() < end) && !exitCond) {
				try {
					exitCond = ((JavascriptExecutor) getDriver()).executeScript("return document.readyState")
							.equals("complete");
				} catch (WebDriverException we) {
					logger.info("Page load throw exception. Status undefined" + we.getMessage());
				}
			}
			if (!exitCond) {
				logger.info("waitForDocument timed out.");
			}
		}
	}

	/**
	 * Gets chunks list.
	 *
	 * @param <T>
	 *            the type parameter
	 * @param clazz
	 *            the clazz
	 * @param page
	 *            the page
	 * @param totalPattern
	 *            the total pattern
	 * @param itemPattern
	 *            the item pattern
	 * @return the chunks list
	 */
	public static <T extends BaseChunk> List<T> getChunksList(Class<T> clazz, BasePage page, String totalPattern,
			String itemPattern) {
		List<T> result = new ArrayList<>();
		int count = new WebElementQueryHelper(getDriver()).findElementsByExtendedCss(totalPattern).size();
		for (int i = 1; i <= count; i++) {
			result.add(BDChunkFactory.create(clazz, page, String.format(itemPattern, i)));
		}
		return result;
	}

	/**
	 * Gets elements list.
	 *
	 * @param <T>
	 *            the type parameter
	 * @param clazz
	 *            the clazz
	 * @param parent
	 *            the parent
	 * @param totalPattern
	 *            the total pattern
	 * @param itemPattern
	 *            the item pattern
	 * @return the elements list
	 */
	public static <T extends BaseHtmlElement> List<T> getElementsList(Class<T> clazz, UIData parent,
			String totalPattern, String itemPattern, int startIndex, int indexAdjustment) {
		List<T> result = new ArrayList<>();
		String prefix = "";
		try {
			prefix = parent.getAbsoluteSelector();
		} catch (Exception ex) {
			logger.debug("No parent prefix locator found");
		}

		int count = 0;
		try {
			getDriver().manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
			count = new WebElementQueryHelper(getDriver()).findElementsByExtendedCss(prefix + " " + totalPattern)
					.size();

		} catch (NoSuchElementException nse) {
			logger.info("Handling non expected returns when 0");
		}
		logger.info("Get elements list total: " + count);
		for (int i = 0; i < count + indexAdjustment; i++) {
			logger.info("Get elements list processing: " + i);
			try {
				T element = clazz.getConstructor(UIData.class, String.class).newInstance(parent,
						String.format(itemPattern, i + startIndex));
				if (Helper.waitForElementToAppear(element.getAbsoluteSelector(), 100)) {
					result.add(element);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation1");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation2");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation3");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation4");
			}
		}
		getDriver().manage().timeouts().implicitlyWait(10000, TimeUnit.MILLISECONDS);
		return result;
	}

	public static <T extends BaseHtmlElement> List<T> getElementsList(Class<T> clazz, UIData parent,
			String totalPattern, String itemPattern) {
		return getElementsList(clazz, parent, totalPattern, itemPattern, 1, 0);
	}

	public static <T extends BaseHtmlElement> List<T> getElementsList(Class<T> clazz, UIData parent,
			String totalPattern, String itemPattern, int index) {
		return getElementsList(clazz, parent, totalPattern, itemPattern, index, 0);
	}

	/**
	 * Gets elements x list.
	 *
	 * @param <T>
	 *            the type parameter
	 * @param clazz
	 *            the clazz
	 * @param parent
	 *            the parent
	 * @param totalPattern
	 *            the total pattern
	 * @param itemPattern
	 *            the item pattern
	 * @return the elements x list
	 */
	public static <T extends BaseHtmlElement> List<T> getElementsXList(Class<T> clazz, UIData parent,
			String totalPattern, String itemPattern) {
		List<T> result = new ArrayList<>();
		int count = new WebElementQueryHelper(getDriver()).getElementCountByXpath(totalPattern);
		for (int i = 1; i <= count; i++) {
			try {
				result.add(clazz.getConstructor(UIData.class, String.class).newInstance(parent,
						String.format(itemPattern, i)));
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation1");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation2");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation3");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new RuntimeException("Error elements list creation4");
			}
		}
		return result;
	}

	public static void logRailResult(TestConstants.GLOBAL_RUN_TESTCASES caseid, TestRailStatus result,
			String message, String message2) {
		// if (true)
		// return;
		TestRailLogger railLogger = new TestRailLogger();
		railLogger.logResult(
				caseid.getValue(), result.getValue(), message, BDPageFactory.create(RootBasePage.class)
						.getSnapshotManager().takeScreenshot(UUID.randomUUID().toString()),
				message2 + getDriver().getCurrentUrl());

	}

	public static void sendLog(String subject, String body) {
		String baseUrl = "http://198.167.140.160:8081/api/v1/addlog";
		RestfulApiUtil client = new RestfulApiUtil(baseUrl);
		String payload = Base64.encodeBase64String(body.getBytes());
		try {
			HttpResponse response = client.doPost(URLEncoder.encode(subject), null,
					new StringEntity(String.format("{\"data\" : \"%s\"}", payload)), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendLogBulk(String subject, String body) {
		String baseUrl = "http://198.167.140.160:8081/api/v1/addlogbulk";
		RestfulApiUtil client = new RestfulApiUtil(baseUrl);
		String payload = Base64.encodeBase64String(body.getBytes());
		try {
			HttpResponse response = client.doPost(URLEncoder.encode(subject), null,
					new StringEntity(String.format("{\"data\" : \"%s\"}", payload)), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
