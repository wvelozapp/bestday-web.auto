package com.applause.auto.pageframework.testrail;

import com.applause.auto.framework.pageframework.util.drivers.DriverWrapper;
import com.applause.auto.framework.pageframework.util.drivers.DriverWrapperManager;
import com.applause.auto.framework.pageframework.util.drivers.DriverWrapperUtil;
import com.applause.auto.framework.pageframework.util.drivers.RunUtil;
import com.applause.auto.framework.pageframework.util.environment.EnvironmentUtil;
import com.applause.auto.framework.pageframework.util.logger.AtomLogger;
import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.framework.pageframework.util.logger.ops.ResultDAO;
import com.applause.auto.framework.pageframework.util.logger.testrail.TestRailStatus;
import com.applause.auto.framework.pageframework.util.screenshots.MobileScreenshotManager;
import com.applause.auto.framework.pageframework.util.screenshots.WebScreenshotManager;
import com.applause.auto.framework.test.listeners.TestNGListenerUtils;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * TestListener class to implement logging of results and other hooks based on
 * test status conditions.
 */
public class BestDayTestListener extends TestListenerAdapter {

	private EnvironmentUtil env = EnvironmentUtil.getInstance();

	private TestRailLogger testRailLogger;

	private RunUtil runUtil = RunUtil.getInstance();

	private static TestNGListenerUtils ngListenerUtil = new TestNGListenerUtils();

	private static LogController logger = new LogController(BestDayTestListener.class);

	private static ResultDAO sqlDao = new ResultDAO();

	private DriverWrapperUtil wrapperUtil = DriverWrapperUtil.getInstance();

	private DriverWrapperManager wrapperManager = DriverWrapperManager.getInstance();

	private static String runLogUrl = "";

	private static String testTags = "";

	private static int screenshotCount = 0;

	private static boolean firstTest = true;

	private String testCaseRunTime = "";

	public BestDayTestListener() {
		if (env.getLogResultsToTestRail()) {
			testRailLogger = new TestRailLogger();
		}
	}

	@Override
	public void onFinish(ITestContext context) {
		Iterator<ITestResult> listOfFailedTests = context.getFailedTests().getAllResults().iterator();
		while (listOfFailedTests.hasNext()) {
			ITestResult failedTest = listOfFailedTests.next();
			ITestNGMethod method = failedTest.getMethod();
			if (context.getFailedTests().getResults(method).size() > 1) {
				listOfFailedTests.remove();
			} else {
				if (context.getPassedTests().getResults(method).size() > 0) {
					listOfFailedTests.remove();
				}
			}
		}

		if (env.getLogATOMResults()) {
			String failed = String.valueOf(context.getFailedTests().size());
			String passed = String.valueOf(context.getPassedTests().size());
			String skipped = String.valueOf(context.getSkippedTests().size());

			runLogUrl = LogController.flushRunLogToS3(runUtil.getRunId(), env.getCustomerS3BucketName());
			String result = "pass";
			if (context.getFailedTests().size() > 0 || context.getSkippedTests().size() > 0) {
				result = "fail";
			}

			// TODO Need to figure out how to get this
			String externalUrl = "";

			String jobName = env.getCIJobName();
			if (env.getCustomerBuildNumber() != null) {
				jobName = jobName + "_" + env.getCustomerBuildNumber();
			}

			// Log to atom
			// AtomLogger.logTestRunResultToAtom(runUtil.getRunId(),
			// runUtil.getRunDate(), result, env.getCIJobName(),
			// passed, failed, skipped, env.getBuildId(),
			// env.getCustomerBuildNumber(), env.getCompanyId(),
			// getProductId(), getDeviceType(), env.getDeviceName(),
			// wrapperUtil.getOs(),
			// wrapperUtil.getOsVersion(), wrapperUtil.getManufacturer(),
			// runLogUrl, externalUrl);
		}

	}

	@Override
	public void onStart(ITestContext context) {
		// get test tags (i.e. Groups)
		testTags = Arrays.toString(context.getIncludedGroups());
		testTags = convertTags(testTags);
		logger.debug("Test Tags:" + testTags);
	}

	private String convertTags(String testTags) {
		String tagString = testTags.substring(1, testTags.length() - 1);
		String[] tags = tagString.split(",");
		StringBuffer tagsBufffer = new StringBuffer();
		tagsBufffer.append("[");
		for (int i = 0; i < tags.length; i++) {
			tagsBufffer.append("\"" + tags[i].trim() + "\"");
			if (tags.length - i > 1) {
				tagsBufffer.append(",");
			}
		}
		tagsBufffer.append("]");
		return tagsBufffer.toString();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {

	}

	@Override
	public void onTestSkipped(ITestResult testResult) {
		ITestNGMethod testNGMethod = testResult.getMethod();
		Method method = testNGMethod.getConstructorOrMethod().getMethod();

		List<String> snapShots = takeScreenshots(testResult, "TestSkipped");

		logger.info("=== Skipped test " + method.getName() + " ===");
		// Log Method Log to S3
		String url = LogController.flushMethodLogToS3(runUtil.getRunId(), method.getName(),
				env.getCustomerS3BucketName());

		String testDescription = ngListenerUtil.getCaseDescription(testResult);
		String executionTime = String.valueOf(testResult.getEndMillis() - testResult.getStartMillis());

		// Log to method to ATOM
		// logTestCaseResult("skip", method.getName(), testTags,
		// testDescription, executionTime, url, "test skipped",
		// testCaseRunTime);

		String testCaseTitle = testResult.getMethod().getDescription();
		logResultsToTestRail(testCaseTitle, snapShots, url, TestRailStatus.RETEST.getValue());
		runUtil.resetImageCounter();
	}

	@Override
	public void onTestStart(ITestResult testResult) {
		// if this is the first test for the run, create the run.
		// we have to create the run after the run_id is already built, which
		// happens as the first test is executed.
		if (firstTest) {
			firstTest = false;
			if (env.getLogATOMResults()) {
				// Log to atom
				AtomLogger.logTestRunResultToAtom(runUtil.getRunId(), runUtil.getRunDate(), "", env.getCIJobName(), "0",
						"0", "0", env.getBuildId(), env.getCustomerBuildNumber(), env.getCompanyId(), getProductId(),
						getDeviceType(), env.getDeviceName(), wrapperUtil.getOs(), wrapperUtil.getOsVersion(),
						wrapperUtil.getManufacturer(), "", "");
			}
		}
		testCaseRunTime = runUtil.getTestCaseRunDate();
		logger.info("=== Starting test " + testResult.getName() + ":" + testCaseRunTime + "===");
		runUtil.resetImageCounter();
		runUtil.setCurrentTestName(testResult.getName());

	}

	@Override
	public void onTestSuccess(ITestResult testResult) {
		ITestNGMethod testNGMethod = testResult.getMethod();
		Method method = testNGMethod.getConstructorOrMethod().getMethod();

		List<String> snapShots = takeScreenshots(testResult, "TestSuccess");

		logger.info("=== Completed test (Passed) " + method.getName() + " ===");
		String url = LogController.flushMethodLogToS3(runUtil.getRunId(), method.getName(),
				env.getCustomerS3BucketName());
		String testDescription = ngListenerUtil.getCaseDescription(testResult);
		String executionTime = String.valueOf(testResult.getEndMillis() - testResult.getStartMillis());
		// Log to method to ATOM
		logTestCaseResult("pass", method.getName(), testTags, testDescription, executionTime, url, "", testCaseRunTime);

		String testCaseTitle = testResult.getMethod().getDescription();
		logResultsToTestRail(testCaseTitle, snapShots, url, TestRailStatus.PASSED.getValue());
		runUtil.resetImageCounter();
	}

	@Override
	public void onTestFailure(ITestResult testResult) {
		ITestNGMethod testNGMethod = testResult.getMethod();
		Method method = testNGMethod.getConstructorOrMethod().getMethod();

		List<String> snapShots = takeScreenshots(testResult, "TestFailure");

		env.setFailedTestDoReset(true);

		// Log Method Log to S3
		String testDescription = ngListenerUtil.getCaseDescription(testResult);
		String executionTime = String.valueOf(testResult.getEndMillis() - testResult.getStartMillis());

		logger.info("=== Completed test (Failed) " + method.getName() + " ===");

		String errorMessage = testResult.getThrowable().getMessage();
		if (errorMessage == null) {
			errorMessage = testResult.getThrowable().getLocalizedMessage();
		} else {
			errorMessage = errorMessage.replace('\b', ' ');
			errorMessage = errorMessage.replace('\f', ' ');
			errorMessage = errorMessage.replace('\n', ' ');
			errorMessage = errorMessage.replace('\r', ' ');
			errorMessage = errorMessage.replace('\t', ' ');
			errorMessage = errorMessage.replace('\"', ' ');
			errorMessage = errorMessage.replace('\\', ' ');
			errorMessage = errorMessage.replace("\r\n", " ");
			errorMessage = errorMessage.replaceAll(System.lineSeparator(), " ");
		}
		logger.info("Test Failure Reason : " + errorMessage);

		logger.info("=============================================================");
		String url = LogController.flushMethodLogToS3(runUtil.getRunId(), method.getName(),
				env.getCustomerS3BucketName());
		// Log to method to ATOM
		// logTestCaseResult("fail", method.getName(), testTags,
		// testDescription, executionTime, url, errorMessage,
		// testCaseRunTime);

		String testCaseTitle = testResult.getMethod().getDescription();
		logResultsToTestRail(testCaseTitle, snapShots, url, errorMessage, TestRailStatus.FAILED.getValue());
		runUtil.resetImageCounter();
	}

	private void logResultsToTestRail(String testCaseTitle, List<String> snapShotUrls, String resultsUrl, int result) {
		logResultsToTestRail(testCaseTitle, snapShotUrls, resultsUrl, "", result);
	}

	private void logResultsToTestRail(String testCaseTitle, List<String> snapShotUrls, String resultsUrl,
			String errorMessage, int result) {

		String markdown = null;
		if (env.getLogResultsToTestRail()) {
			logger.debug("logging result to testrail");
			try {
				// include the url in the comments.
				resultsUrl = String.format("[results log](%s)", resultsUrl);

				// Add the snapshot to comments field
				if (snapShotUrls.size() > 0) {
					markdown = String.format("![browser image](%s)", snapShotUrls.get(0));
				}

				testRailLogger.logResult(testCaseTitle, result, resultsUrl, markdown, errorMessage);

			} catch (Throwable e) {
				logger.warn("Exception logging a TestRail result for : " + testCaseTitle + " - error: "
						+ e.getLocalizedMessage());
			}
		}
	}

	private String getDeviceType() {
		// Determine ProductId
		String deviceType = "";
		if (env.getIsMobileIOS()) {
			deviceType = "mobile and tablet";
		}
		if (env.getIsMobileAndroid()) {
			deviceType = "mobile and tablet";
		} else {
			deviceType = "web and application";
		}
		return deviceType;
	}

	private String getProductId() {
		// Determine ProductId
		String productId = "";
		if (env.getIsMobileIOS()) {
			productId = env.getProductIOSId();
		} else if (env.getIsMobileAndroid()) {
			productId = env.getProductAndroidId();
		} else if (env.getIsMobileWebTest()) {
			productId = env.getProductMobileWebId();
		} else {
			productId = env.getProductWebId();
		}
		return productId;
	}

	private void logTestCaseResult(String result, String testName, String testTags, String testDescription,
			String executionTime, String url, String failureReason, String testCaseRunTime) {
		if (env.getLogATOMResults()) {

			AtomLogger.logTestCaseResultToAtom(runUtil.getRunId(), testName, testTags, testDescription, executionTime,
					runUtil.getRunDate(), result, failureReason, env.getBuildId(), env.getCustomerBuildNumber(),
					env.getCompanyId(), getProductId(), getDeviceType(), env.getDeviceName(), wrapperUtil.getOs(),
					wrapperUtil.getOsVersion(), wrapperUtil.getManufacturer(), url, testCaseRunTime);
		}
		if (env.getSkipMySqlLogs()) {
			logger.info("Skipping MySql Result Logging.");
		} else {
			// log to mySql
			sqlDao.storeScenarioResult(env.getCompanyId(), getProductId(), runUtil.getRunId(), runUtil.getSQLRunDate(),
					testName, testTags, testDescription, Integer.parseInt(executionTime), failureReason,
					env.getBuildId(), env.getCustomerBuildNumber(), getDeviceType(), env.getDeviceName(),
					wrapperUtil.getOs(), wrapperUtil.getOsVersion(), wrapperUtil.getManufacturer(), url, result);
		}
	}

	private List<String> takeScreenshots(ITestResult testResult, String resultType) {
		List<String> screenshots = new ArrayList<>();
		if (env.getTakeScreenshots()) {
			ArrayList<DriverWrapper> wrappers = wrapperManager.getDriverWrappers();
			for (DriverWrapper wrapper : wrappers) {
				try {
					String url;
					if ((env.getIsAndroidMobileWeb()) || (env.getIsIOSMobileWeb())) {
						WebScreenshotManager snapShotManager = (WebScreenshotManager) wrapper.getSnapshotManager();
						url = snapShotManager.takeScreenshot(
								resultType + "_MobileWeb_" + screenshotCount + "_" + testResult.getTestName());
					} else if (env.getIsMobileAndroid() || env.getIsMobileIOS()) {
						MobileScreenshotManager snapShotManager = (MobileScreenshotManager) wrapper
								.getSnapshotManager();
						url = snapShotManager.takeRemoteDeviceScreenshot(
								resultType + "_Device_" + screenshotCount + "_" + testResult.getTestName());
					} else {
						WebScreenshotManager snapShotManager = (WebScreenshotManager) wrapper.getSnapshotManager();
						url = snapShotManager.takeScreenshot(
								resultType + "_Browser_" + screenshotCount + "_" + testResult.getTestName());
					}
					screenshots.add(url);
					screenshotCount++;
				} catch (Exception e) {
					logger.error("Exception thrown when trying to grab screenshot.");
					e.printStackTrace();
				}
			}
		}
		return screenshots;
	}

	/**
	 * Static getter for run log url
	 * 
	 * @return runLogUrl
	 */
	public static String getRunLogUrl() {
		return runLogUrl;
	}
}
