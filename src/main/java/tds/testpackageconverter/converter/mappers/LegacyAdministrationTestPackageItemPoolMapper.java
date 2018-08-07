package tds.testpackageconverter.converter.mappers;

import tds.common.Algorithm;
import tds.testpackage.legacy.model.*;
import tds.testpackage.model.*;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LegacyAdministrationTestPackageItemPoolMapper {
    public static Itempool mapItemPool(final TestPackage testPackage, final Assessment assessment) {
        final Itempool itempool = new Itempool();
        itempool.getPassage().addAll(mapPassages(testPackage, assessment));
        itempool.getTestitem().addAll(mapTestItems(testPackage, assessment));
        return itempool;
    }

    private static List<Passage> mapPassages(final TestPackage testPackage, final Assessment assessment) {
        final Set<Stimulus> stimuli = assessment.getSegments().stream()
                .flatMap(segment -> {
                    if (segment.getAlgorithmType().equalsIgnoreCase(Algorithm.FIXED_FORM.getType())) {
                        return segment.segmentForms().stream()
                                .flatMap(form -> form.itemGroups().stream()
                                        .filter(itemGroup -> itemGroup.getStimulus().isPresent())
                                        .map(itemGroup -> itemGroup.getStimulus().get())
                                );
                    } else {
                        return segment.pool().stream()
                                .filter(itemGroup -> itemGroup.getStimulus().isPresent())
                                .map(itemGroup -> itemGroup.getStimulus().get());
                    }
                })
                .collect(Collectors.toSet());

        return stimuli.stream().map(stimulus -> {
            final Passage passage = new Passage();
            passage.setFilename(String.format("stim-%s-%s.xml", testPackage.getBankKey(), stimulus.getId()));
            final Identifier passageIdentifier = new Identifier();
            passageIdentifier.setUniqueid(String.format("%s-%s", testPackage.getBankKey(), stimulus.getId()));
            passageIdentifier.setVersion(new BigDecimal(testPackage.getVersion()));
            passage.setIdentifier(passageIdentifier);
            return passage;
        }).collect(Collectors.toList());
    }

    private static List<Testitem> mapTestItems(final TestPackage testPackage, final Assessment assessment) {
        final Map<String, BlueprintElement> blueprintMap = testPackage.getBlueprintMap();
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

        return items.stream().map(item -> {
            final Testitem testItem = new Testitem();
            testItem.setFilename(String.format("item-%s-%s.xml", testPackage.getBankKey(), item.getId()));
            testItem.setItemtype(item.getType());

            final Identifier testItemIdentifier = new Identifier();
            testItemIdentifier.setUniqueid(String.format("%s-%s", testPackage.getBankKey(), item.getId()));
            testItemIdentifier.setVersion(new BigDecimal(testPackage.getVersion()));
            testItem.setIdentifier(testItemIdentifier);

            final List<Bpref> bpRefs = testItem.getBpref();

            item.getBlueprintReferences().forEach(bpRef -> {
                final Bpref ref = new Bpref();
                final BlueprintElement bpEl = blueprintMap.get(bpRef.getIdRef());
                if (bpEl.getType().equals("test") || bpEl.getType().equals("segment")) {
                    ref.setContent(TestPackageUtils.getAssessmentKey(testPackage, bpRef.getIdRef()));
                } else {
                    ref.setContent(TestPackageUtils.getBlueprintKeyFromId(bpEl, testPackage.getPublisher()));
                }
                bpRefs.add(ref);
            });

            final List<Poolproperty> itemProperties = testItem.getPoolproperty();

            item.getPresentations().forEach(presentation -> {
                Poolproperty languageProp = new Poolproperty();
                languageProp.setProperty("Language");
                languageProp.setValue(presentation.getCode());
                languageProp.setLabel(presentation.label());
                itemProperties.add(languageProp);
            });

            final Poolproperty itemTypeProperty = new Poolproperty();
            itemTypeProperty.setProperty("--ITEMTYPE--");
            itemTypeProperty.setValue(item.getType());
            itemTypeProperty.setLabel("ItemType = " + item.getType());
            itemProperties.add(itemTypeProperty);

            if (item.getItemGroup().getStimulus().isPresent()) {
                final Passageref passageRef = new Passageref();
                passageRef.setContent(item.getItemGroup().getStimulus().get().getKey());
                testItem.getPassageref().add(passageRef);
            }

            testItem.getItemscoredimension().addAll(mapItemScoreDimensions(item.getItemScoreDimensions()));
            testItem.getPoolproperty().addAll(mapPoolProperties(item.poolProperties()));

            return testItem;
        }).collect(Collectors.toList());
    }

    private static List<Poolproperty> mapPoolProperties(final List<PoolProperty> poolProperties) {
        return poolProperties.stream()
                .map(prop -> {
                    final Poolproperty legacyProperty = new Poolproperty();
                    legacyProperty.setProperty(prop.getName());
                    legacyProperty.setValue(prop.getValue());
                    legacyProperty.setLabel(String.format("%s = %s", prop.getName(), prop.getValue()));
                    return legacyProperty;
                })
                .collect(Collectors.toList());
    }

    private static List<Itemscoredimension> mapItemScoreDimensions(final List<ItemScoreDimension> itemScoreDimensions) {
        return itemScoreDimensions.stream().map(
                itemScoreDimension -> {
                    final Itemscoredimension legacyItemScoreDimension = new Itemscoredimension();
                    legacyItemScoreDimension.setMeasurementmodel(itemScoreDimension.getMeasurementModel());
                    legacyItemScoreDimension.setScorepoints(BigInteger.valueOf(itemScoreDimension.getScorePoints()));
                    legacyItemScoreDimension.setWeight((float) itemScoreDimension.getWeight());

                    if (itemScoreDimension.getDimension().isPresent()) {
                        legacyItemScoreDimension.setDimension(itemScoreDimension.getDimension().get());
                    }

                    final List<Itemscoreparameter> legacyParams = legacyItemScoreDimension.getItemscoreparameter();
                    itemScoreDimension.itemScoreParameters().forEach(
                            param -> {
                                final Itemscoreparameter legacyParam = new Itemscoreparameter();
                                legacyParam.setMeasurementparameter(param.getMeasurementParameter());
                                legacyParam.setValue((float) param.getValue());
                                legacyParams.add(legacyParam);
                            }
                    );

                    return legacyItemScoreDimension;
                }
        ).collect(Collectors.toList());
    }
}
