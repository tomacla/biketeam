package info.tomacla.biketeam.service.permalink;

import info.tomacla.biketeam.common.datatype.Strings;

public abstract class AbstractPermalinkService {

    public String getPermalink(String name) {
        return this.getPermalink(name, 100, false);
    }

    public String getPermalink(String name, int maxSize, boolean forceLowerCase) {
        String permalink = Strings.normalizePermalink(name);
        if (forceLowerCase) {
            permalink = permalink.toLowerCase();
        }
        if (permalink.length() > maxSize) {
            permalink = permalink.substring(0, maxSize);
        }
        for (int i = 0; permalinkExists(permalink); i++) {
            if (permalink.length() == maxSize) {
                permalink = permalink.substring(0, maxSize - 2);
            }
            permalink = permalink + (i + 2);
        }
        return permalink;
    }

    public abstract boolean permalinkExists(String permalink);

}
