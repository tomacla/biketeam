package info.tomacla.biketeam.domain.reaction;

public class ReactionSummary {

    private String html;
    private String unicode;
    private boolean userChoice;
    private long count;

    public ReactionSummary(String html, String unicode, boolean userChoice, long count) {
        this.html = html;
        this.unicode = unicode;
        this.userChoice = userChoice;
        this.count = count;
    }

    public String getHtml() {
        return html;
    }

    public String getUnicode() {
        return unicode;
    }

    public boolean isUserChoice() {
        return userChoice;
    }

    public long getCount() {
        return count;
    }

}
