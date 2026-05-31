package temmiland.pxworlds;

import temmiland.pxworlds.game.PxWorlds;
import temmiland.rollercoaster.Rollercoaster;

/**
 * Haupteinstiegspunkt des pxWorlds-Spiels.
 *
 * Diese Klasse ist der minimale Einstiegspunkt und delegiert die gesamte
 * Initialisierung und Ausführung an das {@link Rollercoaster}-Framework.
 */
public class Main {

    /**
     * Startet pxWorlds über das Rollercoaster-Framework.
     */
    public static void main(String[] args) {
        Rollercoaster.run(PxWorlds.class, PxWorlds.CONFIG_DIRECTORY);
    }

}
