package com.applause.auto.pageframework.testdata;

import com.applause.auto.pageframework.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UtilityClass")
public final class TestConstants {

	public static final String LOCALE_STRINGS_FILE_PATH = "./src/main/resources/environment/"
			+ System.getProperty("env").trim().toLowerCase() + "/localeStrings.csv";

	private TestConstants() {
	}

	/*
	 * Return the locale to test against
	 */
	public static Locale getLocale() {
		String locale = System.getProperty("locale").trim();
		if (locale == null) {
			return Locale.US;
		} else {
			locale = locale.replace('_', '-');
			return Locale.forLanguageTag(locale);
		}
	}

	public enum GLOBAL_RUN_TESTCASES {
		POC_LOGIN(001);

		private final int value;

		GLOBAL_RUN_TESTCASES(final int value) {
			this.value = value;
		}

		public String getValue() {
			return Integer.toString(value);
		}

	}

}
