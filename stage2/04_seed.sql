SET search_path TO s367550, public;

-- роли
INSERT INTO role(code)
SELECT v.code
FROM (VALUES ('ADMIN'),('MEMBER'),('COMPANY_ADMIN'),('ACCOUNTANT')) AS v(code)
WHERE NOT EXISTS (SELECT 1 FROM role r WHERE r.code = v.code);

-- локации
INSERT INTO location(name, address)
SELECT v.name, v.address
FROM (VALUES
  ('Центр','ул. Главная, 1'),
  ('Технопарк','пр. Научный, 77')
) AS v(name, address)
WHERE NOT EXISTS (SELECT 1 FROM location l WHERE l.name = v.name);

-- тарифы (ВАЖНО: тут ::jsonb)
INSERT INTO tariff_plan(name, base_price_cph, cancel_policy)
SELECT v.name, v.base_price_cph, v.cancel_policy
FROM (VALUES
  ('Стандарт', 4000, '{"free_before_minutes": 60, "penalty_percent": 50}'::jsonb),
  ('Пик',       5500, '{"free_before_minutes": 120, "penalty_percent": 70}'::jsonb)
) AS v(name, base_price_cph, cancel_policy)
WHERE NOT EXISTS (SELECT 1 FROM tariff_plan t WHERE t.name = v.name);

-- дальше всё как было
INSERT INTO space(location_id, name, capacity, type, tariff_plan_id)
SELECT v.location_id, v.name, v.capacity, v.type, v.tariff_plan_id
FROM (VALUES
  ((SELECT id FROM location WHERE name='Центр'),      'Переговорная А', 6,'MEETING_ROOM',(SELECT id FROM tariff_plan WHERE name='Стандарт')),
  ((SELECT id FROM location WHERE name='Центр'),      'Open Desk #1',   1,'OPEN_DESK',   (SELECT id FROM tariff_plan WHERE name='Стандарт')),
  ((SELECT id FROM location WHERE name='Технопарк'),  'Переговорная В',10,'MEETING_ROOM',(SELECT id FROM tariff_plan WHERE name='Пик')),
  ((SELECT id FROM location WHERE name='Технопарк'),  'Open Desk #2',   1,'OPEN_DESK',   (SELECT id FROM tariff_plan WHERE name='Стандарт'))
) AS v(location_id, name, capacity, type, tariff_plan_id)
WHERE v.location_id IS NOT NULL
  AND v.tariff_plan_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM space s WHERE s.location_id = v.location_id AND s.name = v.name
  );

INSERT INTO equipment(name)
SELECT v.name
FROM (VALUES ('Проектор'),('Флипчарт'),('Спикерфон')) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM equipment e WHERE e.name = v.name);

INSERT INTO space_equipment(space_id, equipment_id, qty)
SELECT s.id, e.id, 1
FROM space s
JOIN equipment e ON e.name IN ('Проектор','Флипчарт','Спикерфон')
WHERE s.name IN ('Переговорная А','Переговорная В')
  AND NOT EXISTS (
    SELECT 1 FROM space_equipment se WHERE se.space_id = s.id AND se.equipment_id = e.id
  );

INSERT INTO user_account(email, password_hash, full_name, phone)
SELECT v.email, v.password_hash, v.full_name, v.phone
FROM (VALUES
  ('admin@cw.test','<bcrypt>','Администратор','+7-000'),
  ('user1@cw.test','<bcrypt>','Иван Петров','+7-001'),
  ('user2@cw.test','<bcrypt>','Анна Смирнова','+7-002')
) AS v(email, password_hash, full_name, phone)
WHERE NOT EXISTS (SELECT 1 FROM user_account u WHERE u.email = v.email);

INSERT INTO user_role(user_id, role_id)
SELECT ua.id, r.id
FROM user_account ua
JOIN role r ON r.code = 'ADMIN'
WHERE ua.email = 'admin@cw.test'
  AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = ua.id AND ur.role_id = r.id
  );

INSERT INTO booking(user_id, space_id, starts_at, ends_at, status)
SELECT
  (SELECT id FROM user_account WHERE email='user1@cw.test'),
  (SELECT id FROM space WHERE name='Переговорная А' LIMIT 1),
  now() + interval '1 day',
  now() + interval '1 day 2 hour',
  'PENDING'
WHERE EXISTS (SELECT 1 FROM space WHERE name='Переговорная А')
  AND EXISTS (SELECT 1 FROM user_account WHERE email='user1@cw.test');

INSERT INTO booking(user_id, space_id, starts_at, ends_at, status)
SELECT
  (SELECT id FROM user_account WHERE email='user2@cw.test'),
  (SELECT id FROM space WHERE name='Переговорная В' LIMIT 1),
  now() + interval '1 day',
  now() + interval '1 day 1 hour',
  'PENDING'
WHERE EXISTS (SELECT 1 FROM space WHERE name='Переговорная В')
  AND EXISTS (SELECT 1 FROM user_account WHERE email='user2@cw.test');

UPDATE booking
   SET status = 'CONFIRMED'
 WHERE status = 'PENDING';
