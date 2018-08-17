package tds.testpackageconverter.converter.mappers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.*;
import tds.testpackage.model.Assessment;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class LegacyAdministrationTestPackageSegmentMapperTest extends LegacyTestPackageBaseTest {
    @Test
    public void shouldMapMultiSegmentFixedFormTestSuccessfully() {
        Assessment assessment = mockFixedMultiSegmentPackage.getAssessments().get(0);
        Map<String, Long> formKeyMap = ImmutableMap.<String, Long>builder()
                .put("PracTest::MG6::S1::FA17::ENU", 949L)
                .put("PracTest::MG6::S1::FA17::BRL", 950L)
                .put("PracTest::MG6::S1::FA17::SPA", 951L)
                .put("PracTest::MG6::S2::FA17::ENU", 952L)
                .put("PracTest::MG6::S2::FA17::BRL", 953L)
                .put("PracTest::MG6::S2::FA17::SPA", 954L)
                .build();
        List<Adminsegment> adminSegments = LegacyAdministrationTestPackageSegmentMapper.mapAdminSegments(mockFixedMultiSegmentPackage, assessment, formKeyMap);
        assertThat(adminSegments).hasSize(2);

        Adminsegment seg1 = adminSegments.get(0);
        assertThat(seg1.getSegmentid()).isEqualTo("(SBAC_PT)SBAC-SEG1-MATH-6-2018");
        assertThat(seg1.getPosition()).isEqualTo("1");
        assertThat(seg1.getItemselection()).isEqualTo("fixedform");

        assertThat(seg1.getSegmentblueprint().getSegmentbpelement()).hasSize(2);
        Segmentbpelement segBpEl = seg1.getSegmentblueprint().getSegmentbpelement().get(0);
        assertThat(segBpEl.getBpelementid()).isEqualTo("SBAC_PT-MA-Undesignated");
        assertThat(segBpEl.getMinopitems()).isEqualTo(8);
        assertThat(segBpEl.getMaxopitems()).isEqualTo(8);

        Itemselector seg1ItemSelector = seg1.getItemselector();
        assertThat(seg1ItemSelector.getType()).isEqualTo("fixedform");
        assertThat(seg1ItemSelector.getIdentifier().getUniqueid()).isEqualTo("AIR FIXEDFORM1");
        assertThat(seg1ItemSelector.getIdentifier().getName()).isEqualTo("AIR FIXEDFORM1");
        assertThat(seg1ItemSelector.getIdentifier().getLabel()).isEqualTo("AIR FIXEDFORM1");
        assertThat(seg1ItemSelector.getItemselectionparameter()).hasSize(1);
        Itemselectionparameter param = seg1ItemSelector.getItemselectionparameter().get(0);
        assertThat(param.getBpelementid()).isEqualTo("(SBAC_PT)SBAC-SEG1-MATH-6-2018");

        assertThat(param.getProperty()).hasSize(2);
        assertThat(param.getProperty().get(0).getName()).isEqualTo("slope");
        assertThat(param.getProperty().get(0).getValue()).isEqualTo("1");
        assertThat(param.getProperty().get(0).getLabel()).isEqualTo("slope");

        assertThat(param.getProperty().get(1).getName()).isEqualTo("intercept");
        assertThat(param.getProperty().get(1).getValue()).isEqualTo("1");
        assertThat(param.getProperty().get(1).getLabel()).isEqualTo("intercept");

        assertThat(seg1.getSegmentform()).hasSize(3);
        assertThat(seg1.getSegmentform().get(0).getFormpartitionid()).isEqualTo("187-950");
        assertThat(seg1.getSegmentform().get(1).getFormpartitionid()).isEqualTo("187-951");
        assertThat(seg1.getSegmentform().get(2).getFormpartitionid()).isEqualTo("187-949");
    }

    @Test
    public void shouldMapCATTestSuccessfully() {
        Assessment assessment = mockCATAdminLegacyTestPackage.getAssessments().get(0);
        Map<String, Long> formKeyMap = new HashMap<>();
        List<Adminsegment> adminSegments = LegacyAdministrationTestPackageSegmentMapper.mapAdminSegments(mockCATAdminLegacyTestPackage, assessment, formKeyMap);
        assertThat(adminSegments).hasSize(1);

        Adminsegment seg1 = adminSegments.get(0);
        assertThat(seg1.getSegmentid()).isEqualTo("(SBAC_PT)SBAC-IRP-CAT-MATH-11-2018");
        assertThat(seg1.getPosition()).isEqualTo("1");
        assertThat(seg1.getItemselection()).isEqualTo("adaptive");

        assertThat(seg1.getSegmentblueprint().getSegmentbpelement()).hasSize(66);
        Segmentbpelement segBpEl = seg1.getSegmentblueprint().getSegmentbpelement().get(0);
        assertThat(segBpEl.getBpelementid()).isEqualTo("SBAC_PT-1");
        assertThat(segBpEl.getMinopitems()).isEqualTo(4);
        assertThat(segBpEl.getMaxopitems()).isEqualTo(4);

        Itemselector seg1ItemSelector = seg1.getItemselector();
        assertThat(seg1ItemSelector.getType()).isEqualTo("adaptive");
        assertThat(seg1ItemSelector.getIdentifier().getUniqueid()).isEqualTo("AIR ADAPTIVE1");
        assertThat(seg1ItemSelector.getIdentifier().getName()).isEqualTo("AIR ADAPTIVE1");
        assertThat(seg1ItemSelector.getIdentifier().getLabel()).isEqualTo("AIR ADAPTIVE1");
        assertThat(seg1ItemSelector.getItemselectionparameter()).hasSize(66);
        Itemselectionparameter param = seg1ItemSelector.getItemselectionparameter().get(65);
        assertThat(param.getBpelementid()).isEqualTo("(SBAC_PT)SBAC-IRP-CAT-MATH-11-2018");

        assertThat(param.getProperty()).hasSize(27);
        assertThat(param.getProperty().get(0).getName()).isEqualTo("bpweight");
        assertThat(param.getProperty().get(0).getValue()).isEqualTo("1");
        assertThat(param.getProperty().get(0).getLabel()).isEqualTo("bpweight");

        assertThat(param.getProperty().get(1).getName()).isEqualTo("startability");
        assertThat(param.getProperty().get(1).getValue()).isEqualTo("0.506053");
        assertThat(param.getProperty().get(1).getLabel()).isEqualTo("startability");

        assertThat(seg1.getSegmentform()).isEmpty();
    }
}
