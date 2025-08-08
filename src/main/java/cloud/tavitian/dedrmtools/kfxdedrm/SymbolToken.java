/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

record SymbolToken(String text, int sid) {
    public SymbolToken {
        if (text.isEmpty() && sid == 0) throw new IllegalArgumentException("Symbol token must have Text or SID");
    }
}
