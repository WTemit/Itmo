DROP TABLE IF EXISTS Hunter CASCADE;
DROP TABLE IF EXISTS Corral CASCADE;
DROP TABLE IF EXISTS HunterInCorral CASCADE;
DROP TABLE IF EXISTS WorkersBuild CASCADE;
DROP TABLE IF EXISTS Incident CASCADE;
DROP TABLE IF EXISTS VisitHouse CASCADE;
DROP TABLE IF EXISTS Maltun CASCADE;

CREATE TABLE Hunter (Id INT PRIMARY KEY, Intelligence VARCHAR(25), PhysCapab VARCHAR(30), CanEscape BOOLEAN, kind VARCHAR(20) not NULL);
CREATE TABLE Corral (Id INT PRIMARY KEY, ElectrSens BOOLEAN);
CREATE TABLE HunterInCorral (HunterID INT REFERENCES Hunter(id), CorralID INT REFERENCES Corral(id));
CREATE TABLE WorkersBuild (Id INT PRIMARY KEY, CountVictim INT not NULL);
CREATE TABLE VisitHouse (Id INT PRIMARY KEY, SecMeasures VARCHAR(60));
CREATE TABLE Incident (HunterID INT REFERENCES Hunter(Id), VisitHouseID INT REFERENCES VisitHouse(id), WorkersID INT REFERENCES WorkersBuild(id));
CREATE TABLE Maltun (Id INT PRIMARY KEY, FearAbout INT REFERENCES Hunter(id));

INSERT INTO Hunter(id, intelligence, physcapab, canescape, kind) VALUES (1, 'не глупее шимпанзе', 'ловкие лапы', TRUE, 'велоцираптор');
INSERT INTO Hunter(id, intelligence, physcapab, canescape, kind) VALUES (2, 'глупее шимпанзе', 'мощная пасть', TRUE, 'тираннозавр');
INSERT INTO Hunter(id, intelligence, physcapab, canescape, kind) VALUES (3, 'шимпанзе', 'маленький размер', FALSE, 'Пургаториус');

INSERT INTO WorkersBuild(id, countvictim) VALUES (1, 12);
INSERT INTO WorkersBuild(id, countvictim) VALUES (2, 6);
INSERT INTO WorkersBuild(id, countvictim) VALUES (3, 3);

INSERT INTO VisitHouse(id, secmeasures) VALUES (1, 'тяжелый засов, окна из огнеупорного стекла, высокий забор');
INSERT INTO VisitHouse(id, secmeasures) VALUES (2, 'окна из огнеупорного стекла, высокий забор');
INSERT INTO VisitHouse(id, secmeasures) VALUES (3, 'ничего');

INSERT INTO Maltun(id, fearabout) VALUES (1, 1);
INSERT INTO Maltun(id, fearabout) VALUES (2, 2);
INSERT INTO Maltun(id, fearabout) VALUES (3, 3);

INSERT INTO Corral(id, ElectrSens) VALUES (1, TRUE);
INSERT INTO Corral(id, ElectrSens) VALUES (2, FALSE);

INSERT INTO HunterInCorral(HunterID, CorralID) VALUES (1, 1);
INSERT INTO HunterInCorral(HunterID, CorralID) VALUES (2, 1);
INSERT INTO HunterInCorral(HunterID, CorralID) VALUES (1, 2);

INSERT INTO Incident(VisitHouseID, HunterID, WorkersID) VALUES (1, 1, 3);
INSERT INTO Incident(VisitHouseID, HunterID, WorkersID) VALUES (2, 3, 1);
INSERT INTO Incident(VisitHouseID, HunterID, WorkersID) VALUES (2, 2, 1);