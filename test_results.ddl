USE mysql;

CREATE DATABASE testresults;

GRANT ALL ON testresults.* TO 'root'@'%';

USE testresults;

DROP TABLE IF EXISTS test_results;

CREATE TABLE test_results (
    project VARCHAR(50) NOT NULL,
    revision INT NOT NULL,
    timestamp DATETIME NOT NULL,
    groupName VARCHAR(100) NOT NULL,
    testName VARCHAR(100) NOT NULL,
    attribute VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    INDEX(project,timestamp)
);

