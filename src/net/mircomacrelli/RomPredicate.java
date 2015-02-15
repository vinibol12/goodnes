package net.mircomacrelli;


import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.function.Predicate;


public class RomPredicate implements Predicate<Rom> {
    private final PathMatcher MATCHER;

    public RomPredicate(String regexp) {
        MATCHER = FileSystems.getDefault().getPathMatcher("regex:" + regexp);
    }

    @Override
    public boolean test(Rom rom) {
        return MATCHER.matches(rom.path.getFileName());
    }
}
