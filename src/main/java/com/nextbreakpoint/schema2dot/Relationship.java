package com.nextbreakpoint.schema2dot;

import java.util.Objects;

public class Relationship {
    private final Column toColumn;
    private final Column fromColumn;
    private final Boolean exported;

    public Relationship(Column toColumn, Column fromColumn, Boolean exported) {
        this.toColumn = toColumn;
        this.fromColumn = fromColumn;
        this.exported = exported;
    }

    public Column getToColumn() {
        return toColumn;
    }

    public Column getFromColumn() {
        return fromColumn;
    }

    public Boolean getExported() {
        return exported;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return Objects.equals(toColumn, that.toColumn) &&
                Objects.equals(fromColumn, that.fromColumn) &&
                Objects.equals(exported, that.exported);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toColumn, fromColumn, exported);
    }
}
