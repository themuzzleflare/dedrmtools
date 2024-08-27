/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import java.util.ArrayList;
import java.util.List;

import static cloud.tavitian.dedrmtools.kfxdedrm.IonConstants.*;

public final class SymbolTable {
    private final List<String> table;

    public SymbolTable() {
        table = new ArrayList<>();
        // Initialise the table with a fixed size and populate predefined symbols
        for (int i = 0; i < SID_ION_1_0_MAX; i++) table.add(null);

        table.set(SID_ION, SystemSymbols.ION);
        table.set(SID_ION_1_0, SystemSymbols.ION_1_0);
        table.set(SID_ION_SYMBOL_TABLE, SystemSymbols.ION_SYMBOL_TABLE);
        table.set(SID_NAME, SystemSymbols.NAME);
        table.set(SID_VERSION, SystemSymbols.VERSION);
        table.set(SID_IMPORTS, SystemSymbols.IMPORTS);
        table.set(SID_SYMBOLS, SystemSymbols.SYMBOLS);
        table.set(SID_MAX_ID, SystemSymbols.MAX_ID);
        table.set(SID_ION_SHARED_SYMBOL_TABLE, SystemSymbols.ION_SHARED_SYMBOL_TABLE);
    }

    public String findById(int sid) {
        if (sid < 1) throw new IllegalArgumentException("Invalid symbol id");

        if (sid < table.size()) return table.get(sid);
        else return "";
    }

    public void importSymbols(IonCatalogItem catalogItem, int maxId) {
        for (int i = 0; i < maxId; i++) table.add(catalogItem.symnames().get(i));
    }

    public void importUnknown(String name, int maxId) {
        for (int i = 0; i < maxId; i++) table.add(String.format("%s#%d", name, i + 1));
    }
}
