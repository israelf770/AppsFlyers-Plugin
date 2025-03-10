package com.example.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DeviceSelector {

    public static void showDeviceSelector(List<String> devices, String adbPath) {
        try {
            JDialog dialog = new JDialog((Frame) null, "ADB Device Manager", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(new JBColor(new Color(246, 241, 241), Gray._50));

            JLabel headerLabel = new JLabel("Select Android Device");
            headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
            headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            mainPanel.add(headerLabel, BorderLayout.NORTH);

            JPanel deviceListPanel = new JPanel();
            deviceListPanel.setLayout(new BoxLayout(deviceListPanel, BoxLayout.Y_AXIS));
            deviceListPanel.setBackground(new JBColor(Gray._255, Gray._43));
            deviceListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            ButtonGroup group = new ButtonGroup();
            Map<JRadioButton, String> buttonToDevice = new HashMap<>();

            JButton refreshButton = new JButton("Refresh");
            JButton okButton = new JButton("Connect");
            JButton cancelButton = new JButton("Cancel");

            okButton.setEnabled(false);

            for (String deviceId : devices) {
                String deviceInfo = getDeviceInfo(adbPath, deviceId);
                JPanel devicePanel = createDevicePanel(deviceId, deviceInfo, group, buttonToDevice, okButton);
                deviceListPanel.add(devicePanel);
                deviceListPanel.add(Box.createVerticalStrut(5));
            }

            JScrollPane scrollPane = new JBScrollPane(deviceListPanel);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonsPanel.setBackground(mainPanel.getBackground());

            okButton.addActionListener(e -> {
                for (Map.Entry<JRadioButton, String> entry : buttonToDevice.entrySet()) {
                    if (entry.getKey().isSelected()) {
                        LogcatProcessHandler.setSelectedDeviceId(entry.getValue());
                        dialog.dispose();
                        LogcatProcessHandler.startLogcat();
                        return;
                    }
                }
            });

            refreshButton.addActionListener(e -> {
                dialog.dispose();
                LogcatProcessHandler.startLogcat();
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            buttonsPanel.add(refreshButton);
            buttonsPanel.add(Box.createHorizontalStrut(5));
            buttonsPanel.add(okButton);
            buttonsPanel.add(Box.createHorizontalStrut(5));
            buttonsPanel.add(cancelButton);

            mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
            dialog.add(mainPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (Exception e) {
            Logger.getInstance(DeviceSelector.class).error("Error showing device selector", e);
        }
    }

    private static JPanel createDevicePanel(String deviceId, String deviceInfo, ButtonGroup group,
                                            Map<JRadioButton, String> buttonToDevice, JButton okButton) {
        JPanel devicePanel = new JPanel(new BorderLayout(10, 0));
        devicePanel.setBackground(new JBColor(Gray._255, Gray._43));
        devicePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JRadioButton radio = new JRadioButton("Select");
        radio.setBackground(devicePanel.getBackground());
        radio.setFont(new Font("Arial", Font.PLAIN, 12));
        group.add(radio);
        buttonToDevice.put(radio, deviceId);

        radio.addActionListener(e -> okButton.setEnabled(radio.isSelected()));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(devicePanel.getBackground());

        JLabel nameLabel = new JLabel(deviceInfo);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel idLabel = new JLabel("ID: " + deviceId);
        idLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        idLabel.setForeground(JBColor.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(idLabel);

        devicePanel.add(radio, BorderLayout.WEST);
        devicePanel.add(infoPanel, BorderLayout.CENTER);

        devicePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio.setSelected(true);
                okButton.setEnabled(true);
            }
        });

        return devicePanel;
    }

    private static String getDeviceInfo(String adbPath, String deviceId) {
        try {
            ProcessBuilder builder = new ProcessBuilder(adbPath, "-s", deviceId, "shell", "getprop", "ro.product.model");
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String model = reader.readLine();
                return deviceId + " (" + (model != null ? model : "Unknown") + ")";
            }
        } catch (IOException e) {
            return deviceId + " (Unknown)";
        }
    }
}