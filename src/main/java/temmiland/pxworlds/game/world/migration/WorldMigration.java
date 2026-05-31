package temmiland.pxworlds.game.world.migration;

/**
 * Strategie zum Migrieren einer Weltdatei von einer älteren Format-Version
 * auf die aktuelle Version.
 *
 * <p>Implementierungen laden die Welt, patchen fehlende Felder und speichern
 * sie im aktuellen Format. Der {@link MigrationRunner} wählt die passende
 * Implementierung anhand der Quell-Version.</p>
 */
public interface WorldMigration {

    /**
     * Gibt an, ob diese Migration die angegebene Quell-Version behandelt.
     *
     * @param formatVersion Format-Version der zu migrierenden Datei
     * @return {@code true} wenn diese Implementierung die Version migrieren kann
     */
    boolean supports(int formatVersion);

    /**
     * Migriert die Weltdatei mit dem angegebenen Namen.
     * Lädt, patcht und überschreibt die Datei im neuen Format.
     *
     * @param worldName Weltname ohne {@code .pxwl}-Endung
     * @throws RuntimeException bei I/O-Fehler oder ungültigem Format
     */
    void apply(String worldName);
}
