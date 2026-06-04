package com.example.developernetworkingapp.domain.model

data class CollaboratorProfile(
    val id: String,
    val name: String,
    val stack: String,
    val location: String,
    val availability: String,
    val summary: String,
    val projects: List<Pair<String, String>>,
    val collaborationHistory: List<String>,
)

object CollaboratorProfiles {
    private val seeds = mapOf(
        "mina" to CollaboratorProfile(
            id = "mina",
            name = "Mina",
            stack = "Android + Firebase",
            location = "Prishtina (UTC+2)",
            availability = "14-18 hrs/week",
            summary = "Specializes in Android architecture, realtime sync flows, and stable release delivery for mobile-first teams.",
            projects = listOf(
                "DevPulse Mobile Alerts" to "Built Firebase-driven push workflows with segmented notification rules for different user cohorts.",
                "Compose Performance Lab" to "Reduced startup latency by introducing baseline profile optimization and render tracing.",
                "Remote Team Sprintboard" to "Delivered a compact sprint dashboard focused on async collaboration and mobile status updates.",
            ),
            collaborationHistory = listOf(
                "Partnered with backend engineers to define reliable event contracts for mobile sync.",
                "Led weekly release quality reviews across QA and Android contributors.",
                "Mentored 3 junior Android developers on clean architecture and testing.",
            ),
        ),
        "khaled" to CollaboratorProfile(
            id = "khaled",
            name = "Khaled",
            stack = "Backend + AI APIs",
            location = "Tirana (UTC+2)",
            availability = "10-14 hrs/week",
            summary = "Focuses on API reliability, AI integration, and performance tuning for high-traffic collaboration platforms.",
            projects = listOf(
                "Talent Graph Ranking Engine" to "Implemented weighted skill-ranking logic with caching and explainable scoring output.",
                "Realtime Notification Gateway" to "Designed queue-backed notification delivery with retry strategy and failure dashboards.",
                "LLM Workflow Assistant API" to "Integrated AI suggestion endpoints with guardrails, logging, and fallback behaviors.",
            ),
            collaborationHistory = listOf(
                "Coordinated API schemas with mobile and frontend teams to avoid integration drift.",
                "Introduced endpoint SLO tracking and alert thresholds for production incidents.",
                "Reviewed and improved database indexing strategy for search-heavy workloads.",
            ),
        ),
        "nora" to CollaboratorProfile(
            id = "nora",
            name = "Nora",
            stack = "UI/UX + React Native",
            location = "Skopje (UTC+1)",
            availability = "12-16 hrs/week",
            summary = "Combines UX research with React Native execution to ship intuitive, high-retention collaboration experiences.",
            projects = listOf(
                "CollabFlow Design System" to "Created reusable UI primitives and interaction patterns used across multiple product modules.",
                "Mentor Connect Mobile" to "Designed and built onboarding funnels that increased week-one activation rates.",
                "Cross-Platform Team Rooms" to "Shipped collaborative room interfaces with clear task context and lightweight presence cues.",
            ),
            collaborationHistory = listOf(
                "Ran user interview rounds and translated findings into prioritized UX improvements.",
                "Collaborated with product owners to map journeys and reduce onboarding friction.",
                "Worked with frontend engineers on accessible design implementation and consistency.",
            ),
        ),
    )

    fun resolve(id: String, fallbackName: String = id, fallbackStack: String = "Full Stack"): CollaboratorProfile {
        return seeds[id] ?: CollaboratorProfile(
            id = id,
            name = fallbackName.ifBlank { "Collaborator" },
            stack = fallbackStack,
            location = "Remote (UTC+2)",
            availability = "10-12 hrs/week",
            summary = "Experienced collaborator ready to contribute across design, mobile, and backend delivery.",
            projects = listOf(
                "Cross-Team Delivery Board" to "Supported sprint planning and ownership tracking across distributed teams.",
                "Feature Rollout Toolkit" to "Defined release checklists, rollout phases, and post-launch monitoring.",
                "Community Collaboration Hub" to "Contributed to discovery, updates, and async feedback workflows.",
            ),
            collaborationHistory = listOf(
                "Worked across teams to unblock dependencies during sprint execution.",
                "Documented technical decisions and handoff notes for continuity.",
                "Contributed to testing and validation before production rollouts.",
            ),
        )
    }
}
