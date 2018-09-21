package com.applause.auto.pageframework.pages;

import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.web.WebElementLocator;
import com.applause.auto.framework.pageframework.web.factory.WebDesktopImplementation;
import com.applause.auto.framework.pageframework.web.factory.WebPhoneImplementation;
import com.applause.auto.framework.pageframework.web.factory.WebTabletImplementation;
import com.applause.auto.pageframework.chunks.*;
import com.applause.auto.pageframework.controls.*;
import com.applause.auto.pageframework.utils.BasePage;
import com.applause.auto.pageframework.utils.Helper;
import com.applause.auto.pageframework.utils.BDChunkFactory;
import org.openqa.selenium.NoSuchElementException;

import java.lang.invoke.MethodHandles;

/**
 * Class represents the log-in page for Best Day
 */
@SuppressWarnings("unused")
@WebDesktopImplementation(DesktopLogInPage.class)
@WebTabletImplementation(TabletLogInPage.class)
@WebPhoneImplementation(PhoneLogInPage.class)
public abstract class LogInPage extends BasePage {

	/**
	 * The Logger.
	 */
	static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	/*
	 * Class methods
	 */
	protected SignInChunk signInChunk = null;

	/**
	 * Reset flags for new session.
	 */
	public static void resetFlagsForNewSession() {
		logger.debug("Resetting Ad banner flag for new session");
		// Future flags to be set
	}

	/*
	 * Public chunk getters
	 */

	/**
	 * Gets a generic text from a class contains
	 *
	 * @param classContainsToFind
	 *            of the classname used.
	 * @return element with the contain text of the parameter.
	 */
	@WebElementLocator(webDesktop = "//*[contains(@class,'%s')]", webPhone = "//*[contains(@class,'%s')]")
	public Text getGenericClassContains(final String classContainsToFind) {
		return new Text(this, String.format(getLocator(this, "getGenericClassContains"), classContainsToFind));
	}

	/**
	 * Gets a generic text from a class equal
	 *
	 * @param classEqualToFind
	 *            equal in the class to find.
	 * @return element with the equal text of the parameter.
	 */
	@WebElementLocator(webDesktop = "//*[@class='%s']", webPhone = "//*[@class='%s']")
	public Text getGenericClassEqual(final String classEqualToFind) {
		return new Text(this, String.format(getLocator(this, "getGenericClassEqual"), classEqualToFind));
	}

	/*
	 * Protected helpers
	 */

	public SignInChunk setupSignInChunk() {
		return signInChunk = this.getSignInChunk();
	}

	/**
	 * Signs into the account with the given username and password
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @return landingPage the landing page after logged in.
	 */
	public void fillBasicInfo(String username, String password) {
		logger.info("Logging in to account with username: " + username);
		logger.info("Logging in to account with password: " + password);
		signInChunk.setUsername(username);
		signInChunk.setPassword(password);
	}

	/**
	 * Instance of landing page
	 *
	 * @return LandingPage
	 */
	public LandingPage submitLoginInfo() {
		try {
			return signInChunk.submitLogin();
		} catch (Throwable throwable) {
			logger.warn("Something wrong during login: " + throwable.getMessage());
		}

		return null;
	}

	/*
	 * Private element getters
	 */

	/**
	 * Gets sign in popup chunk.
	 *
	 * @return the sign in popup chunk
	 */
	@WebElementLocator(webDesktop = "div.loginForm", webPhone = "div.loginForm")
	public SignInChunk getSignInChunk() {
		return BDChunkFactory.create(SignInChunk.class, this, getLocator(this, "getSignInChunk"));
	}

	/**
	 * navigation back.
	 */
	public void navigateBack() {
		syncHelper.suspend(10000);
		getDriver().navigate().back();
		syncHelper.suspend(5000);
		Helper.waitForPage();
		Helper.waitForLoad();
		Helper.ajaxWait();
	}
}

/**
 * The type Desktop landing page.
 */
class DesktopLogInPage extends LogInPage {

	/**
	 * Creates a new Best Day log-in page and navigates to the given url
	 */
	DesktopLogInPage() {
		super();
	}

	@Override
	protected void waitUntilVisible() {

	}
}

/**
 * The type Tablet landing page.
 */
class TabletLogInPage extends LogInPage {

	/**
	 * Creates a new Best Day log-in page and navigates to the given url
	 */
	TabletLogInPage() {
		super();
	}

	@Override
	protected void waitUntilVisible() {
		Helper.waitForElementToAppear(getAbsoluteSelector(), 60000);
	}
}

/**
 * The type Phone landing page.
 */
class PhoneLogInPage extends LogInPage {

	/**
	 * Creates a new Best Day log-in page and navigates to the given url
	 */
	PhoneLogInPage() {
		super();
	}

	@Override
	protected void waitUntilVisible() {
		Helper.waitForElementToAppear(getAbsoluteSelector(), 60000);
	}
}

