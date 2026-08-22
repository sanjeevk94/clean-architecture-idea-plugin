package com.korina.cleanarch.idea.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CleanArchToolWindowFactory implements ToolWindowFactory {
    private static final Map<String, JTextArea> CONSOLES = new ConcurrentHashMap<>();

    public static void appendOutput(Project project, String text) {
        JTextArea area = CONSOLES.get(key(project));
        if (area != null) {
            area.append(text);
            area.setCaretPosition(area.getDocument().getLength());
        }
    }

    public static void clear(Project project) {
        JTextArea area = CONSOLES.get(key(project));
        if (area != null) {
            area.setText("");
        }
    }

    private static String key(Project project) {
        return project.getLocationHash();
    }

    @Override
    public boolean shouldBeAvailable(Project project) {
        return true;
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        JTextArea console = buildConsole();
        CONSOLES.put(key(project), console);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBScrollPane(console), BorderLayout.CENTER);

        var content = ContentFactory.getInstance().createContent(panel, "Output", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    private JTextArea buildConsole() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setText(
                "=============================================\n" +
                "   Clean Architecture Generator\n" +
                "=============================================\n\n" +
                "Run Tools > Generate Clean Arch Feature...\n" +
                "or right-click a package in the Project view.\n\n"
        );
        return textArea;
    }
}
