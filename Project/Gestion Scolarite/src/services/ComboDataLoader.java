package services;

import db.DatabaseConnection;
import models.Utilisateur;

import javax.swing.*;
import java.sql.*;

public class ComboDataLoader {

    public static void remplirMatieresCombo(Utilisateur enseignant, JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT m.id_matiere, m.nom
                FROM Matiere m
                JOIN Enseignant_Matiere em ON m.id_matiere = em.id_matiere
                WHERE em.id_enseignant = ?
            """;
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

    public static void remplirEpreuvesCombo(int idMatiere, JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_epreuve, type_epreuve FROM Epreuve WHERE id_matiere = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idMatiere);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                combo.addItem(rs.getInt("id_epreuve") + " - " + rs.getString("type_epreuve"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void remplirEtudiantsCombo(int idMatiere, JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT DISTINCT e.id_etudiant, u.nom, u.prenom
            FROM Etudiant e
            JOIN Utilisateur u ON e.id_etudiant = u.id_utilisateur
            JOIN Inscription i ON e.id_etudiant = i.id_etudiant
            JOIN Programme_Matiere pm ON i.id_programme = pm.id_programme
            WHERE pm.id_matiere = ?
        """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idMatiere);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                combo.addItem(rs.getInt("id_etudiant") + " - " +
                        rs.getString("nom") + " " + rs.getString("prenom"));
            }

            if (combo.getItemCount() == 0) {
                combo.addItem("⚠️ Aucun étudiant trouvé !");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
