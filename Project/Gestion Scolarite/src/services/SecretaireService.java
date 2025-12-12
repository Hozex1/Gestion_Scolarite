package services;

import db.DatabaseConnection;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;

public class SecretaireService {

    public static void ajouterEtudiant() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // === Fetch Programmes ===
            JComboBox<String> comboProgramme = new JComboBox<>();
            ResultSet rsProg = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rsProg.next()) {
                comboProgramme.addItem(rsProg.getInt("id_programme") + " - " + rsProg.getString("nom"));
            }

            // === Fetch Années Scolaires ===
            JComboBox<String> comboAnnee = new JComboBox<>();
            ResultSet rsAnnee = conn.createStatement().executeQuery("SELECT id_annee, libelle FROM AnneeScolaire");
            while (rsAnnee.next()) {
                comboAnnee.addItem(rsAnnee.getInt("id_annee") + " - " + rsAnnee.getString("libelle"));
            }

            // === Input Fields ===
            JTextField txtNom = new JTextField();
            JTextField txtPrenom = new JTextField();
            JTextField txtEmail = new JTextField();
            JPasswordField txtMdp = new JPasswordField();
            JTextField txtOrigine = new JTextField();
            JComboBox<String> comboStatut = new JComboBox<>(new String[]{"admis", "redoublant", "exclu"});

            // === Form Layout ===
            JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            panel.add(new JLabel("Nom :"));
            panel.add(txtNom);
            panel.add(new JLabel("Prénom :"));
            panel.add(txtPrenom);
            panel.add(new JLabel("Email :"));
            panel.add(txtEmail);
            panel.add(new JLabel("Mot de passe :"));
            panel.add(txtMdp);
            panel.add(new JLabel("Origine scolaire :"));
            panel.add(txtOrigine);
            panel.add(new JLabel("Statut :"));
            panel.add(comboStatut);
            panel.add(new JLabel("Programme :"));
            panel.add(comboProgramme);
            panel.add(new JLabel("Année scolaire :"));
            panel.add(comboAnnee);

            int result = JOptionPane.showConfirmDialog(null, panel,
                    "➕ Ajouter un nouvel étudiant", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return;

            // === Validate Input ===
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String mdp = new String(txtMdp.getPassword()).trim();
            String origine = txtOrigine.getText().trim();
            String statut = comboStatut.getSelectedItem().toString();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty() || origine.isEmpty()) {
                JOptionPane.showMessageDialog(null, "⚠️ Tous les champs doivent être remplis !", "Erreur", JOptionPane.ERROR_MESSAGE);
                ajouterEtudiant(); // reopen form
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(null, "⚠️ L’adresse email doit contenir '@' et '.'", "Erreur", JOptionPane.ERROR_MESSAGE);
                ajouterEtudiant(); // reopen form
                return;
            }

            if (mdp.length() < 6) {
                JOptionPane.showMessageDialog(null, "⚠️ Le mot de passe doit contenir au moins 6 caractères !", "Erreur", JOptionPane.ERROR_MESSAGE);
                ajouterEtudiant(); // reopen form
                return;
            }

            // === IDs extraction ===
            int idProgramme = Integer.parseInt(comboProgramme.getSelectedItem().toString().split(" - ")[0]);
            int idAnnee = Integer.parseInt(comboAnnee.getSelectedItem().toString().split(" - ")[0]);

            // === Step 1: Insert into Utilisateur ===
            String sqlUser = "INSERT INTO Utilisateur (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, 'etudiant')";
            PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            psUser.setString(1, nom);
            psUser.setString(2, prenom);
            psUser.setString(3, email);
            psUser.setString(4, mdp);
            psUser.executeUpdate();

            ResultSet rsKeys = psUser.getGeneratedKeys();
            if (!rsKeys.next()) {
                JOptionPane.showMessageDialog(null, "❌ Erreur : Impossible de créer l’utilisateur !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idUtilisateur = rsKeys.getInt(1);

            // === Step 2: Insert into Etudiant ===
            String sqlEtudiant = "INSERT INTO Etudiant (id_etudiant, origine_scolaire, statut) VALUES (?, ?, ?)";
            PreparedStatement psEtudiant = conn.prepareStatement(sqlEtudiant);
            psEtudiant.setInt(1, idUtilisateur);
            psEtudiant.setString(2, origine);
            psEtudiant.setString(3, statut);
            psEtudiant.executeUpdate();

            // === Step 3: Insert into Inscription ===
            String sqlInscription = "INSERT INTO Inscription (id_etudiant, id_programme, id_annee) VALUES (?, ?, ?)";
            PreparedStatement psIns = conn.prepareStatement(sqlInscription);
            psIns.setInt(1, idUtilisateur);
            psIns.setInt(2, idProgramme);
            psIns.setInt(3, idAnnee);
            psIns.executeUpdate();

            // === Success ===
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:green;'>✅ Étudiant ajouté avec succès !</b></html>",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:red;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void modifierUtilisateur() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // --- Load all users (excluding admins) ---
            String sqlUsers = """
            SELECT id_utilisateur, nom, prenom, email, role
            FROM Utilisateur
            WHERE role != 'administrateur'
            ORDER BY nom
        """;
            ResultSet rs = conn.createStatement().executeQuery(sqlUsers);

            // Build dropdown list
            JComboBox<String> comboUsers = new JComboBox<>();
            while (rs.next()) {
                comboUsers.addItem(rs.getInt("id_utilisateur") + " - " +
                        rs.getString("nom") + " " + rs.getString("prenom") +
                        " (" + rs.getString("role") + ")");
            }

            if (comboUsers.getItemCount() == 0) {
                JOptionPane.showMessageDialog(null, "⚠️ Aucun utilisateur à modifier !");
                return;
            }

            // --- Choose user ---
            JPanel choosePanel = new JPanel(new GridLayout(2, 1, 10, 10));
            choosePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            choosePanel.add(new JLabel("Sélectionnez un utilisateur à modifier :"));
            choosePanel.add(comboUsers);

            int choix = JOptionPane.showConfirmDialog(null, choosePanel,
                    "✏️ Choisir un utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (choix != JOptionPane.OK_OPTION) return;

            int idUtilisateur = Integer.parseInt(comboUsers.getSelectedItem().toString().split(" - ")[0]);

            // --- Load current user info ---
            String sqlGet = "SELECT nom, prenom, email, mot_de_passe, role FROM Utilisateur WHERE id_utilisateur = ?";
            PreparedStatement psGet = conn.prepareStatement(sqlGet);
            psGet.setInt(1, idUtilisateur);
            ResultSet rsUser = psGet.executeQuery();

            if (!rsUser.next()) {
                JOptionPane.showMessageDialog(null, "⚠️ Utilisateur introuvable !");
                return;
            }

            String currentNom = rsUser.getString("nom");
            String currentPrenom = rsUser.getString("prenom");
            String currentEmail = rsUser.getString("email");
            String currentMdp = rsUser.getString("mot_de_passe");
            String currentRole = rsUser.getString("role");

            // --- Form for modification ---
            JTextField txtNom = new JTextField(currentNom);
            JTextField txtPrenom = new JTextField(currentPrenom);
            JTextField txtEmail = new JTextField(currentEmail);
            JPasswordField txtMdp = new JPasswordField(currentMdp);
            JComboBox<String> comboRole = new JComboBox<>(new String[]{
                    "etudiant", "enseignant", "secretaire", "chefprogramme"
            });
            comboRole.setSelectedItem(currentRole);

            JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            panel.add(new JLabel("Nom :"));
            panel.add(txtNom);
            panel.add(new JLabel("Prénom :"));
            panel.add(txtPrenom);
            panel.add(new JLabel("Email :"));
            panel.add(txtEmail);
            panel.add(new JLabel("Mot de passe :"));
            panel.add(txtMdp);
            panel.add(new JLabel("Rôle :"));
            panel.add(comboRole);

            int result = JOptionPane.showConfirmDialog(null, panel,
                    "✏️ Modifier l’utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return;

            // --- New values (keep old if empty) ---
            String newNom = txtNom.getText().trim().isEmpty() ? currentNom : txtNom.getText().trim();
            String newPrenom = txtPrenom.getText().trim().isEmpty() ? currentPrenom : txtPrenom.getText().trim();
            String newEmail = txtEmail.getText().trim().isEmpty() ? currentEmail : txtEmail.getText().trim();
            String newMdp = new String(txtMdp.getPassword()).trim().isEmpty() ? currentMdp : new String(txtMdp.getPassword()).trim();
            String newRole = comboRole.getSelectedItem().toString();

            // --- Validation ---
            if (!newEmail.contains("@") || !newEmail.contains(".")) {
                JOptionPane.showMessageDialog(null, "⚠️ Email invalide ! Veuillez corriger.", "Erreur", JOptionPane.ERROR_MESSAGE);
                modifierUtilisateur();
                return;
            }

            if (newMdp.length() < 6) {
                JOptionPane.showMessageDialog(null, "⚠️ Le mot de passe doit contenir au moins 6 caractères.", "Erreur", JOptionPane.ERROR_MESSAGE);
                modifierUtilisateur();
                return;
            }

            // --- Confirmation ---
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Voulez-vous vraiment enregistrer les modifications ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // --- Update query ---
            String sqlUpdate = "UPDATE Utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, role=? WHERE id_utilisateur=?";
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setString(1, newNom);
            psUpdate.setString(2, newPrenom);
            psUpdate.setString(3, newEmail);
            psUpdate.setString(4, newMdp);
            psUpdate.setString(5, newRole);
            psUpdate.setInt(6, idUtilisateur);
            int rows = psUpdate.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:green;'>✅ Utilisateur modifié avec succès !</b></html>",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "⚠️ Aucune modification effectuée.",
                        "Information", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:red;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void supprimerUtilisateur() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // --- Load all users except admins ---
            String sqlList = """
            SELECT id_utilisateur, nom, prenom, role
            FROM Utilisateur
            WHERE role != 'administrateur'
            ORDER BY nom
        """;
            PreparedStatement psList = conn.prepareStatement(sqlList);
            ResultSet rs = psList.executeQuery();

            JComboBox<String> comboUsers = new JComboBox<>();
            while (rs.next()) {
                comboUsers.addItem(rs.getInt("id_utilisateur") + " - " +
                        rs.getString("nom") + " " + rs.getString("prenom") +
                        " (" + rs.getString("role") + ")");
            }

            if (comboUsers.getItemCount() == 0) {
                JOptionPane.showMessageDialog(null, "⚠️ Aucun utilisateur disponible à supprimer !");
                return;
            }

            // --- Ask which user to delete ---
            JPanel selectPanel = new JPanel(new GridLayout(2, 1, 10, 10));
            selectPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
            selectPanel.add(new JLabel("Sélectionnez un utilisateur à supprimer :"));
            selectPanel.add(comboUsers);

            int result = JOptionPane.showConfirmDialog(null, selectPanel,
                    "🗑️ Supprimer un utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return;

            int idUtilisateur = Integer.parseInt(comboUsers.getSelectedItem().toString().split(" - ")[0]);

            // --- Verify user role again to avoid deleting admins ---
            String sqlRole = "SELECT role FROM Utilisateur WHERE id_utilisateur=?";
            PreparedStatement psRole = conn.prepareStatement(sqlRole);
            psRole.setInt(1, idUtilisateur);
            ResultSet rsRole = psRole.executeQuery();

            if (!rsRole.next()) {
                JOptionPane.showMessageDialog(null, "⚠️ Utilisateur introuvable !");
                return;
            }

            String role = rsRole.getString("role");
            if ("administrateur".equalsIgnoreCase(role)) {
                JOptionPane.showMessageDialog(null, "🚫 Impossible de supprimer un administrateur !");
                return;
            }

            // --- Confirmation ---
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Voulez-vous vraiment supprimer cet utilisateur ?\nCette action est irréversible.",
                    "⚠️ Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "❎ Suppression annulée.");
                return;
            }

            conn.setAutoCommit(false);

            try {
                // --- If student, delete their inscriptions first ---
                if ("etudiant".equalsIgnoreCase(role)) {
                    String sqlIns = "DELETE FROM Inscription WHERE id_etudiant = ?";
                    PreparedStatement psIns = conn.prepareStatement(sqlIns);
                    psIns.setInt(1, idUtilisateur);
                    psIns.executeUpdate();
                }

                // --- Delete from dependent tables ---
                String[][] tableMappings = {
                        {"Etudiant", "id_etudiant"},
                        {"Enseignant", "id_enseignant"},
                        {"ChefProgramme", "id_chefprog"}
                };

                for (String[] mapping : tableMappings) {
                    String sqlDelete = "DELETE FROM " + mapping[0] + " WHERE " + mapping[1] + "=?";
                    try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                        psDel.setInt(1, idUtilisateur);
                        psDel.executeUpdate();
                    }
                }

                // --- Finally delete from Utilisateur ---
                String sqlUser = "DELETE FROM Utilisateur WHERE id_utilisateur=? AND role != 'administrateur'";
                PreparedStatement psUser = conn.prepareStatement(sqlUser);
                psUser.setInt(1, idUtilisateur);
                int rows = psUser.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    JOptionPane.showMessageDialog(null,
                            "<html><b style='color:green;'>✅ Utilisateur supprimé avec succès !</b></html>",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null,
                            "⚠️ Aucune suppression effectuée (peut-être un administrateur).",
                            "Information", JOptionPane.WARNING_MESSAGE);
                }

            } catch (SQLException e) {
                conn.rollback();
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:red;'>❌ Erreur lors de la suppression :</b> " + e.getMessage() + "</html>",
                        "Erreur SQL", JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:red;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void modifierInscription() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // === 1️⃣ Load all inscriptions ===
            String sqlList = """
            SELECT i.id_inscription, u.nom AS etudiant_nom, u.prenom AS etudiant_prenom,
                   p.nom AS programme_nom, a.libelle
            FROM Inscription i
            JOIN Etudiant e ON i.id_etudiant = e.id_etudiant
            JOIN Utilisateur u ON e.id_etudiant = u.id_utilisateur
            JOIN Programme p ON i.id_programme = p.id_programme
            JOIN AnneeScolaire a ON i.id_annee = a.id_annee
            ORDER BY u.nom
        """;

            PreparedStatement psList = conn.prepareStatement(sqlList);
            ResultSet rs = psList.executeQuery();

            DefaultListModel<String> listModel = new DefaultListModel<>();
            while (rs.next()) {
                listModel.addElement(rs.getInt("id_inscription") + " - " +
                        rs.getString("etudiant_nom") + " " + rs.getString("etudiant_prenom") +
                        " | " + rs.getString("programme_nom") + " (" + rs.getString("libelle") + ")");
            }

            if (listModel.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#D9534F;'>⚠️ Aucune inscription trouvée !</b></html>",
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // === 2️⃣ Searchable list panel ===
            JTextField searchField = new JTextField();
            JList<String> inscriptionList = new JList<>(listModel);
            inscriptionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(inscriptionList);
            scrollPane.setPreferredSize(new Dimension(500, 250));

            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void filter() {
                    String search = searchField.getText().trim().toLowerCase();
                    DefaultListModel<String> filtered = new DefaultListModel<>();

                    for (int i = 0; i < listModel.size(); i++) {
                        String item = listModel.getElementAt(i).toLowerCase();
                        if (item.contains(search)) filtered.addElement(listModel.getElementAt(i));
                    }

                    inscriptionList.setModel(filtered);
                }

                public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            });

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(new JLabel("🔍 Rechercher une inscription à modifier :"), BorderLayout.NORTH);
            panel.add(searchField, BorderLayout.CENTER);
            panel.add(scrollPane, BorderLayout.SOUTH);

            int selection = JOptionPane.showConfirmDialog(null, panel,
                    "✏️ Modifier une inscription", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (selection != JOptionPane.OK_OPTION) return;

            String selected = inscriptionList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#D9534F;'>❌ Veuillez sélectionner une inscription !</b></html>",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idInscription = Integer.parseInt(selected.split(" - ")[0]);

            // === 3️⃣ Get current data ===
            String sqlGet = """
            SELECT i.id_programme, p.nom AS programme_nom, i.id_annee, a.libelle AS annee_nom
            FROM Inscription i
            JOIN Programme p ON i.id_programme = p.id_programme
            JOIN AnneeScolaire a ON i.id_annee = a.id_annee
            WHERE i.id_inscription=?
        """;

            PreparedStatement ps = conn.prepareStatement(sqlGet);
            ps.setInt(1, idInscription);
            ResultSet rsGet = ps.executeQuery();

            if (!rsGet.next()) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#F0AD4E;'>⚠️ Inscription introuvable !</b></html>",
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int currentProg = rsGet.getInt("id_programme");
            int currentAnnee = rsGet.getInt("id_annee");
            String progNom = rsGet.getString("programme_nom");
            String anneeNom = rsGet.getString("annee_nom");

            // === 4️⃣ Create editable form ===
            JComboBox<String> comboProg = new JComboBox<>();
            JComboBox<String> comboAnnee = new JComboBox<>();

            // Fill programmes
            ResultSet rsP = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
            while (rsP.next()) {
                comboProg.addItem(rsP.getInt("id_programme") + " - " + rsP.getString("nom"));
            }

            // Fill années scolaires
            ResultSet rsA = conn.createStatement().executeQuery("SELECT id_annee, libelle FROM AnneeScolaire");
            while (rsA.next()) {
                comboAnnee.addItem(rsA.getInt("id_annee") + " - " + rsA.getString("libelle"));
            }

            Object[] fields = {
                    "Programme actuel : " + progNom,
                    "Sélectionnez un nouveau (ou laissez vide pour garder) :", comboProg,
                    "Année actuelle : " + anneeNom,
                    "Sélectionnez une nouvelle (ou laissez vide pour garder) :", comboAnnee
            };

            int confirm = JOptionPane.showConfirmDialog(null, fields,
                    "🔄 Modifier les informations d'inscription", JOptionPane.OK_CANCEL_OPTION);

            if (confirm != JOptionPane.OK_OPTION) return;

            String selectedProg = (String) comboProg.getSelectedItem();
            String selectedAnnee = (String) comboAnnee.getSelectedItem();

            int newProg = (selectedProg == null || selectedProg.isEmpty())
                    ? currentProg
                    : Integer.parseInt(selectedProg.split(" - ")[0]);
            int newAnnee = (selectedAnnee == null || selectedAnnee.isEmpty())
                    ? currentAnnee
                    : Integer.parseInt(selectedAnnee.split(" - ")[0]);

            // === 5️⃣ Confirm modification ===
            int finalConfirm = JOptionPane.showConfirmDialog(null,
                    "<html>Confirmez-vous la mise à jour de cette inscription ?<br><br>"
                            + "<b>Programme :</b> " + progNom + " → " + (selectedProg != null ? selectedProg : "(inchangé)") + "<br>"
                            + "<b>Année :</b> " + anneeNom + " → " + (selectedAnnee != null ? selectedAnnee : "(inchangé)") + "</html>",
                    "⚠️ Confirmation requise", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (finalConfirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#F0AD4E;'>❎ Modification annulée.</b></html>",
                        "Annulée", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // === 6️⃣ Update in database ===
            conn.setAutoCommit(false);
            try {
                String sqlUpdate = "UPDATE Inscription SET id_programme=?, id_annee=? WHERE id_inscription=?";
                PreparedStatement psUp = conn.prepareStatement(sqlUpdate);
                psUp.setInt(1, newProg);
                psUp.setInt(2, newAnnee);
                psUp.setInt(3, idInscription);
                int rows = psUp.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    JOptionPane.showMessageDialog(null,
                            "<html><b style='color:#5CB85C;'>✅ Inscription modifiée avec succès !</b></html>",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "<html><b style='color:#F0AD4E;'>⚠️ Aucune modification effectuée !</b></html>",
                            "Avertissement", JOptionPane.WARNING_MESSAGE);
                }

            } catch (SQLException e) {
                conn.rollback();
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#D9534F;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:#D9534F;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:#D9534F;'>❌ Erreur inattendue :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void supprimerInscription() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // === 1️⃣ Fetch all inscriptions ===
            String sqlList = """
            SELECT i.id_inscription, u.nom AS etudiant_nom, u.prenom AS etudiant_prenom,
                   p.nom AS programme_nom, a.libelle
            FROM Inscription i
            JOIN Etudiant e ON i.id_etudiant = e.id_etudiant
            JOIN Utilisateur u ON e.id_etudiant = u.id_utilisateur
            JOIN Programme p ON i.id_programme = p.id_programme
            JOIN AnneeScolaire a ON i.id_annee = a.id_annee
        """;

            PreparedStatement psList = conn.prepareStatement(sqlList);
            ResultSet rs = psList.executeQuery();

            DefaultListModel<String> listModel = new DefaultListModel<>();
            while (rs.next()) {
                String entry = rs.getInt("id_inscription") + " - " +
                        rs.getString("etudiant_nom") + " " + rs.getString("etudiant_prenom") +
                        " | " + rs.getString("programme_nom") + " (" + rs.getString("libelle") + ")";
                listModel.addElement(entry);
            }

            if (listModel.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#D9534F;'>⚠️ Aucune inscription trouvée !</b></html>",
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // === 2️⃣ Create Searchable List ===
            JTextField searchField = new JTextField();
            JList<String> inscriptionList = new JList<>(listModel);
            inscriptionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(inscriptionList);
            scrollPane.setPreferredSize(new Dimension(450, 200));

            // 🔍 Live filtering logic
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void filter() {
                    String search = searchField.getText().trim().toLowerCase();
                    DefaultListModel<String> filtered = new DefaultListModel<>();

                    for (int i = 0; i < listModel.size(); i++) {
                        String item = listModel.getElementAt(i).toLowerCase();
                        if (item.contains(search)) filtered.addElement(listModel.getElementAt(i));
                    }

                    inscriptionList.setModel(filtered);
                }

                public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            });

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(new JLabel("🔍 Rechercher une inscription (nom, programme, année) :"), BorderLayout.NORTH);
            panel.add(searchField, BorderLayout.CENTER);
            panel.add(scrollPane, BorderLayout.SOUTH);

            int select = JOptionPane.showConfirmDialog(null, panel,
                    "🗑️ Supprimer une inscription", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (select != JOptionPane.OK_OPTION) return;

            String selected = inscriptionList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#D9534F;'>❌ Veuillez sélectionner une inscription !</b></html>",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idInscription = Integer.parseInt(selected.split(" - ")[0]);

            // === 3️⃣ Ask for confirmation ===
            int confirm = JOptionPane.showConfirmDialog(null,
                    "<html><b>Confirmez-vous la suppression de cette inscription ?</b><br><br>📘 " +
                            selected + "</html>",
                    "⚠️ Confirmation requise", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#F0AD4E;'>❎ Suppression annulée par l'utilisateur.</b></html>",
                        "Annulée", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // === 4️⃣ Perform deletion ===
            conn.setAutoCommit(false);
            try {
                String sqlDelete = "DELETE FROM Inscription WHERE id_inscription=?";
                PreparedStatement psDelete = conn.prepareStatement(sqlDelete);
                psDelete.setInt(1, idInscription);
                int rows = psDelete.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    JOptionPane.showMessageDialog(null,
                            "<html><b style='color:#5CB85C;'>✅ Inscription supprimée avec succès !</b></html>",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "<html><b style='color:#F0AD4E;'>⚠️ Aucune inscription trouvée !</b></html>",
                            "Avertissement", JOptionPane.WARNING_MESSAGE);
                }

            } catch (SQLException e) {
                conn.rollback();
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#D9534F;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:#D9534F;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:#D9534F;'>❌ Erreur inattendue :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // 🔹 Base SQL
            String sqlBase = """
            SELECT u.id_utilisateur, u.nom, u.prenom, u.email,
                   e.origine_scolaire, e.statut,
                   p.nom AS programme, a.libelle AS annee
            FROM Utilisateur u
            JOIN Etudiant e ON u.id_utilisateur = e.id_etudiant
            LEFT JOIN Inscription i ON e.id_etudiant = i.id_etudiant
            LEFT JOIN Programme p ON i.id_programme = p.id_programme
            LEFT JOIN AnneeScolaire a ON i.id_annee = a.id_annee
            WHERE u.role = 'etudiant'
        """;

            // 🔸 Step 1: Choose filter option
            String[] options = {"Tous les étudiants", "Par programme", "Par année scolaire", "Rechercher par nom"};
            JComboBox<String> filterBox = new JComboBox<>(options);

            JPanel filterPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            filterPanel.add(new JLabel("📋 Choisissez une option de filtrage :"));
            filterPanel.add(filterBox);

            int choice = JOptionPane.showConfirmDialog(null, filterPanel, "Filtrer les étudiants",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (choice != JOptionPane.OK_OPTION) return;

            PreparedStatement ps = null;

            // 🔸 Step 2: Apply chosen filter
            switch (filterBox.getSelectedIndex()) {
                case 1 -> { // Par programme
                    JComboBox<String> comboProg = new JComboBox<>();
                    ResultSet rsProg = conn.createStatement().executeQuery("SELECT id_programme, nom FROM Programme");
                    while (rsProg.next())
                        comboProg.addItem(rsProg.getInt("id_programme") + " - " + rsProg.getString("nom"));

                    int resProg = JOptionPane.showConfirmDialog(null, comboProg,
                            "Sélectionner un programme", JOptionPane.OK_CANCEL_OPTION);
                    if (resProg != JOptionPane.OK_OPTION) return;

                    int idProg = Integer.parseInt(comboProg.getSelectedItem().toString().split(" - ")[0]);
                    sqlBase += " AND p.id_programme = ? ORDER BY u.nom, u.prenom";
                    ps = conn.prepareStatement(sqlBase);
                    ps.setInt(1, idProg);
                }

                case 2 -> { // Par année
                    JComboBox<String> comboAnnee = new JComboBox<>();
                    ResultSet rsAnnee = conn.createStatement().executeQuery("SELECT id_annee, libelle FROM AnneeScolaire");
                    while (rsAnnee.next())
                        comboAnnee.addItem(rsAnnee.getInt("id_annee") + " - " + rsAnnee.getString("libelle"));

                    int resAnnee = JOptionPane.showConfirmDialog(null, comboAnnee,
                            "Sélectionner une année scolaire", JOptionPane.OK_CANCEL_OPTION);
                    if (resAnnee != JOptionPane.OK_OPTION) return;

                    int idAnnee = Integer.parseInt(comboAnnee.getSelectedItem().toString().split(" - ")[0]);
                    sqlBase += " AND a.id_annee = ? ORDER BY u.nom, u.prenom";
                    ps = conn.prepareStatement(sqlBase);
                    ps.setInt(1, idAnnee);
                }

                case 3 -> { // Rechercher par nom
                    JTextField txtSearch = new JTextField();
                    int resSearch = JOptionPane.showConfirmDialog(null, txtSearch,
                            "🔍 Entrez un nom ou prénom", JOptionPane.OK_CANCEL_OPTION);
                    if (resSearch != JOptionPane.OK_OPTION) return;

                    String search = txtSearch.getText().trim();
                    if (search.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "⚠️ Veuillez entrer un nom valide !");
                        return;
                    }

                    sqlBase += " AND (u.nom LIKE ? OR u.prenom LIKE ?) ORDER BY u.nom, u.prenom";
                    ps = conn.prepareStatement(sqlBase);
                    ps.setString(1, "%" + search + "%");
                    ps.setString(2, "%" + search + "%");
                }

                default -> { // Tous les étudiants
                    sqlBase += " ORDER BY u.nom, u.prenom";
                    ps = conn.prepareStatement(sqlBase);
                }
            }

            // 🔸 Step 3: Execute and fill JTable
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Nom", "Prénom", "Email", "Programme", "Année", "Origine", "Statut"}, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("programme") != null ? rs.getString("programme") : "-",
                        rs.getString("annee") != null ? rs.getString("annee") : "-",
                        rs.getString("origine_scolaire") != null ? rs.getString("origine_scolaire") : "-",
                        rs.getString("statut") != null ? rs.getString("statut") : "-"
                });
            }

            JTable table = new JTable(model);
            table.setFillsViewportHeight(true);
            table.setAutoCreateRowSorter(true);
            table.getTableHeader().setReorderingAllowed(false);
            table.setRowHeight(25);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(800, 400));

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null,
                        "<html><b style='color:#D9534F;'>⚠️ Aucun étudiant trouvé avec ce filtre.</b></html>",
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 🔸 Step 4: Display results
            JOptionPane.showMessageDialog(null, scrollPane,
                    "📖 Liste des étudiants", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b style='color:#D9534F;'>❌ Erreur SQL :</b> " + e.getMessage() + "</html>",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

}
