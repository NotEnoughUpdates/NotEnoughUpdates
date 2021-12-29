package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamerMode {
    private static final Pattern lobbyPattern = Pattern.compile("(mini|mega|m|M)([0-9]{1,3}[A-Z])");

    public static String filterLobbyNames(String line) {
        Matcher matcher = lobbyPattern.matcher(line);
        if (matcher.find() && matcher.groupCount() == 2) {
            String lobbyType = matcher.group(1);
            String lobbyId = matcher.group(2);
            long lobbyNum = Long.parseLong(lobbyId.substring(0, lobbyId.length() - 1));

            long obfLobbyNum = (lobbyNum * 9182739 + 11) % 500;
            char obfLobbyLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt((int) (obfLobbyNum % 26));

            line = line.replaceAll("(mini|mega|m|M)([0-9]{1,3}[A-Z])", lobbyType + obfLobbyNum + obfLobbyLetter);
        }
        return line;
    }

    public static String filterScoreboard(String line) {
        line = filterLobbyNames(Utils.cleanDuplicateColourCodes(line));
        return line;
    }

    public static String filterChat(String line) {
        line = filterLobbyNames(line);
        return line;
    }
}
