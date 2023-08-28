FROM postgres

ENV POSTGRES_PASSWORD docker
ENV POSTGRES_DB catan_ai

COPY world.sql /docker-entrypoint-initdb.d/
