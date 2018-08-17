package tds.testpackageconverter.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.junit.Before;
import tds.testpackage.diff.TestPackageDiff;
import tds.testpackage.legacy.model.Testspecification;

import java.io.IOException;

public class TestPackageBaseTest {
    protected Testspecification mockPerfAdminLegacyTestPackage;

    protected Testspecification mockCATAdminLegacyTestPackage;

    protected Testspecification mockCombinedScoringPackage;

    protected TestPackageDiff mockTestPackageDiff;

    protected XmlMapper xmlMapper;

    @Before
    public void deserializeTestPackages() throws IOException {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);
        xmlMapper = new XmlMapper(xmlModule);
        xmlMapper.registerModule(new Jdk8Module());
        xmlMapper.registerModule(new JaxbAnnotationModule());
        xmlMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final XmlMapper diffMapper = new XmlMapper();
        diffMapper.registerModule(new Jdk8Module());

        mockPerfAdminLegacyTestPackage = xmlMapper.readValue(this.getClass().getResourceAsStream("/(SBAC_PT)SBAC-IRP-Perf-MATH-11-Summer-2015-2016.xml"),
                Testspecification.class);
        mockCATAdminLegacyTestPackage = xmlMapper.readValue(this.getClass().getResourceAsStream("/(SBAC_PT)SBAC-IRP-CAT-MATH-11-Summer-2015-2016.xml"),
                Testspecification.class);
        mockCombinedScoringPackage = xmlMapper.readValue(this.getClass().getResourceAsStream("/(SBAC_PT)SBAC-IRP-MATH-11-COMBINED-Summer-2015-2016.xml"),
                Testspecification.class);
        mockTestPackageDiff = diffMapper.readValue(this.getClass().getResourceAsStream("/(SBAC_PT)SBAC-IRP-MATH-11-COMBINED-Summer-2015-2016-diff.xml"),
                TestPackageDiff.class);
    }
}
