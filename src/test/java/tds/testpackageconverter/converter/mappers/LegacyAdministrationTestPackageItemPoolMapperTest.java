package tds.testpackageconverter.converter.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.*;
import tds.testpackage.model.Assessment;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyAdministrationTestPackageItemPoolMapperTest extends LegacyTestPackageBaseTest {

    @Test
    public void shouldMapItemPoolSuccessfully() {
        Assessment assessment = mockCATAdminLegacyTestPackage.getAssessments().get(0);
        Itempool itempool = LegacyAdministrationTestPackageItemPoolMapper.mapItemPool(mockCATAdminLegacyTestPackage, assessment);
        assertThat(itempool).isNotNull();
        assertThat(itempool.getTestitem()).hasSize(18);

        Testitem testitem = itempool.getTestitem().stream()
                .filter(item -> item.getIdentifier().getUniqueid().equalsIgnoreCase("187-1899"))
                .findFirst()
                .get();

        assertThat(testitem.getFilename()).isEqualTo("item-187-1899.xml");
        assertThat(testitem.getItemtype()).isEqualTo("EQ");

        List<Bpref> bpRefs = testitem.getBpref();
        assertThat(bpRefs).hasSize(7);

        assertThat(testitem.getPoolproperty()).hasSize(3);

        assertThat(testitem.getPoolproperty().get(0).getProperty()).isEqualTo("Language");
        assertThat(testitem.getPoolproperty().get(0).getValue()).isEqualTo("ENU");
        assertThat(testitem.getPoolproperty().get(0).getLabel()).isEqualTo("English");

        assertThat(testitem.getPoolproperty().get(1).getProperty()).isEqualTo("--ITEMTYPE--");
        assertThat(testitem.getPoolproperty().get(1).getValue()).isEqualTo("EQ");
        assertThat(testitem.getPoolproperty().get(1).getLabel()).isEqualTo("ItemType = EQ");

        // DOK pool property
        assertThat(testitem.getPoolproperty().get(2).getProperty()).isEqualTo("Depth of Knowledge");
        assertThat(testitem.getPoolproperty().get(2).getValue()).isEqualTo("3");
        assertThat(testitem.getPoolproperty().get(2).getLabel()).isEqualTo("Depth of Knowledge = 3");

        assertThat(testitem.getItemscoredimension()).hasSize(1);
        Itemscoredimension dimension = testitem.getItemscoredimension().get(0);
        assertThat(dimension.getMeasurementmodel()).isEqualTo("IRT3PLn");
        assertThat(dimension.getScorepoints()).isEqualTo("1");
        assertThat(dimension.getWeight()).isEqualTo(1F);

        assertThat(dimension.getItemscoreparameter()).hasSize(3);
        assertThat(dimension.getItemscoreparameter().get(0).getMeasurementparameter()).isEqualTo("a");
        assertThat(dimension.getItemscoreparameter().get(0).getValue()).isEqualTo(1F);
        assertThat(dimension.getItemscoreparameter().get(1).getMeasurementparameter()).isEqualTo("b");
        assertThat(dimension.getItemscoreparameter().get(1).getValue()).isEqualTo(.000000000000001F);
        assertThat(dimension.getItemscoreparameter().get(2).getMeasurementparameter()).isEqualTo("c");
        assertThat(dimension.getItemscoreparameter().get(2).getValue()).isEqualTo(0F);
    }

    @Test
    public void shouldMapPassagesSuccessfully() {
        Assessment assessment = mockPerfAdminLegacyTestPackage.getAssessments().get(0);
        Itempool itempool = LegacyAdministrationTestPackageItemPoolMapper.mapItemPool(mockPerfAdminLegacyTestPackage, assessment);
        assertThat(itempool).isNotNull();

        // Test item -> passage references
        assertThat(itempool.getTestitem()).hasSize(2);
        Testitem itemWithPassage = itempool.getTestitem().get(0);

        assertThat(itemWithPassage.getPassageref()).hasSize(1);
        assertThat(itemWithPassage.getPassageref().get(0).getContent()).isEqualTo("187-3688");

        // Passage
        assertThat(itempool.getPassage()).hasSize(1);
        Passage passage = itempool.getPassage().get(0);

        assertThat(passage.getFilename()).isEqualTo("stim-187-3688.xml");
        assertThat(passage.getIdentifier().getUniqueid()).isEqualTo("187-3688");
    }
}
