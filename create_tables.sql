-- Active: 1770559102906@@127.0.0.1@5432@roads_db
CREATE TABLE obstacles(
    ID VARCHAR(50) PRIMARY KEY ,
    ID_ROUTE VARCHAR(500),
    ID_VILLE_DEBUT VARCHAR(50),
    ID_VILLE_FIN VARCHAR(50),
    DISTANCE_DEBUT NUMERIC (10,2),
    DISTANCE_FIN NUMERIC(10,2)
);

CREATE TABLE IF NOT EXISTS commune (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    longitude   NUMERIC(11, 8) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    latitude    NUMERIC(10, 8) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    population  INTEGER
);

CREATE TABLE rn_commune_sert (
    rn_id          INTEGER NOT NULL REFERENCES routes_mada(ogc_fid),
    commune_id     INTEGER NOT NULL REFERENCES commune(id),
    
    distance_m     NUMERIC(10,2),
    
    PRIMARY KEY (rn_id, commune_id)
);