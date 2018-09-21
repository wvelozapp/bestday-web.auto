package com.applause.auto.test.testdata;

import com.applause.auto.framework.pageframework.util.StringUtils;
import com.applause.auto.framework.pageframework.util.environment.EnvironmentUtil;
import com.applause.auto.pageframework.localization.LocaleKeys;
import com.applause.auto.pageframework.localization.LocaleMap;
import com.applause.auto.pageframework.utils.VirtualResourceLocker;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Test Data
 */
@SuppressWarnings({ "UtilityClass", "unused" })
public final class TestData {

	protected static final String USERNAME = "nombreUsuario";
	protected static final String PASSWORD = "contrasenna";
	protected static final String DOMAIN = "Dominio (BDT)";
	protected static final String SYSTEM_LANGUAGE = "Español MX";
	protected static final String DESTINATION = "Cancún";
	protected static final String CANCEL_DATE = "8/2/2018";
	protected static final String BEST_DAY_MATRIX_URL = "https://matrix.bestday.com/1.0.0.4/es-mx";

	private TestData() {
	}

	/*
	 * Return the locale to test against
	 */
	private static Locale getLocale() {
		if (System.getProperty("locale") != null) {
			return Locale.forLanguageTag(System.getProperty("locale"));
		}
		return Locale.US;
	}

	/*
	 * Return a test URL based on the test platform.
	 */
	public static String getHomePageUrl() {
		if (EnvironmentUtil.getInstance().getIsMobileWebTest()) {
			return String.format(LocaleMap.get(LocaleKeys.MOBILE_URL), System.getProperty("env"));
		} else {
			return String.format(LocaleMap.get(LocaleKeys.URL), System.getProperty("env"));
		}
	}

	public static String getAccountUsername() {
		return USERNAME;
	}

	public static String getAccountPassword() {
		return PASSWORD;
	}

	public static String getAccountDomain() {
		return DOMAIN;
	}

	public static String getAccountLanguage() {
		return SYSTEM_LANGUAGE;
	}

	public static String getDestination() {
		return DESTINATION;
	}
	public static String getCancelDate() {
		return CANCEL_DATE;
	}

	public static List<String> getCurrentTestDescription() {
		try {
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			for (StackTraceElement element : trace) {
				String className = element.getClassName();
				String methodName = element.getMethodName();
				Class<?> theClass = Class.forName(className);
				String description = getDescription(methodName, theClass);
				if (description != null) {
					return Arrays.asList(description.split("\\|"));
				}
			}
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public static String getCurrentTestDescriptionAlter() {
		return getCurrentTestDescription() + "a";
	}

	private static String getDescription(String methodName, Class<?> theClass) {
		try {
			Method[] methods = theClass.getDeclaredMethods();
			for (Method theMethod : methods) {
				String candiateName = theMethod.getName();
				if (methodName.equals(candiateName)) {
					if (theMethod.isAnnotationPresent(Test.class)) {
						return theMethod.getAnnotation(Test.class).description();
					}
					if (theMethod.isAnnotationPresent(BeforeMethod.class)) {
						return theMethod.getAnnotation(BeforeMethod.class).description();
					}
				}
			}
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
