package me.snowdrop.licenses.sanitiser.exceptions;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExactVersionMatcherTest {
    private VersionMatcher matcher = new ExactVersionMatcher("1.0.0");

    @Test
    public void matchingString() throws Exception {
        assertThat(matcher.matches("1.0.0")).isTrue();
    }

    @Test
    public void nonmatchingString() throws Exception {
        assertThat(matcher.matches("2.0.0")).isFalse();
    }

    @Test
    public void nullString() throws Exception {
        assertThat(matcher.matches(null)).isFalse();
    }

    @Test
    public void whitespaceDifference() throws Exception {
        assertThat(matcher.matches("1.0.0 ")).isFalse();
    }
}
