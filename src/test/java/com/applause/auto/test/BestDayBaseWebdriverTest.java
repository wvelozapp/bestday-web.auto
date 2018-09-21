package com.applause.auto.test;

import com.applause.auto.framework.pageframework.util.drivers.DriverWrapper;
import com.applause.auto.framework.pageframework.util.drivers.DriverWrapperManager;
import com.applause.auto.framework.pageframework.util.environment.EnvironmentUtil;
import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.util.synchronization.WebSyncHelper;
import com.applause.auto.framework.test.listeners.TestListener;
import com.applause.auto.pageframework.pages.LogInPage;
import com.applause.auto.pageframework.utils.AutoTimer;
import com.applause.auto.pageframework.utils.Helper;
import com.applause.auto.pageframework.utils.BDPageFactory;
import com.applause.auto.test.testdata.TestData;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

@Listeners(TestListener.class)
public class BestDayBaseWebdriverTest {

	private static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	@SuppressWarnings("RedundantFieldInitialization")
	public DriverWrapper driverWrapper = null;

	EnvironmentUtil getEnv() {
		return EnvironmentUtil.getInstance();
	}

	@AfterClass
	public void afterClass() {
		DriverWrapperManager.getInstance().deregisterDriver(driverWrapper);
		logger.info("Test case teardown complete.");
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		logger.info("component, total duration (s), # of times");
		AutoTimer.getDurations().forEach((entry) -> logger.info(String.format("\"%s\", %d, %d", entry.getKey(),
				entry.getValue().getDurationInSeconds(), entry.getValue().getInstances())));
		getDriver().quit();
		DriverWrapperManager.getInstance().deregisterDriver(driverWrapper);
		logger.info("Test case teardown complete.");
	}
	//

	@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
	@BeforeClass(alwaysRun = true)
	public void beforeSuite() {
		getEnv().setRawCssOnly(true);
		logger.info("Test case setup complete.");
	}

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod(Method method) {
		System.setProperty("runId", String.format("%s:%s", method.getName(), System.currentTimeMillis()));
	}

	protected WebDriver getDriver() {
		return (WebDriver) getDriverWrapper().getDriver();
	}

	WebSyncHelper getSyncHelper() {
		return (WebSyncHelper) getDriverWrapper().getSyncHelper();
	}

	private DriverWrapper getDriverWrapper() {
		if (driverWrapper == null) {
			driverWrapper = new DriverWrapper(getEnv().getDriver(), getEnv().getDriverProvider());
		}
		return driverWrapper;
	}

	String getCurrentUrl() {
		Helper.waitForPage();
		Helper.waitForLoad();
		Helper.ajaxWait();
		String currentUrl = null;
		while (currentUrl == null) {
			getSyncHelper().suspend(1000);
			currentUrl = getDriver().getCurrentUrl();
		}
		return currentUrl;
	}

	/**
	 * Navigates to the Michael Kors landing page
	 *
	 * @return LandingPage
	 */
	protected LogInPage navigateToLogInPage() {
		try (AutoTimer ignored = AutoTimer.start("BaseTest.navigateToLogInPage")) {
			logger.info("Navigating to landing page: " + TestData.getHomePageUrl());
			getDriver().get(TestData.getHomePageUrl());
			return BDPageFactory.create(LogInPage.class);
		}
	}

	protected void resetBrowserAfter(long timeout) {
		try {
			logger.info("resetBrowserAfter: Stopping driver...");
			((WebDriver) driverWrapper.getDriver()).quit();
			DriverWrapperManager.getInstance().deregisterDriver(driverWrapper);
		} catch (Exception e) {
			logger.info("resetBrowserAfter threw: " + e.getLocalizedMessage());
		} finally {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			initializeDriverWrapper();
		}
	}

	private void initializeDriverWrapper() {
		logger.info("Initializing driver wrapper...");
		driverWrapper = new DriverWrapper(getEnv().getDriver(), getEnv().getDriverProvider());
		if (getEnv().getDriverProvider().name().toLowerCase().contains("browser_stack")) {
			WebDriver driver = (new Augmenter()).augment(getDriver());
			((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
		}
		Helper.maximizeBrowser(getDriver());
	}
}
