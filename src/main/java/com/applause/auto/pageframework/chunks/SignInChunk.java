package com.applause.auto.pageframework.chunks;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.web.WebElementLocator;
import com.applause.auto.framework.pageframework.web.factory.WebDesktopImplementation;
import com.applause.auto.framework.pageframework.web.factory.WebPhoneImplementation;
import com.applause.auto.framework.pageframework.web.factory.WebTabletImplementation;
import com.applause.auto.framework.pageframework.webcontrols.BaseHtmlElement;
import com.applause.auto.framework.pageframework.webcontrols.Dropdown;
import com.applause.auto.framework.pageframework.webcontrols.Text;
import com.applause.auto.pageframework.controls.Button;
import com.applause.auto.pageframework.controls.EditField;
import com.applause.auto.pageframework.pages.LandingPage;
import com.applause.auto.pageframework.utils.BaseChunk;
import com.applause.auto.pageframework.utils.Helper;
import com.applause.auto.pageframework.utils.BDPageFactory;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;

import java.lang.invoke.MethodHandles;

/**
 * Class represents a page with navigation elements
 */
@WebDesktopImplementation(DesktopSignInChunk.class)
@WebTabletImplementation(TabletSignInChunk.class)
@WebPhoneImplementation(PhoneSignInChunk.class)
public abstract class SignInChunk extends BaseChunk {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	private static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	/**
	 * Constructor
	 *
	 * @param parent
	 *            the parent element the parent
	 * @param selector
	 *            the selector of the chunk the selector
	 */
	SignInChunk(final UIData parent, final String selector) {
		super(parent, selector);
	}

	/**
	 * Click sign in my account page.
	 *
	 * @return the my account page
	 */
	//public MyAccountPage clickSubmit() {
	public void clickSubmit() {
		logger.info("Click on Submit button");
		syncHelper.waitForElementToAppear(getSubmit().getAbsoluteSelector());
		Helper.safeFocus(getSubmit());
		Helper.scrollIntoView(getSubmit(), 0);
		getSubmit().click();
		Helper.ajaxWait();
		//return BDPageFactory.create(MyAccountPage.class, true);
	}

	/**
	 * Get username
	 *
	 * @return com.applause.auto.pageframework.controls.EditField username
	 */
	@WebElementLocator(webDesktop = "#tbUser", webPhone = "#tbUser")
	public EditField getUsername() {
		return new EditField(this, getLocator(this, "getUsername"));
	}

	/**
	 * Sets username.
	 *
	 * @param username
	 *            the username
	 */
	public void setUsername(String username) {
		Helper.ajaxWait();
		logger.info("Set username to: " + username);
		getUsername().setText(username);
	}

	/**
	 * Get password
	 *
	 * @return com.applause.auto.pageframework.controls.EditField password
	 */
	@WebElementLocator(webDesktop = "#tbPassword", webPhone = "#tbPassword")
	public EditField getPassword() {
		return new EditField(this, getLocator(this, "getPassword"));
	}

	/**
	 * Sets password.
	 *
	 * @param password
	 *            the password
	 */
	public void setPassword(final String password) {
		logger.info("Set password to: " + password);
		Helper.ajaxWait();
		Helper.safeFocus(getPassword());
		getPassword().setText(password);
		if (env.getIsMobileWebTest()) {
			getPassword().sendKey(Keys.TAB);
		}
		Helper.ajaxWait(); // wait validation process
	}

	/**
	 * Get domain
	 *
	 * @return com.applause.auto.pageframework.controls.Button domain
	 */
	@WebElementLocator(webDesktop = "#ddlDomains_input", webPhone = "#ddlDomains_input")
	public EditField getDomain() {
		return new EditField(this, getLocator(this, "getDomain"));
	}

	/**
	 * Get domain list
	 *
	 * @return com.applause.auto.pageframework.webcontrols.BaseHtmlElement domainList
	 */
	@WebElementLocator(webDesktop = "#ddlDomainsSelectList", webPhone = "#ddlDomainsSelectList")
	public BaseHtmlElement getDomainList() {
		return new BaseHtmlElement(this, getLocator(this, "getDomainList"));
	}

	/**
	 * Get domain BDT
	 *
	 * @return com.applause.auto.pageframework.webcontrols.BaseHtmlElement domain BDT
	 */
	@WebElementLocator(webDesktop = "li[val='BDT']", webPhone = "li[val='BDT']")
	public BaseHtmlElement getDomainBDT() {
		return new BaseHtmlElement(this, getLocator(this, "getDomainBDT"));
	}

	/**
	 * Get domain local
	 *
	 * @return com.applause.auto.pageframework.webcontrols.BaseHtmlElement domain LOCAL
	 */
	@WebElementLocator(webDesktop = "li[val='LOCAL']", webPhone = "li[val='LOCAL']")
	public BaseHtmlElement getDomainLocal() {
		return new BaseHtmlElement(this, getLocator(this, "getDomainLocal"));
	}

	/**
	 * Triggers domain.
	 */
	public void triggerDomain() {
		Helper.ajaxWait();
		Helper.safeFocus(getDomain());
		getDomain().click();
		Helper.ajaxWait();
	}

	/**
	 * Picks domain
	 *
	 * @param domain
	 */
	public void pickDomain(String domain) {
		logger.info("Set domain to: " + domain);

		if(domain.toUpperCase().contains("BDT")) {
			getDomainBDT().click();
		} else if(domain.toUpperCase().contains("LOCAL")) {
			getDomainLocal().click();
		}

		Helper.ajaxWait();
	}

	/**
	 * Get language
	 *
	 * @return com.applause.auto.pageframework.controls.Button language
	 */
	@WebElementLocator(webDesktop = "#ddlSystemLanguage_input", webPhone = "#ddlSystemLanguage_input")
	public Button getLanguage() {
		return new Button(this, getLocator(this, "getLanguage"));
	}

	/**
	 * Get laguage list
	 *
	 * @return com.applause.auto.pageframework.webcontrols.BaseHtmlElement languageList
	 */
	@WebElementLocator(webDesktop = "#ddlSystemLanguageSelectList", webPhone = "#ddlSystemLanguageSelectList")
	public BaseHtmlElement getLanguageList() {
		return new BaseHtmlElement(this, getLocator(this, "getLanguageList"));
	}

	/**
	 * Get language ES-MX
	 *
	 * @return com.applause.auto.pageframework.webcontrols.BaseHtmlElement language
	 */
	@WebElementLocator(webDesktop = "li[val='es-mx']", webPhone = "li[val='es-mx']")
	public BaseHtmlElement getLanguageEspanolMX() {
		return new BaseHtmlElement(this, getLocator(this, "getDomainBDT"));
	}

	/**
	 * Get language EN-US
	 *
	 * @return com.applause.auto.pageframework.webcontrols.BaseHtmlElement language
	 */
	@WebElementLocator(webDesktop = "li[val='en-us']", webPhone = "li[val='en-us']")
	public BaseHtmlElement getLanguageEnglishUS() {
		return new BaseHtmlElement(this, getLocator(this, "getDomainLocal"));
	}

	/**
	 * Get language PT-BR
	 *
	 * @return com.applause.auto.pageframework.webcontrols.BaseHtmlElement language
	 */
	@WebElementLocator(webDesktop = "li[val='pt-br']", webPhone = "li[val='pt-br']")
	public BaseHtmlElement getLanguagePortuguesBR() {
		return new BaseHtmlElement(this, getLocator(this, "getDomainLocal"));
	}

	/**
	 * Triggers language.
	 */
	public void triggerLanguage() {
		Helper.ajaxWait();
		Helper.safeFocus(getLanguage());
		getDomain().click();
		Helper.ajaxWait();
	}

	/**
	 * Picks language
	 *
	 * @param language
	 */
	public void pickLanguage(String language) {
		logger.info("Set language to: " + language);

		if(language.toUpperCase().contains("US")) {
			getLanguageEnglishUS().click();
		} else if(language.toUpperCase().contains("MX")) {
			getLanguageEspanolMX().click();
		} else if(language.toUpperCase().contains("BR")) {
			getLanguagePortuguesBR().click();
		}

		Helper.ajaxWait();
	}

	/**
	 * Get submit
	 *
	 * @return com.applause.auto.pageframework.controls.Button submit
	 */
	@WebElementLocator(webDesktop = "#btnLogin", webPhone = "#btnLogin")
	private Button getSubmit() {
		return new Button(this, getLocator(this, "getSubmit"));
	}

	/**
	 * Instance of landing page
	 *
	 * @return LandingPage
	 */
	public LandingPage submitLogin() {
		logger.info("Redirecting to landing page");
		Helper.ajaxWait();
		getSubmit().click();
		Helper.ajaxWait();
		return BDPageFactory.create(LandingPage.class);
	}

	/**
	 * Validates if field is empty
	 *
	 * @param field
	 * @return boolean
	 */
	public boolean isFieldEmpty(String field) {

		switch(field) {
			case "Username":
				return getUsername().getText().isEmpty();

			case "Password":
				return getPassword().getText().isEmpty();

			case "Domain":
				return getDomain().getText().isEmpty();

			case "Language":
				return getLanguage().getText().isEmpty();

			default:
				break;
		}

		return true;
	}

	/**
	 * Gets field text
	 *
	 * @param field
	 * @return string
	 */
	public String getFieldText(String field) {

		switch(field) {
			case "Username":
				return getUsername().getText();

			case "Password:":
				return getPassword().getText();

			case "Domain:":
				return getDomain().getText();

			case "Language:":
				return getLanguage().getText();

			default:
				break;
		}

		return new String();
	}
}

class DesktopSignInChunk extends SignInChunk {

	/**
	 * Instantiates a new Sign in chunk.
	 *
	 * @param parent
	 *            the parent element the parent
	 * @param selector
	 *            the selector of the chunk the selector
	 */
	DesktopSignInChunk(final UIData parent, final String selector) {
		super(parent, selector);
	}

	@Override
	protected void waitUntilVisible() {
		if (!Helper.waitForElementToAppear(getAbsoluteSelector())) {
			throw new NoSuchElementException("Not found " + getAbsoluteSelector());
		}
	}
}

class TabletSignInChunk extends SignInChunk {

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            the parent element the parent
	 * @param selector
	 *            the selector of the chunk the selector
	 */
	TabletSignInChunk(final UIData parent, final String selector) {
		super(parent, selector);
	}

	@Override
	protected void waitUntilVisible() {
		Helper.waitForElementToAppear(getAbsoluteSelector(), 60000);
	}

}

class PhoneSignInChunk extends SignInChunk {

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            the parent element the parent
	 * @param selector
	 *            the selector of the chunk the selector
	 */
	PhoneSignInChunk(final UIData parent, final String selector) {
		super(parent, selector);
	}

	@Override
	protected void waitUntilVisible() {
		Helper.waitForElementToAppear(getAbsoluteSelector(), 60000);
	}

}
