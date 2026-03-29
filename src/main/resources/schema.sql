-- schema.sql

-- 1. USERS Table
-- Matches JdbcUserDAO: firstName, lastName, email, password, location, bio, interests_csv, eventTypes_csv
CREATE TABLE IF NOT EXISTS USERS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(255),
    lastName VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    bio CLOB,
    interests_csv VARCHAR(1000),
    eventTypes_csv VARCHAR(1000),
    firstEntry BOOLEAN DEFAULT TRUE
    );

-- 2. EVENTS Table
-- Matches JdbcEventDAO: creatorID, title, description, date, startTime, endTime, location, city, eventType, interests_csv, maxAttendees, imageURL, price
CREATE TABLE IF NOT EXISTS EVENTS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    creatorID INT,
    title VARCHAR(255),
    description CLOB,
    date DATE,
    startTime VARCHAR(20),
    endTime VARCHAR(20),
    location VARCHAR(255),
    city VARCHAR(255),
    eventType VARCHAR(255),
    interests_csv VARCHAR(1000),
    maxAttendees INT,
    imageURL VARCHAR(500),
    price DOUBLE,
    FOREIGN KEY (creatorID) REFERENCES USERS(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS EVENT_ATTENDEES (
    eventID INT,
    userID INT,
    PRIMARY KEY (eventID, userID),
    FOREIGN KEY (eventID) REFERENCES EVENTS(id) ON DELETE CASCADE,
    FOREIGN KEY (userID) REFERENCES USERS(id) ON DELETE CASCADE
    );