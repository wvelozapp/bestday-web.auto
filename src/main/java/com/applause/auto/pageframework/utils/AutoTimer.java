package com.applause.auto.pageframework.utils;

import com.applause.auto.framework.pageframework.util.logger.LogController;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

public class AutoTimer implements AutoCloseable {
	private static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	private static HashMap<String, result> durations = new HashMap<>();
	private static Deque<AutoTimer> running = new ArrayDeque<>(100);
	private long startTime;
	private String name;

	private AutoTimer(String name) {
		this.name = name;
	}

	public static void reset() {
		durations.clear();
	}

	@SuppressWarnings("unchecked")
	public static List<Map.Entry<String, result>> getDurations() {
		return durations.entrySet().stream().sorted((e1, e2) -> {
			long l1 = e1.getValue().getDurationInSeconds();
			long l2 = e2.getValue().getDurationInSeconds();
			return Long.compare(l2, l1);
		}).collect(Collectors.toList());
	}

	public static AutoTimer start(String name) {
		AutoTimer newTimer = new AutoTimer(name);
		running.push(newTimer);
		newTimer.startTime = System.currentTimeMillis();
		return newTimer;
	}

	@Override
	public void close() {
		try {
			running.pop();
			if (running.stream().anyMatch(st -> st.name.equals(this.name))) {
				logger.warn("Recursive timer call for: " + name);
				return;
			}
			long endTime = System.currentTimeMillis();
			long inclusiveDuration = (endTime - startTime) / 1000;
			result currentResult = durations.getOrDefault(this.name, new result(0, 0));
			currentResult.durationInSeconds += inclusiveDuration;
			currentResult.instances++;
			durations.put(this.name, currentResult);
		} catch (Throwable t) {
			logger.warn("Exception in timer: " + t.getMessage());
		}
	}

	public static void main(String[] args) {
		try (AutoTimer ignored = AutoTimer.start("UnitTest")) {
			UnitTest t = new UnitTest();
			t.parent();
			t.sleep(1);
		}
		// Print out a CSV-friendly output
		AutoTimer.getDurations().forEach((entry) -> System.out.println(String.format("\"%s\", %d, %d", entry.getKey(),
				entry.getValue().durationInSeconds, entry.getValue().instances)));
	}

	public static class result {
		private long durationInSeconds;
		private int instances;

		result(long durationInSeconds, int instances) {
			this.durationInSeconds = durationInSeconds;
			this.instances = instances;
		}

		public long getDurationInSeconds() {
			return this.durationInSeconds;
		}

		public long getInstances() {
			return this.instances;
		}
	}
}

class UnitTest {

	void parent() {
		try (AutoTimer ignored = AutoTimer.start("parent")) {
			// 9
			child();
			// 3
			sleep(3);
			// 9
			child();
		}
	}

	// 2/7
	private void child() {
		sleep(2);
		try (AutoTimer ignored = AutoTimer.start("child")) {
			// 3
			grandchild();
			// 1
			sleep(1);
			// 3
			grandchild();
		}
	}

	// 2/1
	private void grandchild() {
		sleep(2);
		try (AutoTimer ignored = AutoTimer.start("grandchild")) {
			// 1
			sleep(1);
		}
	}

	void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (Throwable ignored) {
		}
	}
}
