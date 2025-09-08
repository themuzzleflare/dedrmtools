/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import org.jetbrains.annotations.Contract;

final class SystemSymbols {
    public static final String ION = "$ion";
    public static final String ION_1_0 = "$ion_1_0";
    public static final String ION_SYMBOL_TABLE = "$ion_symbol_table";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String IMPORTS = "imports";
    public static final String SYMBOLS = "symbols";
    public static final String MAX_ID = "max_id";
    public static final String ION_SHARED_SYMBOL_TABLE = "$ion_shared_symbol_table";

    @Contract(pure = true)
    private SystemSymbols() {
    }
}
