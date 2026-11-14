package com.example.demo;

public enum caseType {
    EMPTY(0),
    VIRUS(-1),
    FIREWALL(1);

    public int index;

    caseType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static caseType get(Tools tool) {
        if (tool instanceof Firewall) {
            return FIREWALL;
        } else {
            return EMPTY;
        }
    }
}
