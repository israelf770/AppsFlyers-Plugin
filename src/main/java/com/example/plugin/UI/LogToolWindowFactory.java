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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogToolWindowFactory implements ToolWindowFactory {

    // Reference to the log panel for updates
    private static JPanel logPanel;
    public static JComboBox<String> deviceCombo;



    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // נבנה פאנל ראשי שמכיל את כל התוכן
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Gray._30);

        // צור Panel עליון שישמש לבחירת מכשיר
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Gray._30);
        topPanel.setBorder(JBUI.Borders.empty(8, 15)); // same margin as the log panels


        // צור ComboBox (או כל רכיב אחר שתרצה)
        deviceCombo = new ComboBox<>();
        deviceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, deviceCombo.getPreferredSize().height));
        deviceCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

//        int selectedIndex = deviceCombo.getSelectedIndex();
//        deviceCombo.setSelectedIndex(selectedIndex); // ברירת מחדל - הראשון

        // בעת בחירה ב-ComboBox, נעדכן את המכשיר הנבחר
        deviceCombo.addActionListener(e -> {
            String selectedDevice = (String) deviceCombo.getSelectedItem();
            selectedDevice =  ExtractParentheses(selectedDevice);

            if (selectedDevice != null && !"No devices".equals(selectedDevice)
                    && !"Error retrieving devices".equals(selectedDevice)) {
                showLogs.getAllLogs().clear();
                LogcatProcessHandler.setSelectedDeviceId(selectedDevice);
                LogcatProcessHandler.startLogcat();
            }

        });

        // הוסף את topPanel לחלק העליון של mainPanel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        topPanel.add(deviceCombo, BorderLayout.CENTER);

        // Actions for the tab header
        AnAction showAllAction = new ShowAllAction();
        AnAction showConversionAction = new ShowConversionAction();
        AnAction showEventAction = new ShowEventAction();
        AnAction showLaunchAction = new ShowLaunchAction();
        AnAction RunAction = new RunAction();

        toolWindow.setTitleActions(Arrays.asList(
                RunAction,
                new Separator(),
                showAllAction,
                new Separator(),
                showConversionAction,
                new Separator(),
                showEventAction,
                new Separator(),
                showLaunchAction
        ));

        // Log panel
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(Gray._30);

        JScrollPane scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        loadDevices();
    }

    public static void loadDevices() {
        try {
            // קבל רשימת מכשירים
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

    //Updates the log panel with logs that match the current filter

    public static void updateLogContentPanel() {
        if (logPanel != null) {
            logPanel.removeAll();

            String currentFilter = showLogs.getCurrentFilter();

            // Iterate through all logs and add only those that match the current filter
            for (LogEntry entry : showLogs.getAllLogs()) {
                if (showLogs.logMatchesFilter(entry.getShortLog(), currentFilter)) {
                    JPanel entryPanel = enterLogPanelUI.createLogEntryPanel(entry.getShortLog(), entry.getFullLog());
                    logPanel.add(entryPanel);
                    logPanel.add(Box.createVerticalStrut(10)); // Space between entries
                }
            }

            logPanel.revalidate();
            logPanel.repaint();
        }
    }


    public static String ExtractParentheses(String currentDevice) {
        // הביטוי הרגולרי מחפש כל מה שבין סוגריים
        if (currentDevice == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(currentDevice);
        String insideParentheses = null;
        if (matcher.find()) {
            // הקבוצה הראשונה מכילה את הטקסט שבתוך הסוגריים
            insideParentheses = matcher.group(1);
            System.out.println("Inside parentheses: " + insideParentheses);
        }
        return insideParentheses;
    }

}
