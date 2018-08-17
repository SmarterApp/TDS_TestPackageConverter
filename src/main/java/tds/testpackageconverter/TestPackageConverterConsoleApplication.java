package tds.testpackageconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tds.support.tool.testpackage.configuration.TestPackageObjectMapperConfiguration;

@SpringBootApplication
@Import(TestPackageObjectMapperConfiguration.class)
public class TestPackageConverterConsoleApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TestPackageConverterConsoleApplication.class, args);
    }

}
