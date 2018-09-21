package com.applause.auto.pageframework.controls;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.util.drivers.BrowserType;
import com.applause.auto.pageframework.utils.Helper;

public class EditField extends com.applause.auto.framework.pageframework.webcontrols.EditField implements CustomAction {
	public EditField(UIData parentPage, String locator) {
		super(parentPage, locator);
	}

	@Override
	public void hover() {
		logger.info("=========>>>>>>>..." + Helper.getBrowserType());
		Helper.waitForElementToAppear(getAbsoluteSelector());
		if (Helper.getBrowserType().equals(BrowserType.FIREFOX.name())) {
			Helper.moveToElement(getDriver(), this.getWebElement());
		} else if (env.getBrowserType().equals(BrowserType.IE)) {
			super.hover();
		} else {
			super.hover();
		}
	}

	@Override
	public String getText() {
		if (Helper.getBrowserType().equals(BrowserType.IE.name())) {
			return super.getText().trim();
		}
		return super.getText();
	}

	@Override
	public Boolean isDisplayed() {
		if (Helper.getBrowserType().equals(BrowserType.IE.name())
				|| Helper.getBrowserType().equals(BrowserType.SAFARI.name())) {
			return Helper.isDisplayed(this);
		}
		return super.isDisplayed();
	}

	@Override
	public void click() {
		Helper.click(this);
	}

	@Override
	public void clickCore() {
		super.click();
	}

}
