/* Задание 1*/

SELECT Н_ЛЮДИ.ИД, Н_СЕССИЯ.ИД
FROM Н_ЛЮДИ 
JOIN Н_СЕССИЯ ON Н_ЛЮДИ.ИД = Н_СЕССИЯ.ЧЛВК_ИД 
WHERE Н_ЛЮДИ.ИМЯ = 'Александр' AND Н_СЕССИЯ.ИД > 32199;

CREATE INDEX p_n_ind ON "Н_ЛЮДИ" USING BTREE("ИМЯ"); 
CREATE INDEX ses_p_id_ind ON "Н_СЕССИЯ" USING BTREE ("ЧЛВК_ИД"); 

EXPLAIN ANALYZE SELECT Н_ЛЮДИ.ИД, Н_СЕССИЯ.ИД
FROM Н_ЛЮДИ 
JOIN Н_СЕССИЯ ON Н_ЛЮДИ.ИД = Н_СЕССИЯ.ЧЛВК_ИД 
WHERE Н_ЛЮДИ.ИМЯ = 'Александр' AND Н_СЕССИЯ.ИД > 32199;

/* Задание 2 */

SELECT Н_ЛЮДИ.ИМЯ, Н_ВЕДОМОСТИ.ЧЛВК_ИД, Н_СЕССИЯ.УЧГОД 
FROM Н_ЛЮДИ 
RIGHT JOIN Н_ВЕДОМОСТИ ON Н_ВЕДОМОСТИ.ЧЛВК_ИД = Н_ЛЮДИ.ИД 
RIGHT JOIN Н_СЕССИЯ ON Н_СЕССИЯ.ЧЛВК_ИД = Н_ЛЮДИ.ИД 
WHERE Н_ЛЮДИ.ФАМИЛИЯ = 'Соколов' AND Н_ВЕДОМОСТИ.ИД = 39921;

CREATE INDEX ved_p_id_ind ON "Н_ВЕДОМОСТИ" USING HASH("ЧЛВК_ИД"); 
CREATE INDEX ses_p_id_ind ON "Н_СЕССИЯ" USING HASH("ЧЛВК_ИД"); 
CREATE INDEX p_ln_ind ON "Н_ЛЮДИ" USING BTREE("ФАМИЛИЯ");
CREATE INDEX ved_id_ind ON "Н_ВЕДОМОСТИ" USING BTREE ("ИД"); 

EXPLAIN ANALYZE SELECT Н_ЛЮДИ.ИМЯ, Н_ВЕДОМОСТИ.ЧЛВК_ИД, Н_СЕССИЯ.УЧГОД 
FROM Н_ЛЮДИ 
RIGHT JOIN Н_ВЕДОМОСТИ ON Н_ВЕДОМОСТИ.ЧЛВК_ИД = Н_ЛЮДИ.ИД 
RIGHT JOIN Н_СЕССИЯ ON Н_СЕССИЯ.ЧЛВК_ИД = Н_ЛЮДИ.ИД 
WHERE Н_ЛЮДИ.ФАМИЛИЯ = 'Соколов' AND Н_ВЕДОМОСТИ.ИД = 39921;
