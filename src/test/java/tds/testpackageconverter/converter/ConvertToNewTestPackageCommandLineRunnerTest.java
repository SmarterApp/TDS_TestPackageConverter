package tds.testpackageconverter.converter;

import org.apache.commons.cli.MissingOptionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackageconverter.ConvertToNewTestPackageCommandLineRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToNewTestPackageCommandLineRunnerTest {
    @Mock
    private TestPackageConverterService mockConverterService;

    private ConvertToNewTestPackageCommandLineRunner runner;

    @Before
    public void setup() {
        runner = new ConvertToNewTestPackageCommandLineRunner(mockConverterService);
        runner.init();
    }

    @Test
    public void shouldConvertTestPackageSuccessfully() throws Exception {
        runner.run("convert-to-new",
                "-a", "test-package-admin1.xml", "test-package-admin2.xml",
                "-s", "test-package-scoring.xml",
                "-d", "test-package-diff.xml",
                "converted-test-package.xml");

        verify(mockConverterService, times(0)).extractAndConvertTestSpecifications(eq("converted-test-package.xml"), isA(File.class));
        verify(mockConverterService).convertTestSpecifications(eq("converted-test-package.xml"),
                eq(Arrays.asList("test-package-admin1.xml", "test-package-admin2.xml", "test-package-scoring.xml")), eq("test-package-diff.xml"));
    }

    @Test
    public void shouldConvertTestPackageSuccessfullyFilenameFirst() throws Exception {
        runner.run("convert-to-new",
                "converted-test-package.xml",
                "-a", "test-package-admin1.xml", "test-package-admin2.xml",
                "-s", "test-package-scoring.xml",
                "-d", "test-package-diff.xml");

        verify(mockConverterService, times(0)).extractAndConvertTestSpecifications(eq("converted-test-package.xml"), isA(File.class));
        verify(mockConverterService).convertTestSpecifications(eq("converted-test-package.xml"),
                eq(Arrays.asList("test-package-admin1.xml", "test-package-admin2.xml", "test-package-scoring.xml")), eq("test-package-diff.xml"));
    }

    @Test
    public void shouldConvertTestPackageSuccessfullyVerbose() throws Exception {
        runner.run("convert-to-new",
                "converted-test-package.xml",
                "--administration", "test-package-admin1.xml", "test-package-admin2.xml",
                "--scoring", "test-package-scoring.xml",
                "--diff", "test-package-diff.xml");

        verify(mockConverterService, times(0)).extractAndConvertTestSpecifications(eq("converted-test-package.xml"), isA(File.class));
        verify(mockConverterService).convertTestSpecifications(eq("converted-test-package.xml"),
                eq(Arrays.asList("test-package-admin1.xml", "test-package-admin2.xml", "test-package-scoring.xml")), eq("test-package-diff.xml"));
    }

    @Test
    public void shouldConvertTestPackageSuccessfullyNoDiff() throws Exception {
        runner.run("convert-to-new",
                "-a", "test-package-admin1.xml", "test-package-admin2.xml",
                "-s", "test-package-scoring.xml",
                "converted-test-package.xml");

        verify(mockConverterService, times(0)).extractAndConvertTestSpecifications(eq("converted-test-package.xml"), isA(File.class));
        verify(mockConverterService).convertTestSpecifications(eq("converted-test-package.xml"),
                eq(Arrays.asList("test-package-admin1.xml", "test-package-admin2.xml", "test-package-scoring.xml")));
    }

    @Test
    public void shouldConvertTestPackageSuccessfullyForZip() throws Exception {

        runner.run("convert-to-new",
                "-z", "test-package.zip",
                "converted-test-package.xml");
        verify(mockConverterService).extractAndConvertTestSpecifications(eq("converted-test-package.xml"), isA(File.class));
    }

    @Test
    public void shouldConvertTestPackageSuccessfullyForZipVerbose() throws Exception {

        runner.run("convert-to-new",
                "--zip", "test-package.zip",
                "converted-test-package.xml");
        verify(mockConverterService).extractAndConvertTestSpecifications(eq("converted-test-package.xml"), isA(File.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForZipWithOtherInputs() throws Exception {
        runner.run("convert-to-new",
                "-a", "test-package-admin1.xml", "test-package-admin1.xml",
                "-s", "test-package-scoring.xml",
                "-d", "test-package-diff.xml",
                "-z", "test-package.zip",
                "converted-test-package.xml");
    }

    @Test(expected = MissingOptionException.class)
    public void shouldThrowForNoAdminPackage() throws Exception {
        runner.run("convert-to-new",
                "-s", "input1", "input2",
                "converted-test-package.xml");
    }

    @Test
    public void shouldPrintHelpForScoringPackageWithMultipleInputs() throws Exception {
        runner.run("convert-to-new",
                "converted-test-package.xml",
                "-a", "admin1",
                "-s", "input1", "input2");
    }
}
