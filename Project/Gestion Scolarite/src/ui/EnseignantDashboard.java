package ui;

import models.Utilisateur;
import services.EnseignantService;
import db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EnseignantDashboard extends JFrame {

    private final Utilisateur enseignant;
    private final JPanel contentPanel;

    // SAME COLORS AS ADMIN DASHBOARD
    private final Color primaryBlue = new Color(52, 152, 219);
    private final Color darkBlue = new Color(41, 128, 185);
    private final Color textColor = Color.WHITE;

    public EnseignantDashboard(Utilisateur enseignant) {
        this.enseignant = enseignant;

        setTitle("Espace Enseignant - " + enseignant.getNom());
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= SIDEBAR =================
        JPanel sidebar = new JPanel();
        sidebar.setBackground(primaryBlue);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.gridx = 0;

        JLabel title = new JLabel("ENSEIGNANT", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0;
        sidebar.add(title, gbc);

        gbc.gridy = 1;
        sidebar.add(Box.createVerticalStrut(10), gbc);

        // ===== Styled Buttons =====
        JButton btnSaisirNote = createButton("Saisir une note");
        JButton btnCreerEpreuve = createButton("Créer une épreuve");
        JButton btnModifierEpreuve = createButton("Modifier une épreuve");
        JButton btnSupprimerEpreuve = createButton("Supprimer une épreuve");
        JButton btnConsulter = createButton("Consulter résultats");
        JButton btnCalculerFinale = createButton("Valider Notes Finales");
        JButton btnLogout = createLogoutButton("Déconnexion");

        JButton[] menuButtons = {
                btnSaisirNote, btnCreerEpreuve, btnModifierEpreuve,
                btnSupprimerEpreuve, btnConsulter, btnCalculerFinale, btnLogout
        };

        int row = 2;
        for (JButton btn : menuButtons) {
            gbc.gridy = row++;
            sidebar.add(btn, gbc);
        }

        // ================= CONTENT PANEL =================
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel(
                "<html><h2>Bienvenue, " + enseignant.getNom() + "</h2>" +
                        "<p>Veuillez choisir une action dans le menu.</p></html>"
        );
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        contentPanel.add(welcome, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // ================= BUTTON ACTIONS =================
        btnSaisirNote.addActionListener(e -> afficherSaisieNotePanel());
        btnCreerEpreuve.addActionListener(e -> ouvrirCreationEpreuve());
        btnModifierEpreuve.addActionListener(e -> ouvrirModifierEpreuve());
        btnSupprimerEpreuve.addActionListener(e -> ouvrirSuppressionEpreuve());
        btnConsulter.addActionListener(e -> EnseignantService.consulterResultats(enseignant));
        btnCalculerFinale.addActionListener(e -> EnseignantService.calculerEtValiderNoteFinale(enseignant));

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }

    // ==========================================
    //            UI BUTTON STYLING
    // ==========================================
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setForeground(textColor);
        btn.setBackground(primaryBlue);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(darkBlue); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(primaryBlue); }
            public void mousePressed(java.awt.event.MouseEvent evt) { btn.setBackground(darkBlue.darker()); }
            public void mouseReleased(java.awt.event.MouseEvent evt) { btn.setBackground(darkBlue); }
        });

        return btn;
    }

    private JButton createLogoutButton(String text) {
        Color red = new Color(231, 76, 60);
        Color redDark = new Color(192, 57, 43);

        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(red);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(redDark); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(red); }
            public void mousePressed(java.awt.event.MouseEvent evt) { btn.setBackground(redDark.darker()); }
        });

        return btn;
    }

    // ===========================================================
    //            YOUR OLD PANELS (UNCHANGED LOGIC)
    // ===========================================================
    // I KEEP 100% OF YOUR LOGIC HERE
    // Only UI container/styling is updated above

    private void afficherSaisieNotePanel() {
        contentPanel.removeAll();

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));

        JLabel lblMatiere = new JLabel("Matière :");
        JComboBox<String> cmbMatiere = new JComboBox<>();

        JLabel lblEpreuve = new JLabel("Épreuve :");
        JComboBox<String> cmbEpreuve = new JComboBox<>();

        JLabel lblEtudiant = new JLabel("Étudiant :");
        JComboBox<String> cmbEtudiant = new JComboBox<>();

        JLabel lblNote = new JLabel("Note :");
        JTextField txtNote = new JTextField();

        JButton btnSave = createButton("Enregistrer");

        services.ComboDataLoader.remplirMatieresCombo(enseignant, cmbMatiere);

        cmbMatiere.addActionListener(e -> {
            if (cmbMatiere.getSelectedItem() != null) {
                try {
                    int idMatiere = Integer.parseInt(cmbMatiere.getSelectedItem().toString().split(" - ")[0]);
                    services.ComboDataLoader.remplirEpreuvesCombo(idMatiere, cmbEpreuve);
                    services.ComboDataLoader.remplirEtudiantsCombo(idMatiere, cmbEtudiant);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnSave.addActionListener(e -> {
            try {
                int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                int idEtudiant = Integer.parseInt(cmbEtudiant.getSelectedItem().toString().split(" - ")[0]);
                double note = Double.parseDouble(txtNote.getText());

                services.EnseignantService.saisirNoteGUI(enseignant, idEtudiant, idEpreuve, note);

                JOptionPane.showMessageDialog(this, "Note enregistrée !");
                txtNote.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur de saisie !");
            }
        });

        formPanel.add(lblMatiere); formPanel.add(cmbMatiere);
        formPanel.add(lblEpreuve); formPanel.add(cmbEpreuve);
        formPanel.add(lblEtudiant); formPanel.add(cmbEtudiant);
        formPanel.add(lblNote); formPanel.add(txtNote);
        formPanel.add(new JLabel()); formPanel.add(btnSave);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // The remaining 3 popups (unchanged logic)
    private void ouvrirCreationEpreuve() { JFrame frame = new JFrame("Créer une épreuve");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(this);
        frame.setLayout(new GridLayout(5, 2, 10, 10));

        JComboBox<String> cmbMatiere = new JComboBox<>();

        // ✅ Combo box for type — avoids typing mistakes
        JComboBox<String> cmbType = new JComboBox<>(new String[]{
                "examen", "controle", "tp", "projet"
        });

        // ✅ Default today's date (YYYY-MM-DD)
        JTextField txtDate = new JTextField(java.time.LocalDate.now().toString());
        JButton btnSave = new JButton("Créer");

        // Load matières for this enseignant
        services.EnseignantService.remplirMatieresCombo(enseignant, cmbMatiere);

        frame.add(new JLabel("Matière :"));
        frame.add(cmbMatiere);
        frame.add(new JLabel("Type d'épreuve :"));
        frame.add(cmbType);
        frame.add(new JLabel("Date (YYYY-MM-DD) :"));
        frame.add(txtDate);
        frame.add(new JLabel(""));
        frame.add(btnSave);

        btnSave.addActionListener(e -> {
            try {
                int idMatiere = Integer.parseInt(cmbMatiere.getSelectedItem().toString().split(" - ")[0]);
                String type = cmbType.getSelectedItem().toString();
                String date = txtDate.getText().trim();

                services.EnseignantService.creerEpreuveGUI(enseignant, idMatiere, type, date);
                JOptionPane.showMessageDialog(frame, "✅ Épreuve créée !");
                frame.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "⚠️ Entrée invalide !");
                ex.printStackTrace();
            }
        });

        frame.setVisible(true); }
    private void ouvrirModifierEpreuve() { JFrame frame = new JFrame("✏️ Modifier une épreuve");
        frame.setSize(450, 320);
        frame.setLocationRelativeTo(this);
        frame.setLayout(new GridLayout(5, 2, 10, 10));

        JComboBox<String> cmbEpreuve = new JComboBox<>();
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"examen", "controle", "tp", "projet"});
        JTextField txtDate = new JTextField();
        JButton btnSave = new JButton("💾 Mettre à jour");

        // Load épreuves
        services.EnseignantService.remplirEpreuvesComboParEnseignant(enseignant, cmbEpreuve);

        // Fetch existing data on selection
        cmbEpreuve.addActionListener(e -> {
            try (Connection conn = db.DatabaseConnection.getConnection()) {
                if (cmbEpreuve.getSelectedItem() != null) {
                    int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                    String sql = "SELECT type_epreuve, date_epreuve FROM Epreuve WHERE id_epreuve = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, idEpreuve);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        cmbType.setSelectedItem(rs.getString("type_epreuve"));
                        txtDate.setText(rs.getString("date_epreuve"));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.add(new JLabel("Épreuve :"));
        frame.add(cmbEpreuve);
        frame.add(new JLabel("Nouveau type :"));
        frame.add(cmbType);
        frame.add(new JLabel("Nouvelle date (YYYY-MM-DD) :"));
        frame.add(txtDate);
        frame.add(new JLabel(""));
        frame.add(btnSave);

        btnSave.addActionListener(e -> {
            try {
                int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                String type = cmbType.getSelectedItem().toString();
                String date = txtDate.getText().trim();

                services.EnseignantService.modifierEpreuveGUI(idEpreuve, type, date);
                JOptionPane.showMessageDialog(frame, "✅ Épreuve mise à jour !");
                frame.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "⚠️ Entrée invalide !");
            }
        });

        frame.setVisible(true); }
    private void ouvrirSuppressionEpreuve() { JFrame frame = new JFrame("Supprimer une épreuve");
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(this);
        frame.setLayout(new GridLayout(3, 2, 10, 10));

        JComboBox<String> cmbEpreuve = new JComboBox<>();
        JButton btnDelete = new JButton("Supprimer");

        EnseignantService.remplirEpreuvesComboParEnseignant(enseignant, cmbEpreuve);

        frame.add(new JLabel("Sélectionner une épreuve :"));
        frame.add(cmbEpreuve);
        frame.add(new JLabel(""));
        frame.add(btnDelete);

        btnDelete.addActionListener(e -> {
            try {
                int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                int confirm = JOptionPane.showConfirmDialog(frame, "Supprimer cette épreuve ?", "Confirmation", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    EnseignantService.supprimerEpreuveGUI(idEpreuve);
                    JOptionPane.showMessageDialog(frame, "✅ Épreuve supprimée !");
                    frame.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "⚠️ Sélection invalide !");
            }
        });

        frame.setVisible(true); }
}
