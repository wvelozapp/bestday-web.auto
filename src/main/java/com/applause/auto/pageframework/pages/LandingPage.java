package com.applause.auto.pageframework.pages;

import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.web.WebElementLocator;
import com.applause.auto.framework.pageframework.web.factory.WebDesktopImplementation;
import com.applause.auto.framework.pageframework.web.factory.WebPhoneImplementation;
import com.applause.auto.framework.pageframework.web.factory.WebTabletImplementation;
import com.applause.auto.framework.pageframework.webcontrols.BaseHtmlElement;
import com.applause.auto.pageframework.chunks.SignInChunk;
import com.applause.auto.pageframework.controls.Button;
import com.applause.auto.pageframework.controls.EditField;
import com.applause.auto.pageframework.controls.Text;
import com.applause.auto.pageframework.utils.BDChunkFactory;
import com.applause.auto.pageframework.utils.BasePage;
import com.applause.auto.pageframework.utils.Helper;
import org.openqa.selenium.NoSuchElementException;

import java.lang.invoke.MethodHandles;

/**
 * Class represents the landing page for Best Day
 */
@SuppressWarnings("unused")
@WebDesktopImplementation(DesktopLandingPage.class)
@WebTabletImplementation(TabletLandingPage.class)
@WebPhoneImplementation(PhoneLandingPage.class)
public abstract class LandingPage extends BasePage {

	/**
	 * The Logger.
	 */
	static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	/*
	 * Protected helpers
	 */

	/**
	 * Sets the destination value
	 *
	 * @param destination
	 */
	public void setDestination(String destination) {
		logger.info("Setting destination: " + destination);
		Helper.ajaxWait();
		Helper.safeFocus(getDestination());
		getDestination().setText(destination);
		Helper.ajaxWait();
	}

	/**
	 * Sets the cancellation date
	 *
	 * @param cancellationDate
	 */
	public void setCancellationDate(String cancellationDate) {
		logger.info("Setting cancellation date: " + cancellationDate);
		Helper.ajaxWait();
		Helper.safeFocus(getCancellationDate());
		getDestination().setText(cancellationDate);
		Helper.ajaxWait();
	}

	public void filterSearchTerms() {
		logger.info("Filter results according to search terms");
		Helper.ajaxWait();
		getFilter().click();
		Helper.ajaxWait();
	}

	public boolean isSearchResultContainerVisible() {
		return getSearchResultsContainer().isDisplayed();
	}

	/*
	 * Private element getters
	 */

	/**
	 * Get destination
	 *
	 * @return com.applause.auto.pageframework.controls.EditField destination
	 */
	@WebElementLocator(webDesktop = "#tbDestination", webPhone = "#tbDestination")
	public EditField getDestination() {
		return new EditField(this, getLocator(this, "getDestination"));
	}

	/**
	 * Get cancellation date
	 *
	 * @return com.applause.auto.pageframework.controls.EditField cancellationDate
	 */
	@WebElementLocator(webDesktop = "#", webPhone = "#")
	public EditField getCancellationDate() {
		return new EditField(this, getLocator(this, "getCancellationDate"));
	}

	/**
	 * Get filter
	 *
	 * @return com.applause.auto.pageframework.controls.Button filter
	 */
	@WebElementLocator(webDesktop = "#", webPhone = "#")
	public Button getFilter() {
		return new Button(this, getLocator(this, "getFilter"));
	}

	/**
	 * Get search results container
	 *
	 * @return com.applause.auto.pageframework.controls.Button filter
	 */
	@WebElementLocator(webDesktop = "#", webPhone = "#")
	public BaseHtmlElement getSearchResultsContainer() {
		return new BaseHtmlElement(this, getLocator(this, "getSearchResultsContainer"));
	}

}

/**
 * The type Desktop landing page.
 */
class DesktopLandingPage extends LandingPage {

	/**
	 * Creates a new Best Day landing page and navigates to the given url
	 */
	DesktopLandingPage() {
		super();
	}

	@Override
	protected void waitUntilVisible() {
		if (!Helper.waitForElementToAppear(getAbsoluteSelector())) {
			throw new NoSuchElementException("Not found " + getAbsoluteSelector());
		}
	}
}

/**
 * The type Tablet landing page.
 */
class TabletLandingPage extends LandingPage {

	/**
	 * Creates a new Best Day landing page and navigates to the given url
	 */
	TabletLandingPage() {
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
class PhoneLandingPage extends LandingPage {

	/**
	 * Creates a new Best Day landing page and navigates to the given url
	 */
	PhoneLandingPage() {
		super();
	}

	@Override
	protected void waitUntilVisible() {
		Helper.waitForElementToAppear(getAbsoluteSelector(), 60000);
	}
}

