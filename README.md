# schema2dot (experimental)

Generate a graph file in dot format from a database schema.

## How to use

Run Postgres database using Docker:

    docker run --name some-postgres -p 5432:5432 -e POSTGRES_PASSWORD=password -d postgres

Create an empty database:

    docker run -it --rm -v $(pwd):/script --link some-postgres:postgres postgres psql -h postgres -U postgres -c "create database test"

Create tables and constraints from a DDL file:

    docker run -it --rm -v $(pwd):/script --link some-postgres:postgres postgres psql -h postgres -U postgres -d test  -f /script/ddl.sql

Create or edit the configuration file:

    cat test.properties

    jdbc.url=jdbc:postgresql://localhost/test
    jdbc.username=postgres
    jdbc.password=password

Build the schema2dot command (requires Maven):

    mvn clean package

Generate the graph from the database schema:

    java -jar target/com.nextbreakpoint.schema2dot-1.0.jar test.properties

Generate a PNG image from the graph (requires Graphviz):

    dot -Tpng graph.dot > graph.png

Generate a SVG image from the graph (requires Graphviz):

    dot -Tsvg graph.dot > graph.svg
