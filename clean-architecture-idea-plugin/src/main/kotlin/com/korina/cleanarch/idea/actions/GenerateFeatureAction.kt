package com.korina.cleanarch.idea.actions

import com.korina.cleanarch.idea.generator.DirectFileGenerator
import com.korina.cleanarch.idea.notifications.CleanArchNotifier
import com.korina.cleanarch.idea.toolwindow.CleanArchToolWindowFactory
import com.korina.cleanarch.idea.ui.FeatureSchemaDialog
import com.korina.cleanarch.idea.util.SchemaSerializer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * IntelliJ [AnAction] that drives the full generation workflow:
 *
 * 1. Opens [FeatureSchemaDialog] to collect user input.
 * 2. Converts the UI state to [IdeFeatureSchema] via [SchemaSerializer].
 * 3. Activates the **Clean Architecture Generator** tool window and clears it.
 * 4. Hands the schema to [DirectFileGenerator], which writes all Kotlin files
 *    directly to the project's file system — no Gradle plugin required.
 * 5. Streams progress to the tool window and shows a balloon when done.
 */
class GenerateFeatureAction : AnAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // ── 1. Show dialog ───────────────────────���────────────────────────────
        val dialog = FeatureSchemaDialog(project)
        if (!dialog.showAndGet()) return   // user cancelled

        val uiSchema  = dialog.getSchema()
        val ideSchema = SchemaSerializer.toIdeSchema(uiSchema)

        // ── 2. Activate tool window ───────────────────────────────────────────
        ToolWindowManager.getInstance(project)
            .getToolWindow("CleanArch")
            ?.apply { show(); activate(null) }

        CleanArchToolWindowFactory.clear(project)
        CleanArchToolWindowFactory.appendOutput(
            project,
            "Generating feature '${ideSchema.featureName}'…\n\n"
        )

        // ── 3. Generate files directly (no Gradle required) ───────────────────
        DirectFileGenerator(
            project    = project,
            onOutput   = { line -> CleanArchToolWindowFactory.appendOutput(project, line) },
            onComplete = { success, files ->
                if (success) {
                    CleanArchNotifier.notifySuccess(project, ideSchema.featureName, files.size)
                    if (files.isNotEmpty()) {
                        CleanArchToolWindowFactory.appendOutput(
                            project,
                            "\n  Generated files:\n" + files.joinToString("\n") { "   $it" } + "\n"
                        )
                    }
                } else {
                    CleanArchNotifier.notifyFailure(project, ideSchema.featureName)
                }
            }
        ).generate(ideSchema)
    }
}
