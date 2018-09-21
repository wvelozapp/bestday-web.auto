package com.applause.auto.pageframework.localization;

import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.pageframework.testdata.TestConstants;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class represents map of localized strings.
 */
public final class LocaleMap {

	private static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());
	private static final LocaleMap instance = new LocaleMap();
	private final Map<String, String> localeStringsMap;

	private LocaleMap() {
		localeStringsMap = loadLocaleStrings();
	}

	/**
	 * Loads locale strings map from configuration csv file depending on environment locale.
	 * 
	 * @return Locale strings map.
	 */
	private static HashMap<String, String> loadLocaleStrings() {
		final int localeColumnIndex;
		final HashMap<String, String> localeStringsMap = new HashMap<>();

		// Set the column index based on the supplied locale
		String locale = System.getProperty("locale");

		logger.debug(String.format("Loading locale strings from column index '%s'", locale));

		try {
			logger.debug("Loading the locale strings CSV file.");
			URL url = new File(TestConstants.LOCALE_STRINGS_FILE_PATH).toURI().toURL();
			Reader reader = new InputStreamReader(new BOMInputStream(url.openStream()), "UTF-8");
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
			localeColumnIndex = parser.getHeaderMap().get(locale);
			try {
				logger.debug("Parsing the CSV file.");
				for (CSVRecord record : parser) {
					final String stringKey = record.get(0);
					if (stringKey == null || stringKey.isEmpty()) {
						// Skip blank lines.
						continue;
					}
					logger.info("Loading locale key -> " + stringKey);
					if (localeColumnIndex >= record.size()) {
						throw new RuntimeException(
								"The row for " + stringKey + " does not have a value at column " + localeColumnIndex);
					}
					final String localeStringValue = StringEscapeUtils.unescapeJava(record.get(localeColumnIndex));
					if (localeStringsMap.containsKey(stringKey)) {
						throw new RuntimeException("The locale map already contains a key for: " + stringKey);
					}
					localeStringsMap.put(stringKey, localeStringValue);
				}
			} finally {
				parser.close();
				reader.close();

				logger.debug("Locale strings successfully parsed.");
			}
		} catch (final IOException e) {
			// Throw a runtime exception since we can't move forward.
			throw new RuntimeException("Unable to load locale strings: " + e.getMessage());
		}

		return localeStringsMap;
	}

	/**
	 * Gets locale String by enum.
	 *
	 * @param localeKey
	 *            - LocaleString enum
	 * @return locale string.
	 */
	public static String get(final LocaleKeys localeKey) {
		return get(localeKey.toString());
	}

	/**
	 * Gets locale String by string.
	 *
	 * @param localeStringKey
	 *            - LocaleString string
	 * @return locale string.
	 */
	public static String get(final String localeStringKey) {
		if (instance.localeStringsMap.containsKey(localeStringKey)) {
			return instance.localeStringsMap.get(localeStringKey);
		}
		throw new RuntimeException("The locale map does not contain the key: " + localeStringKey);
	}
}