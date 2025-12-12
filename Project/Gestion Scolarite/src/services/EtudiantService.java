package services;

import db.DatabaseConnection;
import models.Utilisateur;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

public class EtudiantService {

    public static void afficherNotes(Utilisateur etudiant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT m.nom AS matiere, e.type_epreuve AS type, n.note
            FROM Note n
            JOIN Epreuve e ON n.id_epreuve = e.id_epreuve
            JOIN Matiere m ON e.id_matiere = m.id_matiere
            WHERE n.id_etudiant = ?
            ORDER BY m.nom, e.type_epreuve
        """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, etudiant.getId());
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Matière", "Type", "Note"}, 0);
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                model.addRow(new Object[]{
                        rs.getString("matiere"),
                        rs.getString("type"),
                        rs.getDouble("note")
                });
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(null, "⚠️ Aucune note trouvée !");
                return;
            }

            JTable table = new JTable(model);

            // 🎨 Custom cell renderer to color rows based on the note value
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    double note = 0.0;
                    try {
                        note = Double.parseDouble(table.getValueAt(row, 2).toString());
                    } catch (Exception ignored) {}

                    if (!isSelected) {
                        if (note >= 10) {
                            cell.setBackground(new Color(198, 239, 206)); // light green
                            cell.setForeground(new Color(0, 97, 0));
                        } else if (note >= 5) {
                            cell.setBackground(new Color(255, 235, 156)); // light orange
                            cell.setForeground(new Color(156, 101, 0));
                        } else {
                            cell.setBackground(new Color(255, 199, 206)); // light red
                            cell.setForeground(new Color(156, 0, 6));
                        }
                    } else {
                        cell.setBackground(table.getSelectionBackground());
                        cell.setForeground(table.getSelectionForeground());
                    }
                    return cell;
                }
            });

            // Adjust table look
            table.setRowHeight(25);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(450, 250));

            JOptionPane.showMessageDialog(null, scrollPane,
                    "Notes de " + etudiant.getNom(), JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de l'affichage des notes !");
            e.printStackTrace();
        }
    }



    public static void afficherMoyenne(Utilisateur etudiant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT m.nom AS matiere, m.coefficient, nf.moyenne_finale
            FROM NoteFinale nf
            JOIN Matiere m ON nf.id_matiere = m.id_matiere
            WHERE nf.id_etudiant = ? AND nf.valide = TRUE
        """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, etudiant.getId());
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Matière", "Moyenne", "Coef"}, 0);
            double sommePonderee = 0, sommeCoef = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                double moyenne = rs.getDouble("moyenne_finale");
                double coef = rs.getDouble("coefficient");
                String matiere = rs.getString("matiere");
                model.addRow(new Object[]{matiere, moyenne, coef});

                sommePonderee += moyenne * coef;
                sommeCoef += coef;
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(null, "⚠️ Aucune note validée !");
                return;
            }

            JTable table = new JTable(model);
            double moyenneGenerale = sommeCoef > 0 ? sommePonderee / sommeCoef : 0;

            // ✅ Create a nice panel with the table and the average at the bottom
            JLabel lblMoyenne = new JLabel(
                    "📊 Moyenne Générale : " + new DecimalFormat("#0.00").format(moyenneGenerale),
                    SwingConstants.CENTER
            );
            lblMoyenne.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblMoyenne.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(lblMoyenne, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(null, panel,
                    "Moyenne de " + etudiant.getPrenom() + " " + etudiant.getNom(),
                    JOptionPane.INFORMATION_MESSAGE);

            // ✅ Update status automatically (same logic as before)
            String statut;
            if (moyenneGenerale >= 10) statut = "admis";
            else if (moyenneGenerale >= 5) statut = "redoublant";
            else statut = "exclu";

            String sqlUpdate = "UPDATE Etudiant SET statut = ? WHERE id_etudiant = ?";
            PreparedStatement psUp = conn.prepareStatement(sqlUpdate);
            psUp.setString(1, statut);
            psUp.setInt(2, etudiant.getId());
            psUp.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur lors du calcul de la moyenne !");
            e.printStackTrace();
        }
    }


    public static void afficherStatut(Utilisateur etudiant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT statut FROM Etudiant WHERE id_etudiant = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, etudiant.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String statut = rs.getString("statut");
                JOptionPane.showMessageDialog(null, "Statut de " + etudiant.getNom() + " : " + statut);
            } else {
                JOptionPane.showMessageDialog(null, "Étudiant non trouvé !");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la récupération du statut !");
            e.printStackTrace();
        }
    }

    // ✅ Restored as-is (used for automatic updates)
    public static void mettreAJourStatut(int idEtudiant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlMoyenne = "SELECT AVG(n.note) AS moyenne FROM Note n WHERE n.id_etudiant = ?";
            PreparedStatement psMoyenne = conn.prepareStatement(sqlMoyenne);
            psMoyenne.setInt(1, idEtudiant);
            ResultSet rs = psMoyenne.executeQuery();

            double moyenne = 0;
            if (rs.next()) moyenne = rs.getDouble("moyenne");

            String statut;
            if (moyenne >= 10) statut = "admis";
            else if (moyenne >= 5) statut = "redoublant";
            else statut = "exclu";

            String sqlUpdate = "UPDATE Etudiant SET statut = ? WHERE id_etudiant = ?";
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setString(1, statut);
            psUpdate.setInt(2, idEtudiant);
            psUpdate.executeUpdate();

            System.out.printf("✅ Statut mis à jour : moyenne = %.2f → %s%n", moyenne, statut);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void afficherInfosPerso(Utilisateur etudiant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT u.nom, u.prenom, u.email, e.statut, p.nom AS programme, a.libelle
                FROM Utilisateur u
                JOIN Etudiant e ON u.id_utilisateur = e.id_etudiant
                LEFT JOIN Inscription i ON e.id_etudiant = i.id_etudiant
                LEFT JOIN Programme p ON i.id_programme = p.id_programme
                LEFT JOIN AnneeScolaire a ON i.id_annee = a.id_annee
                WHERE u.id_utilisateur = ?
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, etudiant.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String info = """
                        Nom : %s
                        Prénom : %s
                        Email : %s
                        Programme : %s
                        Année : %s
                        Statut : %s
                        """.formatted(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("programme"),
                        rs.getString("libelle"),
                        rs.getString("statut")
                );
                JOptionPane.showMessageDialog(null, info, "Mes informations", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Aucune information trouvée !");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de l'affichage des informations !");
            e.printStackTrace();
        }
    }

    public static void changerMotDePasse(Utilisateur etudiant) {
        JPasswordField ancienField = new JPasswordField();
        int check = JOptionPane.showConfirmDialog(null, ancienField, "Entrez votre ancien mot de passe",
                JOptionPane.OK_CANCEL_OPTION);
        if (check != JOptionPane.OK_OPTION) return;

        String ancien = new String(ancienField.getPassword());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT mot_de_passe FROM Utilisateur WHERE id_utilisateur = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, etudiant.getId());
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getString("mot_de_passe").equals(ancien)) {
                JPasswordField nouveauField = new JPasswordField();
                JPasswordField confirmField = new JPasswordField();
                Object[] inputs = {"Nouveau mot de passe :", nouveauField, "Confirmez :", confirmField};
                int ok = JOptionPane.showConfirmDialog(null, inputs, "Changer le mot de passe", JOptionPane.OK_CANCEL_OPTION);
                if (ok != JOptionPane.OK_OPTION) return;

                String nouveau = new String(nouveauField.getPassword());
                String confirmation = new String(confirmField.getPassword());

                if (!nouveau.equals(confirmation)) {
                    JOptionPane.showMessageDialog(null, "❌ Les mots de passe ne correspondent pas !");
                    return;
                }

                String updateSql = "UPDATE Utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setString(1, nouveau);
                updatePs.setInt(2, etudiant.getId());
                updatePs.executeUpdate();

                JOptionPane.showMessageDialog(null, "✅ Mot de passe mis à jour !");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Ancien mot de passe incorrect !");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur SQL lors du changement de mot de passe !");
            e.printStackTrace();
        }
    }

    public static void afficherBulletin(Utilisateur etudiant) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
            SELECT m.nom AS matiere, m.coefficient, nf.moyenne_finale
            FROM NoteFinale nf
            JOIN Matiere m ON nf.id_matiere = m.id_matiere
            WHERE nf.id_etudiant = ? AND nf.valide = TRUE
        """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, etudiant.getId());
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Matière", "Coef.", "Moyenne"}, 0);
            double sommePonderee = 0, sommeCoef = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String matiere = rs.getString("matiere");
                double coef = rs.getDouble("coefficient");
                double moyenne = rs.getDouble("moyenne_finale");
                model.addRow(new Object[]{matiere, coef, moyenne});

                sommePonderee += moyenne * coef;
                sommeCoef += coef;
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(null, "⚠️ Aucune note finale validée !");
                return;
            }

            JTable table = new JTable(model);
            double moyenneGenerale = sommeCoef > 0 ? sommePonderee / sommeCoef : 0;

            // ✅ Determine the mention based on the moyenne
            String mention;
            if (moyenneGenerale >= 16) mention = "Très Bien";
            else if (moyenneGenerale >= 14) mention = "Bien";
            else if (moyenneGenerale >= 12) mention = "Assez Bien";
            else if (moyenneGenerale >= 10) mention = "Passable";
            else mention = "Insuffisant";

            // ✅ Get the current status
            String sqlStatut = "SELECT statut FROM Etudiant WHERE id_etudiant = ?";
            PreparedStatement psStatut = conn.prepareStatement(sqlStatut);
            psStatut.setInt(1, etudiant.getId());
            ResultSet rsStatut = psStatut.executeQuery();

            String statut = "-";
            if (rsStatut.next()) {
                statut = rsStatut.getString("statut");
            }

            // ✅ Create a bottom panel for average, status, and mention
            JPanel footer = new JPanel(new GridLayout(3, 1));
            footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

            JLabel lblMoyenne = new JLabel("📊 Moyenne Générale : " + new DecimalFormat("#0.00").format(moyenneGenerale), SwingConstants.CENTER);
            lblMoyenne.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JLabel lblStatut = new JLabel("🎓 Statut : " + statut, SwingConstants.CENTER);
            lblStatut.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JLabel lblMention = new JLabel("🏅 Mention : " + mention, SwingConstants.CENTER);
            lblMention.setFont(new Font("Segoe UI", Font.ITALIC, 14));

            footer.add(lblMoyenne);
            footer.add(lblStatut);
            footer.add(lblMention);

            // ✅ Combine everything
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(footer, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(null, panel,
                    "Bulletin de " + etudiant.getPrenom() + " " + etudiant.getNom(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de l'affichage du bulletin !");
            e.printStackTrace();
        }
    }

}
