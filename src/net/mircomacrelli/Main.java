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
    
    private static Predicate<Rom> badDump = match(".+\\[b\\d*\\].+");
    private static Predicate<Rom> hacked = match(".+\\[h\\d*\\].+");
    private static Predicate<Rom> pirated = match(".+\\[p\\d*\\].+");
    private static Predicate<Rom> overdumped = match(".+\\[o\\d*\\].+");
    private static Predicate<Rom> trained = match(".+\\[t\\d*\\].+");
    private static Predicate<Rom> hacks = match(".+\\([^)]*Hack\\).+");
    private static Predicate<Rom> fixed = match(".+\\[f\\d*\\].+");
    private static Predicate<Rom> translation = match(".+\\[T[+-][^\\]]+\\].+");
    private static Predicate<Rom> prototype = match(".+\\(Prototype[^)]*\\).+");
    private static Predicate<Rom> hackedForMapper = match(".+\\[hM\\d*\\].+");
    private static Predicate<Rom> otherBadDump = match(".+\\[b[a-z]\\].+");
    private static Predicate<Rom> alternative = match(".+\\[a\\d+\\].+");
    private static Predicate<Rom> publicDomain = match(".+\\(PD\\).+");
    private static Predicate<Rom> unlicensed = match(".+\\(Unl\\).+");
    private static Predicate<Rom> pc10version = match(".+\\(PC10\\).+");
    private static Predicate<Rom> fdsConversion = match(".+\\(FDS Conversion\\).+");
    private static Predicate<Rom> gbaVersion = match(".+\\(GBA e-Reader\\).+");
    private static Predicate<Rom> vs = match(".+\\(VS\\).+");
    private static Predicate<Rom> farEast = match(".+\\[hFFE\\].+");
    private static Predicate<Rom> aladdin = match(".+\\(Aladdin\\).+");
    private static Predicate<Rom> sachen = match(".+\\(Sachen[^)]*\\).+");
    private static Predicate<Rom> chinese = match(".+\\(Ch\\).+");

    private static Predicate<Rom> checks = badDump.or(hacked).or(pirated).or(overdumped).or(trained).or(hacks)
                                                  .or(fixed).or(translation).or(prototype).or(hackedForMapper)
                                                  .or(otherBadDump).or(alternative).or(publicDomain)
                                                  .or(unlicensed).or(pc10version).or(fdsConversion).or(gbaVersion)
                                                  .or(vs).or(farEast).or(aladdin).or(sachen).or(chinese);

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

        roms.removeIf(checks);

        Map<String, List<Rom>> games = getGames(roms);
        sortRoms(games);

        moveRomsToDownloads(games);
    }

    private static void moveRomsToDownloads(Map<String, List<Rom>> games) throws IOException {
        for (List<Rom> roms : games.values()) {
            Rom rom = roms.get(0);
            Files.copy(rom.path, Paths.get("/Users/mirco/Downloads", rom.path.getFileName().toString()));
        }
    }

    private static void sortRoms(Map<String, List<Rom>> games) {
        for (List<Rom> list : games.values()) {
            if (list.size() > 1) {
                Collections.sort(list);
            }
        }
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
