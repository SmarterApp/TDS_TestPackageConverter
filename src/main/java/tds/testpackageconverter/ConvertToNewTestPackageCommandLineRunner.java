package tds.testpackageconverter;

import org.apache.commons.cli.*;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tds.testpackageconverter.converter.TestPackageConverterService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ConvertToNewTestPackageCommandLineRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ConvertToNewTestPackageCommandLineRunner.class);
    private static final String CONVERT_TO_NEW_COMMAND = "convert-to-new";

    private static final String ZIP_FLAG = "z";
    private static final String ADMINISTRATION_FLAG = "a";
    private static final String SCORING_FLAG = "s";
    private static final String DIFF_FLAG = "d";
    private static final String VERBOSE_FLAG = "v";

    private final TestPackageConverterService service;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    @Autowired
    public ConvertToNewTestPackageCommandLineRunner(final TestPackageConverterService service) {
        this.service = service;
    }

    /**
     * Initiates the sub-command
     */
    @PostConstruct
    public void init () {
        options = new Options();
        parser = new DefaultParser();
        formatter = new HelpFormatter();

        final Option administrationFileOption = Option.builder(ADMINISTRATION_FLAG)
                .argName("administration")
                .hasArgs()
                .longOpt("administration")
                .desc("Administration file(s) to be converted")
                .optionalArg(false)
                .required(false)
                .build();

        final Option scoringFileOption = Option.builder(SCORING_FLAG)
                .argName("scoring")
                .longOpt("scoring")
                .hasArg()
                .desc("Scoring file(s) to be converted")
                .optionalArg(false)
                .required(false)
                .build();

        final Option diffFileOption = Option.builder(DIFF_FLAG)
                .argName("diff")
                .longOpt("diff")
                .hasArg()
                .desc("Diff file to be included in the conversion")
                .optionalArg(false)
                .required(false)
                .build();

        final Option zipFileOption = Option.builder(ZIP_FLAG)
                .argName("zip")
                .longOpt("zip")
                .hasArg()
                .desc("Zip file containing all test package parts")
                .optionalArg(false)
                .required(false)
                .build();

        final Option verboseOption = Option.builder(VERBOSE_FLAG)
                .argName("verbose")
                .longOpt("verbose")
                .hasArg(false)
                .desc("Prints more verbose output in case of errors")
                .required(false)
                .build();

        options.addOption(administrationFileOption);
        options.addOption(scoringFileOption);
        options.addOption(diffFileOption);
        options.addOption(zipFileOption);
        options.addOption(verboseOption);
    }

    @Override
    public void run(final String... args) throws Exception {
        if (args.length < 1) {
            System.out.println("No arguments were provided to the test package converter. Aborting...");
            printHelpAndExit();
            return;
        }

        if (CONVERT_TO_NEW_COMMAND.equals(args[0])) {
            parseAndHandleCommandErrors(args);

            // Get the output test package filename
            final String outputFilename = cmd.getArgList().get(1);

            try {
                if (cmd.hasOption(ZIP_FLAG) && !cmd.getOptionValue(ZIP_FLAG).isEmpty()) {
                    convertTestPackageFromZipFile(outputFilename);
                } else {
                    List<String> testSpecifications = new ArrayList<>();

                    if (cmd.hasOption(ADMINISTRATION_FLAG) && !cmd.getOptionValue(ADMINISTRATION_FLAG).isEmpty()) {
                        System.out.println("Administration package(s): " + Arrays.toString(cmd.getOptionValues(ADMINISTRATION_FLAG)));

                        testSpecifications.addAll(Lists.newArrayList(cmd.getOptionValues(ADMINISTRATION_FLAG)));
                    }

                    if (cmd.hasOption(SCORING_FLAG) && !cmd.getOptionValue(SCORING_FLAG).isEmpty()) {
                        System.out.println("Scoring package: " + cmd.getOptionValue(SCORING_FLAG));
                        testSpecifications.add(cmd.getOptionValue(SCORING_FLAG));
                    }

                    if (cmd.hasOption(DIFF_FLAG) && !cmd.getOptionValue(DIFF_FLAG).isEmpty()) {
                        System.out.println("Diff package: " + cmd.getOptionValue(DIFF_FLAG));
                        service.convertTestSpecifications(outputFilename, testSpecifications, cmd.getOptionValue(DIFF_FLAG));
                    } else {
                        service.convertTestSpecifications(outputFilename, testSpecifications);
                    }

                    System.out.println(String.format("The test package '%s' was successfully created", outputFilename));
                }
            } catch (IOException e) {
                System.out.println(String.format("The test package '%s' was not successfully created", outputFilename));

                if (cmd.hasOption(VERBOSE_FLAG)) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void parseAndHandleCommandErrors(final String[] args) throws ParseException {
        try {
            cmd = parser.parse(options, args);

            // We need two args - one to figure out what our action is, and a second to know the target filename
            if (cmd.getArgList().size() < 2) {
                printHelpAndExit();
            }
        } catch (UnrecognizedOptionException | MissingArgumentException e) {
            printHelpAndExit();
        }

        if (cmd.hasOption(ZIP_FLAG)
                && (cmd.hasOption(ADMINISTRATION_FLAG) || cmd.hasOption(DIFF_FLAG) || cmd.hasOption(SCORING_FLAG))) {
            throw new IllegalArgumentException("If a zip file input is specified, no other additional inputs can be included.");
        }

        if ((cmd.hasOption(SCORING_FLAG) || cmd.hasOption(DIFF_FLAG)) && !cmd.hasOption(ADMINISTRATION_FLAG)) {
            throw new MissingOptionException("A diff or scoring package cannot be provided as an input with no associated administration package");
        }
    }

    private void convertTestPackageFromZipFile(final String outputFilename) {
        System.out.println("Attempting to open and extract the zip file: " + cmd.getOptionValue(ZIP_FLAG));

        try {
            service.extractAndConvertTestSpecifications(outputFilename, new File(cmd.getOptionValue(ZIP_FLAG)));
            System.out.println("Finished converting the test package " + outputFilename);        
        } catch (Exception e) {
            System.out.println(e.getMessage());

            if (cmd.hasOption(VERBOSE_FLAG)) {
                e.printStackTrace();
            }
        }
    }

    private void printHelpAndExit() {
        formatter.printHelp("Sample usage: convert-to-new <OUTPUT FILENAME> [OPTIONS] ", options);
        System.exit(-1);
    }
}
