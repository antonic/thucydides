package net.thucydides.core.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static net.thucydides.core.matchers.FileMatchers.exists;
import static net.thucydides.core.util.FileSeparatorUtil.changeSeparatorIfRequired;
import static net.thucydides.core.util.TestResources.directoryInClasspathCalled;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class WhenMatchingWithFiles {

    @Test
    public void should_check_if_file_exists() {
        File existingFile = new File(directoryInClasspathCalled("/reports"), "sample-report-1.xml");
        assertThat(existingFile, exists());
    }

    @Test
    public void should_check_if_file_does_not_exist() {
        try {
            File existingFile = new File(directoryInClasspathCalled("/reports"), "no-such-report.xml");
            assertThat(existingFile, exists());
        } catch (AssertionError expectedException ) {
            assertThat(expectedException.getMessage(), containsString("no-such-report.xml"));
            return;
        }
        Assert.fail();
 
    }
}
