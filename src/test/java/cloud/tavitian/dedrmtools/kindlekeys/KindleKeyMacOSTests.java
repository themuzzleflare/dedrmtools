/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

@DisplayName("KindleKeyMacOS Tests")
final class KindleKeyMacOSTests {
    @Test
    @DisplayName("Get Mac Addresses Munged Test")
    void getMacAddressesMunged() {
        Set<byte[]> macAddressesMunged = KindleKeyMacOS.getMacAddressesMunged();
        System.out.printf("macAddressesMunged: %s%n", macAddressesMunged);
    }
}
