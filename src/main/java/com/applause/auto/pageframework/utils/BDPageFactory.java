package com.applause.auto.pageframework.utils;

import com.applause.auto.framework.pageframework.web.AbstractPage;
import com.applause.auto.framework.pageframework.web.PageFactory;

public final class BDPageFactory {
	public static <T extends AbstractPage> T create(final Class<T> clazz, final Object... args) {
		String platformClassName = "UNSET";
		T newPage;

		final String screenSize = System.getProperty("screenSize");

		try (AutoTimer ignored = AutoTimer.start("PageFactory.create")) {
			return PageFactory.create(clazz, args);
		}
	}
}
