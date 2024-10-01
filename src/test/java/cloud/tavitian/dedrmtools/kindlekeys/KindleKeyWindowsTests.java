/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KindleKeyWindows Tests")
final class KindleKeyWindowsTests {
    @Test
    @DisplayName("Get Username Test")
    void getUsername() {
        byte[] username = new KindleKeyWindows().getUsername();
        System.out.printf("Username: %s%n", Util.formatByteArray(username));
    }
}
