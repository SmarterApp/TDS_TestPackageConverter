package tds.testpackageconverter.converter.mappers;

import tds.testpackage.legacy.model.Bpelement;
import tds.testpackage.legacy.model.Computationrule;
import tds.testpackage.legacy.model.Identifier;
import tds.testpackage.legacy.model.Performancelevel;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackage.model.BlueprintElement;
import tds.testpackage.model.BlueprintElementTypes;
import tds.testpackage.model.Parameter;
import tds.testpackage.model.PerformanceLevel;
import tds.testpackage.model.Property;
import tds.testpackage.model.Rule;
import tds.testpackage.model.Scoring;
import tds.testpackage.model.TestPackage;
import tds.testpackage.model.Value;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class responsible for mapping between legacy (AIR) and Fairway developed test package blueprint data
 */
public class TestPackageBlueprintMapper {
    /**
     * Maps  one or more legacy {@link Testspecification} test package blueprints to a single blueprint compatible
     * with a {@link TestPackage}
     *
     * @param testPackageName    The name that should be used for the created test package
     * @param testSpecifications A collection of legacy {@link Testspecification}s
     * @return
     */
    public static List<BlueprintElement> mapBlueprint(final String testPackageName,
                                                      final List<Testspecification> testSpecifications) {

        if (testSpecifications.isEmpty()) {
            throw new IllegalArgumentException("No test test packages found. Aborting...");
        }

        // Need to find subject and grade, both of which we assume to be consistent over the set of
        // test specifications. Therefore, we just examine the first one.
        Testspecification firstSpecification = testSpecifications.get(0);
        String subject = findSingleProperty(firstSpecification.getProperty(), "subject").toUpperCase();
        String grade = findSingleProperty(firstSpecification.getProperty(), "grade");

        CsidEnhancer csidEnhancer = new CsidEnhancer(subject, grade);

        // Get a mapping of all blueprint element "unique ids" to "names".
        // In the legacy test specifications, the "uniqueid" was typically the publisher concatenated with the name.
        // For assessment/segment blueprint elements, the id was the "<assessment/segment> key" and the name
        // was the "<assessment/segment> id"
        Map<String, String> blueprintIdsToNames = testSpecifications.stream()
                .flatMap(testSpecification -> TestPackageUtils.isAdministrationPackage(testSpecification)
                        ? testSpecification.getAdministration().getTestblueprint().getBpelement().stream()
                        : testSpecification.getScoring().getTestblueprint().getBpelement().stream())
                .map(Bpelement::getIdentifier)
                .collect(Collectors.toMap(Identifier::getUniqueid, Identifier::getName, (a1, a2) -> a1));

        Map<String, Scoring> scoringMap = new HashMap<>();
        List<Testspecification> adminTestPackages = testSpecifications.stream()
                .filter(TestPackageUtils::isAdministrationPackage)
                .collect(Collectors.toList());
        Optional<Testspecification> maybeScoringPackage = testSpecifications.stream()
                .filter(TestPackageUtils::isScoringPackage)
                .findFirst();

        // We will default the packageId to the test package name, and overwrite for combined test packages
        String packageId = testPackageName;
        if (maybeScoringPackage.isPresent()) {
            mapScoring(scoringMap, maybeScoringPackage.get(), blueprintIdsToNames);
            // If this is a "package" of multiple administration test packages in one (such as with ICAs)
            if (adminTestPackages.size() > 1) {
                // In the legacy test package spec, the combined package id is only found in the scoring package
                packageId = maybeScoringPackage.get().getScoring().getTestblueprint().getBpelement().stream()
                        .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.TEST))
                        .map(bpEl -> bpEl.getIdentifier().getName())
                        .findFirst().get();
            }
        }

        /* Three cases to consider here:

            1. "test" and "segment" blueprint elements - these are structural and must be grouped under the single "package" blueprint element
                if more than one test is present in the test package. Similarly if there is only a single segment in an assessment, the "segment"
                blueprint element is optional
            2. The "claims" and "targets" (known formerly as "strands" and "content levels" - these are scoring and adaptive
                algorithm specific and are structurally grouped
            3. The miscellaneous bp element types - these include "affinity groups" and "sock" blueprint element types currently

         */
        LinkedHashSet<BlueprintElement> blueprintElements = new LinkedHashSet<>(mapTestAndSegmentBlueprintElements(adminTestPackages,
                scoringMap, packageId));
        blueprintElements.addAll(mapMiscellaneousBlueprintElements(testSpecifications, scoringMap));
        blueprintElements.addAll(mapClaimsAndTargetBlueprintElements(adminTestPackages, scoringMap, csidEnhancer));
        return new ArrayList<>(blueprintElements);
    }

    private static void mapScoring(final Map<String, Scoring> scoringMap, final Testspecification scoringPackage,
                                   final Map<String, String> blueprintIdsToNames) {
        // Blueprint id -> performance levels
        // If this is a combined TestPackage, we will use the "package" blueprint elementId, which is the same as the provided testPackageName
        Map<String, List<Performancelevel>> performanceLevelsMap = scoringPackage.getScoring().getPerformancelevels().getPerformancelevel().stream()
                .collect(Collectors.groupingBy(pl -> blueprintIdsToNames.get(pl.getBpelementid())));
        // Blueprint id -> scoring rules
        Map<String, List<Computationrule>> computationRuleMap = scoringPackage.getScoring().getScoringrules().getComputationrule().stream()
                .collect(Collectors.groupingBy(cr -> blueprintIdsToNames.containsKey(cr.getBpelementid())
                        ? blueprintIdsToNames.get(cr.getBpelementid())
                        : cr.getBpelementid()));

        computationRuleMap.forEach((key, value) -> {
            Scoring scoring = Scoring.builder()
                    .setPerformanceLevels(
                            performanceLevelsMap.containsKey(key) ? performanceLevelsMap.get(key).stream()
                                    .map(pl -> PerformanceLevel.builder()
                                            .setPLevel(pl.getPlevel().intValue())
                                            .setScaledHi(pl.getScaledhi())
                                            .setScaledLo(pl.getScaledlo())
                                            .build())
                                    .collect(Collectors.toList()) : null
                    )
                    .setRules(value.stream()
                            .map(cr -> {
                                final String ruleName = cr.getIdentifier().getName();
                                final Rule.Builder builder = Rule.builder()
                                    .setComputationOrder(cr.getComputationorder().intValue())
                                    .setName(ruleName)
                                    .setParameters(cr.getComputationruleparameter().stream()
                                        .map(param -> Parameter.builder()
                                            .setId(param.getIdentifier().getUniqueid())
                                            .setName(param.getIdentifier().getName())
                                            .setType(param.getParametertype())
                                            .setPosition(param.getPosition().intValue())
                                            .setValues(param.getComputationruleparametervalue().stream()
                                                .map(val -> Value.builder()
                                                    .setValue(val.getValue())
                                                    .setIndex(Optional.ofNullable(val.getIndex()))
                                                    .build())
                                                .collect(Collectors.toList())
                                            )
                                            .setProperties(param.getProperty().stream()
                                                .map(property -> Property.builder()
                                                    .setName(property.getName())
                                                    .setValue(property.getValue())
                                                    .build())
                                                .collect(Collectors.toList())
                                            )
                                            .build())
                                        .collect(Collectors.toList())
                                    );
                                final String measure = cr.getIdentifier().getLabel();
                                if (!measure.equals(ruleName)) {
                                    builder.setMeasure(Optional.of(measure));
                                }
                                return builder.build();
                            })
                            .collect(Collectors.toList())
                    )
                    .build();

            scoringMap.put(key, scoring);
        });
    }

    private static List<BlueprintElement> mapTestAndSegmentBlueprintElements(final List<Testspecification> testSpecifications,
                                                                             final Map<String, Scoring> scoringMap,
                                                                             final String packageId) {
        List<BlueprintElement> blueprintElements = new ArrayList<>();
        //  If the number of administration packages > 1, this is a combined test package and needs a root level "package" bpEl
        boolean isCombinedTestPackage = testSpecifications.stream()
                .filter(TestPackageUtils::isAdministrationPackage)
                .collect(Collectors.toList()).size() > 1;

        if (isCombinedTestPackage) {
            List<BlueprintElement> combinedAssessmentBlueprintElements = new ArrayList<>();
            testSpecifications.stream()
                    .filter(testSpecification -> testSpecification.getAdministration() != null)
                    .forEach(assessment -> combinedAssessmentBlueprintElements.add(mapAssessmentBlueprintElements(assessment, scoringMap)));

            // Add the root-level "package" blueprint element
            blueprintElements.add(BlueprintElement.builder()
                    .setId(packageId)
                    .setType(BlueprintElementTypes.PACKAGE)
                    .setBlueprintElements(combinedAssessmentBlueprintElements)
                    .setScoring(Optional.ofNullable(scoringMap.get(packageId)))
                    .build());

        } else { // Single assessment
            Testspecification assessment = testSpecifications.stream()
                    .filter(TestPackageUtils::isAdministrationPackage)
                    .findFirst().get();

            blueprintElements.add(mapAssessmentBlueprintElements(assessment, scoringMap));
        }

        return blueprintElements;
    }

    private static BlueprintElement mapAssessmentBlueprintElements(final Testspecification assessment,
                                                                   final Map<String, Scoring> scoringMap) {
        boolean isSegmented = assessment.getAdministration().getAdminsegment().size() > 1;

        if (isSegmented) {
            List<BlueprintElement> segmentBpElements = assessment.getAdministration().getTestblueprint().getBpelement().stream()
                    .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.SEGMENT))
                    .map(bpEl -> BlueprintElement.builder()
                            .setId(bpEl.getIdentifier().getName())
                            .setType(bpEl.getElementtype())
                            .setScoring(Optional.ofNullable(scoringMap.get(bpEl.getIdentifier().getName())))
                            .build())
                    .collect(Collectors.toList());

            return BlueprintElement.builder()
                    .setType(BlueprintElementTypes.TEST)
                    .setId(assessment.getIdentifier().getName())
                    .setBlueprintElements(segmentBpElements)
                    .setScoring(Optional.ofNullable(scoringMap.get(assessment.getIdentifier().getName())))
                    .build();
        } else { // single-segmented assessment - no "segment"  bp element required
            return BlueprintElement.builder()
                    .setType(BlueprintElementTypes.TEST)
                    .setId(assessment.getIdentifier().getName())
                    .setScoring(Optional.ofNullable(scoringMap.get(assessment.getIdentifier().getName())))
                    .build();
        }
    }


    // Map Affinity Groups, SOCK, and other miscellaneous blueprint elements
    private static List<BlueprintElement> mapMiscellaneousBlueprintElements(final List<Testspecification> testSpecifications,
                                                                            final Map<String, Scoring> scoringMap) {
        return testSpecifications.stream()
                .flatMap(testSpecification -> {
                    if (testSpecification.getAdministration() != null) {
                        return testSpecification.getAdministration().getTestblueprint().getBpelement()
                                .stream()
                                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.AFFINITY_GROUP) ||
                                        bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.SOCK))
                                .map(bpEl -> BlueprintElement.builder()
                                        .setId(bpEl.getIdentifier().getName())
                                        .setType(bpEl.getElementtype())
                                        .setScoring(Optional.ofNullable(scoringMap.get(bpEl.getIdentifier().getName())))
                                        .build());
                    } else {
                        Stream<BlueprintElement> testBlueprints = testSpecification.getScoring().getTestblueprint().getBpelement()
                                .stream()
                                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.AFFINITY_GROUP) ||
                                        bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.SOCK))
                                .map(bpEl -> BlueprintElement.builder()
                                        .setId(bpEl.getIdentifier().getName())
                                        .setType(bpEl.getElementtype())
                                        .setScoring(Optional.ofNullable(scoringMap.get(bpEl.getIdentifier().getName())))
                                        .build());
                        Stream<Computationrule> computationRules = testSpecification.getScoring().getScoringrules().
                            getComputationrule().stream().filter(cr -> testSpecification.getScoring().getTestblueprint().getBpelement().stream().noneMatch(bpelement -> bpelement.getIdentifier().getUniqueid().equalsIgnoreCase(cr.getBpelementid())));
                        Stream<BlueprintElement> computationRuleBlueprints = computationRules.map(cr -> BlueprintElement.builder()
                            .setId(cr.getBpelementid())
                            .setType(cr.getBpelementid().toLowerCase().contains(BlueprintElementTypes.SOCK) ? BlueprintElementTypes.SOCK : BlueprintElementTypes.UNKNOWN)
                            .setScoring(Optional.ofNullable(scoringMap.get(cr.getBpelementid())))
                            .build());
                            return Stream.concat(testBlueprints,computationRuleBlueprints);

                    }
                })
                .collect(Collectors.toList());
    }

    private static List<BlueprintElement> mapClaimsAndTargetBlueprintElements(final List<Testspecification> testSpecifications,
                                                                              final Map<String, Scoring> scoringMap,
                                                                              CsidEnhancer csidEnhancer) {
        // Nest and map Claims/Targets (strands/content levels in legacy)
        Map<String, List<Bpelement>> parentIdToBpElement = testSpecifications.stream()
                .flatMap(testSpecification -> {
                    if (TestPackageUtils.isAdministrationPackage(testSpecification)) {
                        return testSpecification.getAdministration().getTestblueprint().getBpelement()
                                .stream()
                                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.CONTENT_LEVEL));
                    } else {
                        return testSpecification.getScoring().getTestblueprint().getBpelement()
                                .stream()
                                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.CONTENT_LEVEL));
                    }
                })
                .distinct()
                .collect(Collectors.groupingBy(bpEl -> TestPackageUtils.parseIdFromKey(bpEl.getParentid())));

        // Map all claims - these are our root level elements
        LinkedHashSet<BlueprintElement> claimBlueprintElements = testSpecifications.stream()
                .flatMap(testSpecification -> {
                    if (TestPackageUtils.isAdministrationPackage(testSpecification)) {
                        return testSpecification.getAdministration().getTestblueprint().getBpelement()
                                .stream()
                                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.STRAND))
                                .map(bpEl -> BlueprintElement.builder()
                                        .setId(bpEl.getIdentifier().getName())
                                        .setLabel(csidEnhancer.enhance(bpEl.getIdentifier().getName()))
                                        .setType(BlueprintElementTypes.CLAIM)
                                        .setBlueprintElements(new ArrayList<>())
                                        .setScoring(Optional.ofNullable(scoringMap.get(bpEl.getIdentifier().getName())))
                                        .build()
                                );
                    } else {
                        return testSpecification.getScoring().getTestblueprint().getBpelement()
                                .stream()
                                .filter(bpEl -> bpEl.getElementtype().equalsIgnoreCase(BlueprintElementTypes.STRAND))
                                .map(bpEl -> BlueprintElement.builder()
                                        .setId(bpEl.getIdentifier().getName())
                                        .setLabel(csidEnhancer.enhance(bpEl.getIdentifier().getName()))
                                        .setType(BlueprintElementTypes.CLAIM)
                                        .setScoring(Optional.ofNullable(scoringMap.get(bpEl.getIdentifier().getName())))
                                        .setBlueprintElements(new ArrayList<>())
                                        .build()
                                );
                    }
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        claimBlueprintElements.forEach(blueprintElement ->
                mapBlueprintElement(blueprintElement, parentIdToBpElement, scoringMap, csidEnhancer));

        return new ArrayList<>(claimBlueprintElements);
    }

    // Recursively map all blueprint elements breadth-first
    private static void mapBlueprintElement(final BlueprintElement currentEl, final Map<String, List<Bpelement>> parentMap,
                                            final Map<String, Scoring> scoringMap, CsidEnhancer csidEnhancer) {
        if (parentMap.containsKey(currentEl.getId())) {
            List<BlueprintElement> blueprintElements = currentEl.blueprintElements();
            List<Bpelement> bpEl = parentMap.get(currentEl.getId());

            bpEl.forEach(childEl -> {
                BlueprintElement currBpEl = BlueprintElement.builder()
                        .setId(childEl.getIdentifier().getName())
                        .setLabel(csidEnhancer.enhance(childEl.getIdentifier().getName()))
                        .setType(BlueprintElementTypes.TARGET)
                        .setBlueprintElements(new ArrayList<>())
                        .setScoring(Optional.ofNullable(scoringMap.get(childEl.getIdentifier().getUniqueid())))
                        .build();
                blueprintElements.add(currBpEl);
                mapBlueprintElement(currBpEl, parentMap, scoringMap, csidEnhancer);
            });
        }
    }

    private static String findSingleProperty(final List<tds.testpackage.legacy.model.Property> properties, final String propertyName) {
        return properties.stream()
                .filter(property -> property.getName().equalsIgnoreCase(propertyName))
                .map(tds.testpackage.legacy.model.Property::getValue)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No subject property was found in the test specification package"));
    }
}
