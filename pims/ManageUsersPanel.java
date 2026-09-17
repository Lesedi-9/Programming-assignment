package pims;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageUsersPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private int currentUserId;

    public ManageUsersPanel(int currentUserId) {
        this.currentUserId = currentUserId;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{
                "ID", "Username", "Full Name", "Role"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add User");
        JButton deleteBtn = new JButton("Delete User");
        JButton refreshBtn = new JButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm());
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT user_id, username, full_name, role FROM users ORDER BY user_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void openForm() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle("Add User");
        dialog.setSize(370, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JTextField fullNameField = new JTextField(15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Cashier", "Admin"});

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; dialog.add(usernameField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; dialog.add(passwordField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; dialog.add(fullNameField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; dialog.add(roleCombo, gbc); y++;

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password, role, full_name) VALUES (?,?,?,?)")) {
                ps.setString(1, usernameField.getText().trim());
                ps.setString(2, new String(passwordField.getPassword()));
                ps.setString(3, (String) roleCombo.getSelectedItem());
                ps.setString(4, fullNameField.getText().trim());
                ps.executeUpdate();
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

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        if (id == currentUserId) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete user ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cannot delete: " + ex.getMessage());
        }
    }
}