# Sprint 4 Test Report

**Report Date:** 2025-12-19
**Prepared By:** `Paulo Soares`, QA Engineer

## 1. Introduction
This report summarizes the testing activities and results for Sprint 4, the final sprint of the project. The primary goals were to implement full data persistence (US-11), user authentication (US-01, US-02), and finalize UI/UX optimizations for the V2.0 release. This report also covers the final refinement of the LLM recommendation quality (US-12).

## 2. Summary of Test Results

| User Story ID | Title | Status | Notes |
|---|---|---|---|
| `US-01` | Register Account | **Pass** | User registration flow is functional. New users are correctly saved to the H2 database. *See DEF-003 regarding security.* |
| `US-02` | Log In | **Pass** | Login authentication works against the database. Invalid credentials are correctly rejected. *See DEF-003.* |
| `US-11` | Persistent Data Integration | **Pass** | The file-based H2 database is successfully integrated. Data (users, events, attendees) persists between application restarts. `DatabaseManager` correctly initializes the schema. |
| `US-12` | LLM Recommendation Quality Refinement | **Pass** | The RAG architecture has been refined. `EventSearchOrchestrator` and `EventSearchService` successfully handle queries, cache results, and filter based on the persisted user profile. While acceptable for a prototype, the integration could still benefit from further improvements in response accuracy and latency. |
| `Task` | UI/UX Optimizations | **Pass** | The application styling (`style.css`) is consistent. Navigation transitions are smooth, and the "Focus" states on input fields improve accessibility. Readability was also improved across various UI components. |

## 3. Defect Summary

### 3.1 New Defects Found in Sprint

| Defect ID | Severity | Title | Description | Status |
|---|---|---|---|---|
| `DEF-003` | **Critical** | Plaintext Password Storage | **Security Vulnerability:** User passwords are stored and validated in plaintext within `JdbcUserDAO` (lines 25, 166) and the database. There is no hashing (e.g., bcrypt) implemented. This poses a severe security risk for user data. | **Open** |

### 3.2 Open Critical & High Defects

- **`DEF-003` (Critical):** Plaintext Password Storage.

*All previous defects (DEF-001, DEF-002) were closed in Sprint 3.*

## 4. Risks and Impediments

- **Risk `RSK4` (Late Data Persistence) - Closed:** The risk of late database integration causing cascading failures was successfully mitigated. The integration of H2 was completed without breaking existing domain logic.
- **New Security Risk:** The application is functionally complete (MVP), but the lack of password encryption (`DEF-003`) makes it unsuitable for a public production deployment without immediate remediation.

## 5. Conclusion & Recommendation
**Overall quality of the build: Release Candidate Ready (with Security Caveat).**

The team has successfully delivered all functionalities planned for the V2.0 Release.
- **Persistence** is fully operational.
- **Authentication** (Login/Register) is working.
- **AI Features** are stable and integrated with the new data layer.

**Recommendation:**
1**Immediate Hotfix Required:** Before any real-world usage or deployment beyond the demo, `DEF-003` (Password Hashing) **must** be resolved.
