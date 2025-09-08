/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import org.jetbrains.annotations.Contract;

enum ParserState {
    INVALID(1),
    BEFORE_FIELD(2),
    BEFORE_TID(3),
    BEFORE_VALUE(4),
    AFTER_VALUE(5),
    EOF(6);

    private final int value;

    @Contract(pure = true)
    ParserState(int value) {
        this.value = value;
    }

    @Contract(pure = true)
    @SuppressWarnings("unused")
    public int getValue() {
        return value;
    }
}
