package net.mircomacrelli;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class Main {

    private static Predicate<Rom> match(String regexp) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:" + regexp);
        return rom -> matcher.matches(rom.path.getFileName());
    }
    
    private static final Predicate<Rom> BAD_DUMP = match(".+\\[b\\d*\\].+");
    private static final Predicate<Rom> HACKED = match(".+\\[h\\d*\\].+");
    private static final Predicate<Rom> PIRATED = match(".+\\[p\\d*\\].+");
    private static final Predicate<Rom> OVERDUMPED = match(".+\\[o\\d*\\].+");
    private static final Predicate<Rom> TRAINED = match(".+\\[t\\d*\\].+");
    private static final Predicate<Rom> HACKS = match(".+\\([^)]*Hack\\).+");
    private static final Predicate<Rom> FIXED = match(".+\\[f\\d*\\].+");
    private static final Predicate<Rom> TRANSLATION = match(".+\\[T[+-][^\\]]+\\].+");
    private static final Predicate<Rom> PROTOTYPE = match(".+\\(Prototype[^)]*\\).+");
    private static final Predicate<Rom> HACKED_FOR_MAPPER = match(".+\\[hM\\d*\\].+");
    private static final Predicate<Rom> OTHER_BAD_DUMP = match(".+\\[b[a-z]\\].+");
    private static final Predicate<Rom> ALTERNATIVE = match(".+\\[a\\d+\\].+");
    private static final Predicate<Rom> PUBLIC_DOMAIN = match(".+\\(PD\\).+");
    private static final Predicate<Rom> UNLICENSED = match(".+\\(Unl\\).+");
    private static final Predicate<Rom> PC_10_VERSION = match(".+\\(PC10\\).+");
    private static final Predicate<Rom> FDS_CONVERSION = match(".+\\(FDS Conversion\\).+");
    private static final Predicate<Rom> GBA_VERSION = match(".+\\(GBA e-Reader\\).+");
    private static final Predicate<Rom> VERSUS = match(".+\\(VS\\).+");
    private static final Predicate<Rom> FAR_EAST = match(".+\\[hFFE\\].+");
    private static final Predicate<Rom> ALADDIN = match(".+\\(Aladdin\\).+");
    private static final Predicate<Rom> SACHEN = match(".+\\(Sachen[^)]*\\).+");
    private static final Predicate<Rom> CHINESE = match(".+\\(Ch\\).+");

    private static final Predicate<Rom> CHECKS = BAD_DUMP.or(HACKED).or(PIRATED).or(OVERDUMPED).or(TRAINED).or(HACKS)
                                                         .or(FIXED).or(TRANSLATION).or(PROTOTYPE).or(HACKED_FOR_MAPPER)
                                                         .or(OTHER_BAD_DUMP).or(ALTERNATIVE).or(PUBLIC_DOMAIN)
                                                         .or(UNLICENSED).or(PC_10_VERSION).or(FDS_CONVERSION)
                                                         .or(GBA_VERSION).or(VERSUS).or(FAR_EAST).or(ALADDIN).or(SACHEN)
                                                         .or(CHINESE);

    private static Map<String, List<Rom>> getGames(List<Rom> roms) {
        Map<String, List<Rom>> games = new HashMap<>();

        for (Rom rom : roms) {
            List<Rom> list = games.get(rom.gameName);

            if (list == null) {
                list = new LinkedList<>();
                games.put(rom.gameName, list);
            }

            list.add(rom);
        }

        return games;
    }

    public static void main(String[] args) throws IOException {
        Path dir = Paths.get(args[0]);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException(dir + " is not a directory");
        }

        List<Rom> roms = getRoms(dir);

        roms.removeIf(CHECKS);

        Map<String, List<Rom>> games = getGames(roms);
        sortRoms(games);

        moveRomsToRomsFolder(games);
    }

    private static void moveRomsToRomsFolder(Map<String, List<Rom>> games) throws IOException {
        for (List<Rom> roms : games.values()) {
            Rom rom = roms.get(0);
            Files.copy(rom.path, Paths.get("/Users/mirco/Downloads/ROMS", rom.path.getFileName().toString()));
        }
    }

    private static void sortRoms(Map<String, List<Rom>> games) {
        games.values().stream()
             .filter(list -> list.size() > 1)
             .forEach(Collections::sort);
    }

    private static List<Rom> getRoms(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            List<Rom> roms = new LinkedList<>();

            PathMatcher zipFile = FileSystems.getDefault().getPathMatcher("glob:*.zip");
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    if (zipFile.matches(path.getFileName())) {
                        roms.add(new Rom(path));
                    }
                }
            }

            return roms;
        }
    }
}
