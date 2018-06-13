package tds.testpackageconverter.converter.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.apache.commons.io.IOUtils;
import tds.testpackage.diff.TestPackageDiff;
import tds.testpackageconverter.converter.mappers.LegacyAdministrationTestPackageMapper;
import tds.testpackageconverter.converter.TestPackageConverterService;
import tds.testpackageconverter.converter.mappers.LegacyScoringTestPackageMapper;
import tds.testpackageconverter.converter.mappers.TestPackageDiffMapper;
import tds.testpackageconverter.converter.mappers.TestPackageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.support.tool.testpackage.configuration.TestPackageObjectMapperConfiguration;
import tds.testpackage.legacy.model.Testspecification;
import tds.testpackage.model.TestPackage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class TestPackageConverterServiceImpl implements TestPackageConverterService {
    private final Logger log = LoggerFactory.getLogger(TestPackageConverterServiceImpl.class);
    private final XmlMapper legacyXmlMapper;
    private final XmlMapper testPackageMapper;

    @Autowired
    public TestPackageConverterServiceImpl(final TestPackageObjectMapperConfiguration testPackageObjectMapperConfiguration) {
        this.legacyXmlMapper = testPackageObjectMapperConfiguration.getLegacyTestSpecXmlMapper();
        this.testPackageMapper = testPackageObjectMapperConfiguration.getXmlMapper();
    }

    @Override
    public void extractAndConvertTestSpecifications(final String testPackageName, final File file) throws IOException, ParseException {
        ZipFile zipFile = new ZipFile(file);
        File convertedTestPackageFile = new File(testPackageName);

        List<Testspecification> specifications = zipFile.stream()
                .filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".xml")
                        && isTestSpecification(zipFile, entry))
                .map(entry -> unzipAndRead(zipFile, entry))
                .collect(Collectors.toList());

        if (specifications.isEmpty()) {
            throw new IOException("No testspecification XML files were located within the zip file");
        }

        Optional<TestPackageDiff> diff = zipFile.stream()
                .filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".xml")
                        && isTestPackageDiff(zipFile, entry))
                .map(entry -> unzipAndReadDiff(zipFile, entry))
                .findFirst();

        TestPackage testPackage = diff.isPresent()
                ? TestPackageMapper.toNew(testPackageName, specifications, diff.get())
                : TestPackageMapper.toNew(testPackageName, specifications);

        legacyXmlMapper.writeValue(convertedTestPackageFile, testPackage);

        convertedTestPackageFile.createNewFile();
    }


    @Override
    public void convertTestSpecifications(final String testPackageName, final List<String> adminAndScoringFileNames) throws IOException, ParseException {
        convertTestSpecifications(testPackageName, adminAndScoringFileNames, null);
    }

    @Override
    public void convertTestSpecifications(final String testPackageName, final List<String> adminAndScoringFileNames,
                                          final String diffFileName) throws IOException, ParseException {
        File convertedTestPackageFile = new File(testPackageName);

        List<Testspecification> specifications = adminAndScoringFileNames.stream()
                .map(this::readTestSpecification)
                .collect(Collectors.toList());

        TestPackage testPackage = diffFileName == null
                ? TestPackageMapper.toNew(testPackageName, specifications)
                : TestPackageMapper.toNew(testPackageName, specifications, readDiff(diffFileName));
        legacyXmlMapper.writeValue(convertedTestPackageFile, testPackage);

        convertedTestPackageFile.createNewFile();
    }


    @Override
    public void convertTestPackage(final String testPackagePath) {
        final TestPackage testPackage = readTestPackage(testPackagePath);
        final List<Testspecification> administrationPackages = LegacyAdministrationTestPackageMapper.fromNew(testPackage);

        // Create the administration file(s)
        administrationPackages.forEach(testSpecification -> {
            final String administrationOutputFilename = testSpecification.getIdentifier().getUniqueid() + ".xml";
            final File administrationFile = new File(administrationOutputFilename);
            try {
                legacyXmlMapper.writeValue(administrationFile, testSpecification);
                administrationFile.createNewFile();
                System.out.println("Successfully created the administration testspecification file " + administrationOutputFilename);
            } catch (IOException e) {
                log.error("An exception occurred while creating the file: {}", administrationOutputFilename, e);
                throw new RuntimeException(e);
            }
        });

        // Create the scoring file (if scoring data is present)
        if (isScoringDataIncluded(testPackage)) {
            final Testspecification scoringPackage = LegacyScoringTestPackageMapper.fromNew(testPackage, administrationPackages);
            final String scoringOutputFilename = scoringPackage.getIdentifier().getUniqueid() + "-SCORING.xml";
            final File scoringFile = new File(scoringOutputFilename);
            try {
                legacyXmlMapper.writeValue(scoringFile, scoringPackage);
                scoringFile.createNewFile();
                System.out.println("Successfully created the scoring testspecification file " + scoringOutputFilename);
            } catch (IOException e) {
                log.error("An exception occurred while creating the file {}", scoringOutputFilename, e);
                throw new RuntimeException(e);
            }
        }

        // Create the diff file
        final TestPackageDiff diff = TestPackageDiffMapper.fromNew(testPackage);
        final String diffOutputFilePath = String.format("%s-diff.xml", testPackage.getId());
        final File diffFile = new File(diffOutputFilePath);

        try {
            testPackageMapper.writeValue(diffFile, diff);
            diffFile.createNewFile();
            System.out.println("Successfully created the test package diff file " + diffOutputFilePath);
        } catch (IOException e) {
            log.error("An exception occurred while creating the file {}", diffOutputFilePath, e);
            throw new RuntimeException(e);
        }
    }

    private TestPackageDiff readDiff(final String fileName) {
        try {
            return testPackageMapper.readValue(new File(fileName), TestPackageDiff.class);
        } catch (IOException e) {
            log.error("An exception occurred while reading the file: {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    private Testspecification readTestSpecification(final String filePath) {
        try {
            return legacyXmlMapper.readValue(new File(filePath), Testspecification.class);
        } catch (IOException e) {
            log.error("An exception occurred while reading the file: {}", filePath, e);
            throw new RuntimeException(e);
        }
    }

    private TestPackage readTestPackage(final String filePath) {
        try {
            return testPackageMapper.readValue(new File(filePath), TestPackage.class);
        } catch (IOException e) {
            log.error("An exception occurred while reading the file: {}", filePath, e);
            throw new RuntimeException(e);
        }
    }

    private Testspecification unzipAndRead(final ZipFile zipFile, final ZipEntry entry) {
        try {
            System.out.println("Reading file in zip: " + entry.getName());
            InputStream inputStream = zipFile.getInputStream(entry);

            return legacyXmlMapper.readValue(inputStream, Testspecification.class);
        } catch (IOException e) {
            log.error("An exception occurred: {}", e);
            throw new RuntimeException(e);
        }
    }

    private TestPackageDiff unzipAndReadDiff(final ZipFile zipFile, final ZipEntry entry) {
        try {
            System.out.println("Reading file in zip: " + entry.getName());
            InputStream inputStream = zipFile.getInputStream(entry);

            return testPackageMapper.readValue(inputStream, TestPackageDiff.class);
        } catch (IOException e) {
            log.error("An exception occurred: {}", e);
            throw new RuntimeException(e);
        }
    }


    private boolean isTestSpecification(final ZipFile zipFile, final ZipEntry entry) {
        try {
            InputStream inputStream = zipFile.getInputStream(entry);
            String fileText = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            return fileText.contains("<testspecification");
        } catch (IOException e) {
            log.debug("Skipping file {}", entry.getName());
        }

        return false;
    }

    private boolean isTestPackageDiff(final ZipFile zipFile, final ZipEntry entry) {
        try {
            InputStream inputStream = zipFile.getInputStream(entry);
            String fileText = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            return fileText.contains("<TestPackageDiff");
        } catch (IOException e) {
            log.debug("Skipping diff file {}", entry.getName());
        }

        return false;
    }

    private boolean isScoringDataIncluded(final TestPackage testPackage) {
        return testPackage.getBlueprintMap().values().stream()
                .anyMatch(blueprintElement -> blueprintElement.getScoring().isPresent());
    }
}
