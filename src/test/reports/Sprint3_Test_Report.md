# Sprint 3 Test Report

**Report Date:** 2025-12-01
**Prepared By:** `David Loureiro`, QA Engineer

## 1. Introduction
This report summarizes the testing activities and results for Sprint 3. The primary focus of this sprint was to address the critical defects identified in Sprint 2, stabilize the AI-powered search functionality, and resolve the severe performance issues in the event discovery features. The key user stories and defects addressed were `DEF-001` (LLM Hallucination) and `DEF-002` (Broken Search).

## 2. Summary of Test Results

| User Story / Defect ID | Title                                              | Status | Notes |
|------------------------|----------------------------------------------------|---|---|
| `DEF-001` / `US-04b`   | Fix LLM Hallucinations and Improve Recommendations | Pass | The AI is now properly integrated using a Retrieval-Augmented Generation (RAG) architecture, which forces it to use real-time web search results. Prompt engineering was significantly improved to enforce stricter rules on date, time, and location extraction, resolving the hallucination issue. |
| `DEF-002`              | Fix Search and Filter Functionality                | Pass | The main search bar now correctly uses the AI-powered search. The logic has been moved from the `DiscoverEventsController` to a dedicated `EventSearchOrchestrator`, cleaning up the architecture. The carousels for "Recommended" and "Near You" are also now powered by the AI. |
| `N/A`                  | Improve Search Performance and Responsiveness      | Pass | The automatic search-on-type was replaced with a manual search button, giving the user explicit control over when to initiate a search. All API calls are now executed on background threads, which prevents the UI from freezing. This has resolved the performance and responsiveness issues. |
| `N/A`                  | Implement Event Caching                            | Pass | AI-generated events are now cached in the `EventService`. Manual testing confirms that searching for the same event twice retrieves it from the local cache the second time, improving performance and data consistency. |
| `US06`                 | Join Event                                         | Pass | Users can now successfully join an event from the event details page. The system correctly updates the attendee count, and the user's profile reflects the new event in their "Joined Events" list. Tested via manual UI testing. |
| `US07`                 | View User Profile                                  | Pass | The user profile page is now accessible and correctly displays the user's information, including their list of created and joined events. Data is consistent with the backend services. Tested via manual UI testing. |

## 3. Defect Summary

### 3.1 New Defects Found in Sprint

No new critical or high-severity defects were found during this sprint. The focus was on stabilization, and the implemented changes have proven to be effective.

### 3.2 Open Critical & High Defects

- **`DEF-001` (Critical):** LLM Hallucinates and Provides Irrelevant Recommendations - **Status: Closed.**
- **`DEF-002` (Critical):** Search and Filter Functionality is Broken - **Status: Closed.**

All critical defects from the previous sprint have been successfully resolved.

## 4. Risks and Impediments

- **Risk `RSK2` Mitigated - LLM Integration and Performance:** The risk of LLM hallucination has been successfully mitigated by implementing a RAG architecture and stricter prompt engineering. The performance and API usage concerns were addressed by moving API calls to background threads and implementing a manual search button, which prevents both UI freezes and excessive API requests. The AI integration is now considered stable.
- **No new significant risks or impediments** were identified during this sprint. The project is back on a stable track.

## 5. Conclusion & Recommendation
**Overall quality of the build: Stable.**

Sprint 3 was highly successful in resolving the critical issues that made the previous build unstable. The AI-powered search and recommendation features now function as intended. The architectural improvements, including the creation of the `EventSearchOrchestrator`, moving network calls to background threads, and implementing event caching, have made the system robust and performant.

**Recommendation:**
1.  **Proceed with Planned Features:** The team has successfully stabilized the core functionality of the MVP. Development can now proceed to the next planned user stories for Sprint 4.
2.  **Monitor API Usage:** While the manual search button reduces API calls, the team should still monitor the costs and usage of the Google AI and Search APIs to ensure they remain within budget.
3.  **User Feedback:** Now that the core discovery feature is stable, it is recommended to gather feedback from end-users on the quality and relevance of the AI-generated recommendations to guide future refinements.

The project is now in a strong position to move forward.
