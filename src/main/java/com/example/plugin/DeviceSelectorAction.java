package com.example.plugin;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class DeviceSelectorAction extends AnAction {
    private String selectedDevice = "Select Device";

    public DeviceSelectorAction() {
        // פעולה עם טקסט ברירת מחדל
        super("Select Device", "Select device from list", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        try {
            List<String> devices = GetInfo.getConnectedDevices(GetInfo.getAdbPath());
            if (devices.isEmpty()) {
                // לא חובה להראות popup - אפשר פשוט להמשיך להציג "No device connected"
                return;
            }
            showDevicePopup(e, devices);
        } catch (IOException ex) {
            showErrorPopup();
        }
    }


    private void showDevicePopup(AnActionEvent e, List<String> devices) {
        JBList<String> deviceList = new JBList<>(devices);
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceList.setBorder(JBUI.Borders.empty(5));
        deviceList.setBackground(JBColor.background());

        deviceList.addListSelectionListener(event -> {
            String selected = deviceList.getSelectedValue();
            if (selected != null) {
                selectedDevice = selected;
                // עדכן את LogcatProcessHandler
                LogcatProcessHandler.setSelectedDeviceId(selected);
                LogcatProcessHandler.startLogcat();
            }
        });

        // בניית Popup
        PopupChooserBuilder<String> builder = JBPopupFactory.getInstance()
                .createListPopupBuilder(deviceList)
                .setTitle("Select Device")
                .setResizable(true)
                .setMovable(true)
                .setRequestFocus(true);

        // מציגים את ה-popup בסמוך לכפתור
        JBPopup popup = builder.createPopup();
        Component comp = e.getInputEvent().getComponent();
        int xOffset = 0;
        int yOffset = comp.getHeight();
        popup.show(new RelativePoint(comp, new Point(xOffset, yOffset)));
    }


    private void showErrorPopup() {
        JBPopupFactory.getInstance()
                .createMessage("Error retrieving devices")
                .showInFocusCenter();
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(selectedDevice);
        e.getPresentation().setIcon(null); // ליתר ביטחון
    }


    // כשמשתמש בוחר מכשיר ב-Popup:
    private void setSelectedDevice(String device) {
        this.selectedDevice = device;
    }
}
