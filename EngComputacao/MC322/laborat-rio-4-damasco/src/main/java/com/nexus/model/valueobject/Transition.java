package com.nexus.model.valueobject;

import java.util.Objects;

public final class Transition {
    private final String fromStatusId;
    private final String toStatusId;

    public Transition(String fromStatusId, String toStatusId) {
        this.fromStatusId = fromStatusId;
        this.toStatusId = toStatusId;
    }

    public String getFromStatusId() {
        return fromStatusId;
    }

    public String getToStatusId() {
        return toStatusId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Transition that = (Transition) o;
        return Objects.equals(fromStatusId, that.fromStatusId) &&
                Objects.equals(toStatusId, that.toStatusId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromStatusId, toStatusId);
    }
}