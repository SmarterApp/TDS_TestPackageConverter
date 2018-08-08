package tds.testpackageconverter.converter.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.Itempool;
import tds.testpackage.legacy.model.Passage;
import tds.testpackage.legacy.model.Testitem;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyScoringTestPackageItemPoolMapperTest extends LegacyTestPackageBaseTest {

    private List<Testspecification> adminTestPackages;

    @Before
    public void setup() {
        adminTestPackages = LegacyAdministrationTestPackageMapper.fromNew(mockTestPackageWithScoring);
    }

    @Test
    public void shouldMapItemPoolCombined() {
        Itempool itempool = LegacyScoringTestPackageItemPoolMapper.mapItemPool(mockTestPackageWithScoring, adminTestPackages);

        assertThat(itempool).isNotNull();
        assertThat(itempool.getPassage()).hasSize(9);

        Passage passage = itempool.getPassage().stream()
                .filter(p -> p.getIdentifier().getUniqueid().equals("200-1433"))
                .findFirst().get();
        assertThat(passage.getIdentifier().getVersion()).isEqualTo("9787");
        assertThat(passage.getFilename()).isEqualTo("stim-200-1433.xml");

        assertThat(itempool.getTestitem()).hasSize(48);

        Testitem item = itempool.getTestitem().get(0);
        assertThat(item.getFilename()).isEqualTo("item-200-35062.xml");
        assertThat(item.getItemtype()).isEqualTo("MC");
        assertThat(item.getBpref()).hasSize(3);
        assertThat(item.getBpref().get(0).getContent()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-ELA-7-COMBINED-2018");
    }
}
