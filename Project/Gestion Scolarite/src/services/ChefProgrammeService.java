package services;

import db.DatabaseConnection;
import models.Utilisateur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class ChefProgrammeService {

    // ==================== PROGRAMME MANAGEMENT ====================

    public static void ajouterProgramme(Utilisateur chef) {
        JTextField txtNom = new JTextField();
        JTextField txtDesc = new JTextField();

        Object[] fields = {
                "Nom du programme :", txtNom,
                "Description :", txtDesc
        };

        int result = JOptionPane.showConfirmDialog(null, fields, "Créer un programme", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Programme (nom, description) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNom.getText());
            ps.setString(2, txtDesc.getText());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Programme ajouté avec succès !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }

    public static void modifierProgramme(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> combo = new JComboBox<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rs.next()) combo.addItem(rs.getInt("id_programme") + " - " + rs.getString("nom"));

            JTextField txtNom = new JTextField();
            JTextField txtDesc = new JTextField();

            Object[] fields = {"Programme :", combo, "Nouveau nom :", txtNom, "Nouvelle description :", txtDesc};
            int result = JOptionPane.showConfirmDialog(null, fields, "Modifier un programme", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            int idProgramme = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);
            String sql = "UPDATE Programme SET nom=?, description=? WHERE id_programme=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNom.getText());
            ps.setString(2, txtDesc.getText());
            ps.setInt(3, idProgramme);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Programme modifié !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
        }
    }

    public static void supprimerProgramme(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> combo = new JComboBox<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rs.next()) combo.addItem(rs.getInt("id_programme") + " - " + rs.getString("nom"));

            int result = JOptionPane.showConfirmDialog(null, combo, "Supprimer un programme", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            int idProgramme = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);

            int confirm = JOptionPane.showConfirmDialog(null, "⚠️ Supprimer ce programme ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            String sql = "DELETE FROM Programme WHERE id_programme = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idProgramme);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Programme supprimé !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }

    public static void afficherProgrammes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_programme, nom, description FROM Programme";
            ResultSet rs = conn.createStatement().executeQuery(sql);

            DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Nom", "Description"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_programme"),
                        rs.getString("nom"),
                        rs.getString("description")
                });
            }

            JTable table = new JTable(model);
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "📋 Liste des Programmes", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
        }
    }

    // ==================== MATIÈRES MANAGEMENT ====================

    // ==================== AJOUTER MATIERE ====================
    public static void ajouterMatiere(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // --- Select programme ---
            JComboBox<String> comboProg = new JComboBox<>();
            ResultSet rsP = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rsP.next()) comboProg.addItem(rsP.getInt("id_programme") + " - " + rsP.getString("nom"));

            JTextField txtNom = new JTextField();
            JTextField txtCoef = new JTextField();

            Object[] fields = {
                    "Programme :", comboProg,
                    "Nom de la matière :", txtNom,
                    "Coefficient :", txtCoef
            };

            int res = JOptionPane.showConfirmDialog(null, fields, "Ajouter une matière", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idProgramme = Integer.parseInt(comboProg.getSelectedItem().toString().split(" - ")[0]);
            double coef = Double.parseDouble(txtCoef.getText());

            // --- Insert into Matiere ---
            String sqlMat = "INSERT INTO Matiere (nom, coefficient) VALUES (?, ?)";
            PreparedStatement psMat = conn.prepareStatement(sqlMat, Statement.RETURN_GENERATED_KEYS);
            psMat.setString(1, txtNom.getText());
            psMat.setDouble(2, coef);
            psMat.executeUpdate();

            // --- Get generated ID and link it to the selected programme ---
            ResultSet rsKeys = psMat.getGeneratedKeys();
            if (rsKeys.next()) {
                int idMatiere = rsKeys.getInt(1);

                String sqlLink = "INSERT INTO Programme_Matiere (id_programme, id_matiere) VALUES (?, ?)";
                PreparedStatement psLink = conn.prepareStatement(sqlLink);
                psLink.setInt(1, idProgramme);
                psLink.setInt(2, idMatiere);
                psLink.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "✅ Matière ajoutée et liée au programme avec succès !");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void modifierMatiere(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> comboMat = new JComboBox<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_matiere, nom FROM Matiere");
            while (rs.next()) comboMat.addItem(rs.getInt("id_matiere") + " - " + rs.getString("nom"));

            JTextField txtNom = new JTextField();
            JTextField txtCoef = new JTextField();

            Object[] fields = {"Matière :", comboMat, "Nouveau nom :", txtNom, "Nouveau coefficient :", txtCoef};
            int res = JOptionPane.showConfirmDialog(null, fields, "Modifier une matière", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idMat = Integer.parseInt(comboMat.getSelectedItem().toString().split(" - ")[0]);
            String sql = "UPDATE Matiere SET nom=?, coefficient=? WHERE id_matiere=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNom.getText());
            ps.setDouble(2, Double.parseDouble(txtCoef.getText()));
            ps.setInt(3, idMat);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Matière mise à jour !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
        }
    }

    public static void supprimerMatiere(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> combo = new JComboBox<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_matiere, nom FROM Matiere");
            while (rs.next()) combo.addItem(rs.getInt("id_matiere") + " - " + rs.getString("nom"));

            int res = JOptionPane.showConfirmDialog(null, combo, "Supprimer une matière", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idMat = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);
            String sql = "DELETE FROM Matiere WHERE id_matiere=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idMat);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Matière supprimée !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
        }
    }

    // ==================== OTHER FUNCTIONS ====================

    public static void definirPonderationsMatieres(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> combo = new JComboBox<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_matiere, nom FROM Matiere");
            while (rs.next()) combo.addItem(rs.getInt("id_matiere") + " - " + rs.getString("nom"));

            JTextField txtCoef = new JTextField();
            Object[] fields = {"Matière :", combo, "Nouveau coefficient :", txtCoef};
            int res = JOptionPane.showConfirmDialog(null, fields, "Modifier coefficient matière", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idMat = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);
            double coef = Double.parseDouble(txtCoef.getText());

            String sql = "UPDATE Matiere SET coefficient=? WHERE id_matiere=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, coef);
            ps.setInt(2, idMat);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Coefficient mis à jour !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
        }
    }

    public static void definirPrerequisProgramme(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> progA = new JComboBox<>();
            JComboBox<String> progB = new JComboBox<>();

            ResultSet rs = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rs.next()) {
                String item = rs.getInt("id_programme") + " - " + rs.getString("nom");
                progA.addItem(item);
                progB.addItem(item);
            }

            Object[] fields = {"Programme :", progA, "Prérequis :", progB};
            int res = JOptionPane.showConfirmDialog(null, fields, "Définir un prérequis", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idProg = Integer.parseInt(progA.getSelectedItem().toString().split(" - ")[0]);
            int idPre = Integer.parseInt(progB.getSelectedItem().toString().split(" - ")[0]);

            if (idProg == idPre) {
                JOptionPane.showMessageDialog(null, "❌ Un programme ne peut pas être son propre prérequis !");
                return;
            }

            String sql = "INSERT INTO PrerequisProgramme (id_programme, id_programme_prerequis) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idProg);
            ps.setInt(2, idPre);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Prérequis défini !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
        }
    }

    public static void assignerOuModifierEnseignantMatiere(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<String> matieres = new JComboBox<>();
            JComboBox<String> enseignants = new JComboBox<>();

            ResultSet rsM = conn.createStatement().executeQuery("SELECT id_matiere, nom FROM Matiere");
            while (rsM.next()) matieres.addItem(rsM.getInt("id_matiere") + " - " + rsM.getString("nom"));

            ResultSet rsE = conn.createStatement().executeQuery("SELECT id_utilisateur, nom, prenom FROM Utilisateur WHERE role='enseignant'");
            while (rsE.next()) enseignants.addItem(rsE.getInt("id_utilisateur") + " - " + rsE.getString("nom") + " " + rsE.getString("prenom"));

            Object[] fields = {"Matière :", matieres, "Enseignant :", enseignants};
            int res = JOptionPane.showConfirmDialog(null, fields, "Assigner un enseignant", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idMat = Integer.parseInt(matieres.getSelectedItem().toString().split(" - ")[0]);
            int idEns = Integer.parseInt(enseignants.getSelectedItem().toString().split(" - ")[0]);

            String sqlDelete = "DELETE FROM Enseignant_Matiere WHERE id_matiere=?";
            PreparedStatement psDel = conn.prepareStatement(sqlDelete);
            psDel.setInt(1, idMat);
            psDel.executeUpdate();

            String sqlInsert = "INSERT INTO Enseignant_Matiere (id_enseignant, id_matiere) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sqlInsert);
            ps.setInt(1, idEns);
            ps.setInt(2, idMat);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Enseignant assigné !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
        }
    }

    public static void validerMoyennesEtStatuts(Utilisateur chef) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                UPDATE Etudiant e
                JOIN (
                    SELECT id_etudiant,
                           CASE 
                               WHEN AVG(nf.moyenne_finale) >= 10 THEN 'admis'
                               WHEN AVG(nf.moyenne_finale) >= 5 THEN 'redoublant'
                               ELSE 'exclu'
                           END AS statut
                    FROM NoteFinale nf
                    WHERE nf.valide = TRUE
                    GROUP BY nf.id_etudiant
                ) s ON e.id_etudiant = s.id_etudiant
                SET e.statut = s.statut
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Moyennes et statuts validés !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur : " + e.getMessage());
        }
    }

    public static void afficherMatieresParProgramme() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // --- Select the programme ---
            JComboBox<String> combo = new JComboBox<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rs.next()) combo.addItem(rs.getInt("id_programme") + " - " + rs.getString("nom"));

            int res = JOptionPane.showConfirmDialog(null, combo, "Choisir un programme", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idProgramme = Integer.parseInt(combo.getSelectedItem().toString().split(" - ")[0]);

            // --- Retrieve all subjects linked to that programme ---
            String sql = """
            SELECT m.id_matiere, m.nom AS matiere, m.coefficient,
                   u.nom AS enseignant_nom, u.prenom AS enseignant_prenom
            FROM Programme_Matiere pm
            JOIN Matiere m ON pm.id_matiere = m.id_matiere
            LEFT JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
            LEFT JOIN Utilisateur u ON em.id_enseignant = u.id_utilisateur
            WHERE pm.id_programme = ?
            ORDER BY m.nom
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idProgramme);
            ResultSet rsMat = ps.executeQuery();

            // --- Prepare the table model ---
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Matière", "Coefficient", "Enseignant"}, 0);

            while (rsMat.next()) {
                String enseignant = (rsMat.getString("enseignant_nom") != null)
                        ? rsMat.getString("enseignant_nom") + " " + rsMat.getString("enseignant_prenom")
                        : "— Aucun —";
                model.addRow(new Object[]{
                        rsMat.getInt("id_matiere"),
                        rsMat.getString("matiere"),
                        rsMat.getDouble("coefficient"),
                        enseignant
                });
            }

            // --- Display results ---
            JTable table = new JTable(model);
            table.setFillsViewportHeight(true);
            table.setRowHeight(25);

            JOptionPane.showMessageDialog(null, new JScrollPane(table),
                    "📚 Matières du programme sélectionné", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ==================== AJOUTER MATIERE EXISTANTE À PROGRAMME ====================
    public static void ajouterMatiereExistanteAProgramme() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // --- Select an existing subject ---
            JComboBox<String> comboMat = new JComboBox<>();
            ResultSet rsM = conn.createStatement().executeQuery("SELECT id_matiere, nom FROM Matiere");
            while (rsM.next()) comboMat.addItem(rsM.getInt("id_matiere") + " - " + rsM.getString("nom"));

            // --- Select the target programme ---
            JComboBox<String> comboProg = new JComboBox<>();
            ResultSet rsP = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rsP.next()) comboProg.addItem(rsP.getInt("id_programme") + " - " + rsP.getString("nom"));

            Object[] fields = {
                    "Matière existante :", comboMat,
                    "Programme cible :", comboProg
            };
            int res = JOptionPane.showConfirmDialog(null, fields, "Ajouter une matière existante à un programme", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;

            int idMat = Integer.parseInt(comboMat.getSelectedItem().toString().split(" - ")[0]);
            int idProg = Integer.parseInt(comboProg.getSelectedItem().toString().split(" - ")[0]);

            // --- Check if already linked ---
            String checkSql = "SELECT COUNT(*) FROM Programme_Matiere WHERE id_programme = ? AND id_matiere = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, idProg);
            psCheck.setInt(2, idMat);
            ResultSet rsCheck = psCheck.executeQuery();

            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "⚠️ Cette matière est déjà associée à ce programme !");
                return;
            }

            // --- Link the existing subject to the selected programme ---
            String sqlLink = "INSERT INTO Programme_Matiere (id_programme, id_matiere) VALUES (?, ?)";
            PreparedStatement psLink = conn.prepareStatement(sqlLink);
            psLink.setInt(1, idProg);
            psLink.setInt(2, idMat);
            psLink.executeUpdate();

            JOptionPane.showMessageDialog(null, "✅ Matière ajoutée à ce programme avec succès !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }


}
