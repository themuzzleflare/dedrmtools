/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import java.util.List;

record IonCatalogItem(String name, int version, List<String> symnames) {
}
