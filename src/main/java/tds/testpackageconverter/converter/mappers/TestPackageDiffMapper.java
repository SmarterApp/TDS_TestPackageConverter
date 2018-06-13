package tds.testpackageconverter.converter.mappers;

import tds.common.Algorithm;
import tds.testpackage.diff.Assessment;
import tds.testpackage.diff.Item;
import tds.testpackage.diff.TestPackageDiff;
import tds.testpackage.model.Segment;
import tds.testpackage.model.TestPackage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestPackageDiffMapper {
    public static TestPackageDiff fromNew(final TestPackage testPackage) {
        return TestPackageDiff.builder()
                .setAcademicYear(testPackage.getAcademicYear())
                .setBankKey(testPackage.getBankKey())
                .setSubType(testPackage.getSubType())
                .setAssessments(mapAssessments(testPackage))
                .build();
    }

    private static List<Assessment> mapAssessments(final TestPackage testPackage) {
        return testPackage.getAssessments().stream()
                .map(assessment -> Assessment.builder()
                        .setId(assessment.getId())
                        .setSegments(mapSegments(assessment.getSegments()))
                        .setTools(assessment.tools())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<tds.testpackage.diff.Segment> mapSegments(final List<Segment> segments) {
        return segments.stream()
                .map(segment -> tds.testpackage.diff.Segment.builder()
                        .setId(segment.getId())
                        .setItems(mapItems(segment))
                        .setEntryApproval(segment.entryApproval())
                        .setExitApproval(segment.exitApproval())
                        .setLabel(segment.getLabel())
                        .setTools(segment.tools())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<Item> mapItems(final Segment segment) {
        if (segment.getAlgorithmType().equalsIgnoreCase(Algorithm.FIXED_FORM.getType())) {
            return segment.segmentForms().stream()
                    .flatMap(form -> form.itemGroups().stream()
                            .flatMap(itemGroup -> itemGroup.items().stream()
                                    // Skip items that do not have handscoring data
                                    .filter(item -> item.doNotScore() || item.getTeacherHandScoring().isPresent())
                                    .map(item -> Item.builder()
                                            .setId(item.getId())
                                            .setDoNotScore(Optional.of(String.valueOf(item.doNotScore())))
                                            .setTeacherHandScoring(item.getTeacherHandScoring())
                                            .build()
                                    )
                            )
                    ).collect(Collectors.toList());
        } else {
            return segment.pool().stream()
                    .flatMap(itemGroup -> itemGroup.items().stream()
                            // Skip items that do not have handscoring data
                            .filter(item -> item.doNotScore() || item.getTeacherHandScoring().isPresent())
                            .map(item -> Item.builder()
                                    .setId(item.getId())
                                    .setDoNotScore(Optional.of(String.valueOf(item.doNotScore())))
                                    .setTeacherHandScoring(item.getTeacherHandScoring())
                                    .build()))
                    .collect(Collectors.toList());
        }
    }
}
