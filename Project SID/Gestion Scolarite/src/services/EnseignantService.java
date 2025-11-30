package services;

import db.DatabaseConnection;
import models.Utilisateur;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class EnseignantService {

    // ===================== SAISIR NOTE (UI version, multi-programme compatible) =====================
    public static void saisirNote(Utilisateur enseignant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // --- Select épreuve first ---
            JComboBox<String> cmbEpreuve = new JComboBox<>();
            String sqlEpreuves = """
            SELECT e.id_epreuve, e.type_epreuve, m.nom AS matiere
            FROM Epreuve e
            JOIN Matiere m ON e.id_matiere = m.id_matiere
            JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
            WHERE em.id_enseignant = ?
        """;
            PreparedStatement psEpr = conn.prepareStatement(sqlEpreuves);
            psEpr.setInt(1, enseignant.getId());
            ResultSet rsEpr = psEpr.executeQuery();
            while (rsEpr.next()) {
                cmbEpreuve.addItem(rsEpr.getInt("id_epreuve") + " - " +
                        rsEpr.getString("type_epreuve") + " (" + rsEpr.getString("matiere") + ")");
            }

            JComboBox<String> cmbEtudiant = new JComboBox<>();
            JTextField txtNote = new JTextField();

            // --- Load students dynamically when épreuve changes ---
            cmbEpreuve.addActionListener(e -> {
                cmbEtudiant.removeAllItems();
                if (cmbEpreuve.getSelectedItem() != null) {
                    try {
                        int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);

                        String sqlEtudiants = """
                        SELECT DISTINCT e.id_etudiant, u.nom, u.prenom
                        FROM Etudiant e
                        JOIN Utilisateur u ON e.id_etudiant = u.id_utilisateur
                        JOIN Inscription i ON e.id_etudiant = i.id_etudiant
                        JOIN Programme_Matiere pm ON i.id_programme = pm.id_programme
                        JOIN Matiere m ON pm.id_matiere = m.id_matiere
                        JOIN Epreuve ep ON ep.id_matiere = m.id_matiere
                        WHERE ep.id_epreuve = ?
                    """;

                        PreparedStatement psEtu = conn.prepareStatement(sqlEtudiants);
                        psEtu.setInt(1, idEpreuve);
                        ResultSet rsEtu = psEtu.executeQuery();

                        while (rsEtu.next()) {
                            cmbEtudiant.addItem(rsEtu.getInt("id_etudiant") + " - " +
                                    rsEtu.getString("nom") + " " + rsEtu.getString("prenom"));
                        }

                        if (cmbEtudiant.getItemCount() == 0) {
                            cmbEtudiant.addItem("⚠️ Aucun étudiant trouvé !");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "❌ Erreur lors du chargement des étudiants : " + ex.getMessage());
                    }
                }
            });

            // --- Build input panel ---
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.add(new JLabel("Épreuve :"));
            panel.add(cmbEpreuve);
            panel.add(new JLabel("Étudiant :"));
            panel.add(cmbEtudiant);
            panel.add(new JLabel("Note :"));
            panel.add(txtNote);

            int result = JOptionPane.showConfirmDialog(null, panel, "Saisir une note", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                if (cmbEpreuve.getSelectedItem() == null || cmbEtudiant.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(null, "⚠️ Sélection incomplète !");
                    return;
                }

                String etuSelection = cmbEtudiant.getSelectedItem().toString();
                if (etuSelection.startsWith("⚠️")) {
                    JOptionPane.showMessageDialog(null, "❌ Aucun étudiant valide sélectionné !");
                    return;
                }

                int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                int idEtudiant = Integer.parseInt(etuSelection.split(" - ")[0]);
                double note;

                try {
                    note = Double.parseDouble(txtNote.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "⚠️ Veuillez entrer une note valide !");
                    return;
                }

                saisirNoteGUI(enseignant, idEtudiant, idEpreuve, note);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }



    // ===================== SAISIR NOTE (GUI VERSION - simplified, cross-programme ready) =====================
    public static void saisirNoteGUI(Utilisateur enseignant, int idEtudiant, int idEpreuve, double note) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // ✅ Insert or update the note
            String sqlInsert = """
            INSERT INTO Note (id_etudiant, id_epreuve, note)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE note = VALUES(note)
        """;
            PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
            psInsert.setInt(1, idEtudiant);
            psInsert.setInt(2, idEpreuve);
            psInsert.setDouble(3, note);
            psInsert.executeUpdate();

            // ✅ Update student's status (recalculate if needed)
            EtudiantService.mettreAJourStatut(idEtudiant);

            JOptionPane.showMessageDialog(null, "✅ Note enregistrée avec succès !");
            System.out.println("✅ Note enregistrée (multi-programme OK)");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }



    // ===================== CRÉER ÉPREUVE (GUI VERSION) =====================
    public static void creerEpreuveGUI(Utilisateur enseignant, int idMatiere, String type, String date) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            type = type.toLowerCase(); // ✅ just modify the existing variable, not redeclare it
            double coefficient = 0.0;

            // Check if this matière has a project
            boolean hasProject = false;
            String sqlCheck = "SELECT COUNT(*) FROM Epreuve WHERE id_matiere = ? AND type_epreuve = 'projet'";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, idMatiere);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                hasProject = true;
            }

            // Assign coefficient based on type
            if (hasProject) {
                switch (type) {
                    case "controle", "tp", "projet" -> coefficient = 0.2;
                    case "examen" -> coefficient = 0.4;
                }
            } else {
                switch (type) {
                    case "controle", "tp" -> coefficient = 0.2;
                    case "examen" -> coefficient = 0.6;
                }
            }

            // Insert new épreuve with coefficient
            String sqlInsert = """
            INSERT INTO Epreuve (type_epreuve, date_epreuve, coefficient, id_matiere)
            VALUES (?, ?, ?, ?)
        """;
            PreparedStatement ps = conn.prepareStatement(sqlInsert);
            ps.setString(1, type);
            ps.setString(2, date);
            ps.setDouble(3, coefficient);
            ps.setInt(4, idMatiere);
            ps.executeUpdate();

            System.out.println("✅ Épreuve créée avec succès (GUI) !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ===================== MODIFIER ÉPREUVE (GUI VERSION) =====================
    public static void modifierEpreuveGUI(int idEpreuve, String nouveauType, String nouvelleDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Get id_matiere for this épreuve
            String sqlMat = "SELECT id_matiere FROM Epreuve WHERE id_epreuve = ?";
            PreparedStatement psMat = conn.prepareStatement(sqlMat);
            psMat.setInt(1, idEpreuve);
            ResultSet rsMat = psMat.executeQuery();

            if (!rsMat.next()) {
                JOptionPane.showMessageDialog(null, "⚠️ Épreuve introuvable !");
                return;
            }

            int idMatiere = rsMat.getInt("id_matiere");
            nouveauType = nouveauType.toLowerCase();

            // Check if the matière has a project
            boolean hasProject = false;
            String sqlCheck = "SELECT COUNT(*) FROM Epreuve WHERE id_matiere = ? AND type_epreuve = 'projet'";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, idMatiere);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) hasProject = true;

            // Determine coefficient
            double coefficient = 0.0;
            if (hasProject) {
                switch (nouveauType) {
                    case "controle", "tp", "projet" -> coefficient = 0.2;
                    case "examen" -> coefficient = 0.4;
                }
            } else {
                switch (nouveauType) {
                    case "controle", "tp" -> coefficient = 0.2;
                    case "examen" -> coefficient = 0.6;
                }
            }

            // Update the épreuve
            String sqlUpdate = """
            UPDATE Epreuve 
            SET type_epreuve = ?, date_epreuve = ?, coefficient = ?
            WHERE id_epreuve = ?
        """;
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setString(1, nouveauType);
            psUpdate.setString(2, nouvelleDate);
            psUpdate.setDouble(3, coefficient);
            psUpdate.setInt(4, idEpreuve);

            int rows = psUpdate.executeUpdate();
            if (rows > 0)
                JOptionPane.showMessageDialog(null, "✅ Épreuve mise à jour avec succès !");
            else
                JOptionPane.showMessageDialog(null, "⚠️ Échec de la mise à jour !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ===================== SUPPRIMER ÉPREUVE (GUI VERSION) =====================
    public static void supprimerEpreuveGUI(int idEpreuve) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlDelete = "DELETE FROM Epreuve WHERE id_epreuve=?";
            PreparedStatement ps = conn.prepareStatement(sqlDelete);
            ps.setInt(1, idEpreuve);

            int rows = ps.executeUpdate();
            if (rows > 0)
                System.out.println("✅ Épreuve supprimée (GUI) !");
            else
                System.out.println("⚠️ Aucune épreuve trouvée !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void remplirMatieresCombo(Utilisateur enseignant, JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT m.id_matiere, m.nom FROM Matiere m JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere WHERE em.id_enseignant = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, enseignant.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                combo.addItem(rs.getInt("id_matiere") + " - " + rs.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void remplirEpreuvesComboParEnseignant(Utilisateur enseignant, JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT e.id_epreuve, e.type_epreuve, m.nom AS matiere
            FROM Epreuve e
            JOIN Matiere m ON e.id_matiere = m.id_matiere
            JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
            WHERE em.id_enseignant = ?
        """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, enseignant.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                combo.addItem(rs.getInt("id_epreuve") + " - " + rs.getString("matiere") + " (" + rs.getString("type_epreuve") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== CONSULTER RESULTATS (GUI) =====================
    public static JTable getResultatsTable(Utilisateur enseignant) {
        String[] columns = {"Nom", "Prénom", "Matière", "Épreuve", "Note"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT u.nom, u.prenom, m.nom AS matiere, e.type_epreuve, n.note
            FROM Note n
            JOIN Etudiant et ON n.id_etudiant = et.id_etudiant
            JOIN Utilisateur u ON et.id_etudiant = u.id_utilisateur
            JOIN Epreuve e ON n.id_epreuve = e.id_epreuve
            JOIN Matiere m ON e.id_matiere = m.id_matiere
            JOIN Enseignant_Matiere em ON em.id_matiere = m.id_matiere
            WHERE em.id_enseignant = ?
            ORDER BY u.nom, m.nom, e.type_epreuve
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, enseignant.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("matiere"),
                        rs.getString("type_epreuve"),
                        rs.getDouble("note")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setEnabled(false);
        return table;
    }



    // ===================== CREER EPREUVE =====================
    public static void creerEpreuve(Utilisateur enseignant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> cmbMatieres = new JComboBox<>();
            String sqlMatieres = """
                SELECT m.id_matiere, m.nom 
                FROM Matiere m
                JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
                WHERE em.id_enseignant = ?
            """;
            PreparedStatement psMat = conn.prepareStatement(sqlMatieres);
            psMat.setInt(1, enseignant.getId());
            ResultSet rsMat = psMat.executeQuery();

            while (rsMat.next()) {
                cmbMatieres.addItem(rsMat.getInt("id_matiere") + " - " + rsMat.getString("nom"));
            }

            JTextField txtType = new JTextField();
            JTextField txtDate = new JTextField("2025-11-08");

            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.add(new JLabel("Matière :"));
            panel.add(cmbMatieres);
            panel.add(new JLabel("Type (examen/TP/contrôle) :"));
            panel.add(txtType);
            panel.add(new JLabel("Date (YYYY-MM-DD) :"));
            panel.add(txtDate);

            int res = JOptionPane.showConfirmDialog(null, panel, "Créer une épreuve", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                int idMatiere = Integer.parseInt(cmbMatieres.getSelectedItem().toString().split(" - ")[0]);
                String type = txtType.getText().trim();
                String date = txtDate.getText().trim();

                String sql = "INSERT INTO Epreuve (type_epreuve, date_epreuve, id_matiere) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, type);
                ps.setString(2, date);
                ps.setInt(3, idMatiere);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(null, "✅ Épreuve créée avec succès !");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }

    // ===================== MODIFIER EPREUVE =====================
    public static void modifierEpreuve(Utilisateur enseignant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> cmbEpreuve = new JComboBox<>();
            String sqlList = """
                SELECT e.id_epreuve, e.type_epreuve, e.date_epreuve, m.nom AS matiere
                FROM Epreuve e
                JOIN Matiere m ON e.id_matiere = m.id_matiere
                JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
                WHERE em.id_enseignant = ?
            """;
            PreparedStatement psList = conn.prepareStatement(sqlList);
            psList.setInt(1, enseignant.getId());
            ResultSet rs = psList.executeQuery();

            while (rs.next()) {
                cmbEpreuve.addItem(rs.getInt("id_epreuve") + " - " + rs.getString("type_epreuve") +
                        " (" + rs.getString("matiere") + ")");
            }

            JTextField txtType = new JTextField();
            JTextField txtDate = new JTextField("2025-11-08");

            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.add(new JLabel("Épreuve :"));
            panel.add(cmbEpreuve);
            panel.add(new JLabel("Nouveau type :"));
            panel.add(txtType);
            panel.add(new JLabel("Nouvelle date :"));
            panel.add(txtDate);

            int result = JOptionPane.showConfirmDialog(null, panel, "Modifier une épreuve", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                String type = txtType.getText().trim();
                String date = txtDate.getText().trim();

                String sqlUpdate = "UPDATE Epreuve SET type_epreuve=?, date_epreuve=? WHERE id_epreuve=?";
                PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
                psUpdate.setString(1, type);
                psUpdate.setString(2, date);
                psUpdate.setInt(3, idEpreuve);
                int rows = psUpdate.executeUpdate();

                if (rows > 0)
                    JOptionPane.showMessageDialog(null, "✅ Épreuve modifiée avec succès !");
                else
                    JOptionPane.showMessageDialog(null, "⚠️ Épreuve introuvable !");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }

    // ===================== SUPPRIMER EPREUVE =====================
    public static void supprimerEpreuve(Utilisateur enseignant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> cmbEpreuve = new JComboBox<>();
            String sqlList = """
                SELECT e.id_epreuve, e.type_epreuve, m.nom AS matiere
                FROM Epreuve e
                JOIN Matiere m ON e.id_matiere = m.id_matiere
                JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
                WHERE em.id_enseignant = ?
            """;
            PreparedStatement psList = conn.prepareStatement(sqlList);
            psList.setInt(1, enseignant.getId());
            ResultSet rs = psList.executeQuery();
            while (rs.next()) {
                cmbEpreuve.addItem(rs.getInt("id_epreuve") + " - " + rs.getString("type_epreuve") +
                        " (" + rs.getString("matiere") + ")");
            }

            int confirm = JOptionPane.showConfirmDialog(null, cmbEpreuve, "Sélectionner une épreuve à supprimer", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                int idEpreuve = Integer.parseInt(cmbEpreuve.getSelectedItem().toString().split(" - ")[0]);
                String sqlDelete = "DELETE FROM Epreuve WHERE id_epreuve=?";
                PreparedStatement psDel = conn.prepareStatement(sqlDelete);
                psDel.setInt(1, idEpreuve);
                int rows = psDel.executeUpdate();

                if (rows > 0)
                    JOptionPane.showMessageDialog(null, "✅ Épreuve supprimée !");
                else
                    JOptionPane.showMessageDialog(null, "⚠️ Aucune épreuve trouvée !");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }

    // ===================== CONSULTER RESULTATS =====================
    public static void consulterResultats(Utilisateur enseignant) {
        JFrame frame = new JFrame("Résultats des étudiants");
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // --- Top filter panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSearch = new JLabel("🔍 Rechercher :");
        JTextField txtSearch = new JTextField(20);
        topPanel.add(lblSearch);
        topPanel.add(txtSearch);

        // --- Table setup ---
        String[] cols = {"Nom", "Prénom", "Matière", "Type", "Note"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true); // ✅ allow sorting
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);

        // Center alignment for text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scroll = new JScrollPane(table);

        // --- Load data from DB ---
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT u.nom, u.prenom, m.nom AS matiere, e.type_epreuve, n.note
            FROM Note n
            JOIN Etudiant et ON n.id_etudiant = et.id_etudiant
            JOIN Utilisateur u ON et.id_etudiant = u.id_utilisateur
            JOIN Epreuve e ON n.id_epreuve = e.id_epreuve
            JOIN Matiere m ON e.id_matiere = m.id_matiere
            JOIN Enseignant_Matiere em ON em.id_matiere = m.id_matiere
            WHERE em.id_enseignant = ?
            ORDER BY u.nom, m.nom
        """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, enseignant.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("matiere"),
                        rs.getString("type_epreuve"),
                        rs.getDouble("note")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }

        // --- Add live filtering ---
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);

        frame.setVisible(true);
    }


    // ===================== CALCULER ET VALIDER =====================
    public static void calculerEtValiderNoteFinale(Utilisateur enseignant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> cmbMatieres = new JComboBox<>();
            String sqlMat = """
                SELECT m.id_matiere, m.nom
                FROM Matiere m
                JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
                WHERE em.id_enseignant = ?
            """;
            PreparedStatement psMat = conn.prepareStatement(sqlMat);
            psMat.setInt(1, enseignant.getId());
            ResultSet rsMat = psMat.executeQuery();
            while (rsMat.next()) {
                cmbMatieres.addItem(rsMat.getInt("id_matiere") + " - " + rsMat.getString("nom"));
            }

            int res = JOptionPane.showConfirmDialog(null, cmbMatieres, "Choisir une matière", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idMatiere = Integer.parseInt(cmbMatieres.getSelectedItem().toString().split(" - ")[0]);

            // Compute averages
            String sqlEtudiants = """
                SELECT DISTINCT u.id_utilisateur, u.nom, u.prenom
                FROM Note n
                JOIN Epreuve e ON n.id_epreuve = e.id_epreuve
                JOIN Etudiant et ON n.id_etudiant = et.id_etudiant
                JOIN Utilisateur u ON et.id_etudiant = u.id_utilisateur
                WHERE e.id_matiere = ?
            """;
            PreparedStatement psEtu = conn.prepareStatement(sqlEtudiants);
            psEtu.setInt(1, idMatiere);
            ResultSet rsEtu = psEtu.executeQuery();

            while (rsEtu.next()) {
                int idEtu = rsEtu.getInt("id_utilisateur");
                String nom = rsEtu.getString("nom");
                String prenom = rsEtu.getString("prenom");

                String sqlMoy = """
                    SELECT SUM(n.note * e.coefficient) / SUM(e.coefficient) AS moyenne
                    FROM Note n
                    JOIN Epreuve e ON n.id_epreuve = e.id_epreuve
                    WHERE e.id_matiere = ? AND n.id_etudiant = ?
                """;
                PreparedStatement psMoy = conn.prepareStatement(sqlMoy);
                psMoy.setInt(1, idMatiere);
                psMoy.setInt(2, idEtu);
                ResultSet rsMoy = psMoy.executeQuery();

                double moyenne = 0;
                if (rsMoy.next()) moyenne = rsMoy.getDouble("moyenne");

                String sqlSave = """
                    INSERT INTO NoteFinale (id_etudiant, id_matiere, moyenne_finale, valide)
                    VALUES (?, ?, ?, FALSE)
                    ON DUPLICATE KEY UPDATE moyenne_finale = VALUES(moyenne_finale), valide = FALSE
                """;
                PreparedStatement psSave = conn.prepareStatement(sqlSave);
                psSave.setInt(1, idEtu);
                psSave.setInt(2, idMatiere);
                psSave.setDouble(3, moyenne);
                psSave.executeUpdate();
            }

            int confirm = JOptionPane.showConfirmDialog(null, "Valider toutes les notes finales ?", "Validation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String sqlVal = "UPDATE NoteFinale SET valide = TRUE WHERE id_matiere = ?";
                PreparedStatement psVal = conn.prepareStatement(sqlVal);
                psVal.setInt(1, idMatiere);
                psVal.executeUpdate();
                JOptionPane.showMessageDialog(null, "✅ Notes finales validées !");
            } else {
                JOptionPane.showMessageDialog(null, "Annulé, notes non validées.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }
}
