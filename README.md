<img src="imgs/App_Logo.png">

# Eventify

**Eventify** is an academic team project developed at the **University of Coimbra** and later mirrored to GitHub for **portfolio and CV purposes**.

> **Note:** The original development of this project took place in a shared **GitLab** repository as part of a university team workflow. This GitHub repository is a portfolio mirror created afterwards, which is why it may not reflect the original commit history.

## Table of Contents

- [Overview](#overview)
- [Academic Context](#academic-context)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Documentation](#documentation)
- [Team](#team)
- [Original GitLab Repository](#original-gitlab-repository)
- [Project Status](#project-status)

## Overview

Eventify is a desktop application designed to help users **discover local events, join activities, and connect with people with similar interests**.

The project combines a traditional desktop software architecture with external event retrieval and AI-assisted support to improve the user experience when exploring events.

## Academic Context

This project was developed as an **academic team project** at the **University of Coimbra** during the 2025–2026 academic year.

Beyond implementation, the work also involved a full software engineering process, including:

- Requirements analysis
- User stories and release planning
- UI mockups and diagrams
- Risk management
- Iterative team development

This GitHub version is intended to showcase the project in a more professional portfolio format while preserving its academic context.

## Key Features

- User registration and login
- Profile creation and editing
- Event browsing and discovery
- Event detail visualization
- Event creation and editing
- Event participation management
- Persistent local data storage
- AI-assisted event search and recommendation support

## Tech Stack

- **Language:** Java
- **Build Tool:** Maven
- **UI:** JavaFX, FXML, CSS
- **Persistence:** H2 Database, JDBC
- **AI Integration:** LangChain4j, Google Gemini
- **External Data Source:** Ticketmaster API
- **Testing:** JUnit 5, Mockito, TestFX
- **Utilities:** ControlsFX

## Architecture

The project follows a layered structure:

- **UI**  
  JavaFX scenes, controllers, and navigation logic.

- **Model**  
  Core entities, services, and assistant-related logic.

- **Persistence**  
  Database setup and JDBC-based data access implementations.

This separation improves maintainability, readability, and testability.

## Project Structure

```text
src/
├─ main/
│  ├─ java/
│  │  ├─ Model/
│  │  ├─ Persistence/
│  │  └─ UI/
│  └─ resources/
│     ├─ FXMLs/
│     ├─ images/
│     ├─ schema.sql
│     └─ style.css
└─ test/
   ├─ java/
   └─ reports/
```

The repository also includes supporting academic materials such as diagrams, mockups, and presentation assets.

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Java
- Maven
- A JavaFX-compatible environment

### Run the project

```bash
mvn clean javafx:run
```

### Run the tests

```bash
mvn test
```

## Testing

The project includes automated tests covering both application logic and UI-related flows.

Testing tools used:

- **JUnit 5** for unit testing
- **Mockito** for mocking dependencies
- **TestFX** for JavaFX UI testing

## Documentation

As part of its academic scope, the project also includes software engineering documentation and design artifacts such as:

- Use case diagram
- Domain model
- Sequence diagram
- UI mockups
- Release and sprint planning
- Risk analysis

These materials reflect the broader development process followed by the team.

## Team

This project was developed collaboratively as a university team project with colleagues from the University of Coimbra.

Thanks to Tiago, Paulo, David and Paola for being such great teammates throughout the development of the project.

## Project Status

**Completed academic project / portfolio version**

This repository is maintained as a showcase of an academic software engineering project, highlighting both the technical implementation and the development process behind it.
