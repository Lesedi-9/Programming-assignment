// Point of Sale - cart, checkout and stock update
package hfpims;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class POSPanel extends JPanel {
    private JTextField searchField;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private int userId;
    private String cashierName;

    public POSPanel(int userId, String cashierName) {
        this.userId = userId;
        this.cashierName = cashierName;
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search Medicine:"));
        searchField = new JTextField(25);
        top.add(searchField);
        JButton searchBtn = new JButton("Search & Add");
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        cartModel = new DefaultTableModel(new String[]{
                "Medicine ID", "Name", "Price", "Qty", "Subtotal"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        cartTable = new JTable(cartModel);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottom.add(totalLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel();
        JButton clearBtn = new JButton("Clear Cart");
        JButton checkoutBtn = new JButton("Checkout");
        buttons.add(clearBtn);
        buttons.add(checkoutBtn);
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> searchAndAdd());
        searchField.addActionListener(e -> searchAndAdd());
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkout());
    }

    private void searchAndAdd() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) return;

        String sql = "SELECT medicine_id, name, price, quantity_in_stock, expiry_date " +
                "FROM medicines WHERE name LIKE ? AND quantity_in_stock > 0 ORDER BY name LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + term + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int medId = rs.getInt("medicine_id");
                String name = rs.getString("name");
                BigDecimal price = rs.getBigDecimal("price");
                int stock = rs.getInt("quantity_in_stock");
                Date expiry = rs.getDate("expiry_date");

                if (expiry.before(new Date(System.currentTimeMillis()))) {
                    JOptionPane.showMessageDialog(this, "Medicine is expired!");
                    return;
                }

                String qtyStr = JOptionPane.showInputDialog(this,
                        "Available: " + stock + "\nEnter quantity:", "1");
                if (qtyStr == null) return;

                int qty;
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid quantity.");
                    return;
                }

                if (qty <= 0 || qty > stock) {
                    JOptionPane.showMessageDialog(this, "Quantity must be between 1 and " + stock);
                    return;
                }

                addToCart(medId, name, price, qty);
                searchField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Medicine not found or out of stock.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void addToCart(int medId, String name, BigDecimal price, int qty) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int existingId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
            if (existingId == medId) {
                int existingQty = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                int newQty = existingQty + qty;
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(newQty));
                cartModel.setValueAt(newQty, i, 3);
                cartModel.setValueAt(subtotal, i, 4);
                updateTotal();
                return;
            }
        }

        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
        cartModel.addRow(new Object[]{medId, name, price, qty, subtotal});
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total = total.add(new BigDecimal(cartModel.getValueAt(i, 4).toString()));
        }
        totalLabel.setText("Total: " + total.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        updateTotal();
    }

    private void checkout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Complete sale?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total = total.add(new BigDecimal(cartModel.getValueAt(i, 4).toString()));
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String saleSql = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
                PreparedStatement salePs = con.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
                salePs.setBigDecimal(1, total);
                salePs.setInt(2, userId);
                salePs.executeUpdate();

                ResultSet keys = salePs.getGeneratedKeys();
                int saleId = 0;
                if (keys.next()) saleId = keys.getInt(1);

                String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?,?,?,?)";
                String stockSql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id=? AND quantity_in_stock >= ?";

                PreparedStatement itemPs = con.prepareStatement(itemSql);
                PreparedStatement stockPs = con.prepareStatement(stockSql);

                List<String> billLines = new ArrayList<>();

                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int medId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                    String name = cartModel.getValueAt(i, 1).toString();
                    BigDecimal price = new BigDecimal(cartModel.getValueAt(i, 2).toString());
                    int qty = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                    BigDecimal subtotal = new BigDecimal(cartModel.getValueAt(i, 4).toString());

                    itemPs.setInt(1, saleId);
                    itemPs.setInt(2, medId);
                    itemPs.setInt(3, qty);
                    itemPs.setBigDecimal(4, price);
                    itemPs.executeUpdate();

                    stockPs.setInt(1, qty);
                    stockPs.setInt(2, medId);
                    stockPs.setInt(3, qty);
                    int updated = stockPs.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Insufficient stock for " + name);
                    }

                    billLines.add(name + " x" + qty + " @ " + price + " = " + subtotal);
                }

                con.commit();
                clearCart();
                new BillFrame(saleId, billLines, total, cashierName).setVisible(true);
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage());
        }
    }
}
