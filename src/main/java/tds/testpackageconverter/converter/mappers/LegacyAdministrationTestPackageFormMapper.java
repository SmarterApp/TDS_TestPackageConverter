package tds.testpackageconverter.converter.mappers;

import org.assertj.core.util.Lists;
import tds.common.Algorithm;
import tds.testpackage.legacy.model.*;
import tds.testpackage.legacy.model.Property;
import tds.testpackage.model.*;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LegacyAdministrationTestPackageFormMapper {
    public static List<Testform> mapTestForms(final TestPackage testPackage, final Assessment assessment,
                                              final Map<String, Long> formIdToKeyMap) {

        final Map<String, Testform> testFormMap = new HashMap<>();

        assessment.getSegments().stream()
                .filter(segment -> segment.getAlgorithmType().equalsIgnoreCase(Algorithm.FIXED_FORM.getType()))
                .forEach(segment -> segment.segmentForms()
                        .forEach(form -> form.getPresentations()
                                .forEach(presentation -> {
                                    final String testFormId = String.format("%s:%s-%s", assessment.getKey(), form.getCohort(), presentation.getCode());

                                    if (!testFormMap.containsKey(testFormId)) {
                                        final Testform testform = new Testform();
                                        // Get the item count of the items with the same language
                                        final long itemCount = form.itemGroups().stream()
                                                .mapToLong(itemGroup -> itemGroup.items().stream()
                                                        .filter(item -> item.getPresentations().contains(presentation))
                                                        .collect(Collectors.toList())
                                                        .size())
                                                .sum();

                                        testform.setLength(BigInteger.valueOf(itemCount));
                                        final Identifier testFormIdentifier = new Identifier();
                                        testFormIdentifier.setVersion(new BigDecimal(testPackage.getVersion()));
                                        testFormIdentifier.setUniqueid(testFormId);
                                        testFormIdentifier.setName(testFormId);
                                        testform.setIdentifier(testFormIdentifier);
                                        testform.getFormpartition().add(mapFormPartition(form, formIdToKeyMap, presentation,
                                                testPackage.getVersion(), testPackage.getBankKey()));

                                        Property property = new Property();
                                        property.setName("Language");
                                        property.setValue(presentation.getCode());
                                        property.setLabel(presentation.label());
                                        testform.getProperty().add(property);
                                        testFormMap.put(testFormId, testform);

                                    } else {
                                        final Testform testform = testFormMap.get(testFormId);
                                        // Get the item count of the items with the same language
                                        final long itemCount = form.itemGroups().stream()
                                                .mapToLong(itemGroup -> itemGroup.items().stream()
                                                        .filter(item -> item.getPresentations().contains(presentation))
                                                        .collect(Collectors.toList())
                                                        .size())
                                                .sum();
                                        final BigInteger newLength = BigInteger.valueOf(testform.getLength().longValue() + itemCount);
                                        testform.setLength(newLength);
                                        testform.getFormpartition().add(mapFormPartition(form, formIdToKeyMap, presentation,
                                                testPackage.getVersion(), testPackage.getBankKey()));

                                    }
                                })
                        )
                );

        return Lists.newArrayList(testFormMap.values());
    }

    private static Formpartition mapFormPartition(final SegmentForm form, final Map<String, Long> formIdToKeyMap,
                                                  final Presentation presentation,
                                                  final String version, final int bankKey) {
        final Formpartition formPartition = new Formpartition();
        final Identifier formPartitionIdentifier = new Identifier();
        final String formId = form.id(presentation.getCode());
        final String formKey = String.format("%s-%s", bankKey, formIdToKeyMap.get(formId));
        formPartitionIdentifier.setUniqueid(formKey);
        formPartitionIdentifier.setName(formId);
        formPartitionIdentifier.setVersion(new BigDecimal(version));
        formPartition.setIdentifier(formPartitionIdentifier);
        final List<Itemgroup> legacyItemGroups = formPartition.getItemgroup();

        int formPosition = 1;
        int itemGroupFormPosition = 1;
        for (ItemGroup itemGroup : form.itemGroups()) {
            // If the item group includes item with presentations that do not match, skip it
            if (itemGroup.items().stream().anyMatch(item -> !item.getPresentations().contains(presentation))) {
                continue;
            }

            final Itemgroup legacyItemGroup = new Itemgroup();
            legacyItemGroup.setFormposition(String.valueOf(itemGroupFormPosition));
            legacyItemGroup.setMaxitems(itemGroup.maxItems());
            legacyItemGroup.setMaxresponses(itemGroup.maxResponses());

            final Identifier itemGroupIdentifier = new Identifier();

            final String itemGroupId = itemGroup.getStimulus().isPresent()
                    ? String.format("%s:G-%s-%s-0", formKey, bankKey, itemGroup.getId())
                    : String.format("%s:I-%s-%s", formKey, bankKey, itemGroup.getId());
            itemGroupIdentifier.setUniqueid(itemGroupId);
            itemGroupIdentifier.setName(itemGroupId);
            itemGroupIdentifier.setVersion(new BigDecimal(version));
            legacyItemGroup.setIdentifier(itemGroupIdentifier);

            // Map passage reference
            LegacyAdministrationTestPackageSegmentMapper.mapPassageRef(itemGroup, legacyItemGroup);

            // Map items
            for (int itemPositionInGroup = 0; itemPositionInGroup < itemGroup.items().size(); itemPositionInGroup++) {
                final Item item = itemGroup.items().get(itemPositionInGroup);
                final Groupitem groupItem = new Groupitem();
                groupItem.setItemid(item.getKey());
                groupItem.setFormposition(BigInteger.valueOf(formPosition++));
                groupItem.setGroupposition(String.valueOf(itemPositionInGroup + 1));
                groupItem.setAdminrequired(String.valueOf(item.administrationRequired()));
                groupItem.setResponserequired(String.valueOf(item.responseRequired()));
                groupItem.setIsactive(String.valueOf(item.active()));
                groupItem.setIsfieldtest(String.valueOf(item.fieldTest()));
                groupItem.setBlockid("A");
                legacyItemGroup.getGroupitem().add(groupItem);
            }
            itemGroupFormPosition++;
            legacyItemGroups.add(legacyItemGroup);
        }

        return formPartition;
    }
}
