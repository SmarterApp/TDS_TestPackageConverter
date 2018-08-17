package tds.testpackageconverter;

import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tds.testpackageconverter.converter.TestPackageConverterService;

import javax.annotation.PostConstruct;

@Component
public class ConvertFromNewTestPackageCommandLineRunner implements CommandLineRunner {
    private static final String CONVERT_FROM_NEW_COMMAND = "convert-from-new";

    private static final String VERBOSE_FLAG = "v";

    private final TestPackageConverterService service;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    @Autowired
    public ConvertFromNewTestPackageCommandLineRunner(final TestPackageConverterService service) {
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

        final Option verboseOption = Option.builder(VERBOSE_FLAG)
                .argName("verbose")
                .longOpt("verbose")
                .hasArg(false)
                .desc("Prints more verbose output in case of errors")
                .required(false)
                .build();

        options.addOption(verboseOption);
    }

    @Override
    public void run(final String... args) throws Exception {
        if (args.length < 1) {
            System.out.println("No arguments were provided to the test package converter. Aborting...");
            printHelpAndExit();
            return;
        }

        if (CONVERT_FROM_NEW_COMMAND.equals(args[0])) {
            parseAndHandleCommandErrors(args);

            final String testPackagePath = cmd.getArgList().get(1);
            try {
                service.convertTestPackage(testPackagePath);
                System.out.println(String.format("The test package '%s' was successfully converted", testPackagePath));
            } catch (final Exception e) {
                System.out.println(String.format("The test package '%s' was not successfully created", testPackagePath));

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
    }

    private void printHelpAndExit() {
        formatter.printHelp("Sample usage: convert-from-new <FILE PATH> [OPTIONS] ", options);
        System.exit(-1);
    }
}
