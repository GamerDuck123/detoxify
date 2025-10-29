package me.gamerduck.detoxify.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageSanitizer {

    private final Map<String, String> expansions = new HashMap<>();
    private final Map<Pattern, String> symbolReplacements = new LinkedHashMap<>();
    private final Map<Character, Character> latinMap = new HashMap<>();


    public MessageSanitizer(Path expansionFile, Path symbolsFile, Path latinFile) {
        if (Files.notExists(expansionFile.getParent())) {
            try {
                Files.createDirectories(expansionFile.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(expansionFile)) {
            try (InputStream in = MessageSanitizer.class.getClassLoader().getResourceAsStream("maps/expansionsmap.txt")) {
                if (in == null) {
                    throw new FileNotFoundException("Default config.properties not found in JAR!");
                }
                Files.copy(in, expansionFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(symbolsFile)) {
            try (InputStream in = MessageSanitizer.class.getClassLoader().getResourceAsStream("maps/symbolsmap.txt")) {
                if (in == null) {
                    throw new FileNotFoundException("Default config.properties not found in JAR!");
                }
                Files.copy(in, symbolsFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(latinFile)) {
            try (InputStream in = MessageSanitizer.class.getClassLoader().getResourceAsStream("maps/latinmap.txt")) {
                if (in == null) {
                    throw new FileNotFoundException("Default config.properties not found in JAR!");
                }
                Files.copy(in, latinFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            loadStringMap(expansionFile, expansions);
            loadSymbolsMap(symbolsFile, symbolReplacements);
            loadCharMap(latinFile, latinMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public String sanitize(String input) {
        if (input == null) return "";

        // Step 1: normalize + lowercase
        String msg = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ENGLISH);

        // Step 2: convert accented/symbolic Latin letters
        msg = convertToLatin(msg);

        // Step 3: replace leetspeak
        for (var entry : symbolReplacements.entrySet()) {
            msg = entry.getKey().matcher(msg).replaceAll(entry.getValue());
        }

        // Step 4: reconstruct split words like "f u c k" or "s.h.i.t"
        msg = reconstructSplitWords(msg);

        // Step 5: collapse repeated characters (fuuuck -> fuuck)
        msg = msg.replaceAll("(.)\\1{2,}", "$1$1");

        // Step 6: expand abbreviations
        List<String> words = new ArrayList<>(Arrays.asList(msg.split(" ")));
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (expansions.containsKey(word)) {
                words.set(i, expansions.get(word));
            }
        }

        // Step 7: Rejoin the expanded abbreviations
        msg = String.join(" ", words);

        // Step 8: clean up spaces
        msg = msg.replaceAll("\\s+", " ").trim();

        return msg;
    }


    private String convertToLatin(String input) {
        var sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            sb.append(latinMap.getOrDefault(c, c));
        }
        return sb.toString();
    }

    private static final Pattern SPLIT_WORD_PATTERN = Pattern.compile(
            "\\b(?:[a-zA-Z]\\s+){2,}[a-zA-Z]\\b"
    );

    private String reconstructSplitWords(String input) {
        Matcher m = SPLIT_WORD_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String group = m.group();
            // Remove all whitespace inside this detected split word
            String fixed = group.replaceAll("\\s+", "");
            m.appendReplacement(sb, fixed);
        }

        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PAIR = Pattern.compile(
            "^\\s*\"?([^\"=]+?)\"?\\s*=\\s*\"?([^\"].*?)\"?\\s*(?:#.*)?$"
    );

    public static void loadStringMap(Path path, Map<String, String> target) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            loadStringMap(reader, target, path.toString());
        }
    }

    private static void loadStringMap(BufferedReader reader, Map<String, String> target, String sourceName) throws IOException {
        String line;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            Matcher m = PAIR.matcher(line);
            if (!m.matches()) {
                System.err.printf("MapLoader: failed to parse line %d in %s: %s%n", lineNo, sourceName, line);
                continue;
            }

            String key = stripQuotes(m.group(1)).trim();
            String value = stripQuotes(m.group(2)).trim();
            if (key.isEmpty()) continue;

            target.put(key.toLowerCase(Locale.ENGLISH), value.toLowerCase(Locale.ENGLISH));
        }
    }

    public static void loadSymbolsMap(Path path, Map<Pattern, String> target) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            loadSymbolsMap(reader, target, path.toString());
        }
    }

    private static void loadSymbolsMap(BufferedReader reader, Map<Pattern, String> target, String sourceName) throws IOException {
        String line;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            Matcher m = PAIR.matcher(line);
            if (!m.matches()) {
                System.err.printf("MapLoader: failed to parse line %d in %s: %s%n", lineNo, sourceName, line);
                continue;
            }

            String key = stripQuotes(m.group(1)).trim();
            String value = stripQuotes(m.group(2)).trim();
            if (key.isEmpty()) continue;

            target.put(Pattern.compile(key.toLowerCase(Locale.ENGLISH)), value.toLowerCase(Locale.ENGLISH));
        }
    }

    public static void loadCharMap(Path path, Map<Character, Character> target) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            loadCharMap(reader, target, path.toString());
        }
    }

    private static void loadCharMap(BufferedReader reader, Map<Character, Character> target, String sourceName) throws IOException {
        String line;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            Matcher m = PAIR.matcher(line);
            if (!m.matches()) {
                System.err.printf("MapLoader: failed to parse line %d in %s: %s%n", lineNo, sourceName, line);
                continue;
            }

            String key = stripQuotes(m.group(1)).trim();
            String value = stripQuotes(m.group(2)).trim();
            if (key.isEmpty() || value.isEmpty()) continue;

            target.put(key.charAt(0), value.charAt(0));
        }
    }

    private static String stripQuotes(String s) {
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"").replace("\\'", "'").replace("\\\\", "\\");
    }
}
