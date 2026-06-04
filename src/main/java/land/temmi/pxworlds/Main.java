package land.temmi.pxworlds;

import land.temmi.rollercoaster.Rollercoaster;

/**
 * Main entry point of the pxWorlds game.
 *
 * <p>This class is the minimal entry point and delegates all initialization
 * and execution to the {@link Rollercoaster} framework.</p>
 */
public class Main {

    /**
     * Starts pxWorlds through the Rollercoaster framework.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        Rollercoaster.run(PxWorlds.class, PxWorlds.CONFIG_DIRECTORY);
    }

}
