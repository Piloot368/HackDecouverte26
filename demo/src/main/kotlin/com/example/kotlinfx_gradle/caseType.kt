package com.example.kotlinfx_gradle

enum class caseType(var index: Int) {
    EMPTY(0),
    VIRUS(-1),
    BOURGEON(-2),
    ESTABLISHED(-3),
    FIREWALL(1);

    companion object {
        fun get(tool: Tools?): caseType {
            if (tool is Firewall) {
                return caseType.FIREWALL
            } else {
                return caseType.EMPTY
            }
        }
    }
}