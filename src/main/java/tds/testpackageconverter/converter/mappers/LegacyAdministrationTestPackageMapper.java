package tds.testpackageconverter.converter.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tds.testpackage.legacy.model.Administration;
import tds.testpackage.legacy.model.Identifier;
import tds.testpackage.legacy.model.Property;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackage.model.Assessment;
import tds.testpackage.model.TestPackage;
import tds.testpackageconverter.utils.TestPackageUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static tds.testpackageconverter.utils.TestPackageUtils.formatDate;

public class LegacyAdministrationTestPackageMapper {
    private static final Logger log = LoggerFactory.getLogger(LegacyAdministrationTestPackageMapper.class);

    public static List<Testspecification> fromNew(final TestPackage testPackage) {
        return testPackage.getAssessments().stream()
                .map(assessment -> mapTestSpecification(testPackage, assessment))
                .collect(Collectors.toList());
    }

    private static Testspecification mapTestSpecification(final TestPackage testPackage, final Assessment assessment) {
        final BigDecimal version = new BigDecimal(testPackage.getVersion());
        final Testspecification testSpecification = new Testspecification();
        testSpecification.setPurpose("administration");
        testSpecification.setPublisher(testPackage.getPublisher());
        try {
            testSpecification.setPublishdate(formatDate(Instant.parse(testPackage.getPublishDate())));
        } catch (ParseException e) {
            log.warn("Unable to parse publish date time. Adding date without parsing");
            testSpecification.setPublishdate(testPackage.getPublishDate());
        }
        testSpecification.setVersion(new BigDecimal(1));

        // Map Identifier
        final Identifier testSpecIdentifier = new Identifier();
        testSpecIdentifier.setUniqueid(assessment.getKey());
        testSpecIdentifier.setName(assessment.getId());
        testSpecIdentifier.setVersion(version);
        testSpecIdentifier.setLabel(assessment.getLabel());
        testSpecification.setIdentifier(testSpecIdentifier);

        // Map test spec properties
        final List<Property> properties = testSpecification.getProperty();
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
        assessment.getGrades().forEach(grade -> {
            Property gradeProperty = new Property();
            gradeProperty.setName("grade");
            gradeProperty.setValue(grade.getValue());
            gradeProperty.setLabel(grade.getValue());
            properties.add(gradeProperty);
        });

        testSpecification.setAdministration(mapAdministration(testPackage, assessment));

        return testSpecification;
    }

    private static Administration mapAdministration(final TestPackage testPackage, final Assessment assessment) {
        final Map<String, Long> formIdToKeyMap = generateFormKeys(assessment);

        final Administration administration = new Administration();
        administration.setTestblueprint(LegacyAdministrationTestPackageBlueprintMapper.mapBlueprint(testPackage, assessment));
        administration.setItempool(LegacyAdministrationTestPackageItemPoolMapper.mapItemPool(testPackage, assessment));
        administration.getTestform().addAll(LegacyAdministrationTestPackageFormMapper.mapTestForms(testPackage, assessment,
                formIdToKeyMap));
        administration.getAdminsegment().addAll(LegacyAdministrationTestPackageSegmentMapper.mapAdminSegments(testPackage,
                assessment, formIdToKeyMap));
        return administration;
    }

    private static Map<String,Long> generateFormKeys(final Assessment assessment) {
        final Map<String, Long> formKeyMap = new HashMap<>();
        assessment.getSegments()
                .forEach(segment -> segment.segmentForms()
                        .forEach(form -> form.getPresentations()
                            .forEach(presentation ->
                                    formKeyMap.put(TestPackageUtils.getFormIdForLanguage(form.getId(), presentation.getCode()),
                                            (long) TestPackageUtils.generateFormKey(form.getId())))
                        )
                );
        return formKeyMap;
    }
}