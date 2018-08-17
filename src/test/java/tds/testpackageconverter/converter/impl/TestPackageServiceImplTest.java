package tds.testpackageconverter.converter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tds.support.tool.testpackage.configuration.TestPackageObjectMapperConfiguration;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackageconverter.converter.TestPackageBaseTest;
import tds.testpackageconverter.converter.TestPackageConverterService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestPackageServiceImplTest extends TestPackageBaseTest {
    private TestPackageConverterService service;

    @Mock
    private TestPackageObjectMapperConfiguration mockConfiguration;

    @Mock
    private XmlMapper mockMapper;

    @Mock
    private ObjectWriter mockObjectWriter;

    private File mockFile;

    @Before
    public void setup() {
        when(mockConfiguration.getLegacyTestSpecXmlMapper()).thenReturn(mockMapper);
        service = new TestPackageConverterServiceImpl(mockConfiguration);
        mockFile = new File("src/test/resources/legacy-combined-testspec.zip");
    }

    @Test
    public void shouldConvertTestPackageZipSuccessfully() throws IOException, ParseException {
        final String testPackageName = "converted-test-package.xml";
        when(mockMapper.readValue(isA(InputStream.class), eq(Testspecification.class)))
                .thenReturn(mockPerfAdminLegacyTestPackage);
        service.extractAndConvertTestSpecifications(testPackageName, mockFile);
        verify(mockMapper, times(2)).readValue(isA(InputStream.class), eq(Testspecification.class));
    }

    @Test
    public void shouldConvertTestPackageFilesSuccessfully() throws IOException, ParseException {
        final String testPackageName = "converted-test-package.xml";
        final String file1 = "src/test/resources/(SBAC_PT)SBAC-IRP-CAT-MATH-11-Summer-2015-2016.xml";
        final String file2 = "src/test/resources/(SBAC_PT)SBAC-IRP-MATH-11-COMBINED-Summer-2015-2016.xml";
        final String file3 = "src/test/resources/(SBAC_PT)SBAC-IRP-Perf-MATH-11-Summer-2015-2016.xml";

        when(mockMapper.readValue(new File(file1), Testspecification.class)).thenReturn(mockCATAdminLegacyTestPackage);
        when(mockMapper.readValue(new File(file2), Testspecification.class)).thenReturn(mockCombinedScoringPackage);
        when(mockMapper.readValue(new File(file3), Testspecification.class)).thenReturn(mockPerfAdminLegacyTestPackage);

        service.convertTestSpecifications(testPackageName, Arrays.asList(file1, file2, file3));

        verify(mockMapper).readValue(new File(file1), Testspecification.class);
        verify(mockMapper).readValue(new File(file2), Testspecification.class);
        verify(mockMapper).readValue(new File(file3), Testspecification.class);
    }

    @Test(expected = IOException.class)
    public void shouldThrowIOExceptionIfInputIsNotAZip() throws IOException, ParseException {
        final String testPackageName = "converted-test-package.xml";
        File file = new File("src/test/resources/(SBAC_PT)SBAC-IRP-Perf-MATH-11-Summer-2015-2016.xml");
        service.extractAndConvertTestSpecifications(testPackageName, file);
    }

    @Test(expected = IOException.class)
    public void shouldThrowIOExceptionIfZipContainsNoXmlFiles() throws IOException, ParseException {
        final String testPackageName = "converted-test-package.xml";
        File file = new File("src/test/resources/textfile.zip");
        service.extractAndConvertTestSpecifications(testPackageName, file);
    }

    ;
}
