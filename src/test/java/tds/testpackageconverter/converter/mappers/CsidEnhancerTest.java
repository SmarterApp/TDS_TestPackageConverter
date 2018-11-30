package tds.testpackageconverter.converter.mappers;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsidEnhancerTest {

    @Test
    public void shouldEnhanceMathId() {
        CsidEnhancer enhancer = new CsidEnhancer("MATH", "8");

        Optional<String> enhancedFormat = enhancer.enhance("1");
        assertTrue(enhancedFormat.isPresent());
        assertTrue(enhancedFormat.get().startsWith("M."));
        assertTrue(enhancedFormat.get().contains("G8"));
        assertTrue(enhancedFormat.get().contains("C1"));
    }

    @Test
    public void shouldEnhanceElaId() {
        CsidEnhancer enhancer = new CsidEnhancer("ELA", "4");

        Optional<String> enhancedFormat = enhancer.enhance("2");
        assertTrue(enhancedFormat.isPresent());
        assertTrue(enhancedFormat.get().startsWith("E."));
        assertTrue(enhancedFormat.get().contains("G4"));
        assertTrue(enhancedFormat.get().contains("C2"));
    }


    @Test
    public void shouldFailUnknownSubject() {
        CsidEnhancer enhancer = new CsidEnhancer("Bogus", "4");

        Optional<String> enhancedFormat = enhancer.enhance("2");
        assertFalse(enhancedFormat.isPresent());
    }

    @Test
    public void shouldFailBadInput() {
        CsidEnhancer enhancer = new CsidEnhancer("MATH", "4");

        Optional<String> enhancedFormat = enhancer.enhance("Bogus");
        assertFalse(enhancedFormat.isPresent());
    }
}