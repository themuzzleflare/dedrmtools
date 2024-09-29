/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import org.junit.jupiter.api.Test;

import java.util.Set;

final class KindleKeyMacOSTest {
    @Test
    void getMacAddressesMunged() {
        Set<byte[]> macAddressesMunged = KindleKeyMacOS.getMacAddressesMunged();
        System.out.printf("macAddressesMunged: %s%n", macAddressesMunged);
    }
}
