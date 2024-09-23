/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Util;
import org.junit.jupiter.api.Test;

final class KindleKeyWindowsTest {
    @Test
    void getUsername() {
        byte[] username = new KindleKeyWindows().getUsername();
        System.out.printf("Username: %s%n", Util.formatByteArray(username));
    }
}
