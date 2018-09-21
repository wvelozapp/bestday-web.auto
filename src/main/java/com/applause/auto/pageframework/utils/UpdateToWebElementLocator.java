package com.applause.auto.pageframework.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateToWebElementLocator {
	public static void main(String[] args) {
		try {
			String IMPORT_STRING = "import com.applause.auto.framework.pageframework.web.WebElementLocator;";
			String locatorsPath = "src/main/resources/locators.csv";
			Pattern locatorCommentPattern = Pattern.compile("// WEL: (\\S+)");
			Pattern methodPattern = Pattern.compile(
					"(private |protected |public )?(\\S*?)\\s+?(\\w+?)\\((final\\s+)?(String\\s+)?(\\w*?)\\)\\s*?\\{([^}]*?)Locators\\.get\\(LocatorKeys\\.\\w+?\\.(\\w+)(.*?)}",
					Pattern.DOTALL);
			String replacement = "// WEL: $8\n\t@WebElementLocator(webDesktop = \"%s\", webPhone = \"%s\", webTablet = \"%s\")\n\t$1$2 $3($4$5$6) {$7getLocator(this, \"$3\"$9}";
			String srcRoot = "src/main/java";

			Pattern locatorPattern = Pattern.compile("Locators\\.get\\(LocatorKeys\\.\\w+?\\.(\\w+)\\)");
			String locatorReplacement = "getLocator(this, \"$1\")";

			Map<String, List<String>> locatorMap = new HashMap<>();

			Reader reader = new FileReader(locatorsPath);
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
			for (CSVRecord record : parser) {
				// Add slots for all three types: desktop, mobile, tablet
				List<String> list = Arrays.asList("", "", "");
				for (int i = 0; i < record.size() - 1; i++) {
					list.set(i, record.get(i + 1));
				}
				locatorMap.put(record.get(0), list);
			}

			// .filter(p->p.toString().toLowerCase().contains("categorymenuchunk"))
			Files.walk(Paths.get(srcRoot)).forEach(path -> {
				try {

					//
					// Skip files we don't need to process.
					//
					if (!Files.isRegularFile(path)) {
						return;
					}

					String pathString = path.toString().toLowerCase();
					if (!FilenameUtils.getExtension(pathString).equalsIgnoreCase("java")) {
						System.err.println("Skipping non-java file: " + pathString);
						return;
					}

					String filename = FilenameUtils.getName(pathString);
					if (!filename.contains("page") && !filename.contains("chunk")) {
						System.err.println("Skipping non-POM file: " + pathString);
						return;
					}

					{
						// Add the import line to the file if needed.
						String file = new String(Files.readAllBytes(path));
						if (!file.contains(IMPORT_STRING)) {
							file = file.replaceAll("(package com.*?\n)", "$1\n" + IMPORT_STRING + "\n");
						}

						// Search for getter methods and replace with annotations.
						Matcher matcher = methodPattern.matcher(file);
						boolean found = matcher.find();
						if (found) {
							String newFile = matcher.replaceAll(replacement);
							System.out.println("writing: " + path);
							Files.write(path, newFile.getBytes());
						}
					}

					{
						// Load the CSV and replace the empty locators with actual values
						List<String> codeLines = Files.readAllLines(path);
						for (int i = 0; i < codeLines.size(); i++) {
							String commentLine = codeLines.get(i);
							Matcher commentMatcher = locatorCommentPattern.matcher(commentLine);
							if (commentMatcher.find()) {
								String comment = commentMatcher.group(1);
								List<String> locatorList = locatorMap.get(comment);
								if (locatorList != null) {
									codeLines.remove(i);
									String annotation = codeLines.get(i);
									annotation = String.format(annotation, locatorList.get(0), locatorList.get(1),
											locatorList.get(2));
									annotation = annotation.replace(", webTablet = \"\"", "");
									codeLines.set(i, annotation);
								} else {
									System.err.println("No locator found for: " + comment);
								}
							}
						}
						File f = new File(path.toString());
						FileUtils.writeLines(f, codeLines);
					}

					{
						String file = new String(Files.readAllBytes(path));
						// Walk one more time looking for any remaining locators
						Matcher locatorMatcher = locatorPattern.matcher(file);
						boolean found = locatorMatcher.find();
						if (found) {
							String newFile = locatorMatcher.replaceAll(locatorReplacement);
							System.out.println("writing: " + path);
							Files.write(path, newFile.getBytes());
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
