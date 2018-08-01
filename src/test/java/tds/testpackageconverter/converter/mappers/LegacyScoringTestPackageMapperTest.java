package tds.testpackageconverter.converter.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.*;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyScoringTestPackageMapperTest extends LegacyTestPackageBaseTest {
    private List<Testspecification> adminTestPackages;

    @Before
    public void setup() {
        adminTestPackages = LegacyAdministrationTestPackageMapper.fromNew(mockTestPackageWithScoring);
    }

    @Test
    public void shouldMapTestWithScoringCombined() {
        Testspecification scoringPackage = LegacyScoringTestPackageMapper.fromNew(mockTestPackageWithScoring, adminTestPackages);
        assertThat(scoringPackage).isNotNull();
        assertThat(scoringPackage.getPublishdate()).contains("Jun 16 2016");
        assertThat(scoringPackage.getPurpose()).isEqualTo("scoring");
        assertThat(scoringPackage.getPublisher()).isEqualTo("SBAC");
        assertThat(scoringPackage.getIdentifier().getUniqueid()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018");
        assertThat(scoringPackage.getIdentifier().getName()).isEqualTo("SBAC-ICA-FIXED-G7E-COMBINED-2017");
        assertThat(scoringPackage.getIdentifier().getVersion()).isEqualTo("9787");

        Property subjectProp = scoringPackage.getProperty().stream()
                .filter(prop -> prop.getName().equals("subject"))
                .findFirst().get();

        assertThat(subjectProp.getValue()).isEqualTo("ELA");
        assertThat(subjectProp.getLabel()).isEqualTo("ELA");

        Property typeProp = scoringPackage.getProperty().stream()
                .filter(prop -> prop.getName().equals("type"))
                .findFirst().get();

        assertThat(typeProp.getValue()).isEqualTo("interim");
        assertThat(typeProp.getLabel()).isEqualTo("interim");

        Property gradeProp = scoringPackage.getProperty().stream()
                .filter(prop -> prop.getName().equals("grade"))
                .findFirst().get();

        assertThat(gradeProp.getValue()).isEqualTo("7");
        assertThat(gradeProp.getLabel()).isEqualTo("7");

        assertThat(scoringPackage.getScoring().getPerformancelevels().getPerformancelevel()).hasSize(4);

        Performancelevel perfLevel = scoringPackage.getScoring().getPerformancelevels().getPerformancelevel().get(0);
        assertThat(perfLevel.getBpelementid()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018");
        assertThat(perfLevel.getPlevel()).isEqualTo("1");
        assertThat(perfLevel.getScaledlo()).isEqualTo(2258f);
        assertThat(perfLevel.getScaledhi()).isEqualTo(2479f);

        assertThat(scoringPackage.getScoring().getScoringrules()).isNotNull();
        assertThat(scoringPackage.getScoring().getScoringrules().getComputationrule()).hasSize(32);

        Computationrule testLevelCompRule = scoringPackage.getScoring().getScoringrules().getComputationrule().stream()
                .filter(rule -> rule.getBpelementid().equals("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018")
                        && rule.getIdentifier().getName().equals("SBACTheta"))
                .findFirst().get();

        assertThat(testLevelCompRule.getIdentifier().getVersion()).isEqualTo("1");
        assertThat(testLevelCompRule.getIdentifier().getLabel()).isEqualTo("SBACTheta");
        assertThat(testLevelCompRule.getIdentifier().getUniqueid()).isNotNull();

        assertThat(testLevelCompRule.getComputationruleparameter()).hasSize(3);

        Computationruleparameter param = testLevelCompRule.getComputationruleparameter().get(0);
        assertThat(param.getPosition()).isEqualTo("1");
        assertThat(param.getParametertype()).isEqualTo("double");
        assertThat(param.getIdentifier().getName()).isEqualTo("LOT");
        assertThat(param.getIdentifier().getVersion()).isEqualTo("1");


        assertThat(param.getComputationruleparametervalue()).hasSize(1);
        assertThat(param.getComputationruleparametervalue().get(0).getValue()).isEqualTo("-2.9114");

        Computationrule accommodationCompRule = scoringPackage.getScoring().getScoringrules().getComputationrule().stream()
                .filter(rule -> rule.getBpelementid().equals("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018")
                        && rule.getIdentifier().getName().equals("SBACAccommodationUseCodes"))
                .findFirst().get();

        Computationruleparametervalue accomomdationParamVal = accommodationCompRule.getComputationruleparameter().get(0)
                .getComputationruleparametervalue().get(0);

        assertThat(accomomdationParamVal.getIndex()).isEqualTo("Print Size");
    }
}
