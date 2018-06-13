# TDS_TestPackageConverter
The TDS Test Package Converter supports conversion between the existing test pacakge formats and the updated formats.  

## Required Tools
This project uses the following tools

* Java 1.8
* Maven 3

## Building and Running the project
Once you have Java 1.8 and Maven 3 install you can build the project using the following command (in the projects root directory).

`mvn clean install`

This will build the project creating a `target` directory.  Once the project is built you will see something like:

`target/tds-test-package-converter-0.1.0.jar`

Once you see that jar you can execute it with by:

`java -jar target/tds-test-package-converter-0.1.0.jar`

This will run and present an error message that you need to provide more information.  The sections below cover the required commands an the optional flags that can be provided.

## Test Package File terminology
This tool works with the following test package information.  As noted below this can be provided to the tool via command line options or in a single ZIP archive file.

* Test Packages
	* XML file(s) containing the information for a test
* Scoring 
	* XML file containing scoring information for the test
* Diff File
	* XML file containing additional information to be included in the test.  The new test package format contains more information than previous versions.  The Diff file is normally created when going from a new test package format to an older test package format so the information isn't lost.

For more information concerning these files please refer to the [SmarterApp documentation](http://www.smarterapp.org/specifications.html).

## Converting Legacy To New Pacakge Format
To convert a legacy format to the new package format one needs to provide the `convert-to-new` command.  

There are two main ways to convert the files:

* Convert test package information from a single zip archive file
* Convert test package information referencing many files via command line options

More information and options are listed below.

### Converting zip file to new format
The tool has the ability to convert zip archive files containing test packages to the new format.  The command to do that is below.  This option uses a single argument (`-z` or `zip`) to specify the zip file containing all the potential test files.  

<pre>
java -jar tds-test-package-converter.jar convert-to-new handScoreTest.xml -z HandScoreTest.zip
</pre>

The `-z` tells the tool you want to convert an ZIP archive of test pacakges.  Note the `HandScoreTest.zip` in the command is a zip file within the same directory as you run the project.  

#### Options

| Argument | Long Option | Required | Description | Example |
| -------- | ----------- | -------- | ----------- | ------- |
| `-z` | `--zip` | Yes if doing zip | zip file to convert | `convert-to-new newPackage.xml -z zipFile.zip`|
| `-v` | `--verbose` | No | Verbose logging which can help identify errors | `convert-to-new handScoreTest.xml -a handScoreTestLegacy.xml -v`

### Converting Legacy format file to new format

This section covers creating a new package from many different legacy test package files.  An example command is below:

<pre>
java -jar tds-test-package-converter.jar convert-to-new handScoreTest.xml -a handScoreTestLegacy.xml
</pre>

#### Options

| Argument | Long Option | Required | Description | Example |
| -------- | ----------- | -------- | ----------- | ------- |
| `-a` | `--administration` | Yes if not converting from zip | Specify the test package XML file(s) to convert | `convert-to-new newPackage.xml -a <file one> <file two>` |
| `-s` | `--scoring` | No | Specify the the scoring XML file to include in conversion | `convert-to-new newPackage.xml -a <file one> -s <score file>` |
| `-d` | `--diff` | No | Specify the diff XML file to include in conversion | `convert-to-new newPackage.xml -a <file one> -d <diff file>`
| `-v` | `--verbose` | No | Verbose logging which can help identify errors | `convert-to-new handScoreTest.xml -a handScoreTestLegacy.xml -v`

#### Example
The example below leverages all the commands listed above.  

<pre>
java -jar test-package-converter.jar convert-to-new newTestPackage.xml -a handScoreTestPackage1.xml handScoreTestPackage2.xml -s scoringFile.xml -d diffFile.xml -v
</pre>

The command will create a new test package in the new format with the name `newTestPackage.xml`.  It will use test packages `handScoreTestPackage1.xml` and `handScoreTestPackage2.xml`.  It will use scoring file `scoringFile.xml` and diff file `diffFile.xml`.  Lastly, the `-v` will provide more verbose logging.


## Converting from New to Legacy Format

Converting from a new format to the legacy format has fewer commands because the new format contains all the information in a single file.  Previous test packages may have required multiple files to create it.

`java -jar tds-test-package-converter.jar convert-from-new handScoreTestNew.xml`

The output will be one to many test package files using the unique identifier for the test pacakge as the filename.




