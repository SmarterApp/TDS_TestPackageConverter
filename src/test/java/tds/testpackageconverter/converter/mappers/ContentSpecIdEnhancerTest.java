package tds.testpackageconverter.converter.mappers;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContentSpecIdEnhancerTest {

    @Test
    public void shouldEnhanceMathId() {
        ContentSpecIdEnhancer enhancer = new ContentSpecIdEnhancer("MATH", "8");

        Optional<String> enhancedFormat = enhancer.enhance("1");
        assertTrue(enhancedFormat.isPresent());
        assertTrue(enhancedFormat.get().startsWith("M."));
        assertTrue(enhancedFormat.get().contains("G8"));
        assertTrue(enhancedFormat.get().contains("C1"));
    }

    @Test
    public void shouldEnhanceElaId() {
        ContentSpecIdEnhancer enhancer = new ContentSpecIdEnhancer("ELA", "4");

        Optional<String> enhancedFormat = enhancer.enhance("2");
        assertTrue(enhancedFormat.isPresent());
        assertTrue(enhancedFormat.get().startsWith("E."));
        assertTrue(enhancedFormat.get().contains("G4"));
        assertTrue(enhancedFormat.get().contains("C2"));
    }


    @Test
    public void shouldFailUnknownSubject() {
        ContentSpecIdEnhancer enhancer = new ContentSpecIdEnhancer("Bogus", "4");

        Optional<String> enhancedFormat = enhancer.enhance("2");
        assertFalse(enhancedFormat.isPresent());
    }

    @Test
    public void shouldFailBadInput() {
        ContentSpecIdEnhancer enhancer = new ContentSpecIdEnhancer("MATH", "4");

        Optional<String> enhancedFormat = enhancer.enhance("Bogus");
        assertFalse(enhancedFormat.isPresent());
    }
}