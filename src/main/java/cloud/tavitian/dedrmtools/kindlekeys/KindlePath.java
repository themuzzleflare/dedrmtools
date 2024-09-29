/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

record KindlePath(KindleDatabaseType type, String path) {
    @Contract("_ -> new")
    public static @NotNull @Unmodifiable Set<KindlePath> getKindlePathsMac(String homeDir) {
        return Set.of(
                // .kinf2018 file in new location (App Store Kindle for Mac)
                new KindlePath(KindleDatabaseType.K4MACKINF2018, homeDir + "/Library/Containers/com.amazon.Kindle/Data/Library/Application Support/Kindle/storage/.kinf2018"),
                // .kinf2018 files
                new KindlePath(KindleDatabaseType.K4MACKINF2018, homeDir + "/Library/Application Support/Kindle/storage/.kinf2018"),
                // .kinf2011 file in new location (App Store Kindle for Mac)
                new KindlePath(KindleDatabaseType.K4MACKINF2011, homeDir + "/Library/Containers/com.amazon.Kindle/Data/Library/Application Support/Kindle/storage/.kinf2011"),
                // .kinf2011 files from 1.10
                new KindlePath(KindleDatabaseType.K4MACKINF2011, homeDir + "/Library/Application Support/Kindle/storage/.kinf2011"),
                // .rainier-2.1.1-kinf files from 1.6
                new KindlePath(KindleDatabaseType.K4MACRAINIER, homeDir + "/Library/Application Support/Kindle/storage/.rainier-2.1.1-kinf"),
                // .kindle-info files from 1.4
                new KindlePath(KindleDatabaseType.K4MACKINDLEINFO, homeDir + "/Library/Application Support/Kindle/storage/.kindle-info"),
                // .kindle-info file from 1.2.2
                new KindlePath(KindleDatabaseType.K4MACKINDLEINFO, homeDir + "/Library/Application Support/Amazon/Kindle/storage/.kindle-info"),
                // .kindle-info file from 1.0 beta 1 (27214)
                new KindlePath(KindleDatabaseType.K4MACKINDLEINFO, homeDir + "/Library/Application Support/Amazon/Kindle for Mac/storage/.kindle-info")
        );
    }

    @Contract("_ -> new")
    public static @NotNull @Unmodifiable Set<KindlePath> getKindlePathsWindows(String homeDir) {
        return Set.of(
                // (K4PC 1.25.1 and later) .kinf2018 file
                new KindlePath(KindleDatabaseType.K4PC125KINF2018, homeDir + "\\Amazon\\Kindle\\storage\\.kinf2018"),
                // (K4PC 1.9.0 and later) .kinf2011 file
                new KindlePath(KindleDatabaseType.K4PC19KINF2011, homeDir + "\\Amazon\\Kindle\\storage\\.kinf2011"),
                // (K4PC 1.6.0 and later) rainier.2.1.1.kinf file
                new KindlePath(KindleDatabaseType.K4PC1618KINF, homeDir + "\\Amazon\\Kindle\\storage\\rainier.2.1.1.kinf"),
                // (K4PC 1.5.0 and later) rainier.2.1.1.kinf file
                new KindlePath(KindleDatabaseType.K4PC15KINF, homeDir + "\\Amazon\\Kindle For PC\\storage\\rainier.2.1.1.kinf"),
                // original (earlier than K4PC 1.5.0) kindle-info files
                new KindlePath(KindleDatabaseType.K4PCKINDLEINFO, homeDir + "\\Amazon\\Kindle For PC\\{AMAwzsaPaaZAzmZzZQzgZCAkZ3AjA_AY}\\kindle.info")
        );
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return String.format("%s file: %s", type, path);
    }
}
