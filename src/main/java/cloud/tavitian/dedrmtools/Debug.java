/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

public final class Debug {
    private static boolean DEBUG = false;

    public static void log(String message) {
        if (DEBUG) System.out.println(message);
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }
}
