
# Sprint 2 Test Report

**Report Date:** 2025-11-17
**Prepared By:** `Paola Casas`, QA Engineer

## 1. Introduction
This report summarizes the testing activities and results for Sprint 2. The primary objectives of this sprint were to implement the event creation functionality (US-08) and the AI-powered event recommendations (US-04b). This report validates the functionality of the implemented features, assesses their alignment with acceptance criteria, and evaluates the unit test coverage.

## 2. Summary of Test Results

| User Story ID | Title | Status | Notes |
|---|---|---|---|
| `US-08` | Create Event | Pass | The event creation form and its validation logic were tested via unit tests in `EventCreationTest.java`. All tests for input validation (e.g., mandatory fields, correct data formats, valid dates) passed successfully. The component is stable. |
| `US-04b` | Browse Event Recommendations | Fail | The core functionality of this user story, which relies on LLM integration, is not working as expected. No unit tests were created as the feature is considered incomplete and unreliable. |

## 3. Defect Summary

### 3.1 New Defects Found in Sprint

| Defect ID | Severity | Title | Description | Status |
|---|---|---|---|---|
| `DEF-001` | Critical | LLM Hallucinates and Provides Irrelevant Recommendations | The AI-powered event assistant frequently "hallucinates," generating non-existent or irrelevant event suggestions. This fails to meet the core requirement of providing trustworthy recommendations and severely degrades the user experience. | Open |
| `DEF-002` | Critical | Search and Filter Functionality is Broken | The integration of the LLM feature has broken the existing event search bar and filters in the `DiscoverEventsScene`. The system no longer finds events from the application's mock database, making the primary discovery tool unusable. | Open |

### 3.2 Open Critical & High Defects

- **`DEF-001` (Critical):** LLM Hallucinates and Provides Irrelevant Recommendations
- **`DEF-002` (Critical):** Search and Filter Functionality is Broken

## 4. Risks and Impediments

- **Risk `RSK2` Realized - LLM Integration and Performance:** As predicted in the project's risk plan, the integration with the LLM has proven to be a critical failure point. The risk of the LLM "hallucinating" has been fully realized, making the feature unusable. This was identified as the project's highest technical risk and now represents a major impediment to the MVP's success.
- **Dependency on Unstable Feature:** The `DiscoverEventsScene` is now dependent on a non-functional LLM integration, which has caused regressions in previously working features (search and filters).

## 5. Conclusion & Recommendation
**Overall quality of the build: Unstable.**

While the **Event Creation (`US-08`)** feature is stable and passed all its unit tests, the failure of the **AI-Powered Recommendations (`US-04b`)** is a critical issue that compromises the MVP. The LLM integration not only fails to deliver its intended value but has also broken core functionality, such as searching for events within the app's own database.

**Recommendation:**
1.  **Halt Further Development on LLM Features:** Immediately stop any further work on `US-04b` until a clear path to resolving the hallucination and reliability issues is defined.
2.  **Prioritize Defect Fixes:** The development team should prioritize fixing `DEF-002` to restore the search and filter functionality. This may require decoupling the LLM integration from the `DiscoverEventsController` to allow the view to function with the mock database as it did previously.

The project should not proceed to the next sprint's planned work until the critical regressions are fixed and a clear, viable strategy for the AI recommendations is established.
