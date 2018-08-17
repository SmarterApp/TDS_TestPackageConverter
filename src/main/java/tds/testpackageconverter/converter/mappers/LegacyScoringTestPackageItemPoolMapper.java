package tds.testpackageconverter.converter.mappers;

import tds.testpackage.legacy.model.*;
import tds.testpackage.model.TestPackage;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LegacyScoringTestPackageItemPoolMapper {
    public static Itempool mapItemPool(final TestPackage testPackage, final List<Testspecification> administrationPackages) {
        final Itempool itemPool = new Itempool();

        itemPool.getPassage().addAll(mapPassages(administrationPackages));
        itemPool.getTestitem().addAll(mapItems(testPackage, administrationPackages));
        return itemPool;
    }

    private static List<Testitem> mapItems(final TestPackage testPackage, final List<Testspecification> administrationPackages) {
        final Map<String, String> segmentBpElements = administrationPackages.stream()
                .flatMap(adminPackage -> adminPackage.getAdministration().getTestblueprint().getBpelement().stream()
                        .filter(bpEl -> bpEl.getElementtype().equals("segment")))
                .collect(Collectors.toMap(
                        bpEl -> bpEl.getIdentifier().getUniqueid(),
                        bpEl -> administrationPackages.size() > 1
                                ? TestPackageUtils.getCombinedKey(testPackage, bpEl.getIdentifier().getName())
                                : TestPackageUtils.getAssessmentKey(testPackage, bpEl.getIdentifier().getName())));

        if (administrationPackages.size() == 1) {
            return administrationPackages.get(0).getAdministration().getItempool().getTestitem().stream()
                    .map(item -> replaceBpRefs(item, segmentBpElements))
                    .collect(Collectors.toList());
        } else {
            return Stream.concat(
                    administrationPackages.get(0).getAdministration().getItempool().getTestitem().stream()
                            .map(item -> replaceBpRefs(item, segmentBpElements)),
                    administrationPackages.get(1).getAdministration().getItempool().getTestitem().stream()
                            .map(item -> replaceBpRefs(item, segmentBpElements)))
                    .collect(Collectors.toList()); // Combine the two item lists
        }
    }

    private static Testitem replaceBpRefs(final Testitem item, final Map<String, String> segmentBpElNameMap) {
        // We need to iterate over each bpref and translate the test/segment keys to combined keys
        for (Bpref ref : item.getBpref()) {
            if (segmentBpElNameMap.containsKey(ref.getContent())) {
                ref.setContent(segmentBpElNameMap.get(ref.getContent()));
            }
        }

        return item;
    }

    private static List<Passage> mapPassages(final List<Testspecification> administrationPackages) {
        if (administrationPackages.size() == 1) {
            return administrationPackages.get(0).getAdministration().getItempool().getPassage();
        } else {
            return Stream.concat(
                    administrationPackages.get(0).getAdministration().getItempool().getPassage().stream(),
                    administrationPackages.get(1).getAdministration().getItempool().getPassage().stream())
                    .distinct()
                    .collect(Collectors.toList()); // Combine the two passage lists
        }
    }
}
