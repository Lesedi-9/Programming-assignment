/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hfpims;

import javax.swing.*;
import java.awt.*;

public class CashierDashboard extends JFrame {
    public CashierDashboard(int userId, String fullName) {
        setTitle("HealthFirst PIMS - Cashier - " + fullName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Point of Sale", new POSPanel(userId, fullName));
        tabs.addTab("Stock Check", new StockCheckPanel());

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