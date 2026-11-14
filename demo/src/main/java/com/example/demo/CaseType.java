package com.example.demo;

public enum CaseType {
    EMPTY(0),
    VIRUS(1),
    FIREWALL(2);
    private int code;

    CaseType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
