package com.example.plugin.UI;

import com.example.plugin.GetInfo;
import com.example.plugin.LogcatProcessHandler;
import com.example.plugin.actions.*;
import com.example.plugin.showLogs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.plugin.UI.enterLogPanelUI.adjustTextAreaHeight;

// LogToolWindowFactory.java
public class LogToolWindowFactory implements ToolWindowFactory {

    private static JPanel logPanel;
    public static JComboBox<String> deviceCombo;
    private static Project currentProject;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        currentProject = project;

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Gray._30);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Gray._30);
        topPanel.setBorder(JBUI.Borders.empty(8, 15));

        deviceCombo = new ComboBox<>();
        deviceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, deviceCombo.getPreferredSize().height));
        deviceCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        deviceCombo.addActionListener(e -> {
            String selectedDevice = (String) deviceCombo.getSelectedItem();
            selectedDevice = ExtractParentheses(selectedDevice);

            if (selectedDevice != null && !"No devices".equals(selectedDevice)
                    && !"Error retrieving devices".equals(selectedDevice)) {
                showLogs.getAllLogs().clear();
                LogcatProcessHandler.setSelectedDeviceId(selectedDevice);
                LogcatProcessHandler.startLogcat();
            }
        });

        mainPanel.add(topPanel, BorderLayout.NORTH);
        topPanel.add(deviceCombo, BorderLayout.CENTER);

        AnAction showAllAction = new ShowAllAction();
        AnAction showConversionAction = new ShowConversionAction();
        AnAction showEventAction = new ShowEventAction();
        AnAction showLaunchAction = new ShowLaunchAction();
        AnAction RunAction = new RunAction();
        AnAction ShowDeepLinkAction = new ShowDeepLinkAction();

        toolWindow.setTitleActions(Arrays.asList(
                RunAction,
                new Separator(),
                showAllAction,
                new Separator(),
                showConversionAction,
                new Separator(),
                showEventAction,
                new Separator(),
                showLaunchAction,
                new Separator(),
                ShowDeepLinkAction
        ));

        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(Gray._30);

        JScrollPane scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                for (Component comp : logPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        for (Component innerComp : ((JPanel) comp).getComponents()) {
                            if (innerComp instanceof JTextArea) {
                                adjustTextAreaHeight((JTextArea) innerComp, (JPanel) comp);
                            }
                        }
                    }
                }
            }
        });

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        loadDevices();
    }

    public static void loadDevices() {
        try {
            List<String> devices = GetInfo.getConnectedDevices(GetInfo.getAdbPath());
            deviceCombo.removeAllItems();
            if (devices.isEmpty()) {
                if (containsDevice(deviceCombo, "No devices")) {
                    deviceCombo.addItem("No devices");
                }
            } else {
                for (String device : devices) {
                    if (containsDevice(deviceCombo, device)) {
                        String displayName = GetInfo.getDeviceName(GetInfo.getAdbPath(), device);
                        deviceCombo.addItem(displayName + " (" + device + ")");
                    }
                }
            }
        } catch (IOException e) {
            deviceCombo.removeAllItems();
            deviceCombo.addItem("Error retrieving devices");
        }
    }

    private static boolean containsDevice(JComboBox<String> combo, String device) {
        ComboBoxModel<String> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (device.equals(model.getElementAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void updateLogContentPanel() {
        if (logPanel != null) {
            logPanel.removeAll();

            String currentFilter = showLogs.getCurrentFilter();

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            int entryCount = 0;

            for (LogEntry entry : showLogs.getAllLogs()) {
                if (showLogs.logMatchesFilter(entry.getShortLog(), currentFilter)) {
                    JPanel entryPanel = enterLogPanelUI.createLogEntryPanel(entry.getShortLog(), entry.getFullLog());
                    entryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    contentPanel.add(entryPanel);
                    contentPanel.add(Box.createVerticalStrut(10));
                    entryCount++;
                }
            }

            if (entryCount == 0) {
                JLabel noLogsLabel = new JLabel("No logs found matching the current filter");
                noLogsLabel.setForeground(JBColor.GRAY);
                noLogsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(Box.createVerticalGlue());
                contentPanel.add(noLogsLabel);
                contentPanel.add(Box.createVerticalGlue());
            }

            contentPanel.add(Box.createVerticalStrut(20));
            logPanel.setLayout(new BorderLayout());
            logPanel.add(contentPanel, BorderLayout.NORTH);
            logPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);

            logPanel.revalidate();
            logPanel.repaint();
        }
    }

    public static String ExtractParentheses(String currentDevice) {
        if (currentDevice == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(currentDevice);
        String insideParentheses = null;
        if (matcher.find()) {
            insideParentheses = matcher.group(1);
        }
        return insideParentheses;
    }
}
