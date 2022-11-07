#!/bin/bash
set -e

psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    create database biketeam;
    create user biketeam with encrypted password 'biketeam';
    grant all privileges on database biketeam to biketeam;
EOSQL
