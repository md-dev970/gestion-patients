package com.hospital.gateway.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InjectionPatterns Unit Tests")
class InjectionPatternsTest {

    private InjectionPatterns matcher;

    @BeforeEach
    void setUp() {
        matcher = new InjectionPatterns();
    }

    @Test
    @DisplayName("OR 1=1 in query - matches SQLI")
    void match_or1equals1_returnsSqli() {
        assertThat(matcher.match("x OR 1=1")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
        assertThat(matcher.match("or 1=1--")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
        assertThat(matcher.match("' OR 1=1 --")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
    }

    @Test
    @DisplayName("UNION SELECT - matches SQLI")
    void match_unionSelect_returnsSqli() {
        assertThat(matcher.match("x union select id from users")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
        assertThat(matcher.match("UNION ALL SELECT")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
    }

    @Test
    @DisplayName("script tag - matches XSS")
    void match_scriptTag_returnsXss() {
        assertThat(matcher.match("<script>alert(1)</script>")).isEqualTo(InjectionPatterns.CATEGORY_XSS);
        assertThat(matcher.match("prefix <script")).isEqualTo(InjectionPatterns.CATEGORY_XSS);
        assertThat(matcher.match("</script>")).isEqualTo(InjectionPatterns.CATEGORY_XSS);
    }

    @Test
    @DisplayName("javascript: and event handlers - matches XSS")
    void match_javascriptAndHandlers_returnsXss() {
        assertThat(matcher.match("javascript:alert(1)")).isEqualTo(InjectionPatterns.CATEGORY_XSS);
        assertThat(matcher.match("img onerror=alert(1)")).isEqualTo(InjectionPatterns.CATEGORY_XSS);
        assertThat(matcher.match("onload=evil()")).isEqualTo(InjectionPatterns.CATEGORY_XSS);
    }

    @Test
    @DisplayName("safe input - no match")
    void match_safeInput_returnsNull() {
        assertThat(matcher.match("Doe")).isNull();
        assertThat(matcher.match("patient search")).isNull();
        assertThat(matcher.match("")).isNull();
        assertThat(matcher.match(null)).isNull();
        assertThat(matcher.match("normal-text-with-dash")).isNull();
    }

    @Test
    @DisplayName("SQL comment pattern - matches SQLI")
    void match_sqlComment_returnsSqli() {
        assertThat(matcher.match("value--")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
    }

    @Test
    @DisplayName("DROP / INSERT / DELETE - matches SQLI")
    void match_sqlKeywords_returnsSqli() {
        assertThat(matcher.match("'; DROP TABLE users--")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
        assertThat(matcher.match("insert into admin")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
        assertThat(matcher.match("delete from users")).isEqualTo(InjectionPatterns.CATEGORY_SQLI);
    }
}
