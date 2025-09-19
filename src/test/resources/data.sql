-- GARAGEM
INSERT INTO garage (code, name, base_price, max_capacity, occupied)
VALUES ('1','Main Garage', 10.00, 4, 0);

-- SETORES
INSERT INTO garage_sector (garage_id, code, name, max_capacity, occupied, is_closed)
VALUES
  ((SELECT id FROM garage WHERE code='1'), '1', 'Sector 1', 1, 0, FALSE),
  ((SELECT id FROM garage WHERE code='1'), '2', 'Sector 2', 1, 0, FALSE),
  ((SELECT id FROM garage WHERE code='1'), '3', 'Sector 3', 2, 0, FALSE);

-- VAGAS: setor 1 (100 vagas)
INSERT INTO spot (garage_id, sector_id, lat, lng, occupied)
SELECT g.id, s.id,
       -23.561684 + (r.rn/10000.0),
       -46.655981 - (r.rn/10000.0),
       FALSE
FROM garage g
JOIN garage_sector s ON s.garage_id = g.id AND s.code='1'
JOIN SYSTEM_RANGE(1, 2) r(rn)
WHERE g.code='1';

-- VAGAS: setor 2 (100 vagas)
INSERT INTO spot (garage_id, sector_id, lat, lng, occupied)
SELECT g.id, s.id,
       -23.561684 + (r.rn/10000.0),
       -46.655981 - (r.rn/10000.0),
       FALSE
FROM garage g
JOIN garage_sector s ON s.garage_id = g.id AND s.code='2'
JOIN SYSTEM_RANGE(1, 1) r(rn)
WHERE g.code='1';

-- VAGAS: setor 3 (150 vagas)
INSERT INTO spot (garage_id, sector_id, lat, lng, occupied)
SELECT g.id, s.id,
       -23.561684 + (r.rn/10000.0),
       -46.655981 - (r.rn/10000.0),
       FALSE
FROM garage g
JOIN garage_sector s ON s.garage_id = g.id AND s.code='3'
JOIN SYSTEM_RANGE(1, 1) r(rn)
WHERE g.code='1';
