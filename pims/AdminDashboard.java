package pims;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard(int userId, String fullName) {
        setTitle("HealthFirst PIMS - Admin Dashboard - " + fullName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabs.addTab("Summary", new SummaryPanel());
        tabs.addTab("Manage Medicines", new ManageMedicinesPanel());
        tabs.addTab("Manage Suppliers", new ManageSuppliersPanel());
        tabs.addTab("Manage Users", new ManageUsersPanel(userId));
        tabs.addTab("Reports", new ReportsPanel());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.HEADER_BG);
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel welcome = new JLabel("Welcome, " + fullName + "  |  Administrator");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 15));
        top.add(welcome, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        UITheme.styleButton(logoutBtn, UITheme.DANGER);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        top.add(logoutBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}

class SummaryPanel extends JPanel {
    public SummaryPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 252));

        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        grid.setOpaque(false);

        grid.add(createCard("Total Medicines", String.valueOf(getCount(
                "SELECT COUNT(*) FROM medicines")), new Color(70, 130, 180)));
        grid.add(createCard("Total Suppliers", String.valueOf(getCount(
                "SELECT COUNT(*) FROM suppliers")), new Color(60, 179, 113)));
        grid.add(createCard("Total Users", String.valueOf(getCount(
                "SELECT COUNT(*) FROM users")), new Color(147, 112, 219)));
        grid.add(createCard("Low Stock Items", String.valueOf(getCount(
                "SELECT COUNT(*) FROM medicines WHERE quantity_in_stock <= reorder_level")),
                new Color(255, 140, 0)));
        grid.add(createCard("Expiring Soon (30d)", String.valueOf(getCount(
                "SELECT COUNT(*) FROM medicines WHERE expiry_date BETWEEN CURDATE() " +
                        "AND DATE_ADD(CURDATE(), INTERVAL 1 MONTH)")), new Color(220, 20, 60)));
        grid.add(createCard("Today's Sales (R)", String.format("%.2f", getSales()),
                new Color(34, 139, 34)));

        add(grid, BorderLayout.CENTER);

        JLabel footer = new JLabel(
                "HealthFirst Pharmacy Inventory Management System  •  v1.0",
                SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.setForeground(Color.GRAY);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel topStrip = new JPanel();
        topStrip.setBackground(accent);
        topStrip.setPreferredSize(new Dimension(0, 6));
        card.add(topStrip, BorderLayout.NORTH);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLbl.setForeground(new Color(90, 90, 90));
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLbl.setForeground(accent);
        valueLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel center = new JPanel(new GridLayout(2, 1));
        center.setOpaque(false);
        center.add(titleLbl);
        center.add(valueLbl);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private int getCount(String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            return 0;
        }
        return 0;
    }

    private double getSales() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException ex) {
            return 0;
        }
        return 0;
    }
}