package com.applause.auto.pageframework.controls;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.util.drivers.BrowserType;
import com.applause.auto.pageframework.utils.Helper;

public class Image extends com.applause.auto.framework.pageframework.webcontrols.Button implements CustomAction {
	public Image(UIData parentPage, String locator) {
		super(parentPage, locator);
	}

	@Override
	public void hover() {
		if (Helper.getBrowserType().equals(BrowserType.FIREFOX.name())) {
			Helper.moveToElement(getDriver(), this.getWebElement());
		} else if (env.getBrowserType().equals(BrowserType.IE.name())) {
			Helper.waitForElementToAppear(this.getAbsoluteSelector());
			super.hover();
		} else {
			super.hover();
		}
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
