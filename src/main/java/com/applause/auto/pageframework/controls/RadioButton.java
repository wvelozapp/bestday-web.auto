package com.applause.auto.pageframework.controls;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.framework.pageframework.util.drivers.BrowserType;
import com.applause.auto.pageframework.utils.Helper;

public class RadioButton extends com.applause.auto.framework.pageframework.webcontrols.Button implements CustomAction {
	public RadioButton(UIData parentPage, String locator) {
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
		} else {
			super.hover();
		}
	}

	@Override
	public void click() {
		Helper.click(new Button(this.getParent(), selector));
	}

	@Override
	public void clickCore() {
		super.click();
	}
}
