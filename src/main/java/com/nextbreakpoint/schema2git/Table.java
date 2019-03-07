package com.nextbreakpoint.schema2git;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table {
    private final Schema schema;
    private final String name;
    private final List<Column> columns = new ArrayList<>();
    private final Map<String, Column> columnMap = new HashMap<>();
    private final Set<Relationship> relationships = new HashSet<>();
    private final Map<String, Relationship> relationshipMap = new HashMap<>();

    public Table(Schema schema, String name) {
        this.schema = schema;
        this.name = name;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public void addColumn(Column column) {
        columns.add(column);
        columnMap.put(column.getName(), column);
    }

    public Column getColumn(String name) {
        return columnMap.get(name);
    }

    public Set<Column> getColumns() {
        return new HashSet<>(columns);
    }

    public void addRelationship(Relationship relationship) {
        relationships.add(relationship);
        relationshipMap.put(relationship.getToColumn().getName(), relationship);
    }

    public Relationship getRelationship(String name) {
        return relationshipMap.get(name);
    }

    public Set<Relationship> getRelationships() {
        return new HashSet<>(relationships);
    }
}
