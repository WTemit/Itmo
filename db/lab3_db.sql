DROP TABLE IF EXISTS Hunter CASCADE;
DROP TABLE IF EXISTS Corral CASCADE;
DROP TABLE IF EXISTS HunterInCorral CASCADE;
DROP TABLE IF EXISTS WorkersBuild CASCADE;
DROP TABLE IF EXISTS Incident CASCADE;
DROP TABLE IF EXISTS VisitHouse CASCADE;
DROP TABLE IF EXISTS Person CASCADE;
DROP TABLE IF EXISTS People CASCADE;
DROP TABLE IF EXISTS Windowe CASCADE;
DROP TABLE IF EXISTS Door CASCADE;
DROP TABLE IF EXISTS Fence CASCADE;
DROP TABLE IF EXISTS ElectroSens CASCADE;

CREATE TABLE Hunter (
Id INT PRIMARY KEY, 
Intelligence VARCHAR(31), 
PhysCapab VARCHAR(31), 
CanEscape BOOLEAN, 
kind VARCHAR(31) not NULL
);
CREATE TABLE ElectroSens (
Id INT PRIMARY KEY, 
Signal BOOLEAN
);
CREATE TABLE Corral (
Id INT PRIMARY KEY, 
ElectrSens INT REFERENCES ElectroSens(id)
);
CREATE TABLE HunterInCorral (
HunterID INT REFERENCES Hunter(id), 
CorralID INT REFERENCES Corral(id)
);
CREATE TABLE People (
id INT PRIMARY KEY, 
CountP INT NOT NULL
);
CREATE TABLE WorkersBuild (
Id INT PRIMARY KEY, 
CountVictim INT not NULL, 
Counthum INT REFERENCES People(id)
);
CREATE TABLE Windowe (
id INT PRIMARY KEY, 
Typewin VARCHAR(31)
);
CREATE TABLE Door (
id INT PRIMARY KEY, 
Typedoor VARCHAR(31)
);
CREATE TABLE Fence (
id INT PRIMARY KEY, 
Height VARCHAR(31)
);
CREATE TABLE VisitHouse (
Id INT PRIMARY KEY, 
WinID INT REFERENCES Windowe(id), 
DoorID INT REFERENCES Door(id), 
FencID INT REFERENCES Fence(id)
);

CREATE TABLE Incident (
HunterID INT REFERENCES Hunter(Id), 
VisitHouseID INT REFERENCES VisitHouse(id), 
WorkersID INT REFERENCES WorkersBuild(id)
);
CREATE TABLE Person (
Id INT PRIMARY KEY,
Name VARCHAR(31),
FearAbout INT REFERENCES Hunter(id)
);

INSERT INTO Hunter(id, intelligence, physcapab, canescape, kind) VALUES (1, 'не глупее шимпанзе', 'ловкие лапы', TRUE, 'велоцираптор');
INSERT INTO Hunter(id, intelligence, physcapab, canescape, kind) VALUES (2, 'глупее шимпанзе', 'мощная пасть', TRUE, 'тираннозавр');
INSERT INTO Hunter(id, intelligence, physcapab, canescape, kind) VALUES (3, 'шимпанзе', 'маленький размер', FALSE, 'Пургаториус');

INSERT INTO People(id, CountP) VALUES (1, 1);
INSERT INTO People(id, CountP) VALUES (2, 4);
INSERT INTO People(id, CountP) VALUES (3, 8);

INSERT INTO WorkersBuild(id, countvictim, Counthum) VALUES (1, 1, 1);
INSERT INTO WorkersBuild(id, countvictim, Counthum) VALUES (2, 6, 3);
INSERT INTO WorkersBuild(id, countvictim, Counthum) VALUES (3, 3, 2);

INSERT INTO Windowe(id, Typewin) VALUES (1, 'огнеупорное');
INSERT INTO Windowe(id, Typewin) VALUES (2, 'обычное');
INSERT INTO Windowe(id, Typewin) VALUES (3, 'цветное');

INSERT INTO Door(id, Typedoor) VALUES (1, 'с тяжелым засовом');
INSERT INTO Door(id, Typedoor) VALUES (2, 'без тяжелым засовом');

INSERT INTO Fence(id, Height) VALUES (1, 'высокий');
INSERT INTO Fence(id, Height) VALUES (2, 'низкий');
INSERT INTO Fence(id, Height) VALUES (3, 'с человека');

INSERT INTO VisitHouse(id, WinID, DoorID, FencID) VALUES (1, 1, 1, 1);
INSERT INTO VisitHouse(id, WinID, DoorID, FencID) VALUES (2, 3, 2, 1);
INSERT INTO VisitHouse(id, WinID, DoorID, FencID) VALUES (3, 2, 2, 2);

INSERT INTO Person(id, name, fearabout) VALUES (1, 'Malun', 1);
INSERT INTO Person(id, name, fearabout) VALUES (2, 'Malun', 2);
INSERT INTO Person(id, name, fearabout) VALUES (3, 'John', 3);

INSERT INTO ElectroSens(id, Signal) VALUES (1, TRUE);
INSERT INTO ElectroSens(id, Signal) VALUES (2, FALSE);

INSERT INTO Corral(id, ElectrSens) VALUES (1, 1);
INSERT INTO Corral(id, ElectrSens) VALUES (2, 2);
 
INSERT INTO HunterInCorral(HunterID, CorralID) VALUES (1, 1);
INSERT INTO HunterInCorral(HunterID, CorralID) VALUES (2, 1);
INSERT INTO HunterInCorral(HunterID, CorralID) VALUES (1, 2);

INSERT INTO Incident(VisitHouseID, HunterID, WorkersID) VALUES (1, 1, 3);
INSERT INTO Incident(VisitHouseID, HunterID, WorkersID) VALUES (2, 3, 1);
INSERT INTO Incident(VisitHouseID, HunterID, WorkersID) VALUES (3, 2, 1);

CREATE OR REPLACE FUNCTION check_corral_safety()
RETURNS TRIGGER AS $$
DECLARE
    can_hunter_escape BOOLEAN;
    is_sensor_active BOOLEAN;
    hunter_kind_name VARCHAR(31); -- Для использования в сообщении об ошибке
    corral_id_val INT;            -- Для использования в сообщении об ошибке
BEGIN
    SELECT h.CanEscape, h.kind 
    INTO can_hunter_escape, hunter_kind_name
    FROM Hunter h
    WHERE h.Id = NEW.HunterID;
	
    SELECT es.Signal 
    INTO is_sensor_active
    FROM Corral c
    JOIN ElectroSens es ON c.ElectrSens = es.Id -- Связь Corral с ElectroSens через Corral.ElectrSens
    WHERE c.Id = NEW.CorralID;
    
    corral_id_val := NEW.CorralID;

    IF can_hunter_escape IS TRUE AND is_sensor_active IS FALSE THEN
        -- Если условие истинно, генерируем исключение, отменяя вставку
        RAISE EXCEPTION 'ОПАСНОСТЬ! Нельзя помещать хищника вида "%s" (ID: %), способного к побегу, в загон (Corral ID: %) с деактивированным электрическим датчиком.',
                        hunter_kind_name, NEW.HunterID, corral_id_val;
    END IF;
	
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER tr_check_corral_safety()
BEFORE INSERT ON HunterInCorral -- Срабатывает перед вставкой
FOR EACH ROW
EXECUTE FUNCTION check_corral_safety();