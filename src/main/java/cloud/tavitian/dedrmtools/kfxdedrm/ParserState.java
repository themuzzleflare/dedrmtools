/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

enum ParserState {
    INVALID(1),
    BEFORE_FIELD(2),
    BEFORE_TID(3),
    BEFORE_VALUE(4),
    AFTER_VALUE(5),
    EOF(6);

    private final int value;

    ParserState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
