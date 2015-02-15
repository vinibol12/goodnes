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

import static java.util.Map.Entry;


public class Main {

    private static final RomPredicate badDump = new RomPredicate(".+\\[b\\d*\\].+");
    private static final RomPredicate hacked = new RomPredicate(".+\\[h\\d*\\].+");
    private static final RomPredicate pirated = new RomPredicate(".+\\[p\\d*\\].+");
    private static final RomPredicate overdumped = new RomPredicate(".+\\[o\\d*\\].+");
    private static final RomPredicate trained = new RomPredicate(".+\\[t\\d*\\].+");
    private static final RomPredicate hacks = new RomPredicate(".+\\([^)]*Hack\\).+");
    private static final RomPredicate fixed = new RomPredicate(".+\\[f\\d*\\].+");
    private static final RomPredicate translation = new RomPredicate(".+\\[T[+-][^\\]]+\\].+");
    private static final RomPredicate prototype = new RomPredicate(".+\\(Prototype[^)]*\\).+");
    private static final RomPredicate hackedForMapper = new RomPredicate(".+\\[hM\\d*\\].+");
    private static final RomPredicate otherBadDump = new RomPredicate(".+\\[b[a-z]\\].+");
    private static final RomPredicate alternative = new RomPredicate(".+\\[a\\d+\\].+");
    private static final RomPredicate publicDomain = new RomPredicate(".+\\(PD\\).+");
    private static final RomPredicate unlicensed = new RomPredicate(".+\\(Unl\\).+");
    private static final RomPredicate pc10version = new RomPredicate(".+\\(PC10\\).+");
    private static final RomPredicate fdsConversion = new RomPredicate(".+\\(FDS Conversion\\).+");
    private static final RomPredicate gbaVersion = new RomPredicate(".+\\(GBA e-Reader\\).+");
    private static final RomPredicate vs = new RomPredicate(".+\\(VS\\).+");
    private static final RomPredicate farEast = new RomPredicate(".+\\[hFFE\\].+");
    private static final RomPredicate aladdin = new RomPredicate(".+\\(Aladdin\\).+");
    private static final RomPredicate sachen = new RomPredicate(".+\\(Sachen[^)]*\\).+");

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

        roms.removeIf(badDump);
        roms.removeIf(hacked);
        roms.removeIf(pirated);
        roms.removeIf(overdumped);
        roms.removeIf(trained);
        roms.removeIf(hacks);
        roms.removeIf(fixed);
        roms.removeIf(translation);
        roms.removeIf(prototype);
        roms.removeIf(hackedForMapper);
        roms.removeIf(otherBadDump);
        roms.removeIf(alternative);
        roms.removeIf(publicDomain);
        roms.removeIf(unlicensed);
        roms.removeIf(pc10version);
        roms.removeIf(fdsConversion);
        roms.removeIf(gbaVersion);
        roms.removeIf(vs);
        roms.removeIf(farEast);
        roms.removeIf(aladdin);
        roms.removeIf(sachen);

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
