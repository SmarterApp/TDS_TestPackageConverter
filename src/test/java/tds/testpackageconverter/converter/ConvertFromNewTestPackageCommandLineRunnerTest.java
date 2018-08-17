package tds.testpackageconverter.converter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackageconverter.ConvertFromNewTestPackageCommandLineRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConvertFromNewTestPackageCommandLineRunnerTest {
    @Mock
    private TestPackageConverterService mockConverterService;

    private ConvertFromNewTestPackageCommandLineRunner runner;

    @Before
    public void setup() {
        runner = new ConvertFromNewTestPackageCommandLineRunner(mockConverterService);
        runner.init();
    }

    @Test
    public void shouldConvertTestPackageSuccessfully() throws Exception {
        final String testPackagePath = "/path/to/NewTestPackage.xml";
        runner.run("convert-from-new", testPackagePath);

        verify(mockConverterService).convertTestPackage(testPackagePath);
    }

    @Test
    public void shouldDoNothingForDifferentCommand() throws Exception {
        final String testPackagePath = "/path/to/NewTestPackage.xml";
        runner.run("convert-to-new", testPackagePath);

        verify(mockConverterService, times(0)).convertTestPackage(testPackagePath);
    }

    @Test
    public void shouldExecuteWithVerboseFlag() throws Exception {
        final String testPackagePath = "/path/to/NewTestPackage.xml";
        runner.run("convert-from-new", testPackagePath, "-v");

        verify(mockConverterService).convertTestPackage(testPackagePath);
    }
}
