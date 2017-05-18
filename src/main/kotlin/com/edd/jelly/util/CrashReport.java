package com.edd.jelly.util;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CrashReport extends JDialog {

    private final JButton submitIssueButton = new JButton("Submit issue");
    private final JButton closeButton = new JButton("Close");

    private CrashReport(String url, Throwable throwable) {
        String error = toString(throwable);
        initComponents(error);
        initListeners(url, throwable.getMessage(), error);
    }

    /**
     * Show crash report window.
     */
    public static void showReport(String url, Throwable throwable) {
        new CrashReport(url, throwable).setVisible(true);
    }

    /**
     * Convert stack trace to string.
     */
    private String toString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Initialize and pack error report components.
     */
    private void initComponents(String error) {
        setTitle("Crash report");

        // Min window size.
        Dimension minSize = new Dimension(500, 400);
        setPreferredSize(minSize);
        setMinimumSize(minSize);

        // Error info group.
        JPanel errorPane = new JPanel();
        errorPane.setLayout(new BoxLayout(errorPane, BoxLayout.PAGE_AXIS));
        errorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea errorText = new JTextArea();
        JLabel errorLabel = new JLabel("The game has crashed, here is the error that caused it:");

        errorLabel.setLabelFor(errorText);
        errorPane.add(errorLabel);
        errorPane.add(Box.createRigidArea(new Dimension(0, 5)));

        errorText.setText(error);
        errorText.setEditable(false);

        JScrollPane scroll = new JScrollPane(errorText);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorPane.add(scroll);

        // Panel button controls.
        JPanel controlPane = new JPanel();
        controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.LINE_AXIS));
        controlPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        controlPane.add(Box.createHorizontalGlue());
        submitIssueButton.setToolTipText("Submit a new issue to GitHub");
        controlPane.add(submitIssueButton);

        controlPane.add(Box.createRigidArea(new Dimension(10, 0)));
        closeButton.setToolTipText("Close the crash log");
        controlPane.add(closeButton);

        // Add everything up to main content pane.
        add(errorPane, BorderLayout.CENTER);
        add(controlPane, BorderLayout.PAGE_END);

        // Finalize.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(true);
        pack();

        setLocationRelativeTo(null);
    }

    /**
     * Initialize button listeners.
     */
    private void initListeners(String url, String message, String error) {

        // Submit issue to GitHub.
        submitIssueButton.addActionListener(a -> {
            try {
                String body = URLEncoder.encode(
                        String.format("Crash stack trace: \n```\n%s```", error),
                        StandardCharsets.UTF_8.name()
                );

                String title = URLEncoder.encode(
                        String.format("Game crash: %s", message),
                        StandardCharsets.UTF_8.name()
                );

                Desktop.getDesktop()
                        .browse(URI.create(String.format("%s?title=%s&body=%s", url, title, body)));
            } catch (IOException ignored) {
            }
        });

        // Close the error report.
        closeButton.addActionListener(a -> System.exit(0));
    }
}