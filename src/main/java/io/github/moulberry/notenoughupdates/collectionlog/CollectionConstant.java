package io.github.moulberry.notenoughupdates.collectionlog;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class CollectionConstant {
    public static class DropEntry {
        public String type;
        public Pattern regex;
        public HashMap<String, String> items;
    }

    public List<DropEntry> dropdata;
}
