package ui;

import models.Utilisateur;
import services.EnseignantService;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EnseignantDashboard extends JFrame {

    private final Utilisateur enseignant;
    private final JPanel contentPanel; // main workspace

    public EnseignantDashboard(Utilisateur enseignant) {
        this.enseignant = enseignant;

        setTitle("👨‍🏫 Espace Enseignant - " + enseignant.getNom());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Sidebar buttons
        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton btnSaisirNote = new JButton("📝 Saisir une note");
        JButton btnCreerEpreuve = new JButton("🧾 Créer une épreuve");
        JButton btnModifierEpreuve = new JButton("✏️ Modifier une épreuve");
        JButton btnSupprimerEpreuve = new JButton("🗑️ Supprimer une épreuve");
        JButton btnConsulter = new JButton("📊 Consulter résultats");
        JButton btnCalculerFinale = new JButton("✅ Calculer / Valider notes finales");
        JButton btnDeconnexion = new JButton("🚪 Déconnexion");

        for (JButton b : new JButton[]{
                btnSaisirNote, btnCreerEpreuve, btnModifierEpreuve,
                btnSupprimerEpreuve, btnConsulter, btnCalculerFinale, btnDeconnexion
        }) {
            b.setFocusPainted(false);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            menuPanel.add(b);
        }

        // Main content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("Tableau de bord enseignant"));

        add(menuPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Button actions
        btnSaisirNote.addActionListener(e -> afficherSaisieNotePanel());
        btnCreerEpreuve.addActionListener(e -> ouvrirCreationEpreuve());
        btnModifierEpreuve.addActionListener(e -> ouvrirModifierEpreuve());
        btnSupprimerEpreuve.addActionListener(e -> ouvrirSuppressionEpreuve());
        btnConsulter.addActionListener(e -> EnseignantService.consulterResultats(enseignant));
        btnCalculerFinale.addActionListener(e -> EnseignantService.calculerEtValiderNoteFinale(enseignant));
        btnDeconnexion.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }

    // ---------------------- UI PANEL: Saisir Note ----------------------
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

        JButton btnSave = new JButton("💾 Enregistrer la note");

        // Load matières from DB
        services.ComboDataLoader.remplirMatieresCombo(enseignant, cmbMatiere);

        // When matière is selected
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
                if (cmbEpreuve.getSelectedItem() == null || cmbEtudiant.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(this, "⚠️ Sélection incomplète !");
                    return;
                }

                int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                int idEtudiant = Integer.parseInt(cmbEtudiant.getSelectedItem().toString().split(" - ")[0]);
                double note = Double.parseDouble(txtNote.getText());

                services.EnseignantService.saisirNoteGUI(enseignant, idEtudiant, idEpreuve, note);
                JOptionPane.showMessageDialog(this, "✅ Note enregistrée !");
                txtNote.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Erreur de saisie !");
            }
        });

        // Add all components
        formPanel.add(lblMatiere);
        formPanel.add(cmbMatiere);
        formPanel.add(lblEpreuve);
        formPanel.add(cmbEpreuve);
        formPanel.add(lblEtudiant);
        formPanel.add(cmbEtudiant);
        formPanel.add(lblNote);
        formPanel.add(txtNote);
        formPanel.add(new JLabel());
        formPanel.add(btnSave);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    private void ouvrirCreationEpreuve() {
        JFrame frame = new JFrame("Créer une épreuve");
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

        frame.setVisible(true);
    }


    private void ouvrirModifierEpreuve() {
        JFrame frame = new JFrame("✏️ Modifier une épreuve");
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

        frame.setVisible(true);
    }


    private void ouvrirSuppressionEpreuve() {
        JFrame frame = new JFrame("Supprimer une épreuve");
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

        frame.setVisible(true);
    }

    private void afficherResultats() {
        JTable table = EnseignantService.getResultatsTable(enseignant);
        JScrollPane scroll = new JScrollPane(table);

        JFrame f = new JFrame("Résultats des étudiants");
        f.setLayout(new BorderLayout());
        f.add(scroll, BorderLayout.CENTER);
        f.setSize(700, 400);
        f.setLocationRelativeTo(this);
        f.setVisible(true);
    }

}
