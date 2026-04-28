package com.example.developernetworkingapp.ui.preview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.developernetworkingapp.ui.components.EnhancedCard
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.InteractiveButton
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.ProgressBar
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.components.TaskColumn
import com.example.developernetworkingapp.ui.theme.DeveloperNetworkingAppTheme

/**
 * Created by Eljesa on 28-Apr-26.
 */

// ============================================================================
// COMPONENT PREVIEWS
// ============================================================================

/**
 * Preview for GradientHeroCard Component
 *
 * This component displays a hero card with a gradient background and progress text.
 * Used at the top of dashboard and main screens to provide context and status.
 */
@Preview(showBackground = true)
@Composable
fun GradientHeroCardPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            GradientHeroCard(
                title = "Building your personalized feed",
                subtitle = "Loading collaboration intelligence...",
                progress = "Your creator feed, matching engine, and live project discovery are active"
            )
        }
    }
}

/**
 * Preview for PremiumInfoCard Component
 *
 * This component displays information about premium/advanced features.
 * Used to highlight additional functionality and capabilities.
 */
@Preview(showBackground = true)
@Composable
fun PremiumInfoCardPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            PremiumInfoCard(
                title = "Message actions",
                subtitle = "Reply in thread, react with emoji, attach task card, or jump to linked project."
            )
        }
    }
}

/**
 * Preview for TaskColumn Component
 *
 * This component displays a column of tasks with a title and divider.
 * Used in project boards and task management screens.
 */
@Preview(showBackground = true)
@Composable
fun TaskColumnPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            TaskColumn(
                title = "To Do",
                tasks = listOf(
                    "Design system setup",
                    "API documentation",
                    "Database schema review",
                    "Security audit"
                )
            )
        }
    }
}

/**
 * Preview for SectionTitle Component
 *
 * This simple text component is used to separate different sections of content.
 * Provides visual hierarchy and organization.
 */
@Preview(showBackground = true)
@Composable
fun SectionTitlePreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            SectionTitle("Developer Project Feed")
        }
    }
}

/**
 * Preview for InteractiveButton Component
 *
 * This button component features a gradient background and is used throughout
 * the app for primary interactive actions.
 */
@Preview(showBackground = true)
@Composable
fun InteractiveButtonPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            InteractiveButton(
                text = "Explore Module",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview for EnhancedCard Component
 *
 * This card component features gradient backgrounds and optional click handlers.
 * Used for displaying interactive content blocks.
 */
@Preview(showBackground = true)
@Composable
fun EnhancedCardPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            EnhancedCard(
                onClick = {}
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    androidx.compose.material3.Text(
                        "Portfolio",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                    androidx.compose.material3.Text(
                        "GitHub, LinkedIn, Personal Site",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Preview for ProgressBar Component
 *
 * This component displays a visual progress indicator.
 * Used to show task completion, project progress, and other metrics.
 */
@Preview(showBackground = true)
@Composable
fun ProgressBarPreview() {
    DeveloperNetworkingAppTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                androidx.compose.material3.Text(
                    "Project Progress",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                ProgressBar(progress = 0.3f)
                androidx.compose.material3.Text(
                    "30% Complete",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )

                androidx.compose.material3.Text(
                    "Module Completion",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                ProgressBar(progress = 0.75f)
                androidx.compose.material3.Text(
                    "75% Complete",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )

                androidx.compose.material3.Text(
                    "Sprint Progress",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                ProgressBar(progress = 1.0f)
                androidx.compose.material3.Text(
                    "100% Complete",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

