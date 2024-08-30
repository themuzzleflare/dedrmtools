/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

public record KDatabase(String dbFile, KindleDatabase<String> kindleDatabase) {
}
