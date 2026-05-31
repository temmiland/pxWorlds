package temmiland.pxworlds.game.world.migration;

import temmiland.pxworlds.game.PxWorlds;
import temmiland.pxworlds.game.world.WorldMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Führt die passende {@link WorldMigration} für eine gegebene {@link WorldMeta} aus.
 *
 * <p>Bekannte Migrationen (in Reihenfolge):
 * <ol>
 *   <li>{@link MigrationV3ToV4} — migriert Format-Versionen 1–3 auf Version 4</li>
 * </ol>
 * </p>
 */
public final class MigrationRunner {

    /** Logger dieser Klasse. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationRunner.class);

    /** Alle bekannten Migrationen in Reihenfolge. */
    private static final List<WorldMigration> MIGRATIONS = List.of(
            new MigrationV3ToV4()
    );

    private MigrationRunner() { }

    /**
     * Führt die erste passende Migration für die angegebene Welt aus.
     *
     * <p>Vor der Migration wird automatisch ein Backup der Originaldatei als
     * {@code <weltname>.pxwl.bak} angelegt. Eine vorhandene Backup-Datei wird
     * überschrieben. Schlägt das Backup fehl, wird die Migration dennoch fortgesetzt.</p>
     *
     * @param meta Metadaten der zu migrierenden Welt
     * @throws IllegalStateException wenn keine Migration für die Format-Version gefunden wurde
     */
    public static void run(final WorldMeta meta) {
        if (!meta.needsMigration()) {
            return;
        }
        for (final WorldMigration migration : MIGRATIONS) {
            if (migration.supports(meta.formatVersion())) {
                backup(meta.name());
                migration.apply(meta.name());
                return;
            }
        }
        LOGGER.error("Keine Migration für Format-Version {} (Welt '{}')",
                meta.formatVersion(), meta.name());
        throw new IllegalStateException(
                "Keine Migration für Format-Version " + meta.formatVersion());
    }

    /**
     * Kopiert {@code <weltname>.pxwl} nach {@code <basisname>_backup.pxwl}.
     *
     * <p>Für Welten deren Name auf {@code _backup} endet wird kein Backup angelegt —
     * sie sind selbst bereits ein Backup. Ansonsten werden vorhandene {@code _backup}-Suffixe
     * vor dem Anhängen entfernt, damit keine {@code _backup_backup_backup}-Ketten entstehen.
     * Eine vorhandene Backup-Datei wird überschrieben.</p>
     *
     * @param worldName Name der zu sichernden Welt (ohne Dateiendung)
     */
    private static void backup(final String worldName) {
        if (worldName.endsWith("_backup")) {
            LOGGER.info("Kein Backup für Backup-Welt '{}' angelegt.", worldName);
            return;
        }
        final String backupName = worldName + "_backup";
        final File source = new File(PxWorlds.WORLD_DIRECTORY, worldName + ".pxwl");
        final File target = new File(PxWorlds.WORLD_DIRECTORY, backupName + ".pxwl");
        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Backup erstellt: {}", target.getName());
        } catch (IOException e) {
            LOGGER.warn("Backup für '{}' fehlgeschlagen, Migration wird fortgesetzt: {}",
                    worldName, e.getMessage());
        }
    }
}
