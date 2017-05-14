package com.edd.jelly.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class ErrorReport extends JDialog {

    private JPanel contentPane;
    private JButton submitIssue;
    private JButton cancel;
    private JTextArea errorMessage;

    public ErrorReport(String issueUrl, Throwable throwable) {
        String trace = getStackTraceString(throwable);

        initComponents(trace);
        initListeners(issueUrl, throwable.getMessage(), trace);
    }

    /**
     * Get stack trace as string.
     */
    private String getStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Initialize UI components.
     */
    private void initComponents(String trace) {
        errorMessage.setText(trace);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(submitIssue);
    }

    /**
     * Initialize crash log listeners.
     */
    private void initListeners(String issueUrl, String message, String trace) {

        // Submit issue to GitHub.
        submitIssue.addActionListener(e -> {
            try {
                String body = URLEncoder.encode(
                        String.format("Crash stack trace: \n```\n%s```", trace),
                        StandardCharsets.UTF_8.name()
                );

                String title = URLEncoder.encode(
                        String.format("Game crash: %s", message),
                        StandardCharsets.UTF_8.name()
                );;

                Desktop.getDesktop().browse(URI.create(String.format("%s?title=%s&body=%s", issueUrl, title, body)));
            } catch (IOException ignored) {
            }
        });

        // Close the error log.
        cancel.addActionListener(e -> System.exit(0));

        // Cleanup.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}