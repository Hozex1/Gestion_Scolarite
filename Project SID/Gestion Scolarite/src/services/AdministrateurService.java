package services;

import db.DatabaseConnection;
import models.Utilisateur;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.*;

import java.sql.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class AdministrateurService {

    private static final Scanner sc = new Scanner(System.in);

    public static void afficherUtilisateurs() {
        JFrame frame = new JFrame("📋 Liste des utilisateurs");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);

        // === Top Panel: Filters & Search ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        String[] roles = {"Tous", "etudiant", "enseignant", "secretaire", "chefprogramme", "administrateur"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("🔍 Rechercher");
        JButton btnRefresh = new JButton("🔄 Rafraîchir");

        topPanel.add(new JLabel("Filtrer par rôle :"));
        topPanel.add(cmbRole);
        topPanel.add(new JLabel("Nom / Email :"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnRefresh);

        // === Table ===
        String[] columns = {"ID", "Nom", "Prénom", "Email", "Rôle"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // === Layout ===
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // === Function to load users ===
        Runnable loadUsers = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                SELECT id_utilisateur, nom, prenom, email, role 
                FROM Utilisateur 
                WHERE (LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ?)
            """;

                String roleFilter = cmbRole.getSelectedItem().toString();
                if (!roleFilter.equals("Tous")) {
                    // ✅ Case-insensitive and tolerant for underscores
                    sql += " AND REPLACE(LOWER(role), '_', '') = ?";
                }
                sql += " ORDER BY role, nom";

                PreparedStatement ps = conn.prepareStatement(sql);
                String search = "%" + txtSearch.getText().trim().toLowerCase() + "%";
                ps.setString(1, search);
                ps.setString(2, search);
                ps.setString(3, search);

                if (!roleFilter.equals("Tous")) {
                    ps.setString(4, roleFilter.toLowerCase());
                }

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id_utilisateur"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("role")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(frame,
                            "Aucun utilisateur trouvé pour ce filtre.",
                            "Résultat vide",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame,
                        "❌ Erreur SQL : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        // === Button actions ===
        btnSearch.addActionListener(e -> loadUsers.run());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbRole.setSelectedIndex(0);
            loadUsers.run();
        });

        // === Initial Load ===
        loadUsers.run();

        frame.setVisible(true);
    }


    // =======================
    // 2️⃣ AJOUTER UTILISATEUR (ANY ROLE)
    // =======================
    public static void ajouterUtilisateur() {
        JFrame frame = new JFrame("➕ Ajouter un utilisateur");
        frame.setSize(480, 520);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // === Common Fields ===
        JTextField txtNom = new JTextField();
        JTextField txtPrenom = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtMdp = new JPasswordField();
        String[] roles = {"etudiant", "enseignant", "secretaire", "chefprogramme", "administrateur"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);

        panel.add(new JLabel("Nom :"));
        panel.add(txtNom);
        panel.add(new JLabel("Prénom :"));
        panel.add(txtPrenom);
        panel.add(new JLabel("Email :"));
        panel.add(txtEmail);
        panel.add(new JLabel("Mot de passe :"));
        panel.add(txtMdp);
        panel.add(new JLabel("Rôle :"));
        panel.add(cmbRole);

        // === Dedicated Origine scolaire field (for etudiant) ===
        JLabel lblOrigine = new JLabel("Origine scolaire :");
        JTextField txtOrigine = new JTextField();
        lblOrigine.setVisible(false);
        txtOrigine.setVisible(false);
        panel.add(lblOrigine);
        panel.add(txtOrigine);

        // === Dynamic Extra Field (for other roles) ===
        JLabel lblExtra = new JLabel("Info supplémentaire :");
        JTextField txtExtra = new JTextField();
        panel.add(lblExtra);
        panel.add(txtExtra);

        // === Panels for inscription (only for etudiant) ===
        JLabel lblProgramme = new JLabel("Programme :");
        JComboBox<String> cmbProgramme = new JComboBox<>();
        JLabel lblAnnee = new JLabel("Année scolaire :");
        JComboBox<String> cmbAnnee = new JComboBox<>();

        lblProgramme.setVisible(false);
        cmbProgramme.setVisible(false);
        lblAnnee.setVisible(false);
        cmbAnnee.setVisible(false);

        panel.add(lblProgramme);
        panel.add(cmbProgramme);
        panel.add(lblAnnee);
        panel.add(cmbAnnee);

        // === Load programme & année options ===
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement psProg = conn.prepareStatement("SELECT id_programme, nom FROM Programme");
            ResultSet rsProg = psProg.executeQuery();
            while (rsProg.next()) cmbProgramme.addItem(rsProg.getInt("id_programme") + " - " + rsProg.getString("nom"));

            PreparedStatement psAn = conn.prepareStatement("SELECT id_annee, libelle FROM AnneeScolaire");
            ResultSet rsAn = psAn.executeQuery();
            while (rsAn.next()) cmbAnnee.addItem(rsAn.getInt("id_annee") + " - " + rsAn.getString("libelle"));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "⚠️ Erreur chargement des programmes/années : " + ex.getMessage(), "Erreur", JOptionPane.WARNING_MESSAGE);
        }

        // === Change label / visibility dynamically based on role ===
        cmbRole.addActionListener(e -> {
            String role = cmbRole.getSelectedItem().toString();

            boolean isEtudiant = role.equals("etudiant");
            lblProgramme.setVisible(isEtudiant);
            cmbProgramme.setVisible(isEtudiant);
            lblAnnee.setVisible(isEtudiant);
            cmbAnnee.setVisible(isEtudiant);

            lblOrigine.setVisible(isEtudiant);
            txtOrigine.setVisible(isEtudiant);

            // extra field used for non-student roles
            boolean otherRole = !isEtudiant;
            lblExtra.setVisible(otherRole);
            txtExtra.setVisible(otherRole);

            if (isEtudiant) {
                lblExtra.setText("Info supplémentaire :"); // keep label sensible if hidden later
            } else {
                switch (role) {
                    case "enseignant" -> lblExtra.setText("Grade :");
                    case "chefprogramme" -> lblExtra.setText("Département :");
                    case "administrateur" -> lblExtra.setText("Niveau accès (normal/super) :");
                    default -> lblExtra.setText("Info supplémentaire :");
                }
            }

            // clear fields when switching role
            txtExtra.setText("");
            txtOrigine.setText("");
        });

        // === Buttons ===
        JButton btnSave = new JButton("✅ Ajouter");
        JButton btnCancel = new JButton("❌ Annuler");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        frame.add(panel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // === Save Action ===
        btnSave.addActionListener(ev -> {
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String motDePasse = new String(txtMdp.getPassword()).trim();
            String role = cmbRole.getSelectedItem().toString();
            String extra = txtExtra.getText().trim();
            String origine = txtOrigine.getText().trim();

            // Basic validation
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "⚠️ Tous les champs obligatoires doivent être remplis !", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(frame, "❌ Email invalide !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (motDePasse.length() < 6) {
                JOptionPane.showMessageDialog(frame, "⚠️ Le mot de passe doit contenir au moins 6 caractères !", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                // Insert utilisateur
                String sqlUser = """
                INSERT INTO Utilisateur (nom, prenom, email, mot_de_passe, role)
                VALUES (?, ?, ?, ?, ?)
            """;
                PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
                psUser.setString(1, nom);
                psUser.setString(2, prenom);
                psUser.setString(3, email);
                psUser.setString(4, motDePasse);
                psUser.setString(5, role);
                psUser.executeUpdate();

                ResultSet rs = psUser.getGeneratedKeys();
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(frame, "❌ Erreur lors de la création de l'utilisateur !", "Erreur", JOptionPane.ERROR_MESSAGE);
                    conn.rollback();
                    return;
                }
                int idUtilisateur = rs.getInt(1);

                // === Role-specific handling ===
                switch (role) {
                    case "etudiant" -> {
                        if (origine.isEmpty()) origine = "Non spécifié";
                        PreparedStatement psEtu = conn.prepareStatement(
                                "INSERT INTO Etudiant (id_etudiant, origine_scolaire, statut) VALUES (?, ?, 'admis')");
                        psEtu.setInt(1, idUtilisateur);
                        psEtu.setString(2, origine);
                        psEtu.executeUpdate();

                        // Optional inscription if programme & annee selected
                        if (cmbProgramme.getSelectedItem() != null && cmbAnnee.getSelectedItem() != null) {
                            int idProgramme = Integer.parseInt(cmbProgramme.getSelectedItem().toString().split(" - ")[0]);
                            int idAnnee = Integer.parseInt(cmbAnnee.getSelectedItem().toString().split(" - ")[0]);

                            PreparedStatement psIns = conn.prepareStatement(
                                    "INSERT INTO Inscription (id_etudiant, id_programme, id_annee) VALUES (?, ?, ?)");
                            psIns.setInt(1, idUtilisateur);
                            psIns.setInt(2, idProgramme);
                            psIns.setInt(3, idAnnee);
                            psIns.executeUpdate();
                        }
                    }
                    case "enseignant" -> {
                        PreparedStatement psEns = conn.prepareStatement("INSERT INTO Enseignant (id_enseignant, grade) VALUES (?, ?)");
                        psEns.setInt(1, idUtilisateur);
                        psEns.setString(2, extra.isEmpty() ? "Inconnu" : extra);
                        psEns.executeUpdate();
                    }
                    case "chefprogramme" -> {
                        PreparedStatement psChef = conn.prepareStatement("INSERT INTO ChefProgramme (id_chefprog, departement) VALUES (?, ?)");
                        psChef.setInt(1, idUtilisateur);
                        psChef.setString(2, extra.isEmpty() ? "Non spécifié" : extra);
                        psChef.executeUpdate();
                    }
                    case "secretaire" -> {
                        PreparedStatement psSec = conn.prepareStatement("INSERT INTO Secretaire (id_secretaire) VALUES (?)");
                        psSec.setInt(1, idUtilisateur);
                        psSec.executeUpdate();
                    }
                    case "administrateur" -> {
                        PreparedStatement psAdm = conn.prepareStatement("INSERT INTO Administrateur (id_admin, niveau_acces) VALUES (?, ?)");
                        psAdm.setInt(1, idUtilisateur);
                        psAdm.setString(2, extra.isEmpty() ? "normal" : extra);
                        psAdm.executeUpdate();
                    }
                }

                conn.commit();
                JOptionPane.showMessageDialog(frame, "✅ Utilisateur ajouté avec succès !");
                frame.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        btnCancel.addActionListener(e -> frame.dispose());
    }


    // =======================
    // 3️⃣ MODIFIER UTILISATEUR
    // =======================
    public static void modifierUtilisateur() {
        JFrame frame = new JFrame("✏️ Modifier un utilisateur");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // === Top Panel (Filters & Search) ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        String[] roles = {"Tous", "etudiant", "enseignant", "secretaire", "chefprogramme", "administrateur"};
        JComboBox<String> cmbRoleFilter = new JComboBox<>(roles);
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("🔍 Rechercher");
        JButton btnRefresh = new JButton("🔄 Rafraîchir");
        topPanel.add(new JLabel("Filtrer par rôle :"));
        topPanel.add(cmbRoleFilter);
        topPanel.add(new JLabel("Nom / Email :"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnRefresh);

        // === Table listing all users ===
        String[] columns = {"ID", "Nom", "Prénom", "Email", "Rôle"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // === Right Panel: Edit Form ===
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Modifier l'utilisateur sélectionné"));
        JTextField txtNom = new JTextField();
        JTextField txtPrenom = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtMdp = new JPasswordField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"etudiant", "enseignant", "secretaire", "chefprogramme", "administrateur"});
        JTextField txtExtra = new JTextField();
        JLabel lblExtra = new JLabel("Info supplémentaire :");

        formPanel.add(new JLabel("Nom :"));
        formPanel.add(txtNom);
        formPanel.add(new JLabel("Prénom :"));
        formPanel.add(txtPrenom);
        formPanel.add(new JLabel("Email :"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Mot de passe :"));
        formPanel.add(txtMdp);
        formPanel.add(new JLabel("Rôle :"));
        formPanel.add(cmbRole);
        formPanel.add(lblExtra);
        formPanel.add(txtExtra);

        // === Buttons ===
        JButton btnLoad = new JButton("📥 Charger sélection");
        JButton btnSave = new JButton("💾 Enregistrer");
        JButton btnCancel = new JButton("❌ Fermer");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.add(btnLoad);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(formPanel, BorderLayout.EAST);
        frame.add(btnPanel, BorderLayout.SOUTH);

        final int[] currentId = {0};
        final String[] oldRole = {""};

        // === Function to load users ===
        Runnable loadUsers = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                SELECT id_utilisateur, nom, prenom, email, role
                FROM Utilisateur
                WHERE (nom LIKE ? OR prenom LIKE ? OR email LIKE ?)
            """;
                String roleFilter = cmbRoleFilter.getSelectedItem().toString();
                if (!roleFilter.equals("Tous")) sql += " AND role = ?";
                sql += " ORDER BY role, nom";

                PreparedStatement ps = conn.prepareStatement(sql);
                String search = "%" + txtSearch.getText().trim() + "%";
                ps.setString(1, search);
                ps.setString(2, search);
                ps.setString(3, search);
                if (!roleFilter.equals("Tous")) ps.setString(4, roleFilter);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id_utilisateur"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("role")
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + e.getMessage());
            }
        };

        // === Load user list initially ===
        loadUsers.run();
        btnSearch.addActionListener(e -> loadUsers.run());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbRoleFilter.setSelectedIndex(0);
            loadUsers.run();
        });

        // === Role label update ===
        cmbRole.addActionListener(e -> {
            switch (cmbRole.getSelectedItem().toString()) {
                case "etudiant" -> lblExtra.setText("Origine scolaire :");
                case "enseignant" -> lblExtra.setText("Grade :");
                case "chefprogramme" -> lblExtra.setText("Département :");
                case "administrateur" -> lblExtra.setText("Niveau accès :");
                default -> lblExtra.setText("Info supplémentaire :");
            }
        });

        // === Load user data ===
        btnLoad.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "⚠️ Sélectionnez un utilisateur d'abord !");
                return;
            }
            currentId[0] = (int) table.getValueAt(row, 0);
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Utilisateur WHERE id_utilisateur=?");
                ps.setInt(1, currentId[0]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtNom.setText(rs.getString("nom"));
                    txtPrenom.setText(rs.getString("prenom"));
                    txtEmail.setText(rs.getString("email"));
                    txtMdp.setText(rs.getString("mot_de_passe"));
                    cmbRole.setSelectedItem(rs.getString("role"));
                    oldRole[0] = rs.getString("role");

                    // Load role-specific info
                    String role = rs.getString("role");
                    String sqlExtra = switch (role) {
                        case "etudiant" -> "SELECT origine_scolaire FROM Etudiant WHERE id_etudiant=?";
                        case "enseignant" -> "SELECT grade FROM Enseignant WHERE id_enseignant=?";
                        case "chefprogramme" -> "SELECT departement FROM ChefProgramme WHERE id_chefprog=?";
                        case "administrateur" -> "SELECT niveau_acces FROM Administrateur WHERE id_admin=?";
                        default -> null;
                    };
                    if (sqlExtra != null) {
                        PreparedStatement psExtra = conn.prepareStatement(sqlExtra);
                        psExtra.setInt(1, currentId[0]);
                        ResultSet rse = psExtra.executeQuery();
                        if (rse.next()) txtExtra.setText(rse.getString(1));
                    } else txtExtra.setText("");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + ex.getMessage());
            }
        });

        // === Save changes ===
        btnSave.addActionListener(e -> {
            if (currentId[0] == 0) {
                JOptionPane.showMessageDialog(frame, "⚠️ Aucun utilisateur chargé !");
                return;
            }

            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String mdp = new String(txtMdp.getPassword()).trim();
            String newRole = cmbRole.getSelectedItem().toString();
            String extra = txtExtra.getText().trim();

            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(frame, "❌ Email invalide !");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(frame, "Confirmer les modifications ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement psUp = conn.prepareStatement("""
                UPDATE Utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, role=? WHERE id_utilisateur=?
            """);
                psUp.setString(1, nom);
                psUp.setString(2, prenom);
                psUp.setString(3, email);
                psUp.setString(4, mdp);
                psUp.setString(5, newRole);
                psUp.setInt(6, currentId[0]);
                psUp.executeUpdate();

                // Handle role-specific updates
                if (!newRole.equalsIgnoreCase(oldRole[0])) {
                    String[][] tables = {
                            {"Etudiant", "id_etudiant"},
                            {"Enseignant", "id_enseignant"},
                            {"ChefProgramme", "id_chefprog"},
                            {"Secretaire", "id_secretaire"},
                            {"Administrateur", "id_admin"}
                    };
                    for (String[] t : tables) {
                        PreparedStatement psDel = conn.prepareStatement("DELETE FROM " + t[0] + " WHERE " + t[1] + "=?");
                        psDel.setInt(1, currentId[0]);
                        psDel.executeUpdate();
                    }

                    switch (newRole) {
                        case "etudiant" -> {
                            PreparedStatement ps = conn.prepareStatement("INSERT INTO Etudiant VALUES (?, ?, 'admis')");
                            ps.setInt(1, currentId[0]);
                            ps.setString(2, extra);
                            ps.executeUpdate();
                        }
                        case "enseignant" -> {
                            PreparedStatement ps = conn.prepareStatement("INSERT INTO Enseignant VALUES (?, ?)");
                            ps.setInt(1, currentId[0]);
                            ps.setString(2, extra);
                            ps.executeUpdate();
                        }
                        case "chefprogramme" -> {
                            PreparedStatement ps = conn.prepareStatement("INSERT INTO ChefProgramme VALUES (?, ?)");
                            ps.setInt(1, currentId[0]);
                            ps.setString(2, extra);
                            ps.executeUpdate();
                        }
                        case "administrateur" -> {
                            PreparedStatement ps = conn.prepareStatement("INSERT INTO Administrateur VALUES (?, ?)");
                            ps.setInt(1, currentId[0]);
                            ps.setString(2, extra.isEmpty() ? "normal" : extra);
                            ps.executeUpdate();
                        }
                        case "secretaire" -> {
                            PreparedStatement ps = conn.prepareStatement("INSERT INTO Secretaire VALUES (?)");
                            ps.setInt(1, currentId[0]);
                            ps.executeUpdate();
                        }
                    }
                } else {
                    String updateExtra = switch (newRole) {
                        case "etudiant" -> "UPDATE Etudiant SET origine_scolaire=? WHERE id_etudiant=?";
                        case "enseignant" -> "UPDATE Enseignant SET grade=? WHERE id_enseignant=?";
                        case "chefprogramme" -> "UPDATE ChefProgramme SET departement=? WHERE id_chefprog=?";
                        case "administrateur" -> "UPDATE Administrateur SET niveau_acces=? WHERE id_admin=?";
                        default -> null;
                    };
                    if (updateExtra != null) {
                        PreparedStatement ps = conn.prepareStatement(updateExtra);
                        ps.setString(1, extra);
                        ps.setInt(2, currentId[0]);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                JOptionPane.showMessageDialog(frame, "✅ Modifications enregistrées !");
                loadUsers.run();
                conn.setAutoCommit(true);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> frame.dispose());
        frame.setVisible(true);
    }


    // =======================
    // 4️⃣ SUPPRIMER UTILISATEUR
    // =======================
    public static void supprimerUtilisateur() {
        JFrame frame = new JFrame("🗑️ Supprimer un utilisateur");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // === Top Panel (Filter & Search) ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        String[] roles = {"Tous", "etudiant", "enseignant", "secretaire", "chefprogramme", "administrateur"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("🔍 Rechercher");
        JButton btnRefresh = new JButton("🔄 Rafraîchir");

        topPanel.add(new JLabel("Filtrer par rôle :"));
        topPanel.add(cmbRole);
        topPanel.add(new JLabel("Nom / Email :"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnRefresh);

        // === Table ===
        String[] columns = {"ID", "Nom", "Prénom", "Email", "Rôle"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // === Buttons ===
        JButton btnDelete = new JButton("🗑️ Supprimer l'utilisateur sélectionné");
        JButton btnCancel = new JButton("❌ Fermer");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnCancel);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // === Load Users Function ===
        Runnable loadUsers = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                SELECT id_utilisateur, nom, prenom, email, role 
                FROM Utilisateur 
                WHERE (nom LIKE ? OR prenom LIKE ? OR email LIKE ?)
            """;

                String roleFilter = cmbRole.getSelectedItem().toString();
                if (!roleFilter.equals("Tous")) sql += " AND role = ?";
                sql += " ORDER BY role, nom";

                PreparedStatement ps = conn.prepareStatement(sql);
                String search = "%" + txtSearch.getText().trim() + "%";
                ps.setString(1, search);
                ps.setString(2, search);
                ps.setString(3, search);
                if (!roleFilter.equals("Tous")) ps.setString(4, roleFilter);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id_utilisateur"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("role")
                    });
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        };

        // === Button Actions ===
        btnSearch.addActionListener(e -> loadUsers.run());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbRole.setSelectedIndex(0);
            loadUsers.run();
        });

        // === Initial Load ===
        loadUsers.run();

        // === Delete Action ===
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "⚠️ Sélectionnez un utilisateur à supprimer !");
                return;
            }

            int id = (int) table.getValueAt(row, 0);
            String nom = table.getValueAt(row, 1).toString();
            String prenom = table.getValueAt(row, 2).toString();
            String role = table.getValueAt(row, 4).toString();

            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Voulez-vous vraiment supprimer l'utilisateur suivant ?\n\n" +
                            "👤 " + nom + " " + prenom + "\n" +
                            "🎭 Rôle : " + role +
                            "\n\n⚠️ Toutes les données liées seront également supprimées.",
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    // 1️⃣ Delete related data depending on role
                    switch (role.toLowerCase()) {
                        case "etudiant" -> {
                            // delete inscriptions first
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Inscription WHERE id_etudiant = ?")) {
                                ps.setInt(1, id);
                                ps.executeUpdate();
                            }
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Etudiant WHERE id_etudiant = ?")) {
                                ps.setInt(1, id);
                                ps.executeUpdate();
                            }
                        }

                        case "enseignant" -> {
                            // delete from enseignant_matiere and enseignant
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Enseignant_Matiere WHERE id_enseignant = ?")) {
                                ps.setInt(1, id);
                                ps.executeUpdate();
                            }
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Enseignant WHERE id_enseignant = ?")) {
                                ps.setInt(1, id);
                                ps.executeUpdate();
                            }
                        }

                        case "chefprogramme" -> {
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ChefProgramme WHERE id_chefprog = ?")) {
                                ps.setInt(1, id);
                                ps.executeUpdate();
                            }
                        }

                        case "secretaire" -> {
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Secretaire WHERE id_secretaire = ?")) {
                                ps.setInt(1, id);
                                ps.executeUpdate();
                            }
                        }

                        case "administrateur" -> {
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Administrateur WHERE id_admin = ?")) {
                                ps.setInt(1, id);
                                ps.executeUpdate();
                            }
                        }
                    }

                    // 2️⃣ Delete from Utilisateur (parent)
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Utilisateur WHERE id_utilisateur = ?")) {
                        ps.setInt(1, id);
                        int rows = ps.executeUpdate();
                        if (rows > 0)
                            JOptionPane.showMessageDialog(frame, "✅ Utilisateur supprimé avec succès !");
                        else
                            JOptionPane.showMessageDialog(frame, "⚠️ Utilisateur introuvable !");
                    }

                    conn.commit();
                    loadUsers.run();

                } catch (SQLException ex) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "❌ Erreur de connexion : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> frame.dispose());
        frame.setVisible(true);
    }


    // =======================
    // 5️⃣ MODIFIER DROITS D'ACCÈS
    // =======================
    public static void modifierDroitsAcces(Utilisateur currentAdmin) {
        JFrame frame = new JFrame("🛠️ Modifier les droits d'accès");
        frame.setSize(650, 420);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // === Table for listing administrators ===
        String[] columns = {"ID", "Nom", "Prénom", "Email", "Niveau d'accès"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // === Bottom buttons ===
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnEdit = new JButton("✏️ Modifier droits");
        JButton btnRefresh = new JButton("🔄 Rafraîchir");
        JButton btnClose = new JButton("❌ Fermer");
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnClose);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // === Load administrators from DB ===
        Runnable loadAdmins = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                SELECT u.id_utilisateur, u.nom, u.prenom, u.email, a.niveau_acces
                FROM Utilisateur u
                JOIN Administrateur a ON u.id_utilisateur = a.id_admin
                ORDER BY a.niveau_acces DESC, u.nom
            """;
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    // If currentAdmin is a "normal" admin we will hide super admins
                    boolean hideSuper = false;
                    if (currentAdmin != null && "administrateur".equalsIgnoreCase(currentAdmin.getRole())) {
                        // check current admin level
                        String checkSql = "SELECT niveau_acces FROM Administrateur WHERE id_admin = ?";
                        try (PreparedStatement psLvl = conn.prepareStatement(checkSql)) {
                            psLvl.setInt(1, currentAdmin.getId()); // <-- use getId()
                            try (ResultSet rsLvl = psLvl.executeQuery()) {
                                if (rsLvl.next()) {
                                    String lvl = rsLvl.getString("niveau_acces");
                                    hideSuper = "normal".equalsIgnoreCase(lvl);
                                }
                            }
                        }
                    }

                    while (rs.next()) {
                        String niveau = rs.getString("niveau_acces");
                        if (hideSuper && "super".equalsIgnoreCase(niveau)) {
                            continue; // normal admin should not see super admins
                        }
                        model.addRow(new Object[]{
                                rs.getInt("id_utilisateur"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                niveau
                        });
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame,
                        "❌ Erreur SQL : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        };

        // === Edit button ===
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "⚠️ Sélectionnez un administrateur à modifier !");
                return;
            }

            int id = (int) table.getValueAt(row, 0);
            String nom = table.getValueAt(row, 1).toString();
            String prenom = table.getValueAt(row, 2).toString();
            String currentNiveau = table.getValueAt(row, 4).toString();

            // Prevent a normal admin from editing a super admin
            if (currentNiveau.equalsIgnoreCase("super")) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sqlCheck = "SELECT niveau_acces FROM Administrateur WHERE id_admin = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                        ps.setInt(1, currentAdmin.getId()); // <-- use getId()
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && "normal".equalsIgnoreCase(rs.getString("niveau_acces"))) {
                                JOptionPane.showMessageDialog(frame,
                                        "🚫 Vous n'avez pas l'autorisation de modifier un administrateur SUPER.",
                                        "Accès refusé", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Erreur SQL : " + ex.getMessage());
                    return;
                }
            }

            // === Dialog to change level ===
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.add(new JLabel("Administrateur :"));
            panel.add(new JLabel(nom + " " + prenom));
            panel.add(new JLabel("Niveau actuel :"));
            panel.add(new JLabel(currentNiveau));
            panel.add(new JLabel("Nouveau niveau :"));
            String[] niveaux = {"normal", "super"};
            JComboBox<String> cmbNiveau = new JComboBox<>(niveaux);
            cmbNiveau.setSelectedItem(currentNiveau);
            panel.add(cmbNiveau);

            int result = JOptionPane.showConfirmDialog(
                    frame,
                    panel,
                    "Modifier les droits d'accès",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String newNiveau = cmbNiveau.getSelectedItem().toString();
                if (newNiveau.equalsIgnoreCase(currentNiveau)) {
                    JOptionPane.showMessageDialog(frame, "ℹ️ Aucun changement détecté.");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        frame,
                        "Confirmez-vous la modification du niveau d'accès de :\n\n" +
                                "👤 " + nom + " " + prenom + "\n" +
                                "➡️ De " + currentNiveau + " vers " + newNiveau + " ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) return;

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "UPDATE Administrateur SET niveau_acces=? WHERE id_admin=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, newNiveau);
                        ps.setInt(2, id);
                        int rows = ps.executeUpdate();
                        if (rows > 0) {
                            JOptionPane.showMessageDialog(frame, "✅ Droits d'accès mis à jour !");
                            loadAdmins.run();
                        } else {
                            JOptionPane.showMessageDialog(frame, "⚠️ Administrateur introuvable !");
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRefresh.addActionListener(e -> loadAdmins.run());
        btnClose.addActionListener(e -> frame.dispose());

        // === Initial load ===
        loadAdmins.run();
        frame.setVisible(true);
    }



    // =======================
    // 6️⃣ SAUVEGARDE / RESTAURATION
    // =======================
    public static void sauvegarderDonnees() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("💾 Choisir l’emplacement du fichier de sauvegarde");

        // default backup file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileChooser.setSelectedFile(new java.io.File("backup_" + timestamp + ".sql"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "⚠️ Sauvegarde annulée.");
            return;
        }

        String backupFile = fileChooser.getSelectedFile().getAbsolutePath();

        // Database credentials — adjust for your setup
        String dbUser = "root";
        String dbPassword = "YOUR_PASSWORD"; // ⚠️ replace or externalize securely
        String dbName = "scolarite_db";

        try {
            // Detect OS and build correct command
            String command;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows cmd
                command = String.format("cmd /c mysqldump -u%s -p%s %s > \"%s\"",
                        dbUser, dbPassword, dbName, backupFile);
            } else {
                // Linux/Mac bash
                command = String.format("bash -c \"mysqldump -u%s -p%s %s > '%s'\"",
                        dbUser, dbPassword, dbName, backupFile);
            }

            // Show progress
            JOptionPane.showMessageDialog(null, "⏳ Sauvegarde en cours... Veuillez patienter.");

            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            int result = process.waitFor();

            if (result == 0) {
                JOptionPane.showMessageDialog(null,
                        "✅ Sauvegarde réussie !\n\nFichier enregistré sous :\n" + backupFile,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "❌ Échec de la sauvegarde ! Vérifiez vos identifiants MySQL.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Erreur pendant la sauvegarde : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    public static void restaurerDonnees(String backupFilePath) {
        // If no file path was passed (like from AdminDashboard), ask the user
        if (backupFilePath == null || backupFilePath.isEmpty()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("📂 Sélectionner un fichier de sauvegarde (.sql)");
            int result = fileChooser.showOpenDialog(null);

            if (result != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(null, "⚠️ Restauration annulée.");
                return;
            }

            backupFilePath = fileChooser.getSelectedFile().getAbsolutePath();
        }

        // Confirm restoration
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "⚠️ Cette opération remplacera les données actuelles de la base de données.\n\n" +
                        "Souhaitez-vous vraiment continuer ?\n\nFichier sélectionné :\n" + backupFilePath,
                "Confirmation de restauration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Database credentials
        String dbUser = "root";
        String dbPassword = "YOUR_PASSWORD"; // ⚠️ update or externalize safely
        String dbName = "scolarite_db";

        try {
            // Detect OS and prepare the appropriate restore command
            String command;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows command prompt
                command = String.format("cmd /c mysql -u%s -p%s %s < \"%s\"",
                        dbUser, dbPassword, dbName, backupFilePath);
            } else {
                // Linux/Mac bash
                command = String.format("bash -c \"mysql -u%s -p%s %s < '%s'\"",
                        dbUser, dbPassword, dbName, backupFilePath);
            }

            JOptionPane.showMessageDialog(null, "⏳ Restauration en cours... Veuillez patienter.");

            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int result = process.waitFor();

            if (result == 0) {
                JOptionPane.showMessageDialog(null,
                        "✅ Restauration réussie depuis :\n" + backupFilePath,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "❌ Échec de la restauration ! Vérifiez le fichier ou les identifiants MySQL.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Erreur pendant la restauration : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    // =======================
    // 7️⃣ RAPPORTS ET STATISTIQUES
    // =======================
    public static void genererRapportsEtStatistiques() {
        JFrame frame = new JFrame("📊 Rapports et Statistiques");
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        JTextArea txtStats = new JTextArea();
        txtStats.setEditable(false);
        txtStats.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtStats.setBorder(BorderFactory.createTitledBorder("📈 Statistiques globales"));

        JPanel chartPanelContainer = new JPanel(new BorderLayout());
        chartPanelContainer.setBorder(BorderFactory.createTitledBorder("📊 Répartition par programme"));

        JButton btnClose = new JButton("Fermer");
        btnClose.addActionListener(e -> frame.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnClose);

        frame.add(new JScrollPane(txtStats), BorderLayout.NORTH);
        frame.add(chartPanelContainer, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // === Calcul des statistiques globales ===
            int total = getCount(conn, "SELECT COUNT(*) FROM Etudiant");
            int admis = getCount(conn, "SELECT COUNT(*) FROM Etudiant WHERE statut='admis'");
            int redoublants = getCount(conn, "SELECT COUNT(*) FROM Etudiant WHERE statut='redoublant'");
            int exclus = getCount(conn, "SELECT COUNT(*) FROM Etudiant WHERE statut='exclu'");

            StringBuilder sb = new StringBuilder("=== Statistiques globales ===\n\n");
            if (total > 0) {
                sb.append(String.format("Taux de réussite   : %.2f%% (%d / %d)%n", (admis * 100.0) / total, admis, total));
                sb.append(String.format("Taux de redoublement: %.2f%% (%d / %d)%n", (redoublants * 100.0) / total, redoublants, total));
                sb.append(String.format("Taux d'exclusion   : %.2f%% (%d / %d)%n", (exclus * 100.0) / total, exclus, total));
            } else {
                sb.append("Aucun étudiant trouvé.");
            }
            txtStats.setText(sb.toString());

            // === Création du jeu de données pour le graphique ===
            DefaultPieDataset dataset = new DefaultPieDataset();
            String sql = """
            SELECT p.nom AS programme, COUNT(i.id_etudiant) AS total
            FROM Inscription i
            JOIN Programme p ON i.id_programme = p.id_programme
            GROUP BY p.nom
        """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dataset.setValue(rs.getString("programme"), rs.getInt("total"));
            }

            // === Création du graphique circulaire ===
            JFreeChart chart = ChartFactory.createPieChart(
                    "Répartition des étudiants par programme",
                    dataset,
                    true,
                    true,
                    false
            );

            // Personnalisation
            chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));
            chart.setBackgroundPaint(Color.LIGHT_GRAY);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            plot.setCircular(true);
            plot.setBackgroundPaint(new Color(230, 230, 230));
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1} étudiants)"));

            // Ajout au panel
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 400));
            chartPanelContainer.add(chartPanel, BorderLayout.CENTER);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "❌ Erreur SQL : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    private static int getCount(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

}