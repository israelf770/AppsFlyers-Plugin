package com.example.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class LogColorizePlugin extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Messages.showMessageDialog("Hello world!", "Test Title", Messages.getInformationIcon()  );
    }
}
