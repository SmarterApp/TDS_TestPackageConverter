package tds.testpackageconverter.converter.mappers;


import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.Formpartition;
import tds.testpackage.legacy.model.Groupitem;
import tds.testpackage.legacy.model.Itemgroup;
import tds.testpackage.legacy.model.Testform;
import tds.testpackage.model.Assessment;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyAdministrationTestPackageFormMapperTest extends LegacyTestPackageBaseTest {

    @Test
    public void shouldMapFormsSuccessfully() {
        Assessment perfAssessment = mockFixedMultiSegmentPackage.getAssessments().get(0);
        Map<String, Long> formKeyMap = ImmutableMap.<String, Long>builder()
                .put("PracTest::MG6::S1::FA17::ENU", 949L)
                .put("PracTest::MG6::S1::FA17::BRL", 950L)
                .put("PracTest::MG6::S1::FA17::SPA", 951L)
                .put("PracTest::MG6::S2::FA17::ENU", 952L)
                .put("PracTest::MG6::S2::FA17::BRL", 953L)
                .put("PracTest::MG6::S2::FA17::SPA", 954L)
                .build();

        List<Testform> forms = LegacyAdministrationTestPackageFormMapper.mapTestForms(mockFixedMultiSegmentPackage, perfAssessment, formKeyMap);
        assertThat(forms).hasSize(3);

        Testform form = forms.get(0);

        assertThat(form.getLength()).isEqualTo(27);
        assertThat(form.getIdentifier().getUniqueid()).isEqualTo("(SBAC_PT)SBAC-Mathematics-6-2018:Default-ENU-Braille");
        assertThat(form.getIdentifier().getName()).isEqualTo("(SBAC_PT)SBAC-Mathematics-6-2018:Default-ENU-Braille");

        assertThat(form.getProperty()).hasSize(1);
        assertThat(form.getProperty().get(0).getName()).isEqualToIgnoringCase("Language");
        assertThat(form.getProperty().get(0).getValue()).isEqualToIgnoringCase("ENU-Braille");
        assertThat(form.getProperty().get(0).getLabel()).isEqualToIgnoringCase("Braille");

        assertThat(form.getFormpartition()).hasSize(2);
        Formpartition partition = form.getFormpartition().get(0);
        assertThat(partition.getIdentifier().getUniqueid()).isEqualTo("187-950");
        assertThat(partition.getIdentifier().getName()).isEqualTo("PracTest::MG6::S1::FA17::BRL");

        assertThat(partition.getItemgroup()).hasSize(8);
        Itemgroup group = partition.getItemgroup().get(3);

        assertThat(group.getFormposition()).isEqualTo("4");
        assertThat(group.getMaxitems()).isEqualTo("ALL");
        assertThat(group.getMaxresponses()).isEqualTo("0");
        assertThat(group.getIdentifier().getUniqueid()).isEqualTo("187-950:I-187-3585");
        assertThat(group.getIdentifier().getName()).isEqualTo("187-950:I-187-3585");
        assertThat(group.getGroupitem()).hasSize(1);

        Groupitem groupItem = group.getGroupitem().get(0);
        assertThat(groupItem.getItemid()).isEqualTo("187-3585");
        assertThat(groupItem.getFormposition()).isEqualTo(new BigInteger("4"));
        assertThat(groupItem.getGroupposition()).isEqualTo("1");
        assertThat(groupItem.getAdminrequired()).isEqualTo("true");
        assertThat(groupItem.getResponserequired()).isEqualTo("true");
        assertThat(groupItem.getIsactive()).isEqualTo("true");
        assertThat(groupItem.getIsfieldtest()).isEqualTo("true");
        assertThat(groupItem.getBlockid()).isEqualTo("A");

        Formpartition partition2 = form.getFormpartition().get(1);
        assertThat(partition2.getIdentifier().getUniqueid()).isEqualTo("187-953");
        assertThat(partition2.getIdentifier().getName()).isEqualTo("PracTest::MG6::S2::FA17::BRL");
        assertThat(partition2.getItemgroup()).hasSize(19);

        Itemgroup group2 = partition.getItemgroup().get(0);
        assertThat(group2.getFormposition()).isEqualTo("1");
        assertThat(group2.getMaxitems()).isEqualTo("ALL");
        assertThat(group2.getMaxresponses()).isEqualTo("0");

    }
}
