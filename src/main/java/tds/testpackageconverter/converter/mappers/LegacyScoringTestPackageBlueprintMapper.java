package tds.testpackageconverter.converter.mappers;

import tds.testpackage.legacy.model.Bpelement;
import tds.testpackage.legacy.model.Identifier;
import tds.testpackage.legacy.model.Testblueprint;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackage.model.BlueprintElementTypes;
import tds.testpackage.model.TestPackage;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class LegacyScoringTestPackageBlueprintMapper {
    public static Testblueprint mapBlueprint(final TestPackage testPackage,
                                             final List<Testspecification> administrationPackages) {
        final Testblueprint testBlueprint = new Testblueprint();

        final List<Bpelement> bpElements = testBlueprint.getBpelement();

        // Keep a multi-map of all blueprint elements by their ids. In some cases, bpElements may be present in separate
        // test packages. We will eventually want to "merge" these blueprint elements into one on the scoring package
        final Map<String, List<Bpelement>> bpElementIdMap = administrationPackages.stream()
                .flatMap(adminPackage -> adminPackage.getAdministration().getTestblueprint().getBpelement().stream())
                .collect(Collectors.groupingBy(bpEl -> bpEl.getIdentifier().getName(), mapping(bpEl -> bpEl, toList())));

        // In the combined test packages, they seem to group all segments in a test together.
        // The top level "test" element is simply an aggregation of these segments
        final List<Bpelement> segmentBlueprintElements = getSegmentBpElements(testPackage, bpElementIdMap);

        final Bpelement testPackageBpElement = new Bpelement();
        testPackageBpElement.setElementtype(BlueprintElementTypes.TEST);

        // "roll up" the item counts
        testPackageBpElement.setMinftitems(BigInteger.valueOf(segmentBlueprintElements.stream()
                .mapToInt(bpEl -> bpEl.getMinftitems().intValue()).sum()));
        testPackageBpElement.setMaxftitems(BigInteger.valueOf(segmentBlueprintElements.stream()
                .mapToInt(bpEl -> bpEl.getMaxftitems().intValue()).sum()));
        testPackageBpElement.setMinopitems(BigInteger.valueOf(segmentBlueprintElements.stream()
                .mapToInt(bpEl -> bpEl.getMinopitems().intValue()).sum()));
        testPackageBpElement.setMaxopitems(BigInteger.valueOf(segmentBlueprintElements.stream()
                .mapToInt(bpEl -> bpEl.getMaxopitems().intValue()).sum()));
        testPackageBpElement.setOpitemcount(BigInteger.valueOf(segmentBlueprintElements.stream()
                .mapToInt(bpEl -> bpEl.getOpitemcount().intValue()).sum()));
        testPackageBpElement.setFtitemcount(BigInteger.valueOf(segmentBlueprintElements.stream()
                .mapToInt(bpEl -> bpEl.getFtitemcount() != null ? bpEl.getFtitemcount().intValue() : 0).sum()));

        if (segmentBlueprintElements.size() > 1) {
            // Multi segment assessment
            final Identifier identifier = new Identifier();
            identifier.setUniqueid(TestPackageUtils.getAssessmentKey(testPackage, testPackage.getId()));
            identifier.setName(testPackage.getId());
            identifier.setVersion(new BigDecimal(testPackage.getVersion()));
            testPackageBpElement.setIdentifier(identifier);
        } else {
            // Single segment assessment - segment/test ids should match
            final Identifier identifier = new Identifier();
            identifier.setUniqueid(segmentBlueprintElements.get(0).getIdentifier().getUniqueid());
            identifier.setName(segmentBlueprintElements.get(0).getIdentifier().getName());
            identifier.setVersion(new BigDecimal(testPackage.getVersion()));
            testPackageBpElement.setIdentifier(identifier);
        }

        bpElements.add(testPackageBpElement);
        bpElements.addAll(segmentBlueprintElements);
        bpElements.addAll(mapMiscellaneousBpElements(bpElementIdMap));
        
        return testBlueprint;
    }

    private static List<Bpelement> mapMiscellaneousBpElements(final Map<String, List<Bpelement>> bpElementIdMap) {
        // flatten/aggregate all blueprints by id
        return bpElementIdMap.values().stream()
                .filter(bpElements -> !bpElements.get(0).getElementtype().equals(BlueprintElementTypes.SEGMENT)
                        && !bpElements.get(0).getElementtype().equals(BlueprintElementTypes.TEST))
                .map(bpElements -> {
                    final Bpelement combinedBpEl = new Bpelement();
                    final Bpelement firstBpEl = bpElements.get(0);

                    combinedBpEl.setElementtype(firstBpEl.getElementtype());
                    combinedBpEl.setParentid(firstBpEl.getParentid());

                    // sum the item counts
                    combinedBpEl.setMinftitems(BigInteger.valueOf(bpElements.stream()
                            .mapToInt(bpEl -> bpEl.getMinftitems().intValue()).sum()));
                    combinedBpEl.setMaxftitems(BigInteger.valueOf(bpElements.stream()
                            .mapToInt(bpEl -> bpEl.getMaxftitems().intValue()).sum()));
                    combinedBpEl.setMinopitems(BigInteger.valueOf(bpElements.stream()
                            .mapToInt(bpEl -> bpEl.getMinopitems().intValue()).sum()));
                    combinedBpEl.setMaxopitems(BigInteger.valueOf(bpElements.stream()
                            .mapToInt(bpEl -> bpEl.getMaxopitems().intValue()).sum()));
                    combinedBpEl.setOpitemcount(BigInteger.valueOf(bpElements.stream()
                            .mapToInt(bpEl -> bpEl.getOpitemcount().intValue()).sum()));
                    combinedBpEl.setFtitemcount(BigInteger.valueOf(bpElements.stream()
                            .mapToInt(bpEl -> bpEl.getFtitemcount().intValue()).sum()));

                    final Identifier identifier = new Identifier();
                    identifier.setUniqueid(firstBpEl.getIdentifier().getUniqueid());
                    identifier.setName(firstBpEl.getIdentifier().getName());
                    identifier.setVersion(firstBpEl.getIdentifier().getVersion());
                    combinedBpEl.setIdentifier(identifier);

                    return combinedBpEl;
                })
                .collect(Collectors.toList());
    }

    private static List<Bpelement>getSegmentBpElements(final TestPackage testPackage, final Map<String, List<Bpelement>> bpElementIdMap) {
        return testPackage.getAssessments().stream()
                .flatMap(assessment -> assessment.getSegments().stream()
                        .map(segment -> {
                            final Bpelement segmentBpElement = new Bpelement();
                            segmentBpElement.setElementtype(BlueprintElementTypes.SEGMENT);

                            List<Bpelement> bpElementsForId = bpElementIdMap.get(segment.getId());

                            segmentBpElement.setMinftitems(BigInteger.valueOf(bpElementsForId.stream()
                                    .filter(bpEl -> bpEl.getElementtype().equals(BlueprintElementTypes.SEGMENT))
                                    .mapToInt(bpEl -> bpEl.getMinftitems().intValue())
                                    .sum()));
                            segmentBpElement.setMaxftitems(BigInteger.valueOf(bpElementsForId.stream()
                                    .filter(bpEl -> bpEl.getElementtype().equals(BlueprintElementTypes.SEGMENT))
                                    .mapToInt(bpEl -> bpEl.getMaxftitems().intValue())
                                    .sum()));
                            segmentBpElement.setMinopitems(BigInteger.valueOf(bpElementsForId.stream()
                                    .filter(bpEl -> bpEl.getElementtype().equals(BlueprintElementTypes.SEGMENT))
                                    .mapToInt(bpEl -> bpEl.getMinopitems().intValue())
                                    .sum()));
                            segmentBpElement.setMaxopitems(BigInteger.valueOf(bpElementsForId.stream()
                                    .filter(bpEl -> bpEl.getElementtype().equals(BlueprintElementTypes.SEGMENT))
                                    .mapToInt(bpEl -> bpEl.getMaxopitems().intValue())
                                    .sum()));
                            segmentBpElement.setOpitemcount(BigInteger.valueOf(bpElementsForId.stream()
                                    .filter(bpEl -> bpEl.getElementtype().equals(BlueprintElementTypes.SEGMENT))
                                    .mapToInt(bpEl -> bpEl.getOpitemcount().intValue())
                                    .sum()));
                            segmentBpElement.setFtitemcount(BigInteger.valueOf(bpElementsForId.stream()
                                    .filter(bpEl -> bpEl.getElementtype().equals(BlueprintElementTypes.SEGMENT))
                                    .mapToInt(bpEl -> bpEl.getFtitemcount().intValue())
                                    .sum()));

                            final Identifier identifier = new Identifier();
                            identifier.setUniqueid(testPackage.getAssessments().size() > 1
                                    ? TestPackageUtils.getCombinedKey(testPackage, segment.getId())
                                    : TestPackageUtils.getAssessmentKey(testPackage, segment.getId()));
                            identifier.setName(testPackage.getAssessments().size() > 1
                                    ? TestPackageUtils.getCombinedId(segment.getId())
                                    : segment.getId());
                            identifier.setVersion(new BigDecimal(testPackage.getVersion()));
                            segmentBpElement.setIdentifier(identifier);

                            return segmentBpElement;
                        })
                ).collect(toList());
    }
}
