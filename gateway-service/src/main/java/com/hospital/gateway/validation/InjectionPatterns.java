package com.hospital.gateway.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Pattern-based detection for SQLi and XSS in request inputs (query, headers, body).
 * Patterns are compiled once and matched case-insensitively.
 * Returns category "SQLI" or "XSS" on first match; no PII in results.
 */
@Component
public class InjectionPatterns {

    public static final String CATEGORY_SQLI = "SQLI";
    public static final String CATEGORY_XSS = "XSS";

    private final List<PatternWithCategory> patterns;

    public InjectionPatterns() {
        this.patterns = buildDefaultPatterns();
    }

    /**
     * Optional: custom list of regex patterns (pattern string, category).
     * If null or empty, default patterns are used.
     */
    public InjectionPatterns(List<String> regexPatterns, List<String> categories) {
        if (regexPatterns != null && !regexPatterns.isEmpty() && categories != null && categories.size() == regexPatterns.size()) {
            this.patterns = new ArrayList<>();
            for (int i = 0; i < regexPatterns.size(); i++) {
                patterns.add(new PatternWithCategory(
                        Pattern.compile(regexPatterns.get(i), Pattern.CASE_INSENSITIVE),
                        categories.get(i)));
            }
        } else {
            this.patterns = buildDefaultPatterns();
        }
    }

    private static List<PatternWithCategory> buildDefaultPatterns() {
        List<PatternWithCategory> list = new ArrayList<>();
        // SQLi
        add(list, "(?i)(?:^|\\s)or\\s+1\\s*=\\s*1", CATEGORY_SQLI);
        add(list, "(?i)or\\s+1\\s*=\\s*1\\s*--", CATEGORY_SQLI);
        add(list, "(?i)union\\s+select", CATEGORY_SQLI);
        add(list, "(?i)union\\s+all\\s+select", CATEGORY_SQLI);
        add(list, "(?i);\\s*drop\\s+", CATEGORY_SQLI);
        add(list, "(?i)insert\\s+into\\s+", CATEGORY_SQLI);
        add(list, "(?i)delete\\s+from\\s+", CATEGORY_SQLI);
        add(list, "(?i)exec\\s*\\(", CATEGORY_SQLI);
        add(list, "(?i)execute\\s*\\(", CATEGORY_SQLI);
        add(list, "(?i)char\\s*\\(", CATEGORY_SQLI);
        add(list, "(?i)concat\\s*\\(", CATEGORY_SQLI);
        add(list, "--\\s*$", CATEGORY_SQLI);
        add(list, "/\\*.*\\*/", CATEGORY_SQLI);
        add(list, "'.*or.*'.*=", CATEGORY_SQLI);
        // XSS
        add(list, "(?i)<script", CATEGORY_XSS);
        add(list, "(?i)</script>", CATEGORY_XSS);
        add(list, "(?i)javascript:", CATEGORY_XSS);
        add(list, "(?i)onerror\\s*=", CATEGORY_XSS);
        add(list, "(?i)onload\\s*=", CATEGORY_XSS);
        add(list, "(?i)onclick\\s*=", CATEGORY_XSS);
        add(list, "(?i)<iframe", CATEGORY_XSS);
        add(list, "(?i)vbscript:", CATEGORY_XSS);
        add(list, "(?i)data:text/html", CATEGORY_XSS);
        return list;
    }

    private static void add(List<PatternWithCategory> list, String regex, String category) {
        list.add(new PatternWithCategory(Pattern.compile(regex), category));
    }

    /**
     * Returns the first matching category (SQLI or XSS), or null if no match.
     * Short-circuits on first match.
     */
    public String match(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (PatternWithCategory p : patterns) {
            if (p.pattern.matcher(value).find()) {
                return p.category;
            }
        }
        return null;
    }

    private static class PatternWithCategory {
        final Pattern pattern;
        final String category;

        PatternWithCategory(Pattern pattern, String category) {
            this.pattern = pattern;
            this.category = category;
        }
    }
}
