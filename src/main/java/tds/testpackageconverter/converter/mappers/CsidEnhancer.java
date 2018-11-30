package tds.testpackageconverter.converter.mappers;

import org.opentestsystem.shared.contentspecid.ContentSpecId;
import org.opentestsystem.shared.contentspecid.ContentSpecIdConverter;
import org.opentestsystem.shared.contentspecid.enums.ContentSpecFormat;
import org.opentestsystem.shared.contentspecid.enums.ContentSpecGrade;
import org.opentestsystem.shared.contentspecid.enums.ContentSpecSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Enhance legacy format IDs to enhanced format content spec IDs. Since the legacy IDs in the test
 * packages don't contain specification version information, it is assumed that when the subject is Math,
 * the IDs are in MATH, v6 format, and if ELA, then the IDs are in ELA, v1. (For ELA, v1 is the only current
 * legacy format.)
 *
 * The grade level also must be provided because some legacy IDs omit this information.
 */
public class CsidEnhancer {
    private static final Logger log = LoggerFactory.getLogger(LegacyScoringTestPackageFormMapper.class);

    private ContentSpecIdConverter converter = new ContentSpecIdConverter();

    private final ContentSpecSubject subject;
    private String prefix;
    private final ContentSpecGrade grade;

    public CsidEnhancer(String subject, String grade) {
        if (subject.equalsIgnoreCase("MATH")) {
            this.subject = ContentSpecSubject.MATH;
            this.prefix = ContentSpecFormat.MATH_V6.getLegacyPrefix();
        } else if (subject.equalsIgnoreCase("ELA")) {
            this.subject = ContentSpecSubject.ELA;
            this.prefix = ContentSpecFormat.ELA_V1.getLegacyPrefix();
        } else {
            this.subject = ContentSpecSubject.UNSPECIFIED;
            log.warn("Subject " + subject + " is unknown. Will not be able to produce enhanced IDs.");
        }

        this.grade = ContentSpecGrade.fromString(grade);
    }

    public Optional<String> enhance(String legacyContentId) {
        if (this.subject == ContentSpecSubject.UNSPECIFIED) {
            return Optional.empty();
        }

        try {
            String fullId =  legacyContentId.startsWith(prefix) ? legacyContentId: prefix + legacyContentId;

            ContentSpecId id = converter.parse(fullId, grade);
            return Optional.of(converter.format(id, ContentSpecFormat.ENHANCED));
        } catch (Exception e) {
            log.warn("Cannot create an enhanced CSID for " + legacyContentId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
