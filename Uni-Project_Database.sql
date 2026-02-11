CREATE TABLE gaeste (
    gast_id SERIAL PRIMARY KEY,
    vorname VARCHAR(100) NOT NULL,
    nachname VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    telefon VARCHAR(50)
);
