package com.applause.auto.pageframework.testrail;

import com.applause.auto.framework.pageframework.util.StringUtils;
import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.codepine.api.testrail.TestRail;
import com.codepine.api.testrail.model.*;
import com.codepine.api.testrail.model.Plan.Entry;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestRailLogger {

	private static LogController logger = new LogController(TestRailLogger.class);
	private TestRail testRail;
	private Project project;
	private Suite testSuite;
	private Plan testPlan;
	private Entry planEntry;
	private Run run;
	private Test test;
	private String runName;
	private String matrixContainerWorkspace;
	private String lockFileName = "PlanLock.txt";
	private int testId = 0;

	public TestRailLogger() {
		initializeTestRailLogger();
	}

	private void initializeTestRailLogger() {
		this.matrixContainerWorkspace = System.getProperty("matrixContainerWorkspace");
		if (StringUtils.isBlank(this.matrixContainerWorkspace)) {
			throw new IllegalArgumentException(
					"-DmatrixContainerWorkspace must be set in the environment when logging to TestRail");
		}

		String testRailUserName = System.getenv("TESTRAIL_USERNAME");
		if (StringUtils.isBlank(testRailUserName)) {
			throw new IllegalArgumentException(
					"TESTRAIL_USERNAME must be set in the environment when logging to TestRail");
		}

		String testRailPassword = System.getenv("TESTRAIL_TOKEN");
		if (StringUtils.isBlank(testRailPassword)) {
			throw new IllegalArgumentException(
					"TESTRAIL_TOKEN must be set in the environment when logging to TestRail");
		}

		String testRailUrl = System.getProperty("testRailUrl", null);
		if (StringUtils.isBlank(testRailUrl)) {
			throw new IllegalArgumentException(
					"-DtestRailUrl must be set as a Java system property when logging to TestRail");
		}

		int projectId = Integer.parseInt(System.getProperty("testRailProjectId", "-1"));
		if (projectId < 0) {
			throw new IllegalArgumentException(
					"-DtestRailProjectId must be set as a Java system property when logging to TestRail");
		}

		int suiteId = Integer.parseInt(System.getProperty("testRailSuiteId", "-1"));
		if (suiteId < 0) {
			throw new IllegalArgumentException(
					"-DtestRailSuiteId must be set as a Java system property when logging to TestRail");
		}

		runName = System.getProperty("testRailRunName", null);
		if (StringUtils.isBlank(runName)) {
			throw new IllegalArgumentException(
					"-DtestRailRunName must be set as a Java system property when logging to TestRail");
		}

		String planName = System.getProperty("testRailPlanName", null);
		if (StringUtils.isBlank(planName)) {
			throw new IllegalArgumentException(
					"-DtestRailPlanName must be set as a Java system property when logging to TestRail");
		}

		// Set the testrail instance
		this.testRail = TestRail.builder(System.getProperty("testRailUrl"), System.getenv("TESTRAIL_USERNAME"),
				System.getenv("TESTRAIL_TOKEN")).build();

		// Set the project instance
		this.project = testRail.projects().get(projectId).execute();

		// Set the suite instance
		this.testSuite = testRail.suites().get(suiteId).execute();

		// Set the testplan - this contains the entries/runs for an execution
		setTestPlan(planName);

		String allTests = System.getProperty("addAllTestsToPlan");
		String rerun = System.getProperty("testRerun");
		if (allTests != null && allTests.equals("true")) {
			if (rerun != null && !rerun.equals("true")) {
				logger.info("-DaddAllTestsToPlan = true so we are adding ALL tests to this test plan");
				createPlanEntryAllTests();
			}
		}
	}

	/**
	 * Logs a test result to testrail allowing to pass error message on fail
	 * 
	 * @param testCaseId
	 * @param result
	 * @param log
	 * @param screenshot
	 * @param errorMessage
	 */
	public void logResult(String testCaseId, int result, String log, String screenshot, String errorMessage) {

		String resultsInfo = "";
		// create a test result object to log to attach to the test
		List<ResultField> customResultFields = testRail.resultFields().list().execute();

		logger.debug("add result and log to the test");
		resultsInfo = "SCREENSHOT\n" + screenshot + "\n\n\n\n" + "LAST ASSERT\n" + log;
		if (!errorMessage.isEmpty()) {
			resultsInfo = resultsInfo + "\n\n\n\n" + "LAST URL\n" + errorMessage;
		}
		Result testResult = new Result().setStatusId(result).setComment(resultsInfo);

		logger.debug("get testcase list from the suite");
		int caseId = Integer.valueOf(testCaseId.replace("C", ""));

		logger.debug("Set a plan entry for this result");
		setPlanEntry(Integer.valueOf(caseId));

		logger.debug("get the test run from the entry - its the only one");
		this.run = this.planEntry.getRuns().get(0);
		logger.debug("run id = " + this.run.getId());

		logger.debug("get the corresponding test id");
		// Here we are taking care of a race condition - the api requires us to
		// grab all
		// tests, append this one and update the run. Other threads could do
		// this too and
		// post a set of tests that doesn't include this one
		int numTimes = 0;
		while (getTestIdForTestCase(run.getId(), caseId) == 0) {
			logger.warn("Could not find a test associated with this test case - adding test again");
			List<Integer> caseIds = getTestCaseIdsForRun(this.run.getId());
			caseIds.add(Integer.valueOf(caseId));
			this.planEntry.setCaseIds(caseIds);
			this.planEntry = testRail.plans().updateEntry(this.testPlan.getId(), this.planEntry).execute();
			numTimes++;
			if (numTimes == 3000)
				break; // let it try for a few seconds, then break and check for
						// last time
		}

		if (getTestIdForTestCase(run.getId(), caseId) == 0) {
			logger.warn("Could not find a test associated with this test case, please investigate");
		} else {
			testRail.results().add(this.testId, testResult, customResultFields).execute();
		}

	}

	// A TestPlan will hold a collection of entries and runs for a given
	// test execution cycle. This method sets the testplan, either
	// by retrieving one matching the name, or by creating a new one
	private void setTestPlan(String planName) {

		// strip UTC from build timestamp that gets passed into the plan name
		planName = planName.replace(" UTC", "");

		// replace underscores - only used for jenkins and reads better without
		planName = planName.replace("_", " - ");

		// this may be a test rerun so check if the plan already exists
		if (findAndStorePlan(planName)) {
			return;
		}

		// try and create a new plan, if it fails then its probably because
		// a parallel test is already doing that
		if (!createNewPlan(planName)) {
			logger.debug("allowing the plan to be created by other test...");
			delay(3);
			findAndStorePlan(planName);
		}

		deleteDuplicatePlans(planName);

	}

	// Parallel tests can create duplicate plans. This method attempts to create
	// a lock file allowing it to be the sole plan creator. IF the file already
	// exists it will return a false to allow the calling method to wait and
	// then grab the newly created plan
	private boolean createNewPlan(String planName) {
		logger.debug("No plan found - creating a plan");
		try {
			Path file = Paths.get(this.matrixContainerWorkspace, this.lockFileName);
			logger.debug("create the lock file");
			Files.createFile(file);
			do {
				logger.debug("Waiting on file to be created");
			} while (!Files.exists(file));
			logger.debug("create the plan");
			this.testPlan = testRail.plans().add(this.project.getId(), new Plan().setName(planName)).execute();
			logger.debug("delete the lock file");
			Files.delete(file);
			return true;
		} catch (FileAlreadyExistsException ignored) {
			logger.debug("File Exists, a parallel test is already creating this plan");
			return false;
		} catch (IOException ex) {
			logger.warn(ex.getMessage());
			logger.warn(
					"Had issues implementing lock mechanism for creating the test plan - there may be a duplicate plan");
			this.testPlan = testRail.plans().add(this.project.getId(), new Plan().setName(planName)).execute();
			return true;
		}

	}

	// check for the existence of a testplan. If it exists, set the local
	// plan instance to be this very one
	private boolean findAndStorePlan(String planName) {

		checkForLockFile();

		logger.debug("Getting list of plans for the project");
		List<Plan> testPlans = testRail.plans().list(this.project.getId()).execute();

		logger.debug("Looking for a plan matching the name: " + planName);
		for (Plan plan : testPlans) {
			if (plan.getName().equals(planName)) {
				this.testPlan = plan;
				logger.debug("Found plan, returning");
				return true;
			}
		}
		logger.debug("Plan did not exists, need to create");
		return false;
	}

	// A plan entry is a container for a set of runs
	// Until interaction with configs is fixed, we
	// work as if 1 entry = 1 run
	private void setPlanEntry(Integer testCaseId) {

		// At this point, there may have been duplicate plans created
		deleteDuplicatePlans(this.testPlan.getName());

		logger.debug("Now look for the TestPlan entry");
		List<Entry> entries = testRail.plans().get(this.testPlan.getId()).execute().getEntries();

		for (Entry entry : entries) {
			if (entry.getName().equals(this.runName)) {
				logger.debug("Found the corresponding plan entry");
				this.planEntry = entry;
				this.run = this.planEntry.getRuns().get(0);

				logger.debug("store the test cases from the associated run, in a list");
				List<Integer> caseIds = getTestCaseIdsForRun(this.run.getId());

				if (!caseIds.contains(testCaseId)) {
					logger.debug("add the test case passed in, to the list of cases");
					caseIds.add(testCaseId);

					logger.debug("update the plan entry / run to include this test");
					this.planEntry.setCaseIds(caseIds);
					this.planEntry = testRail.plans().updateEntry(this.testPlan.getId(), this.planEntry).execute();
				}
				return;
			}
		}

		logger.debug("No entry found - create an entry and add this test");
		createNewPlanEntry(testCaseId);

	}

	// create a plan entry including all tests from the suite
	private void createPlanEntryAllTests() {

		logger.debug("Now look for the TestPlan entry");
		List<Entry> entries = testRail.plans().get(this.testPlan.getId()).execute().getEntries();

		for (Entry entry : entries) {
			if (entry.getName().equals(this.runName)) {
				logger.debug("Found an existing plan entry");
				return;
			}
		}

		this.planEntry = new Entry();
		this.planEntry.setName(this.runName);
		this.planEntry.setSuiteId(this.testSuite.getId());
		this.planEntry.setIncludeAll(true);
		this.planEntry = testRail.plans().addEntry(this.testPlan.getId(), this.planEntry).execute();

	}

	// Create a new plan entry in the current plan. This equates
	// to a test run
	private void createNewPlanEntry(Integer testCaseId) {
		List<Integer> caseIds = new ArrayList();
		caseIds.add(testCaseId);
		this.planEntry = new Entry();
		this.planEntry.setName(this.runName);
		this.planEntry.setSuiteId(this.testSuite.getId());
		this.planEntry.setIncludeAll(false);
		this.planEntry.setCaseIds(caseIds);
		this.planEntry = testRail.plans().addEntry(this.testPlan.getId(), this.planEntry).execute();

	}

	// A test is a copy of a test case that has been
	// associated with a run, by run id. Get the tests
	// for this run, and then get the corresponding test cases
	private List<Integer> getTestCaseIdsForRun(int runId) {
		logger.debug("Get a list of the test cases for the existing run");
		List<Integer> caseIds = new ArrayList();
		List<Test> tests = testRail.tests().list(runId).execute();

		for (Test test : tests) {
			caseIds.add(test.getCaseId());
		}
		return caseIds;
	}

	// Get the test id for the test case
	// A test is a copy of a test case that has been
	// associated with a run, by run id.
	private int getTestIdForTestCase(int runId, int caseId) {
		logger.debug("Get the corresponding test dD for this test case");
		int testId = 0;
		List<Test> tests = testRail.tests().list(runId).execute();

		for (Test test : tests) {
			if (test.getCaseId() == caseId) {
				testId = test.getId();
				break;
			}
		}
		this.testId = testId;

		return testId;
	}

	private void deleteDuplicatePlans(String planName) {
		logger.debug("Getting list of plans for the project");
		List<Integer> planIds = new ArrayList<Integer>();
		List<Plan> testPlans = testRail.plans().list(this.project.getId()).execute();

		logger.debug("Looking for a plan matching the name: " + planName);
		for (Plan plan : testPlans) {
			if (plan.getName().equals(planName)) {
				planIds.add(plan.getId());
			}
		}

		if (planIds.size() > 1) {
			logger.info("we have duplicate plans");
			logger.info("set the default plan to be the first one created");
			Collections.sort(planIds);

			logger.info("delete the others, keep first one created");
			for (int index = 1; index <= planIds.size() - 1; index++) {
				try {
					testRail.plans().delete(planIds.get(index)).execute();
				} catch (Exception ex) {
					// catch just in case we get an exception trying to delete
					// a plan that has been deleted by another job
				}
			}

		}

		logger.debug("making sure this plan is the same as whats being logged to in testrail");
		this.testPlan = testRail.plans().get(planIds.get(0)).execute();

	}

	private boolean checkForLockFile() {
		logger.info("look for a lock file first as this indicates plan is being created");
		Path file = Paths.get(this.matrixContainerWorkspace, this.lockFileName);
		if (Files.exists(file)) {
			logger.info("lock file exists, let plan be created");
			delay(5);
			return true;
		} else {
			return false;
		}

	}

	private void delay(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
