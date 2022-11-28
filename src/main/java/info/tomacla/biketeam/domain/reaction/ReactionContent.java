package info.tomacla.biketeam.domain.reaction;

public enum ReactionContent {

    HAPPY_1("1F603"),
    HAPPY_2("1F604"),
    HAPPY_3("1F605"),
    HAPPY_4("1F929"),
    NEUTRAL("1F610"),
    SAD_1("1F641"),
    SAD_2("1F625"),
    ANGRY("1F621"),
    FOX("1F98A");

    private String unicode;

    ReactionContent(String unicode) {
        this.unicode = unicode;
    }

    public int[] surrogates() {
        int source = Integer.parseInt(unicode, 16);
        int high = (int) Math.floor((source - 0x10000) / 0x400) + 0xD800;
        int low = ((source - 0x10000) % 0x400) + 0xDC00;
        return new int[]{high, low};
    }

    public static ReactionContent valueOfSurrogates(int[] surrogates) {
        int s = ((surrogates[0] - 0xD800) * 0x400) + (surrogates[1] - 0xDC00) + 0x10000;
        String unicode = Integer.toHexString(s);
        for (ReactionContent value : ReactionContent.values()) {
            if (value.unicode.equalsIgnoreCase(unicode)) {
                return value;
            }
        }
        return null;
    }


    public static ReactionContent valueOfUnicode(String unicode) {
        if (unicode.toUpperCase().startsWith("U+")) {
            unicode = unicode.substring(2);
        }
        for (ReactionContent value : ReactionContent.values()) {
            if (value.unicode.equalsIgnoreCase(unicode)) {
                return value;
            }
        }
        return null;
    }

    public String unicode() {
        return unicode;
    }

    public String unicodeRepresentation() {
        return "U+" + unicode;
    }

    public String htmlRepresentation() {
        return "&#x" + unicode + ";";
    }

}
