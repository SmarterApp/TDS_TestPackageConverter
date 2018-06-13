package tds.testpackageconverter.utils;

import tds.testpackage.legacy.model.Testspecification;
import tds.testpackage.model.BlueprintElement;
import tds.testpackage.model.BlueprintElementTypes;
import tds.testpackage.model.TestPackage;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;

public class TestPackageUtils {
    public static String DATE_PATTERN = "MMM dd yyyy hh:mma";

    public static String getAssessmentKey(final TestPackage testPackage, final String assessmentId) {
        return String.format("(%s)%s-%s", testPackage.getPublisher(), assessmentId, testPackage.getAcademicYear());
    }

    public static String getCombinedKey(final TestPackage testPackage, final String segmentId) {
        return String.format("(%s)%s-COMBINED-%s", testPackage.getPublisher(), segmentId, testPackage.getAcademicYear());
    }

    public static String getCombinedId(final String segmentId) {
        return segmentId + "-COMBINED";
    }

    public static String parseCohort(final String formKey) {
        // <segment key>:<cohort>-<language code>
        // e.g., SBAC-OP-FIXED-G8E-Perf-BrainWorks-Spring-2015-2016:SleepDream-ENU - the cohort is "SleepDream"
        final String cohortAndLanguageCode = formKey.substring(formKey.indexOf(":") + 1);
        return cohortAndLanguageCode.substring(0, cohortAndLanguageCode.indexOf("-"));
    }

    public static String parseIdFromKey(final String key) {
        return key.substring(key.indexOf("-") + 1, key.length());
    }

    public static String parseItemGroupId(final String groupKey) {

        // Sometimes the item group identifiers have a colon - the item group key is on the right side
        final String parsedGroupKey = groupKey.contains(":")
                ? groupKey.substring(groupKey.indexOf(":") + 1, groupKey.length())
                : groupKey;

        final String[] groupKeyStrSplit = parsedGroupKey.split("-");

        return parsedGroupKey.startsWith("I")
                ? groupKeyStrSplit[groupKeyStrSplit.length - 1]
                : groupKeyStrSplit[groupKeyStrSplit.length - 2];
    }

    public static boolean isAdministrationPackage(final Testspecification testSpecification) {
        return testSpecification.getPurpose().equalsIgnoreCase("administration") &&
                testSpecification.getAdministration() != null;
    }

    public static boolean isScoringPackage(final Testspecification testSpecification) {
        return testSpecification.getPurpose().equalsIgnoreCase("scoring") &&
                testSpecification.getScoring() != null;
    }

    public static String parseVersion(final BigDecimal version) {
        final String rawVersion = String.valueOf(version);
        if (rawVersion.contains(".")) {
            return rawVersion.substring(0, rawVersion.indexOf(".")); // ignore the decimal
        }

        return rawVersion;
    }

    public static String formatDate(final String publishDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.parse(publishDate).toInstant().toString();
    }

    public static String formatDate(final Instant publishDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(Date.from(publishDate));
    }

    public static String getBlueprintKeyFromId(final BlueprintElement element, final String clientName) {
        if (BlueprintElementTypes.CLAIM_AND_TARGET_TYPES.contains(element.getType())) {
            return String.format("%s-%s", clientName, element.getId());
        }

        return element.getId();
    }

    public static String getFormIdForLanguage(final String formId, final String languageCode) {
        switch (languageCode) {
            case "ENU":
                return String.format("%s::%s", formId, "ENU");
            case "ESN":
                return String.format("%s::%s", formId, "SPA");
            case "ENU-Braille":
                return String.format("%s::%s", formId, "BRL");
            default:
                return String.format("%s::%s", formId, languageCode);
        }
    }
  
    public static int generateFormKey(final String formId) {
        return Math.abs(formId.hashCode());
    }
}
