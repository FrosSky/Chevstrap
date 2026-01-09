package com.chevstrap.rbx.Enums;

public enum LanguageRecreated {
    Automatic("Automatic"),
    Arabic("Arabic"),
    Filipino("Filipino"),
    Hindi("Hindi"),
    Indonesian("Indonesian"),
    Korean("Korean"),
    Malay("Malay"),
    Portuguese("Portuguese"),
    Portuguese_Brazilian("Portuguese, Brazilian"),
    Russian("Russian"),
    Spanish("Spanish"),
    Thai("Thai"),
    Vietnamese("Vietnamese");

    private final String displayName;

    LanguageRecreated(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

