package info.tomacla.biketeam.service.dotenv;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class DotenvPropertySource extends PropertySource<Dotenv> {

    public static final String DOTENV_PROPERTY_SOURCE_NAME = "env";

    public static void addToEnvironment(ConfigurableEnvironment environment) {
        environment
                .getPropertySources()
                .addAfter(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new DotenvPropertySource(DOTENV_PROPERTY_SOURCE_NAME));
    }

    public DotenvPropertySource(String name) {
        super(name, Dotenv.configure().ignoreIfMissing().load());
    }

    @Override
    public Object getProperty(String name) {
        String actualName = resolvePropertyName(name);
        return source.get(actualName);
    }

    protected final String resolvePropertyName(String name) {
        Assert.notNull(name, "Property name must not be null");
        String resolvedName = checkPropertyName(name);
        if (resolvedName != null) {
            return resolvedName;
        }
        String uppercasedName = name.toUpperCase();
        if (!name.equals(uppercasedName)) {
            resolvedName = checkPropertyName(uppercasedName);
            if (resolvedName != null) {
                return resolvedName;
            }
        }
        return name;
    }

    @Nullable
    private String checkPropertyName(String name) {
        // Check name as-is
        if (containsKey(name)) {
            return name;
        }
        // Check name with just dots replaced
        String noDotName = name.replace('.', '_');
        if (!name.equals(noDotName) && containsKey(noDotName)) {
            return noDotName;
        }
        // Check name with just hyphens replaced
        String noHyphenName = name.replace('-', '_');
        if (!name.equals(noHyphenName) && containsKey(noHyphenName)) {
            return noHyphenName;
        }
        // Check name with dots and hyphens replaced
        String noDotNoHyphenName = noDotName.replace('-', '_');
        if (!noDotName.equals(noDotNoHyphenName) && containsKey(noDotNoHyphenName)) {
            return noDotNoHyphenName;
        }
        // Give up
        return null;
    }

    protected boolean containsKey(String name) {
        return source.get(name) != null;
    }

}
