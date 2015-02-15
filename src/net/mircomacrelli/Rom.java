package net.mircomacrelli;


import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Rom implements Comparable<Rom> {
    private static final Pattern TAG_PATTERN = Pattern.compile("\\([^)]+?\\)|\\[[^]]+?\\]");

    public final Path path;

    public final String gameName;

    public final List<String> tags;

    private final int score;

    public Rom(Path path) {
        this.path = path;

        tags = extractTags(path);
        score = calculateScore();
        gameName = guessGameName(path);
    }

    private int calculateScore() {
        int score = 0;

        for (String tag : tags) {
            switch (tag) {
                case "[!]":
                    score += 10;
                    break;
                case "(U)":
                    score += 1;
                    break;
                case "(E)":
                    score += 2;
                    break;
                case "(PRG1)":
                    score += 1;
                    break;
            }
        }

        return score;
    }

    private static List<String> extractTags(Path path) {
        List<String> tags = new LinkedList<>();

        Matcher matcher = TAG_PATTERN.matcher(path.getFileName().toString());
        while (matcher.find()) {
            tags.add(matcher.group());
        }

        return tags;
    }

    private static String guessGameName(Path path) {
        String fileName = path.getFileName().toString();
        Matcher matcher = TAG_PATTERN.matcher(fileName);

        if (matcher.find()) {
            return fileName.substring(0, matcher.start()).trim();
        }

        return fileName;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public int compareTo(Rom o) {
        return Integer.compare(o.score, score);
    }
}
