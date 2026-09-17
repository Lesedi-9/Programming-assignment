package hfpims;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageSuppliersPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public ManageSuppliersPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Contact Person", "Phone", "Email", "Address", "Medicines Supplied"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        UITheme.styleTable(table);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value,
                        isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.ROW_EVEN : UITheme.ROW_ODD);
                    c.setForeground(Color.BLACK);

                    if (col == 6) {
                        int count = Integer.parseInt(value.toString());
                        if (count == 0) {
                            c.setBackground(new Color(255, 220, 220));
                            c.setForeground(new Color(150, 20, 30));
                        } else if (count >= 5) {
                            c.setBackground(new Color(220, 245, 220));
                            c.setForeground(new Color(20, 100, 30));
                        }
                    }
                }
                setHorizontalAlignment(col == 6 || col == 0
                        ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addBtn = new JButton("Add Supplier");
        JButton updateBtn = new JButton("Update Supplier");
        JButton deleteBtn = new JButton("Delete Supplier");
        JButton refreshBtn = new JButton("Refresh");

        UITheme.styleButton(addBtn, UITheme.SUCCESS);
        UITheme.styleButton(updateBtn, UITheme.HEADER_BG);
        UITheme.styleButton(deleteBtn, UITheme.DANGER);
        UITheme.styleButton(refreshBtn, new Color(108, 117, 125));

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a supplier.");
                return;
            }
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            openForm(id);
        });
        deleteBtn.addActionListener(e -> deleteSupplier());
        refreshBtn.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT s.supplier_id, s.name, s.contact_person, s.phone, s.email, s.address, " +
                "COUNT(m.medicine_id) AS med_count " +
                "FROM suppliers s LEFT JOIN medicines m ON s.supplier_id = m.supplier_id " +
                "GROUP BY s.supplier_id, s.name, s.contact_person, s.phone, s.email, s.address " +
                "ORDER BY s.supplier_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getInt("med_count")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + ex.getMessage());
        }
    }

    private void openForm(Integer supplierId) {
        boolean isEdit = supplierId != null;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(isEdit ? "Update Supplier" : "Add Supplier");
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField contactField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextArea addressArea = new JTextArea(3, 15);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        if (isEdit) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT * FROM suppliers WHERE supplier_id=?")) {
                ps.setInt(1, supplierId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    contactField.setText(rs.getString("contact_person"));
                    phoneField.setText(rs.getString("phone"));
                    emailField.setText(rs.getString("email"));
                    addressArea.setText(rs.getString("address"));
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        }

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Contact Person:"), gbc);
        gbc.gridx = 1; dialog.add(contactField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; dialog.add(phoneField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; dialog.add(emailField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(addressArea), gbc); y++;

        JButton saveBtn = new JButton("Save");
        UITheme.styleButton(saveBtn, isEdit ? UITheme.HEADER_BG : UITheme.SUCCESS);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Supplier name is required.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                if (!isEdit) {
                    String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) " +
                            "VALUES (?,?,?,?,?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setString(2, contactField.getText().trim());
                    ps.setString(3, phoneField.getText().trim());
                    ps.setString(4, emailField.getText().trim());
                    ps.setString(5, addressArea.getText().trim());
                    ps.executeUpdate();
                } else {
                    String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? " +
                            "WHERE supplier_id=?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setString(2, contactField.getText().trim());
                    ps.setString(3, phoneField.getText().trim());
                    ps.setString(4, emailField.getText().trim());
                    ps.setString(5, addressArea.getText().trim());
                    ps.setInt(6, supplierId);
                    ps.executeUpdate();
                }
                dialog.dispose();
                loadData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    private void deleteSupplier() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a supplier.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String name = model.getValueAt(row, 1).toString();
        int medCount = Integer.parseInt(model.getValueAt(row, 6).toString());

        String warning = "Delete supplier \"" + name + "\"?";
        if (medCount > 0) {
            warning += "\n\nNote: " + medCount + " medicine(s) are linked to this supplier.\n" +
                    "Their supplier will be set to NULL.";
        }

        int confirm = JOptionPane.showConfirmDialog(this, warning,
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM suppliers WHERE supplier_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cannot delete: " + ex.getMessage());
        }
    }
}