package com.applause.auto.pageframework.controls;

import com.applause.auto.framework.pageframework.UIData;
import com.applause.auto.pageframework.utils.Helper;

public class Text extends com.applause.auto.framework.pageframework.webcontrols.Text implements CustomAction {
	public Text(UIData parentPage, String locator) {
		super(parentPage, locator);
	}

	@Override
	public void hover() {
		if (Helper.getBrowserType().equals("FIREFOX")) {
			Helper.moveToElement(getDriver(), this.getWebElement());
		} else if (Helper.getBrowserType().equals("IE")) {
			Helper.waitForElementToAppear(this.getAbsoluteSelector());
			super.hover();
		} else {
			super.hover();
		}
	}

	@Override
	public Boolean isDisplayed() {
		if (Helper.getBrowserType().equals("IE")) {
			return Helper.isDisplayed(this);
		} else if (Helper.getBrowserType().equals("SAFARI")) {
			return Helper.isDisplayed(this);
		}
		return super.isDisplayed();
	}

	@Override
	public String getText() {
		return super.getText().replaceAll("(^\\h*)|(\\h*$)", " ").replace("\u00a0", " ").trim();
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
