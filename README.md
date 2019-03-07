# schema2git (WIP)

    docker run --name some-postgres -p 5432:5432 -e POSTGRES_PASSWORD=password -d postgres

    docker run -it --rm -v $(pwd):/script --link some-postgres:postgres postgres psql -h postgres -U postgres

    docker run -it --rm -v $(pwd):/script --link some-postgres:postgres postgres psql -h postgres -U postgres -d test  -f /script/ddl.sql

    dot -Tpng graph.dot > graph.png

    dot -Tsvg graph.dot > graph.svg 