package tds.testpackageconverter.converter.mappers;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.Bpelement;
import tds.testpackage.legacy.model.Testblueprint;
import tds.testpackage.model.Assessment;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyAdministrationTestPackageBlueprintMapperTest extends LegacyTestPackageBaseTest {

    @Test
    public void shouldConvertCATTestPackageBlueprintSuccessfullyForSingleAssessment() {
        Assessment assessment = mockCATAdminLegacyTestPackage.getAssessments().get(0);
        Testblueprint convertedBlueprint = LegacyAdministrationTestPackageBlueprintMapper.mapBlueprint(mockCATAdminLegacyTestPackage, assessment);
        assertThat(convertedBlueprint).isNotNull();

        assertThat(convertedBlueprint.getBpelement()).hasSize(67);

        // Get test blueprint
        Bpelement testBpEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase("test"))
                .findFirst()
                .get();

        assertThat(testBpEl.getIdentifier().getName()).isEqualTo(assessment.getId());
        assertThat(testBpEl.getIdentifier().getUniqueid()).isEqualTo(assessment.getKey());
        assertThat(testBpEl.getMaxftitems()).isEqualTo(0);
        assertThat(testBpEl.getMinftitems()).isEqualTo(0);
        assertThat(testBpEl.getMaxopitems()).isEqualTo(8);
        assertThat(testBpEl.getMinopitems()).isEqualTo(8);
        assertThat(testBpEl.getOpitemcount()).isEqualTo(18);
        assertThat(testBpEl.getFtitemcount()).isEqualTo(0);
        assertThat(testBpEl.getParentid()).isNull();

        // Get segment blueprint
        Bpelement segmentBpEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase("segment"))
                .findFirst()
                .get();

        assertThat(segmentBpEl.getIdentifier().getName()).isEqualTo(assessment.getId());
        assertThat(segmentBpEl.getIdentifier().getUniqueid()).isEqualTo(assessment.getKey());
        assertThat(segmentBpEl.getMaxftitems()).isEqualTo(0);
        assertThat(segmentBpEl.getMinftitems()).isEqualTo(0);
        assertThat(segmentBpEl.getMaxopitems()).isEqualTo(8);
        assertThat(segmentBpEl.getMinopitems()).isEqualTo(8);
        assertThat(segmentBpEl.getOpitemcount()).isEqualTo(18);
        assertThat(segmentBpEl.getFtitemcount()).isEqualTo(0);
        assertThat(segmentBpEl.getParentid()).isNull();

        //Get an affinity group
        Bpelement affinityEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getIdentifier().getUniqueid().equalsIgnoreCase("G11Math_DOK2"))
                .findFirst()
                .get();

        assertThat(affinityEl.getElementtype()).isEqualTo("affinitygroup");
        assertThat(affinityEl.getIdentifier().getName()).isEqualTo("G11Math_DOK2");
        assertThat(affinityEl.getMaxftitems()).isEqualTo(0);
        assertThat(affinityEl.getMinftitems()).isEqualTo(0);
        assertThat(affinityEl.getMaxopitems()).isEqualTo(8);
        assertThat(affinityEl.getMinopitems()).isEqualTo(2);
        assertThat(affinityEl.getOpitemcount()).isEqualTo(12);
        assertThat(affinityEl.getFtitemcount()).isEqualTo(0);
        assertThat(affinityEl.getParentid()).isNull();

        //Get a claim
        Bpelement claimEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getIdentifier().getName().equalsIgnoreCase("1"))
                .findFirst()
                .get();

        assertThat(claimEl.getElementtype()).isEqualTo("strand");
        assertThat(claimEl.getIdentifier().getUniqueid()).isEqualTo("SBAC_PT-1");
        assertThat(claimEl.getMaxftitems()).isEqualTo(0);
        assertThat(claimEl.getMinftitems()).isEqualTo(0);
        assertThat(claimEl.getMaxopitems()).isEqualTo(4);
        assertThat(claimEl.getMinopitems()).isEqualTo(4);
        assertThat(claimEl.getOpitemcount()).isEqualTo(10);
        assertThat(claimEl.getFtitemcount()).isEqualTo(0);
        assertThat(claimEl.getParentid()).isNull();

        //Get a target
        Bpelement targetEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getIdentifier().getName().equalsIgnoreCase("1|A-REI|H|m"))
                .findFirst()
                .get();

        assertThat(targetEl.getElementtype()).isEqualTo("contentlevel");
        assertThat(targetEl.getIdentifier().getUniqueid()).isEqualTo("SBAC_PT-1|A-REI|H|m");
        assertThat(targetEl.getMaxftitems()).isEqualTo(0);
        assertThat(targetEl.getMinftitems()).isEqualTo(0);
        assertThat(targetEl.getMaxopitems()).isEqualTo(0);
        assertThat(targetEl.getMinopitems()).isEqualTo(0);
        assertThat(targetEl.getOpitemcount()).isEqualTo(1);
        assertThat(targetEl.getFtitemcount()).isEqualTo(0);
        assertThat(targetEl.getParentid()).isEqualTo("SBAC_PT-1|A-REI|H");
    }

    @Test
    public void shouldMapMultiSegmentedTestSuccessfully() {
        Assessment assessment = mockFixedMultiSegmentPackage.getAssessments().get(0);
        Testblueprint convertedBlueprint = LegacyAdministrationTestPackageBlueprintMapper.mapBlueprint(mockFixedMultiSegmentPackage, assessment);
        assertThat(convertedBlueprint).isNotNull();

        assertThat(convertedBlueprint.getBpelement()).hasSize(4);

        // Get test blueprint
        Bpelement testBpEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase("test"))
                .findFirst()
                .get();

        assertThat(testBpEl.getIdentifier().getName()).isEqualTo(assessment.getId());
        assertThat(testBpEl.getIdentifier().getUniqueid()).isEqualTo(assessment.getKey());
        assertThat(testBpEl.getMaxftitems()).isEqualTo(0);
        assertThat(testBpEl.getMinftitems()).isEqualTo(0);
        assertThat(testBpEl.getMaxopitems()).isEqualTo(30);
        assertThat(testBpEl.getMinopitems()).isEqualTo(27);
        assertThat(testBpEl.getOpitemcount()).isEqualTo(0);
        assertThat(testBpEl.getFtitemcount()).isEqualTo(30);
        assertThat(testBpEl.getParentid()).isNull();

        // Get segment blueprint
        List<Bpelement> segmentBpEls = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase("segment"))
                .collect(Collectors.toList());

        Bpelement segment1BpEl = segmentBpEls.get(0);

        assertThat(segment1BpEl.getIdentifier().getName()).isEqualTo(assessment.getSegments().get(0).getId());
        assertThat(segment1BpEl.getIdentifier().getUniqueid()).isEqualTo(assessment.getSegments().get(0).getKey());
        assertThat(segment1BpEl.getMaxftitems()).isEqualTo(0);
        assertThat(segment1BpEl.getMinftitems()).isEqualTo(0);
        assertThat(segment1BpEl.getMaxopitems()).isEqualTo(8);
        assertThat(segment1BpEl.getMinopitems()).isEqualTo(8);
        assertThat(segment1BpEl.getOpitemcount()).isEqualTo(0);
        assertThat(segment1BpEl.getFtitemcount()).isEqualTo(8);
        assertThat(segment1BpEl.getParentid()).isNull();

        Bpelement segment2BpEl = segmentBpEls.get(1);

        assertThat(segment2BpEl.getIdentifier().getName()).isEqualTo(assessment.getSegments().get(1).getId());
        assertThat(segment2BpEl.getIdentifier().getUniqueid()).isEqualTo(assessment.getSegments().get(1).getKey());
        assertThat(segment2BpEl.getMaxftitems()).isEqualTo(0);
        assertThat(segment2BpEl.getMinftitems()).isEqualTo(0);
        assertThat(segment2BpEl.getMaxopitems()).isEqualTo(22);
        assertThat(segment2BpEl.getMinopitems()).isEqualTo(19);
        assertThat(segment2BpEl.getOpitemcount()).isEqualTo(0);
        assertThat(segment2BpEl.getFtitemcount()).isEqualTo(22);
        assertThat(segment2BpEl.getParentid()).isNull();
    }

    @Test
    public void shouldConvertFixedFormTestPackageBlueprintSuccessfullyForSingleAssessment() {
        Assessment assessment = mockPerfAdminLegacyTestPackage.getAssessments().get(0);
        Testblueprint convertedBlueprint = LegacyAdministrationTestPackageBlueprintMapper.mapBlueprint(mockPerfAdminLegacyTestPackage, assessment);
        assertThat(convertedBlueprint).isNotNull();

        assertThat(convertedBlueprint.getBpelement()).hasSize(13);

        // Get test blueprint
        Bpelement testBpEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase("test"))
                .findFirst()
                .get();

        assertThat(testBpEl.getIdentifier().getName()).isEqualTo(assessment.getId());
        assertThat(testBpEl.getIdentifier().getUniqueid()).isEqualTo(assessment.getKey());
        assertThat(testBpEl.getMaxftitems()).isEqualTo(0);
        assertThat(testBpEl.getMinftitems()).isEqualTo(0);
        assertThat(testBpEl.getMaxopitems()).isEqualTo(7);
        assertThat(testBpEl.getMinopitems()).isEqualTo(7);
        assertThat(testBpEl.getOpitemcount()).isEqualTo(2);
        assertThat(testBpEl.getFtitemcount()).isEqualTo(0);
        assertThat(testBpEl.getParentid()).isNull();

        // Get segment blueprint
        Bpelement segmentBpEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase("segment"))
                .findFirst()
                .get();

        assertThat(segmentBpEl.getIdentifier().getName()).isEqualTo(assessment.getId());
        assertThat(segmentBpEl.getIdentifier().getUniqueid()).isEqualTo(assessment.getKey());
        assertThat(segmentBpEl.getMaxftitems()).isEqualTo(0);
        assertThat(segmentBpEl.getMinftitems()).isEqualTo(0);
        assertThat(segmentBpEl.getMaxopitems()).isEqualTo(7);
        assertThat(segmentBpEl.getMinopitems()).isEqualTo(7);
        assertThat(segmentBpEl.getOpitemcount()).isEqualTo(2);
        assertThat(segmentBpEl.getFtitemcount()).isEqualTo(0);
        assertThat(segmentBpEl.getParentid()).isNull();

        //Get a claim
        Bpelement claimEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getIdentifier().getName().equalsIgnoreCase("2"))
                .findFirst()
                .get();

        assertThat(claimEl.getElementtype()).isEqualTo("strand");
        assertThat(claimEl.getIdentifier().getUniqueid()).isEqualTo("SBAC_PT-2");
        assertThat(claimEl.getMaxftitems()).isEqualTo(0);
        assertThat(claimEl.getMinftitems()).isEqualTo(0);
        assertThat(claimEl.getMaxopitems()).isEqualTo(7);
        assertThat(claimEl.getMinopitems()).isEqualTo(0);
        assertThat(claimEl.getOpitemcount()).isEqualTo(1);
        assertThat(claimEl.getFtitemcount()).isEqualTo(0);
        assertThat(claimEl.getParentid()).isNull();

        //Get a target
        Bpelement targetEl = convertedBlueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getIdentifier().getName().equalsIgnoreCase("2|F-IF|A"))
                .findFirst()
                .get();

        assertThat(targetEl.getElementtype()).isEqualTo("contentlevel");
        assertThat(targetEl.getIdentifier().getUniqueid()).isEqualTo("SBAC_PT-2|F-IF|A");
        assertThat(targetEl.getMaxftitems()).isEqualTo(0);
        assertThat(targetEl.getMinftitems()).isEqualTo(0);
        assertThat(targetEl.getMaxopitems()).isEqualTo(7);
        assertThat(targetEl.getMinopitems()).isEqualTo(0);
        assertThat(targetEl.getOpitemcount()).isEqualTo(1);
        assertThat(targetEl.getFtitemcount()).isEqualTo(0);
        assertThat(targetEl.getParentid()).isEqualTo("SBAC_PT-2|F-IF");
    }
}
