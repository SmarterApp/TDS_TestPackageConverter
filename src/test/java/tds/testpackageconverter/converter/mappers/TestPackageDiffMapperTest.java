package tds.testpackageconverter.converter.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tds.testpackage.diff.Assessment;
import tds.testpackage.diff.Item;
import tds.testpackage.diff.TestPackageDiff;
import tds.testpackage.model.Option;
import tds.testpackageconverter.converter.LegacyTestPackageBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TestPackageDiffMapperTest extends LegacyTestPackageBaseTest {
    @Test
    public void shouldConvertTestPackageDiffFromMultiAssessmentTestPackage() {
        TestPackageDiff diff = TestPackageDiffMapper.fromNew(mockTestPackageWithScoring);
        assertThat(diff).isNotNull();

        assertThat(diff.getAcademicYear()).isEqualTo("2018");
        assertThat(diff.getBankKey()).isEqualTo(200);
        assertThat(diff.getSubType().get()).isEqualTo("ICA");

        assertThat(diff.getAssessments()).hasSize(2);

        Assessment assessment1 = diff.getAssessments().get(0);
        assertThat(assessment1.getId()).isEqualTo("SBAC-ICA-FIXED-G7E-ELA-7");
        assertThat(assessment1.getSegments()).hasSize(1);
        assertThat(assessment1.tools()).hasSize(2);
        assertThat(assessment1.tools().get(0).getName()).isEqualTo("Expandable Passages");
        assertThat(assessment1.tools().get(0).options()).hasSize(2);

        Option expandablePassagesOnOption = assessment1.tools().get(0).options().get(1);
        assertThat(expandablePassagesOnOption.getCode()).isEqualTo("TDS_ExpandablePassages1");
        assertThat(expandablePassagesOnOption.getSortOrder()).isEqualTo(1);

        assertThat(assessment1.getSegments().get(0).items()).hasSize(2);
        Item itemWithDoNotScore = assessment1.getSegments().get(0).items().get(0);
        assertThat(itemWithDoNotScore.getId()).isEqualTo("29311");
        assertThat(itemWithDoNotScore.doNotScore()).isTrue();

        Item itemWithHandScored = assessment1.getSegments().get(0).items().get(1);
        assertThat(itemWithHandScored.getId()).isEqualTo("58227");
        assertThat(itemWithHandScored.getTeacherHandScoring()).isPresent();
        assertThat(itemWithHandScored.getTeacherHandScoring().get().getExemplar().get()).isEqualTo("G3_2703_TM.pdf");
        assertThat(itemWithHandScored.getTeacherHandScoring().get().getTrainingGuide().get()).isEqualTo("G3_2703_SG.pdf");
        assertThat(itemWithHandScored.getTeacherHandScoring().get().getDescription()).isEqualTo("Mandatory Financial Literacy Classes - SBAC_Field");
        assertThat(itemWithHandScored.getTeacherHandScoring().get().layout()).isEqualTo("WAI");
        assertThat(itemWithHandScored.getTeacherHandScoring().get().dimensions()).isEqualTo("\"\\n                    \\n                                            [\\n                                                {\\n                                                    \\\"conditions\\\": [\\n                                                    {\\n                                                        \\\"code\\\": \\\"B\\\",\\n                                                        \\\"description\\\": \\\"Blank\\\"\\n                                                    },\\n                                                    {\\n                                                        \\\"code\\\": \\\"I\\\",\\n                                                        \\\"description\\\": \\\"Insufficient\\\"\\n                                                    },\\n                                                    {\\n                                                        \\\"code\\\": \\\"L\\\",\\n                                                        \\\"description\\\": \\\"Non-Scorable Language\\\"\\n                                                    }\\n                                                    ],\\n                                                    \\\"description\\\": \\\"CONVENTIONS\\\",\\n                                                    \\\"maxPoints\\\": \\\"2\\\",\\n                                                    \\\"minPoints\\\": \\\"0\\\"\\n                                                },\\n                                                {\\n                                                    \\\"conditions\\\": [\\n                                                    {\\n                                                        \\\"code\\\": \\\"B\\\",\\n                                                        \\\"description\\\": \\\"Blank\\\"\\n                                                    },\\n                                                    {\\n                                                        \\\"code\\\": \\\"I\\\",\\n                                                        \\\"description\\\": \\\"Insufficient\\\"\\n                                                    },\\n                                                    {\\n                                                        \\\"code\\\": \\\"L\\\",\\n                                                        \\\"description\\\": \\\"Non-Scorable Language\\\"\\n                                                    }\\n                                                    ],\\n                                                    \\\"description\\\": \\\"ELABORATION\\\",\\n                                                    \\\"maxPoints\\\": \\\"3\\\",\\n                                                    \\\"minPoints\\\": \\\"0\\\"\\n                                                },\\n                                                {\\n                                                    \\\"conditions\\\": [\\n                                                    {\\n                                                        \\\"code\\\": \\\"B\\\",\\n                                                        \\\"description\\\": \\\"Blank\\\"\\n                                                    },\\n                                                    {\\n                                                        \\\"code\\\": \\\"I\\\",\\n                                                        \\\"description\\\": \\\"Insufficient\\\"\\n                                                    },\\n                                                    {\\n                                                        \\\"code\\\": \\\"L\\\",\\n                                                        \\\"description\\\": \\\"Non-Scorable Language\\\"\\n                                                    }\\n                                                    ],\\n                                                    \\\"description\\\": \\\"ORGANIZATION\\\",\\n                                                    \\\"maxPoints\\\": \\\"3\\\",\\n                                                    \\\"minPoints\\\": \\\"0\\\"\\n                                                }\\n                                            ]\\n                                        \\n                  \"");

        Assessment assessment2 = diff.getAssessments().get(1);
        assertThat(assessment2.getSegments()).hasSize(2);
        assertThat(assessment2.getSegments().get(1).tools()).hasSize(1);
        assertThat(assessment2.getSegments().get(1).tools().get(0).getName()).isEqualTo("Calculator");
    }
}
