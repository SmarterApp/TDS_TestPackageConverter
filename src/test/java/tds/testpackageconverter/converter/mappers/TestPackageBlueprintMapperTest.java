package tds.testpackageconverter.converter.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.model.*;
import tds.testpackageconverter.converter.TestPackageBaseTest;
import tds.testpackageconverter.converter.mappers.TestPackageBlueprintMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TestPackageBlueprintMapperTest extends TestPackageBaseTest {
    @Test
    public void shouldMapTestBlueprintsForAdminTestPackageOnly() {
        List<BlueprintElement> blueprintElements = TestPackageBlueprintMapper.mapBlueprint("testPackageName",
                Collections.singletonList(mockPerfAdminLegacyTestPackage));
        assertThat(blueprintElements).hasSize(4);
        BlueprintElement assessmentBlueprint = blueprintElements.get(0);
        assertThat(assessmentBlueprint.getType()).isEqualTo(BlueprintElementTypes.TEST);
        assertThat(assessmentBlueprint.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");

        assertThat(blueprintElements.stream()
                .filter(bpEl -> bpEl.getType().equalsIgnoreCase(BlueprintElementTypes.CLAIM))
                .collect(Collectors.toList())).hasSize(3);

        BlueprintElement claimBpElement = blueprintElements.get(1);
        assertThat(claimBpElement.getId()).isEqualTo("2");
        assertThat(claimBpElement.blueprintElements()).hasSize(2);
        BlueprintElement targetBpElement = claimBpElement.blueprintElements().get(0);
        assertThat(targetBpElement.getType()).isEqualTo(BlueprintElementTypes.TARGET);
        assertThat(targetBpElement.getId()).isEqualTo("2|A-CED");
        // Nested target
        assertThat(targetBpElement.blueprintElements()).hasSize(1);
        BlueprintElement nestedTargetBpElement = targetBpElement.blueprintElements().get(0);
        assertThat(nestedTargetBpElement.getType()).isEqualTo(BlueprintElementTypes.TARGET);
        assertThat(nestedTargetBpElement.getId()).isEqualTo("2|A-CED|A");
        assertThat(nestedTargetBpElement.blueprintElements()).isEmpty();
    }

    @Test
    public void shouldMapTestBlueprintsForAdminTestPackagesAndScoringPackages() {
        List<BlueprintElement> blueprintElements = TestPackageBlueprintMapper.mapBlueprint("testPackageName",
                Arrays.asList(mockPerfAdminLegacyTestPackage, mockCATAdminLegacyTestPackage, mockCombinedScoringPackage));
        assertThat(blueprintElements).hasSize(9);
        BlueprintElement packageBlueprint = blueprintElements.get(0);
        assertThat(packageBlueprint.getType()).isEqualTo(BlueprintElementTypes.PACKAGE);
        assertThat(packageBlueprint.getId()).isEqualTo("SBAC-IRP-MATH-11-COMBINED");
        assertThat(packageBlueprint.blueprintElements()).hasSize(2); // two assessments

        // Scoring specific
        assertThat(packageBlueprint.getScoring()).isPresent();
        List<PerformanceLevel> performanceLevels = packageBlueprint.getScoring().get().performanceLevels();
        PerformanceLevel performanceLevel = performanceLevels.get(0);
        assertThat(performanceLevels).hasSize(4);
        assertThat(performanceLevel.getPLevel()).isEqualTo(1);
        assertThat(performanceLevel.getScaledHi()).isEqualTo(2543.0);
        assertThat(performanceLevel.getScaledLo()).isEqualTo(2280.0);

        assertThat(packageBlueprint.getScoring().get().getRules()).hasSize(7);
        Rule scoringRule = packageBlueprint.getScoring().get().getRules().get(0);
        assertThat(scoringRule.getName()).isEqualTo("SBACCATTheta");
        assertThat(scoringRule.getComputationOrder()).isEqualTo(20);

        // Parameters
        assertThat(scoringRule.parameters()).hasSize(5);
        Parameter param = scoringRule.parameters().get(0);
        assertThat(param.getId()).isEqualTo("686D3098-28B0-40A0-AC92-EB71DD389125");
        assertThat(param.getName()).isEqualTo("LOT");
        assertThat(param.getPosition()).isEqualTo(1);
        assertThat(param.getValues()).hasSize(1);
        assertThat(param.getValues().get(0).getValue()).isEqualTo("-2.9564");

        BlueprintElement assessmentBlueprint = packageBlueprint.blueprintElements().get(0);
        assertThat(assessmentBlueprint.getType()).isEqualTo(BlueprintElementTypes.TEST);
        assertThat(assessmentBlueprint.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");

        assertThat(blueprintElements.stream()
                .filter(bpEl -> bpEl.getType().equalsIgnoreCase(BlueprintElementTypes.CLAIM))
                .collect(Collectors.toList())).hasSize(4);
        BlueprintElement affinityGroupBpElement = blueprintElements.get(1);
        assertThat(affinityGroupBpElement.getId()).isEqualTo("G11Math_Claim1_MC/MS");
        assertThat(affinityGroupBpElement.getType()).isEqualTo(BlueprintElementTypes.AFFINITY_GROUP);

        BlueprintElement claimBpElement = blueprintElements.get(5);
        assertThat(claimBpElement.getId()).isEqualTo("2");
        assertThat(claimBpElement.blueprintElements()).hasSize(4);
        BlueprintElement targetBpElement = claimBpElement.blueprintElements().get(0);
        assertThat(targetBpElement.getType()).isEqualTo(BlueprintElementTypes.TARGET);
        assertThat(targetBpElement.getId()).isEqualTo("2|A-CED");
        // Nested target
        assertThat(targetBpElement.blueprintElements()).hasSize(1);
        BlueprintElement nestedTargetBpElement = targetBpElement.blueprintElements().get(0);
        assertThat(nestedTargetBpElement.getType()).isEqualTo(BlueprintElementTypes.TARGET);
        assertThat(nestedTargetBpElement.getId()).isEqualTo("2|A-CED|A");
        assertThat(nestedTargetBpElement.blueprintElements()).isEmpty();

    }
}
