/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

public final class Debug {
    private static boolean isEnabled = false;

    private Debug() {
    }

    public static <T> void println(T message) {
        if (isEnabled) System.out.println(message);
    }

    public static <T> void print(T message) {
        if (isEnabled) System.out.print(message);
    }

    public static void printf(String format, Object... args) {
        if (isEnabled) System.out.printf(format, args);
    }

    public static void enable() {
        isEnabled = true;
    }

    public static void disable() {
        isEnabled = false;
    }

    public static void setEnabled(boolean debug) {
        isEnabled = debug;
    }
}
