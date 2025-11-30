package services;

import db.DatabaseConnection;
import models.Utilisateur;

import java.sql.*;

public class AuthService {

    // Returns Utilisateur if login succeeds, null otherwise
    public static Utilisateur login(String email, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_utilisateur, nom, prenom, email, mot_de_passe, role FROM Utilisateur WHERE email=? AND mot_de_passe=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Build and return the Utilisateur object
                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role")
                );
            } else {
                return null; // login failed
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
