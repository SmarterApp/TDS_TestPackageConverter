package tds.testpackageconverter.converter.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.Property;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyAdministrationTestPackageMapperTest extends LegacyTestPackageBaseTest {
    @Test
    public void shouldMapMultipleTestAdministrationFiles() {
        List<Testspecification> testSpecifications = LegacyAdministrationTestPackageMapper.fromNew(mockCombinedAdministrationPackage);
        assertThat(testSpecifications).hasSize(2);

        Testspecification perfTestSpec = testSpecifications.get(0);

        assertThat(perfTestSpec.getPurpose()).isEqualTo("administration");
        assertThat(perfTestSpec.getPublishdate()).isEqualTo("Aug 19 2015 10:44PM");
        assertThat(perfTestSpec.getIdentifier().getName()).isEqualTo("SBAC-IRP-Perf-MATH-11");
        assertThat(perfTestSpec.getIdentifier().getUniqueid()).isEqualTo("(SBAC_PT)SBAC-IRP-Perf-MATH-11-2018");
        assertThat(perfTestSpec.getIdentifier().getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(perfTestSpec.getPublisher()).isEqualTo("SBAC_PT");

        Property subjectProp = perfTestSpec.getProperty().stream()
                .filter(prop -> prop.getName().equals("subject"))
                .findFirst().get();

        assertThat(subjectProp.getValue()).isEqualTo("MATH");
        assertThat(subjectProp.getLabel()).isEqualTo("MATH");

        Property typeProp = perfTestSpec.getProperty().stream()
                .filter(prop -> prop.getName().equals("type"))
                .findFirst().get();

        assertThat(typeProp.getValue()).isEqualTo("summative");
        assertThat(typeProp.getLabel()).isEqualTo("summative");

        Property gradeProp = perfTestSpec.getProperty().stream()
                .filter(prop -> prop.getName().equals("grade"))
                .findFirst().get();

        assertThat(gradeProp.getValue()).isEqualTo("11");
        assertThat(gradeProp.getLabel()).isEqualTo("11");

        Testspecification catTestSpec = testSpecifications.get(1);

        assertThat(catTestSpec.getPurpose()).isEqualTo("administration");
        assertThat(catTestSpec.getPublishdate()).isEqualTo("Aug 19 2015 10:44PM");
        assertThat(catTestSpec.getIdentifier().getName()).isEqualTo("SBAC-IRP-CAT-MATH-11");
        assertThat(catTestSpec.getIdentifier().getUniqueid()).isEqualTo("(SBAC_PT)SBAC-IRP-CAT-MATH-11-2018");
        assertThat(catTestSpec.getIdentifier().getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(catTestSpec.getPublisher()).isEqualTo("SBAC_PT");

        Property subjectProp2 = catTestSpec.getProperty().stream()
                .filter(prop -> prop.getName().equals("subject"))
                .findFirst().get();

        assertThat(subjectProp2.getValue()).isEqualTo("MATH");
        assertThat(subjectProp2.getLabel()).isEqualTo("MATH");

        Property typeProp2 = catTestSpec.getProperty().stream()
                .filter(prop -> prop.getName().equals("type"))
                .findFirst().get();

        assertThat(typeProp2.getValue()).isEqualTo("summative");
        assertThat(typeProp2.getLabel()).isEqualTo("summative");

        Property gradeProp2 = catTestSpec.getProperty().stream()
                .filter(prop -> prop.getName().equals("grade"))
                .findFirst().get();

        assertThat(gradeProp2.getValue()).isEqualTo("11");
        assertThat(gradeProp2.getLabel()).isEqualTo("11");
    }

}
