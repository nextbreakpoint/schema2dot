package com.nextbreakpoint.schema2dot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Database {
    private final List<Schema> schemas = new ArrayList<>();
    private final Map<String, Schema> schemaMap = new HashMap<>();

    public void addSchema(Schema schema) {
        schemas.add(schema);
        schemaMap.put(schema.getName(), schema);
    }

    public Schema getSchema(String name) {
        return schemaMap.get(name);
    }

    public Set<Schema> getSchemas() {
        return new HashSet<>(schemas);
    }
}
