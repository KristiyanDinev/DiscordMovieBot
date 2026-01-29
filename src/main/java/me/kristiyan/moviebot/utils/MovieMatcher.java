package me.kristiyan.moviebot.utils;

import java.text.Normalizer;

public class MovieMatcher {
    /**
     * Check if a guess matches the actual movie title.
     * This method is lenient and handles:
     * - Case insensitivity
     * - Special characters
     * - Partial matches (if the guess contains the key words)
     * - Extra whitespace
     * - Common articles (the, a, an)
     */
    public static boolean isMatch(String guess, String actualTitle) {
        if (guess == null || actualTitle == null) {
            return false;
        }

        // Normalize both strings
        String normalizedGuess = normalize(guess);
        String normalizedTitle = normalize(actualTitle);

        // Exact match after normalization
        if (normalizedGuess.equals(normalizedTitle)) {
            return true;
        }

        // Check if guess contains all significant words from the title
        String[] titleWords = getSignificantWords(normalizedTitle);
        String[] guessWords = getSignificantWords(normalizedGuess);

        // If the guess contains all significant words from the title, it's a match
        if (containsAllWords(guessWords, titleWords)) {
            return true;
        }

        // Use Levenshtein distance for typos (allow up to 20% character difference)
        int distance = levenshteinDistance(normalizedGuess, normalizedTitle);
        int maxLength = Math.max(normalizedGuess.length(), normalizedTitle.length());
        double similarity = 1.0 - ((double) distance / maxLength);

        return similarity >= 0.8; // 80% similarity threshold
    }

    /**
     * Normalize a string by:
     * - Converting to lowercase
     * - Removing special characters
     * - Removing extra whitespace
     * - Removing accents/diacritics
     */
    private static String normalize(String text) {
        // Remove diacritics
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");

        // Convert to lowercase
        normalized = normalized.toLowerCase();

        // Remove special characters except spaces
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");

        // Remove extra whitespace
        normalized = normalized.trim().replaceAll("\\s+", " ");

        return normalized;
    }

    /**
     * Get significant words (excluding common articles and short words)
     */
    private static String[] getSignificantWords(String text) {
        String[] words = text.split("\\s+");
        return java.util.Arrays.stream(words)
                .filter(word -> word.length() > 2) // Ignore very short words
                .filter(word -> !isCommonArticle(word))
                .toArray(String[]::new);
    }

    /**
     * Check if a word is a common article or filler word
     */
    private static boolean isCommonArticle(String word) {
        return word.equals("the") || word.equals("a") || word.equals("an") ||
                word.equals("of") || word.equals("and");
    }

    /**
     * Check if the guess contains all words from the title
     */
    private static boolean containsAllWords(String[] guessWords, String[] titleWords) {
        if (titleWords.length == 0) {
            return false;
        }

        String guessText = String.join(" ", guessWords);

        for (String titleWord : titleWords) {
            if (!guessText.contains(titleWord)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculate Levenshtein distance between two strings
     * (minimum number of single-character edits to transform one string into another)
     */
    private static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[len1][len2];
    }
}
