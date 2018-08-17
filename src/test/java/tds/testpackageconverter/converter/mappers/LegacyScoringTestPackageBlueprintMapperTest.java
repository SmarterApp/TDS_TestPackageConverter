package tds.testpackageconverter.converter.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.legacy.model.Bpelement;
import tds.testpackage.legacy.model.Testblueprint;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegacyScoringTestPackageBlueprintMapperTest extends LegacyTestPackageBaseTest {
    private List<Testspecification> adminTestPackages;

    @Before
    public void setup() {
        adminTestPackages = LegacyAdministrationTestPackageMapper.fromNew(mockTestPackageWithScoring);
    }

    @Test
    public void shouldMapCombinedBlueprint() {
        Testblueprint blueprint = LegacyScoringTestPackageBlueprintMapper.mapBlueprint(mockTestPackageWithScoring, adminTestPackages);
        assertThat(blueprint).isNotNull();
        assertThat(blueprint.getBpelement()).hasSize(30);

        Bpelement testBp = blueprint.getBpelement().get(0);

        assertThat(testBp.getElementtype()).isEqualTo("test");
        assertThat(testBp.getParentid()).isNull();
        assertThat(testBp.getIdentifier().getUniqueid()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-COMBINED-2017-2018");
        assertThat(testBp.getIdentifier().getName()).isEqualTo("SBAC-ICA-FIXED-G7E-COMBINED-2017");
        assertThat(testBp.getMinopitems()).isEqualTo(48);
        assertThat(testBp.getMaxopitems()).isEqualTo(48);
        assertThat(testBp.getMinftitems()).isEqualTo(0);
        assertThat(testBp.getMaxftitems()).isEqualTo(0);
        assertThat(testBp.getOpitemcount()).isEqualTo(48);
        assertThat(testBp.getFtitemcount()).isEqualTo(0);

        List<Bpelement> segmentBpElements = blueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equals("segment"))
                .collect(Collectors.toList());

        assertThat(segmentBpElements).hasSize(3);

        Bpelement seg1BpEl = segmentBpElements.get(0);
        assertThat(seg1BpEl.getParentid()).isNull();
        assertThat(seg1BpEl.getIdentifier().getUniqueid()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-ELA-7-COMBINED-2018");
        assertThat(seg1BpEl.getIdentifier().getName()).isEqualTo("SBAC-ICA-FIXED-G7E-ELA-7-COMBINED");
        assertThat(seg1BpEl.getMinopitems()).isEqualTo(44);
        assertThat(seg1BpEl.getMaxopitems()).isEqualTo(44);
        assertThat(seg1BpEl.getMinftitems()).isEqualTo(0);
        assertThat(seg1BpEl.getMaxftitems()).isEqualTo(0);
        assertThat(seg1BpEl.getOpitemcount()).isEqualTo(44);
        assertThat(seg1BpEl.getFtitemcount()).isEqualTo(0);

        Bpelement seg2BpEl = segmentBpElements.get(1);
        assertThat(seg2BpEl.getParentid()).isNull();
        assertThat(seg2BpEl.getIdentifier().getUniqueid()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-Perf-TechnologyInTheClassroomA-ELA-7-COMBINED-2018");
        assertThat(seg2BpEl.getIdentifier().getName()).isEqualTo("SBAC-ICA-FIXED-G7E-Perf-TechnologyInTheClassroomA-ELA-7-COMBINED");
        assertThat(seg2BpEl.getMaxopitems()).isEqualTo(3);
        assertThat(seg2BpEl.getMinopitems()).isEqualTo(3);
        assertThat(seg2BpEl.getMinftitems()).isEqualTo(0);
        assertThat(seg2BpEl.getMaxftitems()).isEqualTo(0);
        assertThat(seg2BpEl.getOpitemcount()).isEqualTo(3);
        assertThat(seg2BpEl.getFtitemcount()).isEqualTo(0);

        Bpelement seg3BpEl = segmentBpElements.get(2);
        assertThat(seg3BpEl.getParentid()).isNull();
        assertThat(seg3BpEl.getIdentifier().getUniqueid()).isEqualTo("(SBAC)SBAC-ICA-FIXED-G7E-Perf-TechnologyInTheClassroomB-ELA-7-COMBINED-2018");
        assertThat(seg3BpEl.getIdentifier().getName()).isEqualTo("SBAC-ICA-FIXED-G7E-Perf-TechnologyInTheClassroomB-ELA-7-COMBINED");
        assertThat(seg3BpEl.getMinopitems()).isEqualTo(1);
        assertThat(seg3BpEl.getMaxopitems()).isEqualTo(1);
        assertThat(seg3BpEl.getMinftitems()).isEqualTo(0);
        assertThat(seg3BpEl.getMaxftitems()).isEqualTo(0);
        assertThat(seg3BpEl.getOpitemcount()).isEqualTo(1);
        assertThat(seg3BpEl.getFtitemcount()).isEqualTo(0);

        Bpelement contentLevelBpEl = blueprint.getBpelement().stream()
                .filter(bpEl -> bpEl.getElementtype().equals("contentlevel") && bpEl.getIdentifier().getName().equals("1-IT|14-7"))
                .findFirst().get();

        assertThat(contentLevelBpEl.getIdentifier().getUniqueid()).isEqualTo("SBAC-1-IT|14-7");
        assertThat(contentLevelBpEl.getMinopitems()).isEqualTo(0);
        assertThat(contentLevelBpEl.getMaxopitems()).isEqualTo(45);
        assertThat(contentLevelBpEl.getMinftitems()).isEqualTo(0);
        assertThat(contentLevelBpEl.getMaxftitems()).isEqualTo(0);
        assertThat(contentLevelBpEl.getOpitemcount()).isEqualTo(1);
        assertThat(contentLevelBpEl.getFtitemcount()).isEqualTo(0);
    }
}
