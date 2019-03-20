package com.nextbreakpoint.schema2dot;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;

public class Main {
    private static final String SEPARATOR = "***************************************************************************";

    private static final String[] TYPES = {"TABLE"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing argument: properties file");
            return;
        }

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));
            String jdbcUrl = properties.getProperty("jdbc.url");
            String jdbcUsername = properties.getProperty("jdbc.username");
            String jdbcPassword = properties.getProperty("jdbc.password");
            new Main().run(jdbcUrl, jdbcUsername, jdbcPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface RuntimeFunction<I, O> {
        O apply(I in) throws Exception;
    }

    private static <I, O> Function<I, O> wrapFunction(RuntimeFunction<I, O> func) {
        return (rs) -> {
            try {
                return func.apply(rs);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    private interface RuntimeConsumer<I> {
        void accept(I in) throws Exception;
    }

    private static <I> Consumer<I> wrapConsumer(RuntimeConsumer<I> func) {
        return (rs) -> {
            try {
                func.accept(rs);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    private void run(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
        try (Connection conn = DriverManager.getDriver(jdbcUrl).connect(jdbcUrl, createJdbcProperties(jdbcUsername, jdbcPassword))) {
            Database database = new Database();

            try (FileWriter writer = new FileWriter("graph.dot")) {
                write(writer, "digraph database {\n");
                write(writer, "\tsplines=true; overlap=portho; model=subset;\n");
                write(writer, "\tedge [color=gray50, fontname=Calibri, fontsize=11]\n");
                write(writer, "\tnode [shape=plaintext, fontname=Calibri, fontsize=11]\n");

                scanSchemas(conn, database);

                database.getSchemas().forEach(wrapConsumer(schema -> scanTables(conn, schema)));

                database.getSchemas().forEach(wrapConsumer(schema -> schema.getTables().forEach(wrapConsumer(table -> scanColumns(conn, schema, table.getName())))));

                database.getSchemas().forEach(wrapConsumer(schema -> schema.getTables().forEach(wrapConsumer(table -> scanExportedKeys(conn, schema, table.getName())))));

                database.getSchemas().forEach(wrapConsumer(schema -> schema.getTables().forEach(wrapConsumer(table -> scanImportedKeys(conn, schema, table.getName())))));

                database.getSchemas().forEach(wrapConsumer(schema -> generateNodes(writer, schema, getTableNames(schema))));

                database.getSchemas().forEach(wrapConsumer(schema -> generateConnections(writer, schema, getTableNames(schema))));

                write(writer, "}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Set<String> getTableNames(Schema schema) {
        return schema.getTables().stream()
                .map(Table::getName)
                .collect(Collectors.toSet());
    }

    private void generateNodes(Writer writer, Schema schema, Set<String> tableNames) throws IOException {
        schema.getTables().stream().filter(table -> tableNames.contains(table.getName())).forEach(table -> {
            StringBuilder builder = new StringBuilder();
            builder.append("<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">\n");
            builder.append("\t\t<TR><TD ALIGN=\"center\" COLSPAN=\"2\"><B>").append(table.getSchema().getName().toUpperCase()).append(".").append(table.getName().toUpperCase()).append("</B></TD></TR>\n");
            builder.append("\t\t<TR><TD ALIGN=\"left\">Name</TD><TD ALIGN=\"left\">Type</TD></TR>\n");
            table.getColumns().forEach(column -> {
                if (table.getRelationship(column.getName()) != null) {
                    builder.append("\t\t<TR><TD ALIGN=\"left\" PORT=\"").append(column.getName()).append("\">").append(column.getName().toUpperCase()).append("</TD><TD ALIGN=\"left\">").append(column.getType().toUpperCase()).append("</TD></TR>\n");
                } else {
                    builder.append("\t\t<TR><TD ALIGN=\"left\">").append(column.getName().toUpperCase()).append("</TD><TD ALIGN=\"left\">").append(column.getType().toUpperCase()).append("</TD></TR>\n");
                }
            });
            builder.append("\t</TABLE>>");
            String label = builder.toString();
            write(writer, "\t" + escape(table.getName().toUpperCase()) + " [label=" + label + "];\n");
        });
    }

    private void generateConnections(Writer writer, Schema schema, Set<String> tableNames) throws IOException {
        schema.getTables().stream().flatMap(table -> table.getRelationships().stream()).filter(Relationship::getExported).forEach(relationship -> {
            write(writer, "\t" + relationship.getFromColumn().getTable().getName().toUpperCase());
            write(writer, " -> ");
            write(writer, relationship.getToColumn().getTable().getName().toUpperCase());
            write(writer, " [label=\"" + relationship.getFromColumn().getName().toUpperCase()  + ":" + relationship.getToColumn().getName().toUpperCase() + "\"];\n");
        });
    }

    private String escape(String name) {
        return name.replace("$", "_");
    }

    private void write(Writer writer, String text) {
        try {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void println(String text) {
        System.out.println(text);
    }

    private void scanSchemas(Connection conn, Database database) throws SQLException {
        println(SEPARATOR);
        println("List schemas");
        println(SEPARATOR);
        try (ResultSet catalogs = conn.getMetaData().getSchemas()) {
            printColumnNames(catalogs.getMetaData());
            while (catalogs.next()) {
                final Schema schema = new Schema(database, catalogs.getString("TABLE_SCHEM"));
                println("schema " + schema.getName());
                database.addSchema(schema);
            }
        }
    }

    private void scanTables(Connection conn, Schema schema) throws SQLException {
        println(SEPARATOR);
        println("List tables for schema " + schema.getName());
        println(SEPARATOR);
        try (ResultSet tables = conn.getMetaData().getTables(null, schema.getName(), "%", TYPES)) {
            printColumnNames(tables.getMetaData());
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                println("table " + tableName);
                schema.addTable(new Table(schema, tableName));
            }
        }
    }

    private void scanColumns(Connection conn, Schema schema, String tableName) throws SQLException {
        println(SEPARATOR);
        println("List columns for table " + tableName);
        println(SEPARATOR);
        try (ResultSet columns = conn.getMetaData().getColumns(null, schema.getName(), tableName, "%")) {
            printColumnNames(columns.getMetaData());
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                println("column " + columnName + ", " + columnType);
                Table table = schema.getTable(tableName);
                table.addColumn(new Column(table, columnName, columnType));
            }
        }
    }

    private void scanExportedKeys(Connection conn, Schema schema, String tableName) throws SQLException {
        println(SEPARATOR);
        println("List exported keys for table " + tableName);
        println(SEPARATOR);
        try (ResultSet columns = conn.getMetaData().getExportedKeys(null, schema.getName(), tableName)) {
            printColumnNames(columns.getMetaData());
            while (columns.next()) {
                String fromColumnName = columns.getString("PKCOLUMN_NAME");
                String toColumnName = columns.getString("FKCOLUMN_NAME");
                String toTableName = columns.getString("FKTABLE_NAME");
                String toSchemaName = columns.getString("FKTABLE_SCHEM");
                println("exported key " + tableName + "." + fromColumnName + " -> " + toTableName + "." + toColumnName);
                schema.getTable(tableName).addRelationship(new Relationship(schema.getTable(tableName).getColumn(fromColumnName), schema.getDatabase().getSchema(toSchemaName).getTable(toTableName).getColumn(toColumnName), true));
            }
        }
    }

    private void scanImportedKeys(Connection conn, Schema schema, String tableName) throws SQLException {
        println(SEPARATOR);
        println("List imported keys for table " + tableName);
        println(SEPARATOR);
        try (ResultSet columns = conn.getMetaData().getImportedKeys(null, schema.getName(), tableName)) {
            printColumnNames(columns.getMetaData());
            while (columns.next()) {
                String fromColumnName = columns.getString("FKCOLUMN_NAME");
                String toColumnName = columns.getString("PKCOLUMN_NAME");
                String toTableName =  columns.getString("PKTABLE_NAME");
                String toSchemaName = columns.getString("PKTABLE_SCHEM");
                println("imported key " + tableName + "." + fromColumnName + " -> " + toTableName + "." + toColumnName);
                schema.getTable(tableName).addRelationship(new Relationship(schema.getTable(tableName).getColumn(fromColumnName), schema.getDatabase().getSchema(toSchemaName).getTable(toTableName).getColumn(toColumnName), false));
            }
        }
    }

    private void printColumnNames(ResultSetMetaData metaData) throws SQLException {
        rangeClosed(1, metaData.getColumnCount())
                .mapToObj(i -> wrapFunction((ResultSetMetaData rs) -> rs.getColumnName(i)).apply(metaData))
                .forEach(System.out::println);
    }

    private Properties createJdbcProperties(String jdbcUsername, String jdbcPassword) {
        Properties aughProperties = new Properties();
        aughProperties.put("user", jdbcUsername);
        aughProperties.put("password", jdbcPassword);
        return aughProperties;
    }
}
