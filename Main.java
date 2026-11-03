package com.itwings.hospital;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class Main extends JFrame {
    private final Color navy = new Color(18, 40, 70);
    private final Color teal = new Color(0, 145, 150);

    public Main() {
        setTitle("ITWINGS | Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 248, 252));
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(navy);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("ITWINGS  |  HOSPITAL MANAGEMENT SYSTEM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Patient • Doctor • Appointment • Billing");
        subtitle.setForeground(new Color(195, 220, 235));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JPanel headings = new JPanel(new GridLayout(2, 1, 0, 5));
        headings.setOpaque(false); headings.add(title); headings.add(subtitle);
        header.add(headings, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Patients", patientsPanel());
        tabs.addTab("Doctors", doctorsPanel());
        tabs.addTab("Appointments", appointmentsPanel());
        tabs.addTab("Billing", billingPanel());
        add(tabs, BorderLayout.CENTER);

        JLabel footer = new JLabel("  Demo project for learning purposes — verify data before real-world use.");
        footer.setBorder(new EmptyBorder(8, 12, 8, 12));
        footer.setForeground(new Color(90, 105, 120));
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel basePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setBackground(new Color(245, 248, 252));
        return p;
    }

    private JButton button(String label) {
        JButton b = new JButton(label);
        b.setBackground(teal); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }

    private DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(25);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        return t;
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Database / Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel patientsPanel() {
        JPanel p = basePanel();
        DefaultTableModel m = model("ID", "Name", "Age", "Gender", "Phone", "Disease");
        JTable t = table(m);
        JTextField name = new JTextField(12), age = new JTextField(4), phone = new JTextField(10), disease = new JTextField(12);
        JComboBox<String> gender = new JComboBox<>(new String[]{"Female", "Male", "Other"});
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.setBackground(Color.WHITE);
        form.add(new JLabel("Name")); form.add(name); form.add(new JLabel("Age")); form.add(age);
        form.add(new JLabel("Gender")); form.add(gender); form.add(new JLabel("Phone")); form.add(phone);
        form.add(new JLabel("Disease")); form.add(disease);
        JButton add = button("Add Patient"), delete = button("Delete Selected"), refresh = button("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        actions.add(add); actions.add(delete); actions.add(refresh);
        p.add(form, BorderLayout.NORTH); p.add(new JScrollPane(t), BorderLayout.CENTER); p.add(actions, BorderLayout.SOUTH);

        Runnable load = () -> {
            m.setRowCount(0);
            try (Connection c = DB.getConnection(); Statement s = c.createStatement();
                 ResultSet r = s.executeQuery("SELECT id,name,age,gender,phone,disease FROM patients ORDER BY id DESC")) {
                while (r.next()) m.addRow(new Object[]{r.getInt(1), r.getString(2), r.getInt(3), r.getString(4), r.getString(5), r.getString(6)});
            } catch (Exception ex) { error(ex); }
        };
        add.addActionListener(e -> {
            try {
                if (name.getText().isBlank() || age.getText().isBlank()) throw new IllegalArgumentException("Name and age are required.");
                int a = Integer.parseInt(age.getText().trim());
                if (a < 0 || a > 125) throw new IllegalArgumentException("Enter a valid age.");
                try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO patients(name,age,gender,phone,disease) VALUES(?,?,?,?,?)")) {
                    ps.setString(1, name.getText().trim()); ps.setInt(2, a); ps.setString(3, (String) gender.getSelectedItem());
                    ps.setString(4, phone.getText().trim()); ps.setString(5, disease.getText().trim()); ps.executeUpdate();
                }
                name.setText(""); age.setText(""); phone.setText(""); disease.setText(""); load.run();
            } catch (Exception ex) { error(ex); }
        });
        delete.addActionListener(e -> {
            int row = t.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
            int id = (int)m.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete selected patient and related records?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM patients WHERE id=?")) { ps.setInt(1, id); ps.executeUpdate(); load.run(); } catch (Exception ex) { error(ex); }
        });
        refresh.addActionListener(e -> load.run()); load.run(); return p;
    }

    private JPanel doctorsPanel() {
        JPanel p = basePanel();
        DefaultTableModel m = model("ID", "Doctor Name", "Specialization", "Phone");
        JTable t = table(m);
        JTextField name = new JTextField(14), spec = new JTextField(14), phone = new JTextField(12);
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT)); form.setBackground(Color.WHITE);
        form.add(new JLabel("Doctor")); form.add(name); form.add(new JLabel("Specialization")); form.add(spec); form.add(new JLabel("Phone")); form.add(phone);
        JButton add = button("Add Doctor"), delete = button("Delete Selected"), refresh = button("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(add); actions.add(delete); actions.add(refresh);
        p.add(form, BorderLayout.NORTH); p.add(new JScrollPane(t), BorderLayout.CENTER); p.add(actions, BorderLayout.SOUTH);
        Runnable load = () -> {
            m.setRowCount(0);
            try (Connection c = DB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT id,name,specialization,phone FROM doctors ORDER BY id DESC")) {
                while (r.next()) m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getString(4)});
            } catch (Exception ex) { error(ex); }
        };
        add.addActionListener(e -> {
            try {
                if (name.getText().isBlank() || spec.getText().isBlank()) throw new IllegalArgumentException("Doctor name and specialization are required.");
                try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO doctors(name,specialization,phone) VALUES(?,?,?)")) {
                    ps.setString(1,name.getText().trim()); ps.setString(2,spec.getText().trim()); ps.setString(3,phone.getText().trim()); ps.executeUpdate();
                }
                name.setText(""); spec.setText(""); phone.setText(""); load.run();
            } catch (Exception ex) { error(ex); }
        });
        delete.addActionListener(e -> {
            int row=t.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a doctor first.");return;}
            int id=(int)m.getValueAt(row,0);
            if(JOptionPane.showConfirmDialog(this,"Delete selected doctor?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM doctors WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();load.run();}catch(Exception ex){error(ex);}
        });
        refresh.addActionListener(e -> load.run()); load.run(); return p;
    }

    private JPanel appointmentsPanel() {
        JPanel p = basePanel();
        DefaultTableModel m = model("ID", "Patient", "Doctor", "Date", "Status", "Notes");
        JTable t = table(m);
        JComboBox<Item> patients = new JComboBox<>(), doctors = new JComboBox<>();
        JTextField date = new JTextField(LocalDate.now().toString(), 10), notes = new JTextField(14);
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT)); form.setBackground(Color.WHITE);
        form.add(new JLabel("Patient")); form.add(patients); form.add(new JLabel("Doctor")); form.add(doctors);
        form.add(new JLabel("Date (YYYY-MM-DD)")); form.add(date); form.add(new JLabel("Notes")); form.add(notes);
        JButton add = button("Book Appointment"), refresh = button("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(add); actions.add(refresh);
        p.add(form, BorderLayout.NORTH); p.add(new JScrollPane(t), BorderLayout.CENTER); p.add(actions, BorderLayout.SOUTH);
        Runnable loadChoices = () -> {
            patients.removeAllItems(); doctors.removeAllItems();
            try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,name FROM patients ORDER BY name")){
                while(r.next())patients.addItem(new Item(r.getInt(1),r.getString(2)));
            }catch(Exception ex){error(ex);}
            try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,name FROM doctors ORDER BY name")){
                while(r.next())doctors.addItem(new Item(r.getInt(1),r.getString(2)));
            }catch(Exception ex){error(ex);}
        };
        Runnable load = () -> {
            m.setRowCount(0);
            String sql="SELECT a.id,p.name,d.name,a.appointment_date,a.status,a.notes FROM appointments a JOIN patients p ON p.id=a.patient_id JOIN doctors d ON d.id=a.doctor_id ORDER BY a.appointment_date DESC";
            try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){
                while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getDate(4),r.getString(5),r.getString(6)});
            }catch(Exception ex){error(ex);}
        };
        add.addActionListener(e -> {
            try {
                Item patient=(Item)patients.getSelectedItem(), doctor=(Item)doctors.getSelectedItem();
                if(patient==null||doctor==null)throw new IllegalArgumentException("Add at least one patient and one doctor first.");
                Date d=Date.valueOf(date.getText().trim());
                try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO appointments(patient_id,doctor_id,appointment_date,notes) VALUES(?,?,?,?)")){
                    ps.setInt(1,patient.id);ps.setInt(2,doctor.id);ps.setDate(3,d);ps.setString(4,notes.getText().trim());ps.executeUpdate();
                }
                notes.setText("");load.run();
            }catch(Exception ex){error(ex);}
        });
        refresh.addActionListener(e -> {loadChoices.run();load.run();}); loadChoices.run(); load.run(); return p;
    }

    private JPanel billingPanel() {
        JPanel p = basePanel();
        DefaultTableModel m = model("Bill ID", "Patient", "Description", "Amount", "Bill Date");
        JTable t = table(m);
        JComboBox<Item> patients = new JComboBox<>();
        JTextField description = new JTextField(16), amount = new JTextField(8), date = new JTextField(LocalDate.now().toString(), 10);
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT)); form.setBackground(Color.WHITE);
        form.add(new JLabel("Patient")); form.add(patients); form.add(new JLabel("Description")); form.add(description);
        form.add(new JLabel("Amount ₹")); form.add(amount); form.add(new JLabel("Date")); form.add(date);
        JButton add = button("Create Bill"), refresh = button("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(add); actions.add(refresh);
        p.add(form, BorderLayout.NORTH); p.add(new JScrollPane(t), BorderLayout.CENTER); p.add(actions, BorderLayout.SOUTH);
        Runnable loadChoices = () -> {
            patients.removeAllItems();
            try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,name FROM patients ORDER BY name")){
                while(r.next())patients.addItem(new Item(r.getInt(1),r.getString(2)));
            }catch(Exception ex){error(ex);}
        };
        Runnable load = () -> {
            m.setRowCount(0);
            try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT b.id,p.name,b.description,b.amount,b.bill_date FROM bills b JOIN patients p ON p.id=b.patient_id ORDER BY b.id DESC")){
                while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getBigDecimal(4),r.getDate(5)});
            }catch(Exception ex){error(ex);}
        };
        add.addActionListener(e -> {
            try {
                Item patient=(Item)patients.getSelectedItem();
                if(patient==null)throw new IllegalArgumentException("Add a patient first.");
                if(description.getText().isBlank())throw new IllegalArgumentException("Enter a bill description.");
                double value=Double.parseDouble(amount.getText().trim());
                if(value<0)throw new IllegalArgumentException("Amount cannot be negative.");
                Date d=Date.valueOf(date.getText().trim());
                try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO bills(patient_id,description,amount,bill_date) VALUES(?,?,?,?)")){
                    ps.setInt(1,patient.id);ps.setString(2,description.getText().trim());ps.setBigDecimal(3,java.math.BigDecimal.valueOf(value));ps.setDate(4,d);ps.executeUpdate();
                }
                description.setText("");amount.setText("");load.run();
            }catch(Exception ex){error(ex);}
        });
        refresh.addActionListener(e -> {loadChoices.run();load.run();}); loadChoices.run(); load.run(); return p;
    }

    private static class Item {
        final int id; final String name;
        Item(int id, String name) { this.id=id; this.name=name; }
        @Override public String toString() { return id + " - " + name; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}
