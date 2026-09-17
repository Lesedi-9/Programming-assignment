// Add edit delete medicines, colours show expired / low stock
package hfpims;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageMedicinesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public ManageMedicinesPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Company", "Type", "Price", "Qty", "Reorder", "Expiry", "Supplier"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a medicine.");
                return;
            }
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            openForm(id);
        });
        deleteBtn.addActionListener(e -> deleteMedicine());
        refreshBtn.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, " +
                "m.quantity_in_stock, m.reorder_level, m.expiry_date, s.name AS supplier_name " +
                "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "ORDER BY m.medicine_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("medicine_id"),
                        rs.getString("name"),
                        rs.getString("company"),
                        rs.getString("medicine_type"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity_in_stock"),
                        rs.getInt("reorder_level"),
                        rs.getDate("expiry_date"),
                        rs.getString("supplier_name")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + ex.getMessage());
        }
    }

    private void openForm(Integer medicineId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(medicineId == null ? "Add Medicine" : "Update Medicine");
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField companyField = new JTextField(15);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Tablet", "Capsule", "Syrup", "Injection", "Cream", "Ointment", "Drops"
        });
        JTextField priceField = new JTextField(15);
        JTextField qtyField = new JTextField(15);
        JTextField reorderField = new JTextField(15);
        JTextField expiryField = new JTextField(15);
        JComboBox<String> supplierCombo = new JComboBox<>();

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT supplier_id, name FROM suppliers ORDER BY name")) {
            while (rs.next()) {
                supplierCombo.addItem(rs.getInt("supplier_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Error loading suppliers: " + ex.getMessage());
        }

        if (medicineId != null) {
            String sql = "SELECT * FROM medicines WHERE medicine_id=?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, medicineId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    companyField.setText(rs.getString("company"));
                    typeCombo.setSelectedItem(rs.getString("medicine_type"));
                    priceField.setText(rs.getBigDecimal("price").toString());
                    qtyField.setText(String.valueOf(rs.getInt("quantity_in_stock")));
                    reorderField.setText(String.valueOf(rs.getInt("reorder_level")));
                    expiryField.setText(rs.getDate("expiry_date").toString());

                    int supId = rs.getInt("supplier_id");
                    for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                        if (supplierCombo.getItemAt(i).startsWith(supId + " - ")) {
                            supplierCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error loading medicine: " + ex.getMessage());
            }
        }

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Company:"), gbc);
        gbc.gridx = 1; dialog.add(companyField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; dialog.add(typeCombo, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; dialog.add(priceField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; dialog.add(qtyField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Reorder Level:"), gbc);
        gbc.gridx = 1; dialog.add(reorderField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Expiry (yyyy-mm-dd):"), gbc);
        gbc.gridx = 1; dialog.add(expiryField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1; dialog.add(supplierCombo, gbc); y++;

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String company = companyField.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());
                int reorder = Integer.parseInt(reorderField.getText().trim());
                Date expiry = Date.valueOf(expiryField.getText().trim());
                int supplierId = Integer.parseInt(supplierCombo.getSelectedItem().toString().split(" - ")[0]);

                if (name.isEmpty() || company.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and company required.");
                    return;
                }

                if (medicineId == null) {
                    String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?,?,?,?,?,?,?,?)";
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setString(2, company);
                        ps.setString(3, type);
                        ps.setDouble(4, price);
                        ps.setInt(5, qty);
                        ps.setInt(6, reorder);
                        ps.setDate(7, expiry);
                        ps.setInt(8, supplierId);
                        ps.executeUpdate();
                    }
                } else {
                    String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setString(2, company);
                        ps.setString(3, type);
                        ps.setDouble(4, price);
                        ps.setInt(5, qty);
                        ps.setInt(6, reorder);
                        ps.setDate(7, expiry);
                        ps.setInt(8, supplierId);
                        ps.setInt(9, medicineId);
                        ps.executeUpdate();
                    }
                }

                dialog.dispose();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage());
            }
        });

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    private void deleteMedicine() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a medicine.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete medicine ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM medicines WHERE medicine_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cannot delete: " + ex.getMessage());
        }
    }
}
