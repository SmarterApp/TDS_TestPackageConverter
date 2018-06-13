package tds.testpackageconverter.converter.mappers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tds.common.Algorithm;
import tds.testpackage.diff.TestPackageDiff;
import tds.testpackage.legacy.model.*;
import tds.testpackage.legacy.model.Property;
import tds.testpackage.model.*;

import tds.testpackageconverter.utils.TestPackageUtils;

import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class responsible for mapping between legacy (AIR) and Fairway developed test packages
 */
public class TestPackageMapper {
    private static final Logger log = LoggerFactory.getLogger(TestPackageMapper.class);

    private static final Map<String, String> languageLabelMap = ImmutableMap.of(
            "ENU", "English",
            "ESN", "Spanish",
            "ENU-Braille", "Braille"
    );

    /**
     * Maps one or more legacy {@link Testspecification}s to a {@link TestPackage}. At minimum one administration legacy
     * test packages are required for the conversion. If a scoring package is identified, scoring data will be added to the
     * test blueprint
     *
     * @param testPackageName    The name that should be used for the created test package
     * @param testSpecifications A collection of legacy {@link Testspecification}s
     * @return The converted {@link TestPackage}
     */
    public static TestPackage toNew(final String testPackageName, final List<Testspecification> testSpecifications) throws ParseException {
        return toNew(testPackageName, testSpecifications, Optional.empty());
    }

    /**
     * Maps one or more legacy {@link Testspecification}s to a {@link TestPackage}. At minimum one administration legacy
     * test packages are required for the conversion. If a scoring package is identified, scoring data will be added to the
     * test blueprint
     *
     * @param testPackageName    The name that should be used for the created test package
     * @param testSpecifications A collection of legacy {@link Testspecification}s
     * @return The converted {@link TestPackage}
     */
    public static TestPackage toNew(final String testPackageName, final List<Testspecification> testSpecifications, final TestPackageDiff diff) throws ParseException {
        return toNew(testPackageName, testSpecifications, Optional.of(diff));
    }

    private static TestPackage toNew(final String testPackageName, final List<Testspecification> testSpecifications, final Optional<TestPackageDiff> diff) throws ParseException {
        List<Testspecification> adminTestPackages = testSpecifications.stream()
                .filter(TestPackageUtils::isAdministrationPackage)
                .collect(Collectors.toList());
        List<Testspecification> scoringTestPackages = testSpecifications.stream()
                .filter(TestPackageUtils::isScoringPackage)
                .collect(Collectors.toList());

        if (adminTestPackages.isEmpty()) {
            throw new IllegalArgumentException("No administration test packages found. Aborting...");
        }

        log.debug("Converting {} administration and {} scoring packages to a new TestPackage called {}",
                adminTestPackages.size(), scoringTestPackages.size(), testPackageName);
        log.debug("Administration: ");
        adminTestPackages.forEach(admin -> log.debug("\t {}", admin.getIdentifier().getUniqueid()));
        log.debug("Administration: ");
        scoringTestPackages.forEach(scoring -> log.debug("\t {}", scoring.getIdentifier().getUniqueid()));

        // We will assume that the first "test specification" contains the root-level "TestPackage" metadata such as
        // bank key and publisher information
        Testspecification testSpecification = adminTestPackages.get(0);
        return TestPackage.builder()
                .setId(testPackageName.replace(".xml", ""))
                /* Attributes */
                .setVersion(TestPackageUtils.parseVersion(testSpecification.getIdentifier().getVersion()))
                .setPublisher(testSpecification.getPublisher())
                .setPublishDate(TestPackageUtils.formatDate(testSpecification.getPublishdate()))
                // This value is not found in the legacy spec, but is required in the new spec
                // we will use the current year as a placeholder
                .setAcademicYear(diff.isPresent()
                        ? diff.get().getAcademicYear()
                        : String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))
                .setSubType(diff.isPresent() ? diff.get().getSubType() : Optional.empty())
                .setSubject(findSingleProperty(testSpecification.getProperty(), "subject"))
                .setType(findSingleProperty(testSpecification.getProperty(), "type"))
                .setBankKey(findBankKey(testSpecification.getAdministration()))
                /* Child Elements */
                .setAssessments(mapAssessments(adminTestPackages, diff))
                .setBlueprint(TestPackageBlueprintMapper.mapBlueprint(testPackageName, testSpecifications))
                .build();
    }

    private static List<Assessment> mapAssessments(final List<Testspecification> adminTestPackages, final Optional<TestPackageDiff> diff) {
        return adminTestPackages.stream().map(testSpecification -> {
            final String id = testSpecification.getIdentifier().getName();
            final List<Tool> tools = diff.flatMap(d ->
                    d.getAssessments().stream().filter(a -> a.getId().equals(id))
                            .map(tds.testpackage.diff.Assessment::tools).findFirst()).orElse(Collections.emptyList());
            return Assessment.builder()
                    /* Attributes */
                    .setId(id)
                    .setLabel(testSpecification.getIdentifier().getLabel())
                    /* Child Elements */
                    .setGrades(mapGrades(testSpecification.getProperty()))
                    .setSegments(mapSegments(testSpecification.getAdministration(), diff))
                    .setTools(tools)
                    .build();
        }).collect(Collectors.toList());
    }

    private static List<Segment> mapSegments(final Administration administration, final Optional<TestPackageDiff> diff) {
        return administration.getAdminsegment().stream()
                .map(adminSegment -> {
                    boolean isFixedForm = adminSegment.getItemselection().equalsIgnoreCase(Algorithm.FIXED_FORM.getType());
                    Map<String, String> blueprintIdsToNames = administration.getTestblueprint().getBpelement().stream()
                            .map(Bpelement::getIdentifier)
                            .collect(Collectors.toMap(Identifier::getUniqueid, Identifier::getName, (a1, a2) -> a1));
                    final List<Tool> tools = diff.isPresent()
                            ? diff.get().getAssessments().stream()
                                .flatMap(a -> a.getSegments().stream()
                                        .filter(s -> s.getId().equals(adminSegment.getSegmentid()))
                                        .flatMap(segment -> segment.tools().stream()))
                                .collect(Collectors.toList())
                            : Collections.emptyList();
                    final String segmentId = blueprintIdsToNames.get(adminSegment.getSegmentid());

                    return Segment.builder()
                            /* Attributes */
                            // The legacy spec calls the segmentKey a "segment id". We need to fetch the actual segmentId from the blueprint
                            .setId(findSegmentIdFromBlueprint(adminSegment.getSegmentid(), administration.getTestblueprint().getBpelement()))
                            .setPosition(Integer.parseInt(adminSegment.getPosition()))
                            .setAlgorithmType(adminSegment.getItemselection().equalsIgnoreCase(Algorithm.ADAPTIVE_2.getType())
                                    ? "adaptive" : adminSegment.getItemselection())
                            .setAlgorithmImplementation(adminSegment.getItemselector().getIdentifier().getUniqueid())
                            .setLabel(diff.isPresent()
                                    ?  diff.get().getAssessments().stream()
                                        .flatMap(a -> a.getSegments().stream())
                                        .filter(s -> s.getId().equals(segmentId))
                                        .map(tds.testpackage.diff.Segment::getLabel)
                                        .findFirst().orElse(Optional.of(adminSegment.getSegmentid()))
                                    : Optional.of(adminSegment.getSegmentid()))
                            /* Child Elements */
                            .setPool(isFixedForm ? null : mapItemGroups(adminSegment.getSegmentpool().getItemgroup(),
                                    administration.getItempool(), blueprintIdsToNames, null, diff))
                            .setSegmentForms(mapSegmentForms(adminSegment.getSegmentform(),
                                    administration.getTestform(), administration.getItempool(), blueprintIdsToNames, diff))
                            .setSegmentBlueprint(mapSegmentBlueprint(adminSegment, administration.getTestblueprint().getBpelement()))
                            .setEntryApproval(diff.isPresent()
                                ? diff.get().getAssessments().stream()
                                    .flatMap(a -> a.getSegments().stream())
                                            .filter(s -> s.getId().equals(segmentId))
                                            .map(tds.testpackage.diff.Segment::entryApproval)
                                            .findFirst().orElse(false)
                                : false)
                            .setExitApproval(diff.isPresent()
                                    ? diff.get().getAssessments().stream()
                                        .flatMap(a -> a.getSegments().stream())
                                            .filter(s -> s.getId().equals(segmentId))
                                            .map(tds.testpackage.diff.Segment::exitApproval)
                                            .findFirst().orElse(false)
                                    : false)
                            .setTools(tools)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static List<SegmentBlueprintElement> mapSegmentBlueprint(final Adminsegment adminsegment, List<Bpelement> bpElements) {

        Map<String, Itemselectionparameter> bpIdsToItemSelection = adminsegment.getItemselector().getItemselectionparameter().stream()
                .collect(Collectors.toMap(param -> param.getBpelementid().equals(adminsegment.getSegmentid())
                        ? findSegmentIdFromBlueprint(adminsegment.getSegmentid(), bpElements)
                        : TestPackageUtils.parseIdFromKey(param.getBpelementid()), Function.identity()));

        List<SegmentBlueprintElement> segmentBlueprintElements = adminsegment.getSegmentblueprint().getSegmentbpelement().stream()
                .map(legacySegmentBp -> SegmentBlueprintElement.builder()
                        // The part before the "-" is the publisher/clientname - lets strip that out.
                        .setIdRef(TestPackageUtils.parseIdFromKey(legacySegmentBp.getBpelementid()))
                        .setMaxExamItems(legacySegmentBp.getMaxopitems().intValue())
                        .setMinExamItems(legacySegmentBp.getMinopitems().intValue())
                        .setMaxFieldTestItems(legacySegmentBp.getMaxftitems() != null
                                ? Optional.of(legacySegmentBp.getMaxftitems().intValue()) : Optional.empty())
                        .setMinFieldTestItems(legacySegmentBp.getMinftitems() != null
                                ? Optional.of(legacySegmentBp.getMinftitems().intValue()) : Optional.empty())
                        .setItemSelection(!bpIdsToItemSelection.containsKey(TestPackageUtils.parseIdFromKey(legacySegmentBp.getBpelementid()))
                                ? null : bpIdsToItemSelection.get(TestPackageUtils.parseIdFromKey(legacySegmentBp.getBpelementid())).getProperty().stream()
                                .map(property -> tds.testpackage.model.Property.builder()
                                        .setName(property.getName())
                                        .setValue(property.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build()
                )
                .collect(Collectors.toList());

        Bpelement segmentBpElement = bpElements.stream()
                .filter(bpEl -> bpEl.getIdentifier().getUniqueid().equals(adminsegment.getSegmentid()))
                .findFirst().get();

        // Add the segment blueprint explicitly
        segmentBlueprintElements.add(SegmentBlueprintElement.builder()
                .setIdRef(segmentBpElement.getIdentifier().getName())
                .setMinExamItems(segmentBpElement.getMinopitems().intValue())
                .setMaxExamItems(segmentBpElement.getMaxopitems().intValue())
                .setMinFieldTestItems(Optional.of(segmentBpElement.getMinftitems().intValue()))
                .setMaxFieldTestItems(Optional.of(segmentBpElement.getMaxftitems().intValue()))
                .setItemSelection(bpIdsToItemSelection.get(segmentBpElement.getIdentifier().getName()).getProperty().stream()
                        .map(prop -> tds.testpackage.model.Property.builder()
                                .setName(prop.getName())
                                .setValue(prop.getValue())
                                .build())
                        .collect(Collectors.toList()))
                .build());
        return segmentBlueprintElements;
    }

    // Add new item to an existing list of items.
    // Used in the merging of segment forms.
    // If the new item already exists in the item list,
    // only add the new item's presentation to the existing item in the list.
    private static List<Item> addItem(final List<Item> items, final Item newItem) {
        final Optional<Item> duplicateOptional = items.stream().filter(i -> i.getId().equals(newItem.getId())).findFirst();
        if (!duplicateOptional.isPresent()) {
            items.add(newItem);
            return items;
        }

        return items.stream().map(item -> {
            if (item.getId().equals(newItem.getId())) {
                final Set<Presentation> presentations = new HashSet<>(item.getPresentations());
                presentations.addAll(newItem.getPresentations());
                return item.withPresentations(new ArrayList<>(presentations));
            }
            return item;
        }).collect(Collectors.toList());
    }

    // Given two lists of items, merge them into one list of items.
    // If items have the same ids, combine them into one item.
    private static List<Item> mergeItems(final List<Item> acc, final LinkedList<Item> items) {
        if (!items.isEmpty()) {
            final Item head = items.pop();
            return mergeItems(addItem(acc, head), items);
        }
        return acc;
    }


    private static ItemGroup mergeItemGroups(final ItemGroup itemGroup, final ItemGroup itemGroup1) {
        final List<Item> mergedItems = mergeItems(itemGroup.items(), new LinkedList<>(itemGroup1.items()));
        return itemGroup.withItems(mergedItems);
    }

    private static SegmentForm mergeSegmentForms(
            final SegmentForm existingSegmentForm,
            final SegmentForm newSegmentForm) {

        final Set<Presentation> presentationSet = new HashSet<>(existingSegmentForm.getPresentations());
        presentationSet.addAll(newSegmentForm.getPresentations());

        final List<ItemGroup> mergedItemGroups = existingSegmentForm.itemGroups().stream().map(itemGroup0 -> {
            final Optional<ItemGroup> itemGroupOptional = newSegmentForm.itemGroups().stream().filter(itemGroup1 -> itemGroup0.getId().equals(itemGroup1.getId())).findFirst();
            return itemGroupOptional.map(duplicate -> mergeItemGroups(itemGroup0, duplicate)).orElse(itemGroup0);
        }).collect(Collectors.toList());

        return existingSegmentForm.toBuilder().
                setPresentations(new ArrayList<>(presentationSet)).
                setItemGroups(mergedItemGroups).
                build();
    }


    private static List<SegmentForm> mergeDuplicateSegmentForms(final List<SegmentForm> segmentForms) {
        final Map<String, List<SegmentForm>> segmentFormsByCohort = segmentForms.stream().collect(Collectors.groupingBy(SegmentForm::getCohort));
        final Stream<Optional<SegmentForm>> mergedSegmentForms = segmentFormsByCohort.entrySet().stream().map(entry -> entry.getValue().stream().reduce(TestPackageMapper::mergeSegmentForms));
        return mergedSegmentForms.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private static List<SegmentForm> mapSegmentForms(final List<Segmentform> legacySegmentForms, final List<Testform> testForms,
                                                     final Itempool itemPool, final Map<String, String> bluePrintIdsToNames, final Optional<TestPackageDiff> diff) {
        final List<SegmentForm> segmentForms = new ArrayList<>();

        // Get the set of unique formKeys - usually in a similar format as item/stimuli keys (<bankKey>-<formId>)
        final Set<String> formPartitionIds = legacySegmentForms.stream().map(Segmentform::getFormpartitionid).collect(Collectors.toSet());
        // We need data from both the parent "Testform" and "Formpartition". This map contains a mapping from the
        // form partition id to it's parent testform
        final Map<String, Testform> testFormMap = new HashMap<>();
        testForms.forEach(testForm ->
                testForm.getFormpartition().stream()
                        .filter(formPartition -> formPartitionIds.contains(formPartition.getIdentifier().getUniqueid()))
                        .forEach(formPartition -> testFormMap.put(formPartition.getIdentifier().getUniqueid(), testForm))
        );

        for (Segmentform legacyForm : legacySegmentForms) {
            Testform testForm = testFormMap.get(legacyForm.getFormpartitionid());
            // Find the particular form partition we are dealing with from within it's parent Testform
            Formpartition formPartition = testForm.getFormpartition().stream()
                    .filter(fp -> fp.getIdentifier().getUniqueid().equalsIgnoreCase(legacyForm.getFormpartitionid()))
                    .findFirst().orElseThrow(IllegalArgumentException::new);

            final Property languageProperty = testForm.getProperty().stream().filter(p -> p.getName().equalsIgnoreCase("language")).findFirst().get();
            final String presentationCode = languageProperty.getValue();
            final String presentationLabel = languageLabelMap.containsKey(languageProperty.getValue())
                    ? languageLabelMap.get(languageProperty.getValue())
                    : languageProperty.getLabel();
            Presentation presentation = Presentation.builder().setCode(presentationCode).setLabel(presentationLabel).build();
            segmentForms.add(SegmentForm.builder()
                    .setCohort(TestPackageUtils.parseCohort(testForm.getIdentifier().getUniqueid()))
                    .setId(formPartition.getIdentifier().getName())
                    .setPresentations(Collections.singletonList(presentation))
                    .setItemGroups(mapItemGroups(formPartition.getItemgroup(), itemPool, bluePrintIdsToNames, presentation, diff))
                    .build());
        }

        return mergeDuplicateSegmentForms(segmentForms);
    }

    private static List<ItemGroup> mapItemGroups(final List<Itemgroup> itemGroups, final Itempool itemPool,
                                                 final Map<String, String> bluePrintIdsToNames, final Presentation presentation,
                                                 final Optional<TestPackageDiff> diff) {
        return itemGroups.stream()
                .filter(itemGroup -> !itemGroupContainsNonExistentItem(itemGroup, itemPool))
                .map(ig ->
                        ItemGroup.builder()
                                // If legacy itemgroup id is "G-187-1234-0", parse out "1234"
                                .setId(TestPackageUtils.parseItemGroupId(ig.getIdentifier().getUniqueid()))
                                .setMaxItems(Optional.of(ig.getMaxitems()))
                                .setMaxResponses(Optional.of(ig.getMaxresponses()))
                                .setStimulus(mapStimuli(ig.getPassageref()))
                                .setItems(mapItems(ig.getGroupitem(), itemPool, bluePrintIdsToNames, presentation, diff))
                                .build())
                .collect(Collectors.toList());
    }

    private static boolean itemGroupContainsNonExistentItem(final Itemgroup itemgroup, final Itempool itemPool) {
        Map<String, Testitem> testItemMap = itemPool.getTestitem().stream()
                .collect(Collectors.toMap(ti -> ti.getIdentifier().getUniqueid(), Function.identity()));

        return itemgroup.getGroupitem().stream()
                .anyMatch(item -> !testItemMap.containsKey(item.getItemid()));
    }

    private static List<Item> mapItems(final List<Groupitem> groupItems, final Itempool itemPool,
                                       final Map<String, String> bluePrintIdsToNames, final Presentation presentation,
                                       final Optional<TestPackageDiff> diff) {
        Set<String> groupItemIds = groupItems.stream().map(Groupitem::getItemid).collect(Collectors.toSet());
        Map<String, Testitem> testItemMap = itemPool.getTestitem().stream()
                .filter(testItem -> groupItemIds.contains(testItem.getIdentifier().getUniqueid()))
                .collect(Collectors.toMap(ti -> ti.getIdentifier().getUniqueid(), Function.identity()));

        return groupItems.stream()
                .peek(gi -> {
                    if (!testItemMap.containsKey(gi.getItemid())) {
                        log.warn("No item defined in the item pool for the item {}. Skipping...",
                                gi.getItemid());
                    }
                })
                .filter(gi -> testItemMap.containsKey(gi.getItemid()))
                .map(gi -> {
                    final Testitem item = testItemMap.get(gi.getItemid());
                    final Optional<tds.testpackage.diff.Item> diffItem = diff.isPresent()
                            ? diff.get().getAssessments().stream()
                                .flatMap(assessment -> assessment.getSegments().stream()
                                        .flatMap(segment -> segment.items().stream()
                                                .filter(i -> String.format("%s-%s", diff.get().getBankKey(), i.getId()).equals(gi.getItemid()))
                                        )
                                ).findFirst()
                            : Optional.empty();
                    return Item.builder()
                            // If the item key is "187-1234" the item ID is "1234"
                            .setId(TestPackageUtils.parseIdFromKey(gi.getItemid()))
                            .setAdministrationRequired(Optional.ofNullable(gi.getAdminrequired()))
                            .setFieldTest(Optional.ofNullable(gi.getIsfieldtest()))
                            .setActive(Optional.ofNullable(gi.getIsactive()))
                            .setHandScored(Optional.of(
                                    // Check if any "HandScored" poolproperty is defined
                                    String.valueOf(item.getPoolproperty().stream()
                                            .anyMatch(prop -> prop.getValue().equals("HandScored")))))
                            .setResponseRequired(Optional.ofNullable(gi.getResponserequired()))
                            .setType(item.getItemtype())
                            .setPresentations((presentation != null)
                                    ? Collections.singletonList(presentation)
                                    : mapPresentations(item.getPoolproperty()))
                            .setItemScoreDimensions(mapItemScoreDimensions(item.getItemscoredimension()))
                            .setPoolProperties(mapPoolProperties(item.getPoolproperty()))
                            .setBlueprintReferences(mapBlueprintReferences(item.getBpref(), bluePrintIdsToNames))
                            .setDoNotScore(diffItem.map(i -> Optional.of(String.valueOf(i.doNotScore())))
                                    .orElseGet(() -> Optional.of(String.valueOf(false))))
                            .setTeacherHandScoring(diffItem.isPresent()
                                ? diffItem.get().getTeacherHandScoring()
                                : Optional.empty())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static List<PoolProperty> mapPoolProperties(final List<Poolproperty> legacyPoolProperties) {
        return legacyPoolProperties.stream()
                .filter(prop -> !prop.getProperty().equals("--ITEMTYPE--") && !prop.getProperty().equals("Language"))
                .map(prop -> PoolProperty.builder()
                        .setName(prop.getProperty())
                        .setValue(prop.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<BlueprintReference> mapBlueprintReferences(final List<Bpref> bpref,
                                                                   final Map<String, String> bluePrintIdsToNames) {
        return bpref.stream()
                .map(bpRef -> BlueprintReference.builder()
                        .setIdRef(bluePrintIdsToNames.get(bpRef.getContent()))
                        .build())
                .collect(Collectors.toList());
    }

    private static List<ItemScoreDimension> mapItemScoreDimensions(final List<Itemscoredimension> legacyISDs) {
        return legacyISDs.stream().map(
                legacyISD -> ItemScoreDimension.builder()
                        .setMeasurementModel(legacyISD.getMeasurementmodel())
                        .setScorePoints(legacyISD.getScorepoints().intValue())
                        .setWeight(legacyISD.getWeight())
                        .setDimension(legacyISD.getDimension() == null ? Optional.empty() : Optional.of(legacyISD.getDimension()))
                        .setItemScoreParameters(
                                // map the legacy item score params
                                legacyISD.getItemscoreparameter().stream()
                                        .map(param -> ItemScoreParameter.builder()
                                                .setMeasurementParameter(param.getMeasurementparameter())
                                                .setValue(param.getValue())
                                                .build())
                                        .collect(Collectors.toList()))
                        .build()
        ).collect(Collectors.toList());
    }

    private static List<Presentation> mapPresentations(final List<Poolproperty> poolProperties) {
        return poolProperties.stream()
                .filter(property -> property.getProperty().equalsIgnoreCase("Language"))
                .map(property -> Presentation.builder()
                        .setCode(property.getValue())
                        .setLabel(property.getLabel())
                        .build())
                .collect(Collectors.toList());
    }

    private static Optional<Stimulus> mapStimuli(final List<Passageref> passageRefs) {
        if (passageRefs.size() == 0) {
            return Optional.empty();
        }

        // The stimulus id is the last part of the stimulus key (after the "-")
        final String stimuliKey = passageRefs.get(0).getContent();
        return Optional.of(Stimulus.builder()
                .setId(TestPackageUtils.parseIdFromKey(stimuliKey))
                .build());
    }

    private static List<Grade> mapGrades(final List<Property> properties) {
        return properties.stream()
                .filter(property -> property.getName().equalsIgnoreCase("grade"))
                .map(property -> Grade.builder()
                        .setValue(property.getValue())
                        .setLabel(property.getLabel() != null ? Optional.of(property.getLabel()) : Optional.empty())
                        .build())
                .collect(Collectors.toList());
    }

    private static String findSegmentIdFromBlueprint(final String segmentKey, final List<Bpelement> bpElements) {
        return bpElements.stream()
                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.SEGMENT)
                        && bpEl.getIdentifier().getUniqueid().equalsIgnoreCase(segmentKey))
                .map(bpEl -> bpEl.getIdentifier().getName())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Could not find a segment id in the blueprint for the segment with key " + segmentKey));
    }

    private static int findBankKey(final Administration administration) {
        // We will assume the bank key is the same for all items/forms. In our case we'll just get the first item in the item
        // pool and peek at it's item key. The bank key is the first part of the item key (e.g., "187" in "187-1234")
        final String anyItemId = administration.getItempool().getTestitem().get(0).getIdentifier().getUniqueid();
        return Integer.parseInt(anyItemId.substring(0, anyItemId.indexOf("-")));
    }

    private static String findSingleProperty(final List<Property> properties, final String propertyName) {
        return properties.stream()
                .filter(property -> property.getName().equalsIgnoreCase(propertyName))
                .map(Property::getValue)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No subject property was found in the test specification package"));
    }
}
