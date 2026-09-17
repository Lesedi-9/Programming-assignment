package hfpims;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class StockCheckPanel extends JPanel {
    private JTextField searchField;
    private JTextArea resultArea;

    public StockCheckPanel() {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Medicine Name:"));
        searchField = new JTextField(25);
        top.add(searchField);
        JButton searchBtn = new JButton("Check");
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> checkStock());
        searchField.addActionListener(e -> checkStock());
    }

    private void checkStock() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) return;

        String sql = "SELECT m.*, s.name AS supplier_name " +
                "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id=s.supplier_id " +
                "WHERE m.name LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + term + "%");
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder();
            boolean found = false;

            while (rs.next()) {
                found = true;
                sb.append("ID: ").append(rs.getInt("medicine_id")).append("\n");
                sb.append("Name: ").append(rs.getString("name")).append("\n");
                sb.append("Company: ").append(rs.getString("company")).append("\n");
                sb.append("Type: ").append(rs.getString("medicine_type")).append("\n");
                sb.append("Price: ").append(rs.getBigDecimal("price")).append("\n");
                sb.append("Stock: ").append(rs.getInt("quantity_in_stock")).append("\n");
                sb.append("Expiry: ").append(rs.getDate("expiry_date")).append("\n");
                sb.append("Supplier: ").append(rs.getString("supplier_name")).append("\n");
                sb.append("----------------------------------------\n");
            }

            if (!found) sb.append("No medicine found.");
            resultArea.setText(sb.toString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
