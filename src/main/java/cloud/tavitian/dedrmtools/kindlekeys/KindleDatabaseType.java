/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

enum KindleDatabaseType {
    K4MACKINF2018("k4mac kinf2018"),
    K4MACKINF2011("k4mac kinf2011"),
    K4MACRAINIER("k4mac rainier"),
    K4MACKINDLEINFO("k4mac kindle-info"),

    K4PC125KINF2018("K4PC 1.25+ kinf2018"),
    K4PC19KINF2011("K4PC 1.9+ kinf2011"),
    K4PC1618KINF("K4PC 1.6-1.8 kinf"),
    K4PC15KINF("K4PC 1.5 kinf"),
    K4PCKINDLEINFO("K4PC kindle.info");

    private final String name;

    KindleDatabaseType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
