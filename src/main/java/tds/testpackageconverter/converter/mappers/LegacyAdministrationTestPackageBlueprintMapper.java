package tds.testpackageconverter.converter.mappers;

import tds.common.Algorithm;
import tds.testpackage.legacy.model.Bpelement;
import tds.testpackage.legacy.model.Identifier;
import tds.testpackage.legacy.model.Testblueprint;
import tds.testpackage.model.*;
import tds.testpackageconverter.converter.BlueprintElementCounts;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tds.testpackageconverter.utils.TestPackageUtils.getBlueprintKeyFromId;

public class LegacyAdministrationTestPackageBlueprintMapper {
    public static Testblueprint mapBlueprint(final TestPackage testPackage, final Assessment assessment) {
        // Get the access counts for each blueprint element
        final Map<String, BlueprintElementCounts> blueprintElementReferenceCount = countBlueprintElementReferences(assessment);
        // Group the segment blueprint elements, containing total counts of exam and field test items
        // In cases where there are multiple segments that reference the same blueprint element, we will have multiple values
        final Map<String, List<SegmentBlueprintElement>> segBpElMap = assessment.getSegments().stream()
                .flatMap(segment -> segment.segmentBlueprint().stream())
                .collect(Collectors.groupingBy(SegmentBlueprintElement::getIdRef));

        final Testblueprint testBlueprint = new Testblueprint();
        final List<Bpelement> bpElements = testBlueprint.getBpelement();

        // Map test and segment blueprints
        final List<SegmentBlueprintElement> testBpEls = segBpElMap.get(assessment.getId());
        final BlueprintElementCounts counts = blueprintElementReferenceCount.get(assessment.getId());
        final Bpelement testBpElement = new Bpelement();
        testBpElement.setElementtype("test");

        if (!assessment.isSegmented()) {
            testBpElement.setMinftitems(BigInteger.valueOf(testBpEls.stream().mapToInt(SegmentBlueprintElement::minFieldTestItems).sum()));
            testBpElement.setMaxftitems(BigInteger.valueOf(testBpEls.stream().mapToInt(SegmentBlueprintElement::maxFieldTestItems).sum()));
            testBpElement.setMinopitems(BigInteger.valueOf(testBpEls.stream().mapToInt(SegmentBlueprintElement::getMinExamItems).sum()));
            testBpElement.setMaxopitems(BigInteger.valueOf(testBpEls.stream().mapToInt(SegmentBlueprintElement::getMaxExamItems).sum()));
            testBpElement.setOpitemcount(BigInteger.valueOf(counts.getExamItemCount()));
            testBpElement.setFtitemcount(BigInteger.valueOf(counts.getFieldTestItemCount()));
        } else { // Multi segmented test - add up the segment blueprint element properties
            int minTestExamItems = 0;
            int maxTestExamItems = 0;
            int minTestFieldTestItems = 0;
            int maxTestFieldTestItems = 0;
            int testExamItemCount = 0;
            int testFieldTestItemCount = 0;

            for (final Segment segment : assessment.getSegments()) {
                List<SegmentBlueprintElement> segmentBlueprintElements = segBpElMap.get(segment.getId());
                final BlueprintElementCounts segmentCounts = blueprintElementReferenceCount.get(segment.getId());

                minTestExamItems += segmentBlueprintElements.stream().mapToInt(SegmentBlueprintElement::getMinExamItems).sum();
                maxTestExamItems += segmentBlueprintElements.stream().mapToInt(SegmentBlueprintElement::getMaxExamItems).sum();
                minTestFieldTestItems += segmentBlueprintElements.stream().mapToInt(SegmentBlueprintElement::minFieldTestItems).sum();
                maxTestFieldTestItems += segmentBlueprintElements.stream().mapToInt(SegmentBlueprintElement::maxFieldTestItems).sum();
                testExamItemCount += segmentCounts.getExamItemCount();
                testFieldTestItemCount += segmentCounts.getFieldTestItemCount();
            }

            testBpElement.setMinftitems(BigInteger.valueOf(minTestFieldTestItems));
            testBpElement.setMaxftitems(BigInteger.valueOf(maxTestFieldTestItems));
            testBpElement.setMinopitems(BigInteger.valueOf(minTestExamItems));
            testBpElement.setMaxopitems(BigInteger.valueOf(maxTestExamItems));
            testBpElement.setOpitemcount(BigInteger.valueOf(testExamItemCount));
            testBpElement.setFtitemcount(BigInteger.valueOf(testFieldTestItemCount));
        }


        final Identifier testBpElIdentifier = new Identifier();
        testBpElIdentifier.setName(assessment.getId());
        testBpElIdentifier.setUniqueid(assessment.getKey());
        testBpElIdentifier.setVersion(new BigDecimal(testPackage.getVersion()));
        testBpElement.setIdentifier(testBpElIdentifier);

        bpElements.add(testBpElement);

        for (Segment segment : assessment.getSegments()) {
            final List<SegmentBlueprintElement> segmentBpEls = segBpElMap.get(segment.getId());
            final BlueprintElementCounts segmentCounts = blueprintElementReferenceCount.get(segment.getId());

            final Bpelement segmentBpElement = new Bpelement();
            segmentBpElement.setElementtype("segment");
            segmentBpElement.setMinftitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::minFieldTestItems).sum()));
            segmentBpElement.setMaxftitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::maxFieldTestItems).sum()));
            segmentBpElement.setMinopitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::getMinExamItems).sum()));
            segmentBpElement.setMaxopitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::getMaxExamItems).sum()));
            segmentBpElement.setOpitemcount(BigInteger.valueOf(segmentCounts.getExamItemCount()));
            segmentBpElement.setFtitemcount(BigInteger.valueOf(segmentCounts.getFieldTestItemCount()));

            final Identifier segmentBpElIdentifier = new Identifier();
            segmentBpElIdentifier.setName(segment.getId());
            segmentBpElIdentifier.setUniqueid(segment.getKey());
            segmentBpElIdentifier.setVersion(new BigDecimal(testPackage.getVersion()));
            segmentBpElement.setIdentifier(segmentBpElIdentifier);

            bpElements.add(segmentBpElement);
        }

        bpElements.addAll(mapMiscellaneousBlueprints(testPackage, segBpElMap, blueprintElementReferenceCount));
        bpElements.addAll(mapStrandAndContentLevelBlueprints(testPackage, segBpElMap, blueprintElementReferenceCount));
        return testBlueprint;
    }

    private static List<Bpelement> mapMiscellaneousBlueprints(final TestPackage testPackage,
                                                              final Map<String, List<SegmentBlueprintElement>> segmentBlueprintElementMap,
                                                              final Map<String, BlueprintElementCounts> blueprintElementReferenceCount) {
        return testPackage.getBlueprint().stream()
                .filter(blueprintElement -> blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.AFFINITY_GROUP)
                    || blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.SOCK))
                .filter(blueprintElement -> segmentBlueprintElementMap.containsKey(blueprintElement.getId()))
                .map(blueprintElement -> {
                    final List<SegmentBlueprintElement> segmentBpEls = segmentBlueprintElementMap.get(blueprintElement.getId());
                    final BlueprintElementCounts segmentCounts = blueprintElementReferenceCount.get(blueprintElement.getId());

                    final Bpelement bpElement = new Bpelement();
                    bpElement.setElementtype(blueprintElement.getType());
                    bpElement.setMinftitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::minFieldTestItems).sum()));
                    bpElement.setMaxftitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::maxFieldTestItems).sum()));
                    bpElement.setMinopitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::getMinExamItems).sum()));
                    bpElement.setMaxopitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::getMaxExamItems).sum()));
                    bpElement.setOpitemcount(BigInteger.valueOf(segmentCounts.getExamItemCount()));
                    bpElement.setFtitemcount(BigInteger.valueOf(segmentCounts.getFieldTestItemCount()));

                    final Identifier bpElementIdentifier = new Identifier();
                    bpElementIdentifier.setName(blueprintElement.getId());
                    bpElementIdentifier.setUniqueid(getBlueprintKeyFromId(blueprintElement, testPackage.getPublisher()));
                    bpElementIdentifier.setVersion(new BigDecimal(testPackage.getVersion()));
                    bpElement.setIdentifier(bpElementIdentifier);
                    return bpElement;
                })
                .collect(Collectors.toList());
    }

    private static Map<String, BlueprintElementCounts> countBlueprintElementReferences(final Assessment assessment) {
        final Map<String, BlueprintElementCounts> blueprintElementCountsMap = new HashMap<>();

        // Get a flat list of all the items
        final List<Item> items = assessment.getSegments().stream()
                .flatMap(segment -> {
                    if (segment.getAlgorithmType().equalsIgnoreCase(Algorithm.FIXED_FORM.getType())) {
                        return segment.segmentForms().stream()
                                .flatMap(form -> form.itemGroups().stream()
                                        .flatMap(itemGroup -> itemGroup.items().stream()
                                        )
                                );
                    } else {
                        return segment.pool().stream()
                                .flatMap(itemGroup -> itemGroup.items().stream());
                    }
                })
                .collect(Collectors.toList());

        // Go through each of the items and increment their respective reference counts
        items.forEach(item -> item.getBlueprintReferences()
                .forEach(ref -> {
                    if (!blueprintElementCountsMap.containsKey(ref.getIdRef())) {
                        BlueprintElementCounts counts = new BlueprintElementCounts(ref.getIdRef());

                        if (item.fieldTest()) {
                            counts.incrementFieldTestItemCount();
                        } else {
                            counts.incrementExamItemCount();
                        }

                        blueprintElementCountsMap.put(ref.getIdRef(), counts);
                    } else {
                        final BlueprintElementCounts counts = blueprintElementCountsMap.get(ref.getIdRef());
                        if (item.fieldTest()) {
                            counts.incrementFieldTestItemCount();
                        } else {
                            counts.incrementExamItemCount();
                        }
                    }
                })
        );

        return blueprintElementCountsMap;
    }

    private static List<Bpelement> mapStrandAndContentLevelBlueprints(final TestPackage testPackage,
                                                                      final Map<String, List<SegmentBlueprintElement>> segmentBlueprintElementMap,
                                                                      final Map<String, BlueprintElementCounts> blueprintElementReferenceCount) {
        final List<Bpelement> bpElements = new ArrayList<>();
        final Map<String, BlueprintElement> blueprintMap = testPackage.getBlueprintMap();

        for (BlueprintElement blueprintElement : blueprintMap.values()) {
            // Skip non-claim/target blueprint elements
            if (!BlueprintElementTypes.CLAIM_AND_TARGET_TYPES.contains(blueprintElement.getType())
                    || !segmentBlueprintElementMap.containsKey(blueprintElement.getId())) {
                continue;
            }

            final List<SegmentBlueprintElement> segmentBpEls = segmentBlueprintElementMap.get(blueprintElement.getId());
            final BlueprintElementCounts segmentCounts = blueprintElementReferenceCount.get(blueprintElement.getId());

            final Bpelement bpElement = new Bpelement();
            bpElement.setElementtype(blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.CLAIM)
                    ? BlueprintElementTypes.STRAND
                    : BlueprintElementTypes.CONTENT_LEVEL);
            bpElement.setMinftitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::minFieldTestItems).sum()));
            bpElement.setMaxftitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::maxFieldTestItems).sum()));
            bpElement.setMinopitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::getMinExamItems).sum()));
            bpElement.setMaxopitems(BigInteger.valueOf(segmentBpEls.stream().mapToInt(SegmentBlueprintElement::getMaxExamItems).sum()));
            bpElement.setOpitemcount(BigInteger.valueOf(segmentCounts != null ? segmentCounts.getExamItemCount() : 0));
            bpElement.setFtitemcount(BigInteger.valueOf(segmentCounts != null ? segmentCounts.getFieldTestItemCount() : 0));

            if (blueprintElement.getParentBlueprintElement().isPresent()) {
                bpElement.setParentid(TestPackageUtils.getBlueprintKeyFromId(blueprintElement.getParentBlueprintElement().get(),
                        testPackage.getPublisher()));
            }

            final Identifier bpElementIdentifier = new Identifier();
            bpElementIdentifier.setName(blueprintElement.getId());
            bpElementIdentifier.setUniqueid(getBlueprintKeyFromId(blueprintElement, testPackage.getPublisher()));
            bpElementIdentifier.setVersion(new BigDecimal(testPackage.getVersion()));
            bpElement.setIdentifier(bpElementIdentifier);
            bpElements.add(bpElement);
        }

        return bpElements;
    }

}
