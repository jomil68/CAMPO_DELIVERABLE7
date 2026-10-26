package com.barangay.transtrac.model;

public enum Role {
    ADMINISTRATOR("Administrator"),
    STAFF("Barangay Staff / Clerk"),
    CAPTAIN("Barangay Captain / Official"),
    TREASURER("Treasurer");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
