package com.applause.auto.pageframework.controls;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.util.drivers.BrowserType;
import com.applause.auto.pageframework.utils.Helper;

public class Button extends com.applause.auto.framework.pageframework.webcontrols.Button implements CustomAction {
	public Button(UIData parentPage, String locator) {
		super(parentPage, locator);
	}

	@Override
	public void hover() {
		if (Helper.getBrowserType().equals(BrowserType.FIREFOX.name())) {
			Helper.waitForElementToAppear(this.getAbsoluteSelector());
			Helper.moveToElement(getDriver(), this.getWebElement());
		} else if (Helper.getBrowserType().equals(BrowserType.IE.name())) {
			Helper.waitForElementToAppear(this.getAbsoluteSelector());
			super.hover();
		} else if (Helper.getBrowserType().equals(BrowserType.SAFARI.name())) {
			Helper.waitForElementToAppear(this.getAbsoluteSelector());
			Helper.moveToElement(getDriver(), this.getWebElement());
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
		if (Helper.getBrowserType().equals("IE") || Helper.getBrowserType().equals("SAFARI")) {
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
