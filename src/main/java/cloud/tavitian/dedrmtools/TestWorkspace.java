/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import cloud.tavitian.dedrmtools.kindlekeys.KindleKey;

public class TestWorkspace {
    public static void main(String[] args) {
        Debug.enable();
        KindleKey kindleKey = KindleKey.getInstance();
        kindleKey.getKey("C:\\Users\\paultavitian\\Downloads");
    }
}
