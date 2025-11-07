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


## Scénario A : Join Filter
<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 54 05 PM" src="https://github.com/user-attachments/assets/d6a925bb-2e8f-418d-a44a-453580135c21" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 54 14 PM" src="https://github.com/user-attachments/assets/98d87416-fc6c-46cb-8ea2-149fcc020af8" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 54 23 PM" src="https://github.com/user-attachments/assets/9c6edd20-7fd5-4c08-ab20-13a4debbbeab" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 54 30 PM" src="https://github.com/user-attachments/assets/8691f30b-2799-4441-ba0f-33fdf0e0cfcf" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 54 38 PM" src="https://github.com/user-attachments/assets/2c5d23d2-b176-42be-97b3-d62ab1e2956d" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 54 46 PM" src="https://github.com/user-attachments/assets/0100859c-0045-40a6-9020-81b506f8c94a" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 54 54 PM" src="https://github.com/user-attachments/assets/15301102-ff49-4f18-a46b-314a35dd3fda" />
<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 55 13 PM" src="https://github.com/user-attachments/assets/99b7af74-a849-4790-935e-bfe04d6eed0c" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 55 24 PM" src="https://github.com/user-attachments/assets/e300b52e-f22a-49bd-a3f8-2cdee4009340" />

<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 55 31 PM" src="https://github.com/user-attachments/assets/72a4dac2-28f2-4677-bebf-9c92744fb93e" />
<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 55 40 PM" src="https://github.com/user-attachments/assets/49a3a175-e9eb-43c3-9d44-621c06000ee4" />


<img width="1298" height="810" alt="Screenshot 2025-11-07 at 3 55 49 PM" src="https://github.com/user-attachments/assets/2161db01-ba97-49d0-b93d-522cb73f7230" />

### Thread 60 
<img width="1588" height="916" alt="Screenshot 2025-11-07 at 3 58 33 PM" src="https://github.com/user-attachments/assets/ac814491-4f09-4953-bb8e-b1cb64010f50" />

<img width="1588" height="916" alt="Screenshot 2025-11-07 at 3 58 42 PM" src="https://github.com/user-attachments/assets/336c0cb5-bba6-45de-a360-f87feb55cc70" />

<img width="1588" height="916" alt="Screenshot 2025-11-07 at 3 58 49 PM" src="https://github.com/user-attachments/assets/59888c79-9ba2-4c10-8adb-a5ba976f8619" />

<img width="1588" height="916" alt="Screenshot 2025-11-07 at 3 58 58 PM" src="https://github.com/user-attachments/assets/7a2d4a17-f32d-4c35-b48a-e6ae838eec5a" />

<img width="1294" height="805" alt="Screenshot 2025-11-07 at 3 59 18 PM" src="https://github.com/user-attachments/assets/e997de2a-f714-4608-bcad-5e8868e0ee51" />

<img width="1365" height="821" alt="Screenshot 2025-11-07 at 3 59 31 PM" src="https://github.com/user-attachments/assets/9cda7a03-d91e-483d-9ee7-280bc46dad32" />
<img width="1365" height="821" alt="Screenshot 2025-11-07 at 3 59 36 PM" src="https://github.com/user-attachments/assets/9e0085f0-bce4-491a-b6a0-70d996ea1dd3" />
<img width="1365" height="821" alt="Screenshot 2025-11-07 at 3 59 49 PM" src="https://github.com/user-attachments/assets/f452ca14-bdbc-4682-89c6-60171e2f1f58" />

<img width="1365" height="821" alt="Screenshot 2025-11-07 at 3 59 56 PM" src="https://github.com/user-attachments/assets/a56e7483-230c-4572-ab8e-e2e79dd03480" />

### Thread 120

<img width="1299" height="805" alt="Screenshot 2025-11-07 at 4 00 47 PM" src="https://github.com/user-attachments/assets/978b021f-250c-4354-a508-79b3f95c6058" />

<img width="1589" height="922" alt="Screenshot 2025-11-07 at 4 01 02 PM" src="https://github.com/user-attachments/assets/ed02ffc1-385d-47a2-8717-cdfbee32b5a7" />
<img width="1589" height="922" alt="Screenshot 2025-11-07 at 4 01 10 PM" src="https://github.com/user-attachments/assets/c3b4599b-1843-4d75-ba42-552067e9b2b6" />
<img width="1589" height="922" alt="Screenshot 2025-11-07 at 4 01 16 PM" src="https://github.com/user-attachments/assets/872f966a-9d3e-474e-a4f2-b479d7c8088a" />
<img width="1589" height="922" alt="Screenshot 2025-11-07 at 4 01 26 PM" src="https://github.com/user-attachments/assets/74e33f99-0aeb-4c4d-9c96-aec07bd8f09f" />
<img width="1456" height="805" alt="Screenshot 2025-11-07 at 4 01 51 PM" src="https://github.com/user-attachments/assets/3fe53972-cdb9-4000-ae6f-d260c6d702a2" />

# Scenario Mixed
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 28 59 PM" src="https://github.com/user-attachments/assets/0a160fe3-f068-4468-9243-20441a5af258" />
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 30 50 PM" src="https://github.com/user-attachments/assets/84a438d7-a673-4ab9-8a5a-021091e8e358" />
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 31 50 PM" src="https://github.com/user-attachments/assets/8c0d2bc1-9ddc-4ce5-9bd1-6df6994ba48d" />
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 32 42 PM" src="https://github.com/user-attachments/assets/71a5ee97-fe2a-4eed-94f8-b7eaf18e7ba2" />
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 33 24 PM" src="https://github.com/user-attachments/assets/f3f51e3d-fe8b-4373-b427-fb09719f532d" />
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 33 48 PM" src="https://github.com/user-attachments/assets/e1513f6b-add1-4425-9c60-122c95490214" />
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 34 19 PM" src="https://github.com/user-attachments/assets/155eb571-fb1e-462f-9ba7-70a4e35a7035" />
<img width="1456" height="952" alt="Screenshot 2025-11-07 at 4 36 24 PM" src="https://github.com/user-attachments/assets/b7074acf-ec5b-41c7-be1b-44560b7ddb7b" />
