package tds.testpackageconverter.converter.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tds.testpackage.legacy.model.*;
import tds.testpackage.legacy.model.Property;
import tds.testpackage.legacy.model.Scoring;
import tds.testpackage.model.*;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static tds.testpackageconverter.utils.TestPackageUtils.formatDate;

public class LegacyScoringTestPackageMapper {
    private static final Logger log = LoggerFactory.getLogger(LegacyScoringTestPackageMapper.class);

    public static Testspecification fromNew(final TestPackage testPackage,
                                            final List<Testspecification> administrationPackages) {
        final BigDecimal version = new BigDecimal(testPackage.getVersion());
        final Testspecification scoringSpecification = new Testspecification();
        scoringSpecification.setPurpose("scoring");
        scoringSpecification.setPublisher(testPackage.getPublisher());
        try {
            scoringSpecification.setPublishdate(formatDate(Instant.parse(testPackage.getPublishDate())));
        } catch (ParseException e) {
            log.warn("Unable to parse publish date time. Adding date without parsing");
            scoringSpecification.setPublishdate(testPackage.getPublishDate());
        }
        scoringSpecification.setVersion(new BigDecimal(1));

        // Map Identifier
        final Identifier testSpecIdentifier = new Identifier();
        testSpecIdentifier.setUniqueid(administrationPackages.size() > 1
                ? String.format("(%s)%s-%s", testPackage.getPublisher(), testPackage.getId(), testPackage.getAcademicYear())
                : administrationPackages.get(0).getIdentifier().getUniqueid());
        testSpecIdentifier.setName(administrationPackages.size() > 1
                ? testPackage.getId()
                : administrationPackages.get(0).getIdentifier().getName());
        testSpecIdentifier.setVersion(version);
        testSpecIdentifier.setLabel(testPackage.getId());
        scoringSpecification.setIdentifier(testSpecIdentifier);

        // Map test spec properties
        final List<Property> properties = scoringSpecification.getProperty();
        // Subject
        final Property subjectProperty = new Property();
        subjectProperty.setName("subject");
        subjectProperty.setValue(testPackage.getSubject());
        subjectProperty.setLabel(testPackage.getSubject());
        properties.add(subjectProperty);

        // Test type
        final Property typeProperty = new Property();
        typeProperty.setName("type");
        typeProperty.setValue(testPackage.getType());
        typeProperty.setLabel(testPackage.getType());
        properties.add(typeProperty);

        //Grade(s)
        testPackage.getAssessments().forEach(assessment -> assessment.getGrades()
                .forEach(grade -> {
                    Property gradeProperty = new Property();
                    gradeProperty.setName("grade");
                    gradeProperty.setValue(grade.getValue());
                    gradeProperty.setLabel(grade.getValue());
                    properties.add(gradeProperty);
                })
        );

        scoringSpecification.setScoring(mapScoring(testPackage, administrationPackages));

        return scoringSpecification;
    }

    private static Scoring mapScoring(final TestPackage testPackage,
                                      final List<Testspecification> administrationPackages) {
        final Map<String, String> bpElementNameMap = administrationPackages.stream()
                .flatMap(adminPackage -> adminPackage.getAdministration().getTestblueprint().getBpelement().stream())
                .collect(Collectors.toMap(
                        bpEl -> bpEl.getIdentifier().getName(),
                        bpEl -> bpEl.getIdentifier().getUniqueid(),
                        (x1, x2) -> x1));

        final Scoring scoring = new Scoring();
        scoring.setTestblueprint(LegacyScoringTestPackageBlueprintMapper.mapBlueprint(testPackage, administrationPackages));
        scoring.setItempool(LegacyScoringTestPackageItemPoolMapper.mapItemPool(testPackage, administrationPackages));
        scoring.setPerformancelevels(mapPerformanceLevels(testPackage, bpElementNameMap));
        scoring.setScoringrules(mapScoringRules(testPackage, bpElementNameMap));
        scoring.getTestform().addAll(LegacyScoringTestPackageFormMapper.mapTestForms(testPackage, administrationPackages));

        return scoring;
    }

    private static Scoringrules mapScoringRules(final TestPackage testPackage, final Map<String, String> bpElementsMap) {
        final Map<String, BlueprintElement> blueprintMap = testPackage.getBlueprintMap();
        final Scoringrules rules = new Scoringrules();

        blueprintMap.values().stream()
                .filter(blueprintElement -> blueprintElement.getScoring().isPresent())
                .forEach(blueprintElement -> blueprintElement.getScoring().get().getRules()
                        .forEach(rule -> {
                            final Computationrule legacyRule = new Computationrule();
                            // If the element is a test/segment, get the "combined" blueprint element id. Otherwise, just find the blueprint
                            // element uniqueId that corresponds to the blueprint element name in the legacy blueprint
                            if (blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.PACKAGE)) {
                                legacyRule.setBpelementid(TestPackageUtils.getAssessmentKey(testPackage, testPackage.getId()));
                            } else if (blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.TEST) ||
                                    blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.SEGMENT)) {
                                legacyRule.setBpelementid(testPackage.getAssessments().size() > 1
                                        ? TestPackageUtils.getCombinedKey(testPackage, blueprintElement.getId())
                                        : TestPackageUtils.getAssessmentKey(testPackage, blueprintElement.getId()));
                            } else {
                                legacyRule.setBpelementid(bpElementsMap.containsKey(blueprintElement.getId())
                                        ? bpElementsMap.get(blueprintElement.getId())
                                        : blueprintElement.getId());
                            }

                            legacyRule.setComputationorder(BigInteger.valueOf(rule.getComputationOrder()));

                            final Identifier identifier = new Identifier();
                            identifier.setUniqueid(UUID.randomUUID().toString());
                            identifier.setName(rule.getName());
                            identifier.setLabel(rule.getName());
                            identifier.setVersion(new BigDecimal(1));

                            legacyRule.setIdentifier(identifier);

                            legacyRule.getComputationruleparameter().addAll(mapRuleParameters(rule));

                            rules.getComputationrule().add(legacyRule);
                        }));

        return rules;
    }

    private static List<Computationruleparameter> mapRuleParameters(final Rule rule) {
        return rule.parameters().stream()
                .map(param -> {
                    final Computationruleparameter legacyParam = new Computationruleparameter();
                    legacyParam.setPosition(BigInteger.valueOf(param.getPosition()));
                    legacyParam.setParametertype(param.getType());

                    final Identifier identifier = new Identifier();
                    identifier.setUniqueid(param.getId());
                    identifier.setName(param.getName());
                    identifier.setVersion(new BigDecimal(1));

                    legacyParam.getProperty().addAll(param.properties().stream()
                            .map(prop -> {
                                Property property = new Property();
                                property.setName(prop.getName());
                                property.setValue(prop.getValue());
                                return property;
                            }).collect(Collectors.toList())
                    );

                    legacyParam.getComputationruleparametervalue().addAll(mapParamValues(param));

                    legacyParam.setIdentifier(identifier);
                    return legacyParam;
                }).collect(Collectors.toList());
    }

    private static List<Computationruleparametervalue> mapParamValues(final Parameter param) {

        return param.getValues().stream()
                .map(val -> {
                    final Computationruleparametervalue legacyVal = new Computationruleparametervalue();
                    legacyVal.setIndex(String.valueOf(val.getIndex()));
                    legacyVal.setValue(val.getValue());
                    return legacyVal;
                })
                .collect(Collectors.toList());
    }

    private static Performancelevels mapPerformanceLevels(final TestPackage testPackage,
                                                          final Map<String, String> bpElementsMap) {
        final Map<String, BlueprintElement> blueprintMap = testPackage.getBlueprintMap();
        final Performancelevels levels = new Performancelevels();

        blueprintMap.values().stream()
                .filter(blueprintElement -> blueprintElement.getScoring().isPresent())
                .forEach(blueprintElement -> blueprintElement.getScoring().get().performanceLevels()
                        .forEach(pl -> {

                            // If the blueprintId containing the performance levels is for a combined test package,
                            // simply use the test package id
                            final Performancelevel level = new Performancelevel();
                            // If the element is a test/segment, get the "combined" blueprint element id. Otherwise, just find the blueprint
                            // element uniqueId that corresponds to the blueprint element name in the legacy blueprint
                            if (blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.PACKAGE)) {
                                level.setBpelementid(TestPackageUtils.getAssessmentKey(testPackage, testPackage.getId()));
                            } else if (blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.TEST) ||
                                    blueprintElement.getType().equalsIgnoreCase(BlueprintElementTypes.SEGMENT)) {
                                level.setBpelementid(testPackage.getAssessments().size() > 1
                                        ? TestPackageUtils.getCombinedKey(testPackage, blueprintElement.getId())
                                        : TestPackageUtils.getAssessmentKey(testPackage, blueprintElement.getId()));
                            } else {
                                level.setBpelementid(bpElementsMap.containsKey(blueprintElement.getId())
                                        ? bpElementsMap.get(blueprintElement.getId())
                                        : blueprintElement.getId());
                            }
                            level.setPlevel(BigInteger.valueOf(pl.getPLevel()));
                            level.setScaledlo((float) pl.getScaledLo());
                            level.setScaledhi((float) pl.getScaledHi());
                            levels.getPerformancelevel().add(level);
                        }));

        return levels;
    }


}
