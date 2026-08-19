package com.korina.cleanarch.idea.gradle

import com.intellij.openapi.project.Project

/**
 * @deprecated No longer used. Code generation is now handled entirely by
 * [com.korina.cleanarch.idea.generator.DirectFileGenerator] which writes
 * files directly — no Gradle task invocation required.
 *
 * Kept as a stub to preserve git history. Safe to delete.
 */
@Deprecated("Use DirectFileGenerator instead")
class GradleTaskInvoker(
    private val project: Project,
    private val onOutput: (String) -> Unit,
    private val onComplete: (success: Boolean, generatedFiles: List<String>) -> Unit
) {
    fun invoke(@Suppress("UNUSED_PARAMETER") schemaJson: String) {
        onOutput("⚠️  GradleTaskInvoker is deprecated. Use DirectFileGenerator.\n")
        onComplete(false, emptyList())
    }
}
