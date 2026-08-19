package com.korina.cleanarch.idea.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * Factory that creates the **Clean Architecture Generator** bottom tool window.
 *
 * The tool window hosts a monospaced read-only [JTextArea] that receives live
 * output forwarded from [com.korina.cleanarch.idea.generator.DirectFileGenerator].
 *
 * Static helpers [appendOutput] and [clear] allow other components to write
 * into the console without holding a direct reference to the tool-window panel.
 */
class CleanArchToolWindowFactory : ToolWindowFactory {

    // ── Static console registry (keyed by project hash) ──────────────────────
    companion object {
        private val consoles = mutableMapOf<String, JTextArea>()

        /** Appends [text] to the console of the given [project]. Thread-safe (EDT only). */
        fun appendOutput(project: Project, text: String) {
            consoles[key(project)]?.let { area ->
                area.append(text)
                // Auto-scroll to the bottom
                area.caretPosition = area.document.length
            }
        }

        /** Clears the console of the given [project]. */
        fun clear(project: Project) {
            consoles[key(project)]?.text = ""
        }

        private fun key(project: Project) = project.locationHash
    }

    // ── ToolWindowFactory contract ────────────────────────────────────────────

    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val console = buildConsole()
        consoles[key(project)] = console

        val panel = JPanel(BorderLayout())
        panel.add(JBScrollPane(console), BorderLayout.CENTER)

        val content = ContentFactory.getInstance()
            .createContent(panel, "Output", /* isLockable = */ false)
        toolWindow.contentManager.removeAllContents(true)
        toolWindow.contentManager.addContent(content)
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun buildConsole(): JTextArea = JTextArea().apply {
        isEditable  = false
        lineWrap    = false
        font        = Font(Font.MONOSPACED, Font.PLAIN, 12)
        text        = buildString {
            appendLine("╔══════════════════════════════════════════════════╗")
            appendLine("║        Clean Architecture Generator              ║")
            appendLine("╚══════════════════════════════════════════════════╝")
            appendLine()
            appendLine("Run  Tools > Generate Clean Arch Feature…")
            appendLine("or right-click a package in the Project view.")
            appendLine()
        }
    }
}
