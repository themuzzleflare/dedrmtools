/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

public final class Debug {
    private static boolean isEnabled = false;

    private Debug() {
    }

    public static synchronized <T> void println(T message) {
        if (isEnabled) System.out.println(message);
    }

    @SuppressWarnings("unused")
    public static synchronized <T> void print(T message) {
        if (isEnabled) System.out.print(message);
    }

    public static synchronized void printf(String format, Object... args) {
        if (isEnabled) System.out.printf(format, args);
    }

    @SuppressWarnings("unused")
    public static synchronized void enable() {
        isEnabled = true;
    }

    @SuppressWarnings("unused")
    public static synchronized void disable() {
        isEnabled = false;
    }

    public static synchronized void setEnabled(boolean debug) {
        isEnabled = debug;
    }
}
