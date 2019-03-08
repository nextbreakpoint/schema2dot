package com.nextbreakpoint.schema2dot;

import java.util.Objects;

public class Column {
    private final Table table;
    private final String name;
    private final String type;

    public Column(Table table, String name, String type) {
        this.table = table;
        this.name = name;
        this.type = type;
    }

    public Table getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return Objects.equals(table, column.table) &&
                Objects.equals(name, column.name) &&
                Objects.equals(type, column.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, name, type);
    }
}
