package com.applause.auto.pageframework.utils;

import com.applause.auto.framework.pageframework.util.TestHelper;
import com.applause.auto.framework.pageframework.util.logger.LogController;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandles;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Virtual resource locker.
 */
public class VirtualResourceLocker {

	protected static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	/**
	 * Instantiates a new Virtual resource locker. Creates a lock file for specific symbolic resource
	 *
	 * @param items
	 *            the set of potential resources
	 * @param resourceClass
	 *            the resource class - prefix to avoid interfere
	 * @param hashed
	 *            the hashed - true if @param items already hashed
	 */
	public VirtualResourceLocker(List<String> items, String resourceClass, boolean hashed) {
		this.config.prefix += resourceClass;
		this.config.hashed = hashed;
		loadResources(items);
	}

	private void loadResources(List<String> items) {
		resourceList = new ArrayList<>();
		for (String item : items) {
			logger.info("Loaded resource item: " + item);
			logger.info("Converted item: " + (!this.config.hashed ? convertResourceToFilename(item) : item));
			resourceList.add(new ResourceItem(item, !this.config.hashed ? convertResourceToFilename(item) : item));
		}
	}

	/**
	 * Gets and lock available resource + keep a live remote webdriver
	 *
	 * @param timeout
	 *            the timeout
	 * @return the locked resource name
	 */
	public String getAndLockAvailableResourceKeepALive(long timeout) {
		try (AutoTimer ignored = AutoTimer.start("VirtualResourceLocker.getAndLockAvailableResourceKeepAlive")) {
			long startTime = TestHelper.getCurrentGMT6Time();
			long endTime = startTime + timeout * 1000;
			while (true) {
				try {
					return getAndLockAvailableResource();
				} catch (RuntimeException rEx) {
					Helper.waitForPage();
					Helper.ajaxWait();
				}
				if (TestHelper.getCurrentGMT6Time() > endTime) {
					throw new RuntimeException("Unable to lock resource: " + config.prefix);
				}
			}
		}
	}

	/**
	 * Gets and lock available resource.
	 *
	 * @return the locked resource name
	 */
	private String getAndLockAvailableResource() {
		try (AutoTimer ignored = AutoTimer.start("VirtualResourceLocker.getAndLockAvailableResource")) {
			for (ResourceItem resourceCandidate : resourceList) {
				logger.info("Trying lock resource " + resourceCandidate.resource + " lock file: "
						+ resourceCandidate.lockFileName);
				if (lockResource(resourceCandidate.lockFileName)) {
					logger.info("Resource open");
					return resourceCandidate.resource;
				}
				logger.info("Resource closed");
			}
			logger.info("No resources available");
			throw new RuntimeException("Unable to lock resource: " + config.prefix);
		}
	}

	private boolean lockResource(String filename) {
		try (AutoTimer ignored = AutoTimer.start("VirtualResourceLocker.lockResource")) {
			FileLock lock;
			RandomAccessFile randomAccessFile;
			try {
				// Get a file channel for the file
				File file = new File(filename);
				randomAccessFile = new RandomAccessFile(config.sharedFolder + config.prefix + file, "rw");
				FileChannel channel = randomAccessFile.getChannel();

				try {
					lock = channel.tryLock();
				} catch (OverlappingFileLockException e) {
					// File is already locked in this thread or virtual machine
					logger.debug("Failed to lock");
					randomAccessFile.close();
					return false;
				}

			} catch (Exception e) {
				logger.debug("Failed to open file");
				return false;
			}
			return lock != null;
		}
	}

	private List<ResourceItem> resourceList;
	private Config config = new Config();

	/**
	 * Convert resource to lockfile name.
	 *
	 * @param resource
	 *            the resource name
	 * @return the filename
	 */
	static String convertResourceToFilename(String resource) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(resource.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable build filename for  " + resource);
		}
	}

	/**
	 * The type Resource item.
	 */
	class ResourceItem {
		/**
		 * Instantiates a new Resource item.
		 *
		 * @param resource
		 *            the resource
		 * @param lockFileName
		 *            the lock file name
		 */
		ResourceItem(String resource, String lockFileName) {
			this.resource = resource;
			this.lockFileName = lockFileName;
		}

		/**
		 * The Resource.
		 */
		String resource;
		/**
		 * The Lock file name.
		 */
		String lockFileName;
	}

	/**
	 * The type Config.
	 */
	class Config {
		/**
		 * The Prefix.
		 */
		String prefix = "applause_lock_";
		/**
		 * The Shared folder.
		 */
		String sharedFolder = "/tmp/";
		/**
		 * The Hashed.
		 */
		boolean hashed = false;
	}
}
