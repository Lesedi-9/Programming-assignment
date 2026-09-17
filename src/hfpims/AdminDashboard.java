package hfpims;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard(int userId, String fullName) {
        setTitle("HealthFirst PIMS - Admin Dashboard - " + fullName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Medicines", new ManageMedicinesPanel());
        tabs.addTab("Manage Suppliers", new ManageSuppliersPanel());
        tabs.addTab("Manage Users", new ManageUsersPanel(userId));
        tabs.addTab("Reports", new ReportsPanel());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(new JLabel("Logged in as: " + fullName));
        top.add(logoutBtn);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}