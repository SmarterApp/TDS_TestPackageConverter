package tds.testpackageconverter.converter.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.common.Algorithm;
import tds.testpackage.model.*;
import tds.testpackageconverter.converter.TestPackageBaseTest;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TestPackageMapperTest extends TestPackageBaseTest {
    @Test
    public void shouldParseCohort() {
        // Default-ENU-Braille
        String cohort = TestPackageUtils.parseCohort("(SBAC_PT)SBAC-Perf-MATH-7-Fall-2017-2018:Default-ENU-Braille-1");
        assertThat(cohort).isEqualToIgnoringCase("Default");
    }

    @Test
    public void shouldConvertFixedFormTestPackage() throws ParseException {
        assertThat(mockPerfAdminLegacyTestPackage).isNotNull();
        TestPackage testPackage = TestPackageMapper.toNew("(SBAC_PT)SBAC-IRP-Perf-MATH-11-Summer-2015-2016",
                Collections.singletonList(mockPerfAdminLegacyTestPackage));

        assertThat(testPackage).isNotNull();
        assertThat(testPackage.getPublisher()).isEqualTo("SBAC_PT");
        assertThat(testPackage.getPublishDate()).isNotNull();
        assertThat(testPackage.getSubject()).isEqualTo("MATH");
        assertThat(testPackage.getType()).isEqualTo("summative");
        assertThat(testPackage.getVersion()).isEqualTo("8185");
        assertThat(testPackage.getBankKey()).isEqualTo(187);
        assertThat(testPackage.getAcademicYear()).isNotNull();
        // Assessments
        assertThat(testPackage.getAssessments()).hasSize(1);
        Assessment assessment = testPackage.getAssessments().get(0);
        assertThat(assessment.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");
        assertThat(assessment.getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(assessment.getGrades()).hasSize(1);
        assertThat(assessment.getGrades().get(0).getValue()).isEqualTo("11");
        assertThat(assessment.getGrades().get(0).getLabel().get()).isEqualTo("grade 11");

        // Segment
        assertThat(assessment.getSegments()).hasSize(1);
        Segment segment = assessment.getSegments().get(0);
        assertThat(segment.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");
        assertThat(segment.entryApproval()).isFalse();
        assertThat(segment.exitApproval()).isFalse();
        assertThat(segment.getAlgorithmType()).isEqualTo(Algorithm.FIXED_FORM.getType());
        assertThat(segment.getAlgorithmImplementation()).isEqualTo("AIR FIXEDFORM1");
        assertThat(segment.segmentBlueprint()).hasSize(12);
        assertThat(segment.pool()).isEmpty();
        // Forms
        assertThat(segment.segmentForms()).hasSize(1);
        SegmentForm segmentForm = segment.segmentForms().get(0);
        assertThat(segmentForm.getId()).isEqualTo("IRP::MathG11::Perf::SP15");
        assertThat(segmentForm.getCohort()).isEqualTo("Default");
        assertThat(segmentForm.getPresentations()).hasSize(1);
        assertThat(segmentForm.getPresentations().get(0).getCode()).isEqualTo("ENU");
        // Item groups
        assertThat(segmentForm.itemGroups()).hasSize(1);
        ItemGroup itemGroup = segmentForm.itemGroups().get(0);
        assertThat(itemGroup.getId()).isEqualTo("3688");
        assertThat(itemGroup.position()).isEqualTo(1);
        assertThat(itemGroup.maxItems()).isEqualTo("ALL");
        assertThat(itemGroup.maxResponses()).isEqualTo("ALL");
        // Items
        assertThat(itemGroup.items()).hasSize(2);
        Item item = itemGroup.items().get(0);
        assertThat(item.getId()).isEqualTo("1434");
        assertThat(item.fieldTest()).isFalse();
        assertThat(item.getBlueprintReferences()).hasSize(4);
        assertThat(item.getPresentations()).hasSize(1);
        assertThat(item.getPresentations().get(0).getCode()).isEqualTo("ENU");
        assertThat(item.getType()).isEqualTo("GI");
        assertThat(item.administrationRequired()).isFalse();
        assertThat(item.active()).isTrue();
        assertThat(item.handScored()).isTrue();
        assertThat(item.doNotScore()).isFalse();

        // Item Score Dimension
        assertThat(item.getItemScoreDimensions()).hasSize(1);
        assertThat(item.getItemScoreDimensions().get(0)).isNotNull();
        assertThat(item.getItemScoreDimensions().get(0).getDimension()).isNotPresent();
        assertThat(item.getItemScoreDimensions().get(0).getMeasurementModel()).isEqualTo("IRT3PLn");
        assertThat(item.getItemScoreDimensions().get(0).getScorePoints()).isEqualTo(1);
        assertThat(item.getItemScoreDimensions().get(0).getWeight()).isEqualTo(1.0);
        // Item Score Parameter
        assertThat(item.getItemScoreDimensions().get(0).itemScoreParameters()).hasSize(3);
        ItemScoreParameter parameter = item.getItemScoreDimensions().get(0).itemScoreParameters().get(0);
        assertThat(parameter.getMeasurementParameter()).isEqualTo("a");
        assertThat(parameter.getValue()).isEqualTo(0.6389300227165222);
    }

    @Test
    public void shouldConvertCATTestPackage() throws ParseException {
        assertThat(mockPerfAdminLegacyTestPackage).isNotNull();
        TestPackage testPackage = TestPackageMapper.toNew("(SBAC_PT)SBAC-IRP-CAT-MATH-11-Summer-2015-2016",
                Collections.singletonList(mockCATAdminLegacyTestPackage));

        assertThat(testPackage).isNotNull();
        assertThat(testPackage.getPublisher()).isEqualTo("SBAC_PT");
        assertThat(testPackage.getPublishDate()).isNotNull();
        assertThat(testPackage.getSubject()).isEqualTo("MATH");
        assertThat(testPackage.getType()).isEqualTo("summative");
        assertThat(testPackage.getVersion()).isEqualTo("8185");
        assertThat(testPackage.getBankKey()).isEqualTo(187);
        assertThat(testPackage.getAcademicYear()).isNotNull();
        // Assessments
        assertThat(testPackage.getAssessments()).hasSize(1);
        Assessment assessment = testPackage.getAssessments().get(0);
        assertThat(assessment.getId()).isEqualTo("SBAC-IRP-CAT-MATH-11");
        assertThat(assessment.getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(assessment.getGrades()).hasSize(1);
        assertThat(assessment.getGrades().get(0).getValue()).isEqualTo("11");
        assertThat(assessment.getGrades().get(0).getLabel().get()).isEqualTo("grade 11");

        // Segment
        assertThat(assessment.getSegments()).hasSize(1);
        Segment segment = assessment.getSegments().get(0);
        assertThat(segment.getId()).isEqualTo("SBAC-IRP-CAT-MATH-11");
        assertThat(segment.entryApproval()).isFalse();
        assertThat(segment.exitApproval()).isFalse();
        assertThat(segment.getAlgorithmType()).isEqualTo("adaptive");
        assertThat(segment.getAlgorithmImplementation()).isEqualTo("AIR ADAPTIVE1");
        assertThat(segment.segmentBlueprint()).hasSize(66);
        assertThat(segment.segmentForms()).isEmpty();
        assertThat(segment.pool()).hasSize(18);
        // Item groups
        ItemGroup itemGroup = segment.pool().get(0);
        assertThat(itemGroup.getId()).isEqualTo("1899");
        assertThat(itemGroup.position()).isEqualTo(1);
        assertThat(itemGroup.maxItems()).isEqualTo("ALL");
        assertThat(itemGroup.maxResponses()).isEqualTo("ALL");
        // Items
        assertThat(itemGroup.items()).hasSize(1);
        Item item = itemGroup.items().get(0);
        assertThat(item.getId()).isEqualTo("1899");
        assertThat(item.fieldTest()).isFalse();
        assertThat(item.getBlueprintReferences()).hasSize(7);
        assertThat(item.getPresentations()).hasSize(1);
        assertThat(item.getPresentations().get(0).getCode()).isEqualTo("ENU");
        assertThat(item.getType()).isEqualTo("EQ");
        assertThat(item.administrationRequired()).isTrue();
        assertThat(item.active()).isTrue();
        assertThat(item.handScored()).isFalse();
        assertThat(item.doNotScore()).isFalse();
        // Item Score Dimension
        assertThat(item.getItemScoreDimensions()).hasSize(1);
        assertThat(item.getItemScoreDimensions().get(0)).isNotNull();
        assertThat(item.getItemScoreDimensions().get(0).getDimension()).isNotPresent();
        assertThat(item.getItemScoreDimensions().get(0).getMeasurementModel()).isEqualTo("IRT3PLn");
        assertThat(item.getItemScoreDimensions().get(0).getScorePoints()).isEqualTo(1);
        assertThat(item.getItemScoreDimensions().get(0).getWeight()).isEqualTo(1.0);
        // Item Score Parameter
        assertThat(item.getItemScoreDimensions().get(0).itemScoreParameters()).hasSize(3);
        ItemScoreParameter parameter = item.getItemScoreDimensions().get(0).itemScoreParameters().get(0);
        assertThat(parameter.getMeasurementParameter()).isEqualTo("a");
        assertThat(parameter.getValue()).isEqualTo(1.0);
    }

    @Test
    public void shouldConvertCombinedTestPackage() throws ParseException {
        assertThat(mockPerfAdminLegacyTestPackage).isNotNull();
        TestPackage testPackage = TestPackageMapper.toNew("(SBAC_PT)SBAC-IRP-COMBINED-MATH-11-Summer-2015-2016",
                Arrays.asList(mockPerfAdminLegacyTestPackage, mockCATAdminLegacyTestPackage));

        assertThat(testPackage).isNotNull();
        assertThat(testPackage.getPublisher()).isEqualTo("SBAC_PT");
        assertThat(testPackage.getPublishDate()).isNotNull();
        assertThat(testPackage.getSubject()).isEqualTo("MATH");
        assertThat(testPackage.getType()).isEqualTo("summative");
        assertThat(testPackage.getVersion()).isEqualTo("8185");
        assertThat(testPackage.getBankKey()).isEqualTo(187);
        assertThat(testPackage.getAcademicYear()).isNotNull();

        // Fixed Form Assessment
        assertThat(testPackage.getAssessments()).hasSize(2);
        Assessment catAssessment = testPackage.getAssessments().get(0);
        assertThat(catAssessment.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");
        assertThat(catAssessment.getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(catAssessment.getGrades()).hasSize(1);
        assertThat(catAssessment.getGrades().get(0).getValue()).isEqualTo("11");
        assertThat(catAssessment.getGrades().get(0).getLabel().get()).isEqualTo("grade 11");

        // Segment
        assertThat(catAssessment.getSegments()).hasSize(1);
        Segment catSegment = catAssessment.getSegments().get(0);
        assertThat(catSegment.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");
        assertThat(catSegment.entryApproval()).isFalse();
        assertThat(catSegment.exitApproval()).isFalse();
        assertThat(catSegment.getAlgorithmType()).isEqualTo(Algorithm.FIXED_FORM.getType());
        assertThat(catSegment.getAlgorithmImplementation()).isEqualTo("AIR FIXEDFORM1");
        assertThat(catSegment.segmentBlueprint()).hasSize(12);
        assertThat(catSegment.pool()).isEmpty();
        // Forms
        assertThat(catSegment.segmentForms()).hasSize(1);
        SegmentForm catSegmentForm = catSegment.segmentForms().get(0);
        assertThat(catSegmentForm.getId()).isEqualTo("IRP::MathG11::Perf::SP15");
        assertThat(catSegmentForm.getCohort()).isEqualTo("Default");
        assertThat(catSegmentForm.getPresentations()).hasSize(1);
        assertThat(catSegmentForm.getPresentations().get(0).getCode()).isEqualTo("ENU");
        // Item groups
        assertThat(catSegmentForm.itemGroups()).hasSize(1);
        ItemGroup catItemGroup = catSegmentForm.itemGroups().get(0);
        assertThat(catItemGroup.getId()).isEqualTo("3688");
        assertThat(catItemGroup.position()).isEqualTo(1);
        assertThat(catItemGroup.maxItems()).isEqualTo("ALL");
        assertThat(catItemGroup.maxResponses()).isEqualTo("ALL");
        // Items
        assertThat(catItemGroup.items()).hasSize(2);
        Item catItem = catItemGroup.items().get(0);
        assertThat(catItem.getId()).isEqualTo("1434");
        assertThat(catItem.fieldTest()).isFalse();
        assertThat(catItem.getBlueprintReferences()).hasSize(4);
        assertThat(catItem.getPresentations()).hasSize(1);
        assertThat(catItem.getPresentations().get(0).getCode()).isEqualTo("ENU");
        assertThat(catItem.getType()).isEqualTo("GI");
        assertThat(catItem.administrationRequired()).isFalse();
        assertThat(catItem.active()).isTrue();
        assertThat(catItem.handScored()).isTrue();
        assertThat(catItem.doNotScore()).isFalse();
        // Item Score Dimension
        assertThat(catItem.getItemScoreDimensions()).hasSize(1);
        assertThat(catItem.getItemScoreDimensions().get(0)).isNotNull();
        assertThat(catItem.getItemScoreDimensions().get(0).getDimension()).isNotPresent();
        assertThat(catItem.getItemScoreDimensions().get(0).getMeasurementModel()).isEqualTo("IRT3PLn");
        assertThat(catItem.getItemScoreDimensions().get(0).getScorePoints()).isEqualTo(1);
        assertThat(catItem.getItemScoreDimensions().get(0).getWeight()).isEqualTo(1.0);
        // Item Score Parameter
        assertThat(catItem.getItemScoreDimensions().get(0).itemScoreParameters()).hasSize(3);
        ItemScoreParameter catParameter = catItem.getItemScoreDimensions().get(0).itemScoreParameters().get(0);
        assertThat(catParameter.getMeasurementParameter()).isEqualTo("a");
        assertThat(catParameter.getValue()).isEqualTo(0.6389300227165222);

        Assessment fixedFormAssessment = testPackage.getAssessments().get(1);
        assertThat(fixedFormAssessment.getId()).isEqualTo("SBAC-IRP-CAT-MATH-11");
        assertThat(fixedFormAssessment.getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(fixedFormAssessment.getGrades()).hasSize(1);
        assertThat(fixedFormAssessment.getGrades().get(0).getValue()).isEqualTo("11");
        assertThat(fixedFormAssessment.getGrades().get(0).getLabel().get()).isEqualTo("grade 11");

        // Segment
        assertThat(fixedFormAssessment.getSegments()).hasSize(1);
        Segment fixedFormSegment = fixedFormAssessment.getSegments().get(0);
        assertThat(fixedFormSegment.getId()).isEqualTo("SBAC-IRP-CAT-MATH-11");
        assertThat(fixedFormSegment.entryApproval()).isFalse();
        assertThat(fixedFormSegment.exitApproval()).isFalse();
        assertThat(fixedFormSegment.getAlgorithmType()).isEqualTo("adaptive");
        assertThat(fixedFormSegment.getAlgorithmImplementation()).isEqualTo("AIR ADAPTIVE1");
        assertThat(fixedFormSegment.segmentBlueprint()).hasSize(66);
        assertThat(fixedFormSegment.segmentForms()).isEmpty();
        assertThat(fixedFormSegment.pool()).hasSize(18);
        // Item groups
        ItemGroup fixedFormItemGroup = fixedFormSegment.pool().get(0);
        assertThat(fixedFormItemGroup.getId()).isEqualTo("1899");
        assertThat(fixedFormItemGroup.position()).isEqualTo(1);
        assertThat(fixedFormItemGroup.maxItems()).isEqualTo("ALL");
        assertThat(fixedFormItemGroup.maxResponses()).isEqualTo("ALL");
        // Items
        assertThat(fixedFormItemGroup.items()).hasSize(1);
        Item fixedFormItem = fixedFormItemGroup.items().get(0);
        assertThat(fixedFormItem.getId()).isEqualTo("1899");
        assertThat(fixedFormItem.fieldTest()).isFalse();
        assertThat(fixedFormItem.getBlueprintReferences()).hasSize(7);
        assertThat(fixedFormItem.getPresentations()).hasSize(1);
        assertThat(fixedFormItem.getPresentations().get(0).getCode()).isEqualTo("ENU");
        assertThat(fixedFormItem.getType()).isEqualTo("EQ");
        assertThat(fixedFormItem.administrationRequired()).isTrue();
        assertThat(fixedFormItem.active()).isTrue();
        assertThat(fixedFormItem.handScored()).isFalse();
        assertThat(fixedFormItem.doNotScore()).isFalse();
        assertThat(fixedFormItem.poolProperties()).hasSize(1);
        assertThat(fixedFormItem.poolProperties().get(0).getName()).isEqualTo("Depth of Knowledge");
        assertThat(fixedFormItem.poolProperties().get(0).getValue()).isEqualTo("3");
        // Item Score Dimension
        assertThat(fixedFormItem.getItemScoreDimensions()).hasSize(1);
        assertThat(fixedFormItem.getItemScoreDimensions().get(0)).isNotNull();
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getDimension()).isNotPresent();
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getMeasurementModel()).isEqualTo("IRT3PLn");
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getScorePoints()).isEqualTo(1);
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getWeight()).isEqualTo(1.0);
        // Item Score Parameter
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).itemScoreParameters()).hasSize(3);
        ItemScoreParameter fixedFormParameter = fixedFormItem.getItemScoreDimensions().get(0).itemScoreParameters().get(0);
        assertThat(fixedFormParameter.getMeasurementParameter()).isEqualTo("a");
        assertThat(fixedFormParameter.getValue()).isEqualTo(1.0);
    }

    @Test
    public void shouldConvertCombinedTestPackageWithScoring() throws ParseException {
        assertThat(mockPerfAdminLegacyTestPackage).isNotNull();
        TestPackage testPackage = TestPackageMapper.toNew("(SBAC_PT)SBAC-IRP-MATH-11-COMBINED-Summer-2015-2016",
                Arrays.asList(mockPerfAdminLegacyTestPackage, mockCATAdminLegacyTestPackage, mockCombinedScoringPackage));

        assertThat(testPackage).isNotNull();
        assertThat(testPackage.getPublisher()).isEqualTo("SBAC_PT");
        assertThat(testPackage.getPublishDate()).isNotNull();
        assertThat(testPackage.getSubject()).isEqualTo("MATH");
        assertThat(testPackage.getType()).isEqualTo("summative");
        assertThat(testPackage.getVersion()).isEqualTo("8185");
        assertThat(testPackage.getBankKey()).isEqualTo(187);
        assertThat(testPackage.getAcademicYear()).isNotNull();

        // Scoring blueprint data for combined
        assertThat(testPackage.getBlueprint().get(0).getScoring()).isPresent();

        // Fixed Form Assessment
        assertThat(testPackage.getAssessments()).hasSize(2);
        Assessment catAssessment = testPackage.getAssessments().get(0);
        assertThat(catAssessment.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");
        assertThat(catAssessment.getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(catAssessment.getGrades()).hasSize(1);
        assertThat(catAssessment.getGrades().get(0).getValue()).isEqualTo("11");
        assertThat(catAssessment.getGrades().get(0).getLabel().get()).isEqualTo("grade 11");

        // Segment
        assertThat(catAssessment.getSegments()).hasSize(1);
        Segment catSegment = catAssessment.getSegments().get(0);
        assertThat(catSegment.getId()).isEqualTo("SBAC-IRP-Perf-MATH-11");
        assertThat(catSegment.entryApproval()).isFalse();
        assertThat(catSegment.exitApproval()).isFalse();
        assertThat(catSegment.getAlgorithmType()).isEqualTo(Algorithm.FIXED_FORM.getType());
        assertThat(catSegment.getAlgorithmImplementation()).isEqualTo("AIR FIXEDFORM1");
        assertThat(catSegment.segmentBlueprint()).hasSize(12);
        assertThat(catSegment.pool()).isEmpty();
        // Forms
        assertThat(catSegment.segmentForms()).hasSize(1);
        SegmentForm catSegmentForm = catSegment.segmentForms().get(0);
        assertThat(catSegmentForm.getId()).isEqualTo("IRP::MathG11::Perf::SP15");
        assertThat(catSegmentForm.getCohort()).isEqualTo("Default");
        assertThat(catSegmentForm.getPresentations()).hasSize(1);
        assertThat(catSegmentForm.getPresentations().get(0).getCode()).isEqualTo("ENU");
        // Item groups
        assertThat(catSegmentForm.itemGroups()).hasSize(1);
        ItemGroup catItemGroup = catSegmentForm.itemGroups().get(0);
        assertThat(catItemGroup.getId()).isEqualTo("3688");
        assertThat(catItemGroup.position()).isEqualTo(1);
        assertThat(catItemGroup.maxItems()).isEqualTo("ALL");
        assertThat(catItemGroup.maxResponses()).isEqualTo("ALL");
        // Items
        assertThat(catItemGroup.items()).hasSize(2);
        Item catItem = catItemGroup.items().get(0);
        assertThat(catItem.getId()).isEqualTo("1434");
        assertThat(catItem.fieldTest()).isFalse();
        assertThat(catItem.getBlueprintReferences()).hasSize(4);
        assertThat(catItem.getPresentations()).hasSize(1);
        assertThat(catItem.getPresentations().get(0).getCode()).isEqualTo("ENU");
        assertThat(catItem.getType()).isEqualTo("GI");
        assertThat(catItem.administrationRequired()).isFalse();
        assertThat(catItem.active()).isTrue();
        assertThat(catItem.handScored()).isTrue();
        assertThat(catItem.doNotScore()).isFalse();
        // Item Score Dimension
        assertThat(catItem.getItemScoreDimensions()).hasSize(1);
        assertThat(catItem.getItemScoreDimensions().get(0)).isNotNull();
        assertThat(catItem.getItemScoreDimensions().get(0).getDimension()).isNotPresent();
        assertThat(catItem.getItemScoreDimensions().get(0).getMeasurementModel()).isEqualTo("IRT3PLn");
        assertThat(catItem.getItemScoreDimensions().get(0).getScorePoints()).isEqualTo(1);
        assertThat(catItem.getItemScoreDimensions().get(0).getWeight()).isEqualTo(1.0);
        // Item Score Parameter
        assertThat(catItem.getItemScoreDimensions().get(0).itemScoreParameters()).hasSize(3);
        ItemScoreParameter catParameter = catItem.getItemScoreDimensions().get(0).itemScoreParameters().get(0);
        assertThat(catParameter.getMeasurementParameter()).isEqualTo("a");
        assertThat(catParameter.getValue()).isEqualTo(0.6389300227165222);

        Assessment fixedFormAssessment = testPackage.getAssessments().get(1);
        assertThat(fixedFormAssessment.getId()).isEqualTo("SBAC-IRP-CAT-MATH-11");
        assertThat(fixedFormAssessment.getLabel()).isEqualTo("Grade 11 MATH");
        assertThat(fixedFormAssessment.getGrades()).hasSize(1);
        assertThat(fixedFormAssessment.getGrades().get(0).getValue()).isEqualTo("11");
        assertThat(fixedFormAssessment.getGrades().get(0).getLabel().get()).isEqualTo("grade 11");

        // Segment
        assertThat(fixedFormAssessment.getSegments()).hasSize(1);
        Segment fixedFormSegment = fixedFormAssessment.getSegments().get(0);
        assertThat(fixedFormSegment.getId()).isEqualTo("SBAC-IRP-CAT-MATH-11");
        assertThat(fixedFormSegment.entryApproval()).isFalse();
        assertThat(fixedFormSegment.exitApproval()).isFalse();
        assertThat(fixedFormSegment.getAlgorithmType()).isEqualTo("adaptive");
        assertThat(fixedFormSegment.getAlgorithmImplementation()).isEqualTo("AIR ADAPTIVE1");
        assertThat(fixedFormSegment.segmentBlueprint()).hasSize(66);
        assertThat(fixedFormSegment.segmentForms()).isEmpty();
        assertThat(fixedFormSegment.pool()).hasSize(18);
        // Item groups
        ItemGroup fixedFormItemGroup = fixedFormSegment.pool().get(0);
        assertThat(fixedFormItemGroup.getId()).isEqualTo("1899");
        assertThat(fixedFormItemGroup.position()).isEqualTo(1);
        assertThat(fixedFormItemGroup.maxItems()).isEqualTo("ALL");
        assertThat(fixedFormItemGroup.maxResponses()).isEqualTo("ALL");
        // Items
        assertThat(fixedFormItemGroup.items()).hasSize(1);
        Item fixedFormItem = fixedFormItemGroup.items().get(0);
        assertThat(fixedFormItem.getId()).isEqualTo("1899");
        assertThat(fixedFormItem.fieldTest()).isFalse();
        assertThat(fixedFormItem.getBlueprintReferences()).hasSize(7);
        assertThat(fixedFormItem.getPresentations()).hasSize(1);
        assertThat(fixedFormItem.getPresentations().get(0).getCode()).isEqualTo("ENU");
        assertThat(fixedFormItem.getType()).isEqualTo("EQ");
        assertThat(fixedFormItem.administrationRequired()).isTrue();
        assertThat(fixedFormItem.active()).isTrue();
        assertThat(fixedFormItem.handScored()).isFalse();
        assertThat(fixedFormItem.doNotScore()).isFalse();
        // Item Score Dimension
        assertThat(fixedFormItem.getItemScoreDimensions()).hasSize(1);
        assertThat(fixedFormItem.getItemScoreDimensions().get(0)).isNotNull();
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getDimension()).isNotPresent();
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getMeasurementModel()).isEqualTo("IRT3PLn");
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getScorePoints()).isEqualTo(1);
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).getWeight()).isEqualTo(1.0);
        // Item Score Parameter
        assertThat(fixedFormItem.getItemScoreDimensions().get(0).itemScoreParameters()).hasSize(3);
        ItemScoreParameter fixedFormParameter = fixedFormItem.getItemScoreDimensions().get(0).itemScoreParameters().get(0);
        assertThat(fixedFormParameter.getMeasurementParameter()).isEqualTo("a");
        assertThat(fixedFormParameter.getValue()).isEqualTo(1.0);
    }

    @Test
    public void shouldConvertTestPackagesWithDiff() throws ParseException {
        assertThat(mockPerfAdminLegacyTestPackage).isNotNull();
        TestPackage testPackage = TestPackageMapper.toNew("(SBAC_PT)SBAC-IRP-MATH-11-COMBINED-Summer-2015-2016",
                Arrays.asList(mockPerfAdminLegacyTestPackage, mockCATAdminLegacyTestPackage), mockTestPackageDiff);
        assertThat(testPackage).isNotNull();
        assertThat(testPackage.getAcademicYear()).isEqualTo("2015-2016");
        assertThat(testPackage.getAssessments().get(0).getSegments().get(0).getLabel().get()).isEqualTo("SBAC IRP Performance Segment 1");
        Item item1434 = testPackage.getAssessments().get(0).getSegments().get(0).segmentForms().get(0).itemGroups().get(0).items().get(0);
        Item item1432 = testPackage.getAssessments().get(0).getSegments().get(0).segmentForms().get(0).itemGroups().get(0).items().get(1);

        assertThat(item1434.getId()).isEqualTo("1434");
        assertThat(item1434.doNotScore()).isFalse();
        assertThat(item1434.getTeacherHandScoring()).isPresent();
        assertThat(item1434.getTeacherHandScoring().get().getExemplar().get()).isEqualTo("G3_2703_TM.pdf");
        assertThat(item1434.getTeacherHandScoring().get().getTrainingGuide().get()).isEqualTo("G3_2703_SG.pdf");
        assertThat(item1434.getTeacherHandScoring().get().layout()).isEqualTo("WAI");
        assertThat(item1434.getTeacherHandScoring().get().getDescription()).isEqualTo("Mandatory Financial Literacy Classes - SBAC_Field");
        assertThat(item1434.getTeacherHandScoring().get().dimensions()).isNotNull();

        assertThat(item1432.getId()).isEqualTo("1432");
        assertThat(item1432.doNotScore()).isTrue();

    }
}
