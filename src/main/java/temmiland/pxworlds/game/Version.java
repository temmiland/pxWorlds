package temmiland.pxworlds.game;

import temmiland.pxworlds.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {

    /** Aktuelle Spielversion, aus pom.xml via Filtered Resources ermittelt. */
    public static final String GAME = loadVersion();

    public static final String ROLLERCOASTER = loadLibVersion();

    /** {@code true} wenn das Spiel nicht aus einer JAR gestartet wurde (d.h. via mvnw/IDE). */
    public static final boolean DEV_MODE = detectDevMode();

    private static String loadVersion() {
        final Properties properties = new Properties();
        try (InputStream input = Constants.class.getResourceAsStream("/build.properties")) {
            if (input == null) {
                return "dev";
            }
            properties.load(input);
            return properties.getProperty("app.version", "dev");
        } catch (IOException e) {
            return "dev";
        }
    }

    private static String loadLibVersion() {
        final Properties properties = new Properties();
        try (InputStream input = Constants.class.getResourceAsStream("/build.properties")) {
            if (input == null) {
                return "dev";
            }
            properties.load(input);
            return properties.getProperty("rollercoaster.version", "dev");
        } catch (IOException e) {
            return "dev";
        }
    }

    private static boolean detectDevMode() {
        final java.net.URL url = Constants.class.getResource("Constants.class");
        return url == null || !"jar".equals(url.getProtocol());
    }
}
