package tds.testpackageconverter.converter.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.Formpartition;
import tds.testpackage.legacy.model.Itemgroup;
import tds.testpackage.legacy.model.Testform;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyScoringTestPackageFormMapperTest extends LegacyTestPackageBaseTest {
    private List<Testspecification> adminTestPackages;

    @Before
    public void setup() {
        adminTestPackages = LegacyAdministrationTestPackageMapper.fromNew(mockTestPackageWithScoring);
    }

    @Test
    public void shouldMapSegmentForms() {
        List<Testform> testForms = LegacyScoringTestPackageFormMapper.mapTestForms(mockTestPackageWithScoring, adminTestPackages);
        assertThat(testForms).hasSize(2);

        Testform enuTestForm = testForms.stream()
                .filter(form -> form.getIdentifier().getUniqueid().equalsIgnoreCase("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018:Default-ENU"))
                .findFirst().get();

        assertThat(enuTestForm.getLength()).isEqualTo(48);
        assertThat(enuTestForm.getIdentifier().getName()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018:Default-ENU");
        assertThat(enuTestForm.getIdentifier().getVersion().intValue()).isEqualTo(9787);
        assertThat(enuTestForm.getFormpartition()).hasSize(3);

        Formpartition partition1 = enuTestForm.getFormpartition().get(0);
        assertThat(partition1.getIdentifier().getUniqueid()).isNotNull();
        assertThat(partition1.getIdentifier().getName()).isEqualTo("ELA ICA G7 2017 ENG::ENU COMBINED");
        assertThat(partition1.getItemgroup()).hasSize(23);

        Formpartition partition2 = enuTestForm.getFormpartition().get(1);
        assertThat(partition2.getIdentifier().getUniqueid()).isNotNull();
        assertThat(partition2.getIdentifier().getName()).isEqualTo("ELA ICA Perf G7a 2017 ENG::ENU COMBINED");
        assertThat(partition2.getItemgroup()).hasSize(1);

        Formpartition partition3 = enuTestForm.getFormpartition().get(2);
        assertThat(partition3.getIdentifier().getUniqueid()).isNotNull();
        assertThat(partition3.getIdentifier().getName()).isEqualTo("ELA ICA Perf G7b 2017 ENG::ENU COMBINED");
        assertThat(partition3.getItemgroup()).hasSize(1);

        Testform brailleTestForm = testForms.stream()
                .filter(form -> form.getIdentifier().getUniqueid().equalsIgnoreCase("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018:Default-ENU-Braille"))
                .findFirst().get();

        assertThat(brailleTestForm.getProperty()).hasSize(1);
        assertThat(brailleTestForm.getProperty().get(0).getName()).isEqualTo("Language");
        assertThat(brailleTestForm.getProperty().get(0).getValue()).isEqualTo("ENU-Braille");
        assertThat(brailleTestForm.getProperty().get(0).getLabel()).isEqualTo("Braille");

        assertThat(brailleTestForm.getLength()).isEqualTo(48);
        assertThat(brailleTestForm.getIdentifier().getName()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018:Default-ENU-Braille");
        assertThat(brailleTestForm.getIdentifier().getVersion().intValue()).isEqualTo(9787);
        assertThat(brailleTestForm.getFormpartition()).hasSize(3);

        Formpartition braillePartition1 = brailleTestForm.getFormpartition().get(0);
        assertThat(braillePartition1.getIdentifier().getUniqueid()).isNotNull();
        assertThat(braillePartition1.getIdentifier().getName()).isEqualTo("ELA ICA G7 2017 ENG::BRL COMBINED");
        assertThat(braillePartition1.getItemgroup()).hasSize(23);

        Formpartition braillePartition2 = brailleTestForm.getFormpartition().get(1);
        assertThat(braillePartition2.getIdentifier().getUniqueid()).isNotNull();
        assertThat(braillePartition2.getIdentifier().getName()).isEqualTo("ELA ICA Perf G7a 2017 ENG::BRL COMBINED");
        assertThat(braillePartition2.getItemgroup()).hasSize(1);

        Formpartition braillePartition3 = brailleTestForm.getFormpartition().get(2);
        assertThat(braillePartition3.getIdentifier().getUniqueid()).isNotNull();
        assertThat(braillePartition3.getIdentifier().getName()).isEqualTo("ELA ICA Perf G7b 2017 ENG::BRL COMBINED");
        assertThat(braillePartition3.getItemgroup()).hasSize(1);

    }
}
