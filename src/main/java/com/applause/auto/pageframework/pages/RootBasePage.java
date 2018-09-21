package com.applause.auto.pageframework.pages;

import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.web.factory.WebDesktopImplementation;
import com.applause.auto.framework.pageframework.web.factory.WebPhoneImplementation;
import com.applause.auto.pageframework.utils.BasePage;

import java.lang.invoke.MethodHandles;

@WebDesktopImplementation(DesktopRootBasePage.class)
@WebPhoneImplementation(PhoneRootBasePage.class)
public class RootBasePage extends BasePage {

	protected static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	public RootBasePage() {
		super();
	}

	@Override
	protected void waitUntilVisible() {
	}

}

class DesktopRootBasePage extends RootBasePage {

}

class PhoneRootBasePage extends RootBasePage {

}
