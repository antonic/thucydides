package net.thucydides.jbehave;

import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestResult;
import net.thucydides.core.model.TestStep;
import net.thucydides.core.reports.xml.XMLTestOutcomeReporter;
import net.thucydides.core.util.MockEnvironmentVariables;
import net.thucydides.core.webdriver.Configuration;
import net.thucydides.core.webdriver.SystemPropertiesConfiguration;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.TxtOutput;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WhenRunningJBehaveStories extends AbstractJBehaveStory {

    private static final int TOTAL_NUMBER_OF_JBEHAVE_SCENARIOS = 8;

    final class AllStories extends JUnitThucydidesStories {}

    @Test
    public void all_stories_on_the_classpath_should_be_run_by_default() throws Throwable {

        // Given
        JUnitThucydidesStories stories = new AllStories();
        stories.setSystemConfiguration(systemConfiguration);
        stories.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(stories);

        // Then
        List<TestOutcome> outcomes = loadTestOutcomes();
        assertThat(outcomes.size(), is(TOTAL_NUMBER_OF_JBEHAVE_SCENARIOS));
    }

    final class StoriesInTheSubsetFolder extends JUnitThucydidesStories {
        public void configure() {
            findStoriesIn("stories/subset");
        }
    }

    @Test
    public void a_subset_of_the_stories_can_be_run_individually() throws Throwable {

        // Given
        JUnitThucydidesStories stories = new StoriesInTheSubsetFolder();
        stories.setSystemConfiguration(systemConfiguration);
        stories.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(stories);

        // Then

        List<TestOutcome> outcomes = loadTestOutcomes();
        assertThat(outcomes.size(), is(2));
    }

    final class SomePassingStories extends JUnitThucydidesStories {
        public void configure() {
            findStoriesCalled("*PassingStory.story");
        }
    }
    @Test
    public void stories_with_a_matching_name_can_be_run() throws Throwable {

        // Given
        JUnitThucydidesStories stories = new SomePassingStories();
        stories.setSystemConfiguration(systemConfiguration);
        stories.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(stories);

        // Then
        List<TestOutcome> outcomes = loadTestOutcomes();
        assertThat(outcomes.size(), is(3));
    }

    @Test
    public void pending_stories_should_be_reported_as_pending() throws Throwable {

        // Given
        JUnitThucydidesStories pendingStory = new JUnitThucydidesStories() {
            public void configure() {
                findStoriesCalled("aPendingStory.story");
            }
        };

        pendingStory.setSystemConfiguration(systemConfiguration);
        pendingStory.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(pendingStory);

        // Then
        List<TestOutcome> outcomes = loadTestOutcomes();
        assertThat(outcomes.size(), is(1));
        assertThat(outcomes.get(0).getResult(), is(TestResult.PENDING));
    }

    @Test
    public void implemented_pending_stories_should_be_reported_as_pending() throws Throwable {

        // Given
        JUnitThucydidesStories pendingStory = new JUnitThucydidesStories() {
            public void configure() {
                findStoriesCalled("aPendingImplementedStory.story");
            }
        };

        pendingStory.setSystemConfiguration(systemConfiguration);
        pendingStory.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(pendingStory);

        // Then
        List<TestOutcome> outcomes = loadTestOutcomes();
        assertThat(outcomes.size(), is(1));
        assertThat(outcomes.get(0).getResult(), is(TestResult.PENDING));
    }

    @Test
    public void passing_stories_should_be_reported_as_passing() throws Throwable {

        // Given
        JUnitThucydidesStories pendingStory = new JUnitThucydidesStories() {
            public void configure() {
                findStoriesCalled("aPassingStory.story");
            }
        };

        pendingStory.setSystemConfiguration(systemConfiguration);
        pendingStory.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(pendingStory);

        // Then
        List<TestOutcome> outcomes = loadTestOutcomes();
        assertThat(outcomes.size(), is(1));
        assertThat(outcomes.get(0).getResult(), is(TestResult.SUCCESS));
    }

    @Test
    public void a_passing_story_with_steps_should_record_the_steps() throws Throwable {

        // Given
        JUnitThucydidesStories pendingStory = new JUnitThucydidesStories() {
            public void configure() {
                findStoriesCalled("aPassingStoryWithSteps.story");
            }
        };

        pendingStory.setSystemConfiguration(systemConfiguration);
        pendingStory.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(pendingStory);

        // Then
        List<TestOutcome> outcomes = loadTestOutcomes();
        assertThat(outcomes.size(), is(1));
        assertThat(outcomes.get(0).getResult(), is(TestResult.SUCCESS));
        assertThat(outcomes.get(0).getNestedStepCount(), is(7));
    }

    @Test
    public void the_given_when_then_clauses_should_count_as_steps() throws Throwable {

        // Given
        JUnitThucydidesStories story = new JUnitThucydidesStories() {
            public void configure() {
                findStoriesCalled("aPassingStoryWithSteps.story");
            }
        };

        story.setSystemConfiguration(systemConfiguration);
        story.configuredEmbedder().configuration().storyReporterBuilder().withReporters(printOutput);

        // When
        run(story);

        // Then
        List<TestOutcome> outcomes = loadTestOutcomes();

        List<TestStep> steps = outcomes.get(0).getTestSteps();
        assertThat(steps.get(0).getDescription(), is("Given I have an implemented JBehave scenario"));
        assertThat(steps.get(1).getDescription(), is("And the scenario has steps"));
        assertThat(steps.get(2).getDescription(), is("When I run the scenario"));
        assertThat(steps.get(3).getDescription(), is("Then the steps should appear in the outcome"));
    }

}