package com.applause.auto.pageframework.controls;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.util.drivers.BrowserType;
import com.applause.auto.pageframework.utils.Helper;

public class Link extends com.applause.auto.framework.pageframework.webcontrols.Link implements CustomAction {
	public Link(UIData parentPage, String locator) {
		super(parentPage, locator);
	}

	@Override
	public void hover() {
		if (Helper.getBrowserType().equals(BrowserType.FIREFOX.name())) {
			Helper.moveToElement(getDriver(), this.getWebElement());
		} else if (env.getBrowserType().equals(BrowserType.IE)) {
			Helper.waitForElementToAppear(this.getAbsoluteSelector());
			syncHelper.suspend(5000);
			int retry = 0;
			while (retry < 10)
				try {
					super.hover();
					syncHelper.suspend(5000);
					break;
				} catch (Exception ex) {
					retry++;
				}
		} else {
			super.hover();
			syncHelper.suspend(2000);
		}
	}

	@Override
	public Boolean isDisplayed() {
		if (Helper.getBrowserType().equals("IE")) {
			return Helper.isDisplayed(this);
		}
		return super.isDisplayed();
	}

	@Override
	public String getText() {
		if (Helper.getBrowserType().equals("IE")) {
			return super.getText().trim();
		}
		return super.getText();
	}

	@Override
	public void click() {
		Helper.click(this);
	}

	public void clickCore() {
		super.click();
	}
}
