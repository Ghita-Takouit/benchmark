# Benchmark

## Tech Stack

- **Java 17**
- **PostgreSQL**
- **Apache JMeter** (tests de charge)
- **Docker & Docker Compose** (PostgreSQL, Prometheus, Grafana, InfluxDB)

## T0 — Configuration matérielle & logicielle

<img width="1116" height="673" alt="Screenshot 2025-11-04 at 10 03 48 PM" src="https://github.com/user-attachments/assets/830355f9-a6fe-4458-bb52-d3dd325c4b81" />

## Database Setup

### PostgreSQL Schema

```sql
-- Création des tables
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    stock INT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES category(id),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_item_category ON item(category_id);
CREATE INDEX idx_item_updated_at ON item(updated_at);
```

### Data Population

```sql
-- Insertion des 2000 catégories
INSERT INTO category (code, name)
SELECT
    'CAT' || LPAD(i::TEXT, 4, '0') AS code,
    'Category ' || i AS name
FROM generate_series(1, 2000) AS s(i);

-- Insertion des 100 000 items (≈ 50 par catégorie)
INSERT INTO item (sku, name, price, stock, category_id)
SELECT
    'SKU' || LPAD((c.id * 50 + i)::TEXT, 6, '0') AS sku,
    'Item ' || c.id || '-' || i AS name,
    ROUND((RANDOM() * 1000 + 1)::NUMERIC, 2) AS price,   -- prix aléatoire entre 1 et 1000
    (RANDOM() * 100)::INT AS stock,                      -- stock aléatoire entre 0 et 100
    c.id AS category_id
FROM category c
CROSS JOIN generate_series(1, 50) AS s(i);
```
# Variante C : Spring Boot + @RestController (Spring MVC) + JPA/Hibernate.

<img width="1607" height="232" alt="Screenshot 2025-11-04 at 9 21 11 PM" src="https://github.com/user-attachments/assets/7883c50e-35fa-4682-a77c-2cb137924721" />

<img width="1579" height="918" alt="Screenshot 2025-11-04 at 9 31 23 PM" src="https://github.com/user-attachments/assets/7fbcb777-1678-468f-a15c-ae67a15aad1b" />

<img width="1579" height="918" alt="Screenshot 2025-11-04 at 9 32 14 PM" src="https://github.com/user-attachments/assets/b885f48e-e872-4bf8-b3cb-bdfdac27258e" />

<img width="1603" height="324" alt="Screenshot 2025-11-04 at 9 32 43 PM" src="https://github.com/user-attachments/assets/bd0666f4-3cc8-44e2-995c-ba4ce1220196" />

<img width="1557" height="915" alt="Screenshot 2025-11-04 at 9 33 08 PM" src="https://github.com/user-attachments/assets/d1448d81-9e3e-4ebe-826f-f85766a36f5d" />


## Scénario A : READ-heavy

### Principe

Ce scénario simule plusieurs utilisateurs qui font **des requêtes GET** :

- `/items`
- `/items/by-category` (avec différents `categoryId`)

C’est le test de base : il mesure la performance de lecture simple.

### Configuration de Grafana/Prometheux/InfluxDB:

<img width="1298" height="810" alt="Screenshot 2025-11-04 at 9 38 22 PM" src="https://github.com/user-attachments/assets/1cd4f903-b5f2-4b74-9984-1f8aa6f4e022" />

<img width="1594" height="920" alt="Screenshot 2025-11-04 at 9 38 43 PM" src="https://github.com/user-attachments/assets/100ca8a3-87b4-4097-a5b0-ce10b6550eca" />

<img width="1594" height="920" alt="Screenshot 2025-11-04 at 9 39 51 PM" src="https://github.com/user-attachments/assets/c689b3da-144b-4ecd-810d-b363f42eff65" />

<img width="1594" height="920" alt="Screenshot 2025-11-04 at 9 40 28 PM" src="https://github.com/user-attachments/assets/ddd463d7-463e-49ac-a069-99edf99f1ad9" />

<img width="1594" height="920" alt="Screenshot 2025-11-04 at 9 40 45 PM" src="https://github.com/user-attachments/assets/e7b016ff-5224-492a-b8fb-323cad389920" />

<img width="1594" height="920" alt="Screenshot 2025-11-04 at 9 40 58 PM" src="https://github.com/user-attachments/assets/d488d287-4519-4523-bb04-0fdb373f0904" />

<img width="1594" height="920" alt="Screenshot 2025-11-04 at 9 41 41 PM" src="https://github.com/user-attachments/assets/9768151c-bac1-44c5-abdc-6681a59c9089" />

<img width="1037" height="824" alt="Screenshot 2025-11-04 at 9 42 06 PM" src="https://github.com/user-attachments/assets/4248f5b1-0e24-4eb1-8407-47cef2f7245f" />

### Configuration du Thread Group dans JMeter :
## Ajouter les CSV Data Set Config - Ajouter un HTTP Request Defaults - Créer les 4 requêtes GET avec leurs pourcentages - Ajouter le Backend Listener (InfluxDB)
<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 47 48 PM" src="https://github.com/user-attachments/assets/3bf4d94a-a6c8-4e0f-8ce4-e5055e812f22" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 48 05 PM" src="https://github.com/user-attachments/assets/5f86d94a-4674-4ad5-b5b2-d3fa66a2b155" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 48 14 PM" src="https://github.com/user-attachments/assets/4c92d96e-faba-44ea-b685-26c1c6f4f6d6" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 48 24 PM" src="https://github.com/user-attachments/assets/35a63cc5-cf08-4466-8ea9-ca60ad773421" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 48 32 PM" src="https://github.com/user-attachments/assets/97658f0f-0d18-4a38-96ee-88cf62a0ab93" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 48 42 PM" src="https://github.com/user-attachments/assets/1ea87a9a-5c56-4e62-9204-7d9c8b230b92" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 48 52 PM" src="https://github.com/user-attachments/assets/9fdd5638-c508-4461-8a61-7c1a1b387532" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 49 03 PM" src="https://github.com/user-attachments/assets/a1538f7c-0b4f-43f5-a93c-1e3033ffe7ed" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 49 27 PM" src="https://github.com/user-attachments/assets/9d9eb3ba-48c6-4ea4-b7a0-69626c0123b6" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 49 36 PM" src="https://github.com/user-attachments/assets/fc80ab22-4599-4b1d-8570-cc5f76ab7c40" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 49 51 PM" src="https://github.com/user-attachments/assets/a2c040b4-9b71-4107-b604-2baccab8f4c1" />

<img width="1301" height="805" alt="Screenshot 2025-11-04 at 9 50 00 PM" src="https://github.com/user-attachments/assets/733bdf49-2eb6-4aa3-bb7d-e37b2fc6534f" />

## Lancement du scenario A avec 50 threads pendant 10 minutes:
<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 51 09 PM" src="https://github.com/user-attachments/assets/c0ae8b01-4c4e-4510-80a6-a5274d296b81" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 51 20 PM" src="https://github.com/user-attachments/assets/4e579f8f-0fca-4a1d-9ebc-16c32d1913c3" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 51 32 PM" src="https://github.com/user-attachments/assets/90712108-e7ba-4a84-a23d-1ae7e4b51641" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 51 41 PM" src="https://github.com/user-attachments/assets/3af3454c-50b1-4639-879e-0ef27f9bd04f" />

## Lancement du scenario A avec 100 threads pendant 10 minutes:
<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 52 15 PM" src="https://github.com/user-attachments/assets/72029999-1cac-4fc4-ab21-42df497fe3df" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 52 24 PM" src="https://github.com/user-attachments/assets/6b58a2ad-d310-44c9-93f8-07026904602c" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 52 35 PM" src="https://github.com/user-attachments/assets/4f236a9e-812c-49ce-8692-99dfa50c2e53" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 52 43 PM" src="https://github.com/user-attachments/assets/110a613f-621a-4093-8d5c-2df3a0121686" />

## Lancement du scenario A avec 200 threads pendant 10 minutes:
<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 53 49 PM" src="https://github.com/user-attachments/assets/2b6284d5-418e-4e14-b6cc-4c853fd48891" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 53 56 PM" src="https://github.com/user-attachments/assets/475cb413-77aa-4a8e-a6c7-f09c2fa6c04e" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 54 04 PM" src="https://github.com/user-attachments/assets/bbed843c-d8bd-4448-931f-bde2c74b6091" />

<img width="1579" height="919" alt="Screenshot 2025-11-04 at 9 54 12 PM" src="https://github.com/user-attachments/assets/f1bc9fb0-67a5-43a0-b736-79d3aadbe9a8" />

<img width="1304" height="811" alt="Screenshot 2025-11-04 at 9 54 36 PM" src="https://github.com/user-attachments/assets/d516a670-20fd-4774-a48e-a576d1183abe" />
