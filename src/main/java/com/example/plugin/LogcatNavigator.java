package com.example.plugin;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;


public class LogcatNavigator {
    /**
     * Navigates to the Android Studio Logcat window and filters for a specific timestamp
     * @param timestamp The timestamp to search for in Logcat
     */
    public static void navigateToLogcatEntry(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return;
        }

        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length == 0) {
            return;
        }

        // Use the current active project
        Project project = openProjects[0];

        // Schedule on EDT to avoid threading issues
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Find and focus the Logcat tool window
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            ToolWindow logcatWindow = toolWindowManager.getToolWindow("Logcat");

            if (logcatWindow != null) {
                // Activate the Logcat tool window
                logcatWindow.show(() -> {
                    // After window is shown, apply filter to find the specific log
                    applyLogcatFilter(project, timestamp);
                });
            }
        });
    }

    /**
     * Applies a filter to the Logcat window to find logs with a specific timestamp
     * @param project The current project
     * @param timestamp The timestamp to filter for
     */
    private static void applyLogcatFilter(Project project, String timestamp) {
        // Get the Logcat tool window
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow logcatWindow = toolWindowManager.getToolWindow("Logcat");

        if (logcatWindow == null) {
            System.out.println("Logcat window not found");
            return;
        }

        ContentManager contentManager = logcatWindow.getContentManager();
        Content[] contents = contentManager.getContents();

        System.out.println("Logcat Navigator: Connection to Logcat established");

        for (Content content : contents) {
            System.out.println("Content class: " + content.getComponent().getClass().getName());

            // Find the FilterTextField component by its class name
            java.awt.Component filterTextField = findComponentByClassName(
                    content.getComponent(),
                    "com.android.tools.idea.logcat.filters.FilterTextField"
            );

            if (filterTextField != null) {
                System.out.println("Found FilterTextField component");

                // Try to find the FilterEditorTextField within it
                java.awt.Component editorTextField = findComponentByClassName(
                        filterTextField,
                        "com.android.tools.idea.logcat.filters.FilterTextField$FilterEditorTextField"
                );

                if (editorTextField != null) {
                    System.out.println("Found FilterEditorTextField component");

                    // Try to access the editor component directly or via reflection
                    try {
                        // Option 1: Try direct method if available
                        java.lang.reflect.Method setTextMethod = editorTextField.getClass().getMethod("setText", String.class);
                        setTextMethod.invoke(editorTextField, timestamp);
                        System.out.println("Set filter text to: " + timestamp);

                        // Try to notify the component of changes
                        java.lang.reflect.Method applyMethod = editorTextField.getClass().getMethod("fireDocumentChanged");
                        if (applyMethod != null) {
                            applyMethod.invoke(editorTextField);
                        }
                        return;
                    } catch (Exception e1) {
                        System.out.println("Could not set text directly: " + e1.getMessage());

                        // Option 2: Try to find an editor component inside
                        java.awt.Component editorComponent = findComponentByClassName(
                                editorTextField,
                                "com.intellij.openapi.editor.impl.EditorComponentImpl"
                        );

                        if (editorComponent != null) {
                            System.out.println("Found EditorComponentImpl");
                            try {
                                // Try to access the editor through reflection
                                java.lang.reflect.Method getEditorMethod = editorComponent.getClass().getMethod("getEditor");
                                Object editor = getEditorMethod.invoke(editorComponent);

                                if (editor != null) {
                                    java.lang.reflect.Method getDocumentMethod = editor.getClass().getMethod("getDocument");
                                    Object document = getDocumentMethod.invoke(editor);

                                    if (document != null) {
                                        java.lang.reflect.Method setTextMethod = document.getClass().getMethod("setText", String.class);
                                        setTextMethod.invoke(document, timestamp);
                                        System.out.println("Set document text to: " + timestamp);
                                        return;
                                    }
                                }
                            } catch (Exception e2) {
                                System.out.println("Could not set editor text: " + e2.getMessage());
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Could not find filter text field in Logcat UI");
    }

    private static java.awt.Component findComponentByClassName(java.awt.Component component, String className) {
        if (component == null) return null;

        // Check if this component matches the class name
        if (component.getClass().getName().equals(className)) {
            return component;
        }

        // Search in child components
        if (component instanceof java.awt.Container) {
            java.awt.Container container = (java.awt.Container) component;
            for (java.awt.Component child : container.getComponents()) {
                java.awt.Component result = findComponentByClassName(child, className);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

}
