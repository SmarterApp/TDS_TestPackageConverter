package tds.testpackageconverter.converter;

import tds.testpackage.model.TestPackage;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * An interface for a service responsible for unzipping and deserializing test packages
 */
public interface TestPackageConverterService {
    /**
     * @param testPackageName The name of the test package to convert to
     * @param file            The zip file containing legacy {@link tds.testpackage.legacy.model.Testspecification} files
     * @return {@code true} if the file was created, {@code false} if the file already exists
     * @throws IOException If an error occurs while unzipping the test package zip file, or deserializing the content
     */
    void extractAndConvertTestSpecifications(final String testPackageName, final File file) throws IOException, ParseException;

    /**
     * Converts one or more legacy test specifications to the new Test Package format
     *
     * @param testPackageName          A the name of the test package to produce
     * @param adminAndScoringFileNames The filenames of the administration and scoring packages to convert
     * @throws IOException    If there is an error reading any of the test packages
     * @throws ParseException If there is an error parsing any of the test packages
     */
    void convertTestSpecifications(final String testPackageName, final List<String> adminAndScoringFileNames) throws IOException, ParseException;

    /**
     * Converts one or more legacy test specifications to the new Test Package format
     *
     * @param testPackageName          A the name of the test package to produce
     * @param adminAndScoringFileNames The filenames of the administration and scoring packages to convert
     * @param diffFileName             The filename of the "diff" test package
     * @throws IOException    If there is an error reading any of the test packages
     * @throws ParseException If there is an error parsing any of the test packages
     */
    void convertTestSpecifications(final String testPackageName, final List<String> adminAndScoringFileNames,
                                   final String diffFileName) throws IOException, ParseException;

    /**
     * Converts a {@link TestPackage} into one or more legacy administration packages and potentially a scoring package
     *
     * @param testPackagePath The path of the {@link TestPackage} to unmarshall and convert
     */
    void convertTestPackage(final String testPackagePath);
}
