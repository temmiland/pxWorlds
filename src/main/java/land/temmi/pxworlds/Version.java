package land.temmi.pxworlds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides version information about the game and the underlying Rollercoaster
 * library, resolved at startup from the bundled {@code build.properties}.
 *
 * <p>The properties file is produced by Maven resource filtering from
 * {@code pom.xml}, so the values reflect the build that created the artifact.</p>
 */
public class Version {

    /** Current game version, resolved from {@code pom.xml} via filtered resources. */
    public static final String GAME = loadVersion();

    /** Version of the Rollercoaster framework the game was built against. */
    public static final String ROLLERCOASTER = loadLibVersion();

    /** {@code true} when the game was not started from a JAR (i.e. via mvnw/IDE). */
    public static final boolean DEV_MODE = detectDevMode();

    /**
     * Loads the game version from the bundled build properties.
     *
     * @return the game version, or {@code "dev"} if it cannot be resolved
     */
    private static String loadVersion() {
        final Properties properties = new Properties();
        try (InputStream input = Version.class.getResourceAsStream("/build.properties")) {
            if (input == null) {
                return "dev";
            }
            properties.load(input);
            return properties.getProperty("app.version", "dev");
        } catch (IOException e) {
            return "dev";
        }
    }

    /**
     * Loads the Rollercoaster library version from the bundled build properties.
     *
     * @return the library version, or {@code "dev"} if it cannot be resolved
     */
    private static String loadLibVersion() {
        final Properties properties = new Properties();
        try (InputStream input = Version.class.getResourceAsStream("/build.properties")) {
            if (input == null) {
                return "dev";
            }
            properties.load(input);
            return properties.getProperty("rollercoaster.version", "dev");
        } catch (IOException e) {
            return "dev";
        }
    }

    /**
     * Detects whether the game is running outside of a packaged JAR.
     *
     * @return {@code true} if the classes are not loaded from a JAR
     */
    private static boolean detectDevMode() {
        final java.net.URL url = Version.class.getResource("Version.class");
        return url == null || !"jar".equals(url.getProtocol());
    }
}
