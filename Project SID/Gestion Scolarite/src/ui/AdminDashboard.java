package ui;

import models.Utilisateur;
import services.AdministrateurService;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private final Utilisateur admin;
    private final JPanel contentPanel;

    public AdminDashboard(Utilisateur admin) {
        this.admin = admin;

        setTitle("⚙️ Espace Administrateur - " + admin.getNom());
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Sidebar menu
        JPanel menuPanel = new JPanel(new GridLayout(10, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton btnAfficher = new JButton("📋 Afficher utilisateurs");
        JButton btnAdd = new JButton("➕ Ajouter utilisateur");
        JButton btnEdit = new JButton("✏️ Modifier utilisateur");
        JButton btnDelete = new JButton("🗑️ Supprimer utilisateur");
        JButton btnDroits = new JButton("🔐 Modifier droits d'accès");
        JButton btnSauvegarde = new JButton("💾 Sauvegarder données");
        JButton btnRestaurer = new JButton("📂 Restaurer données");
        JButton btnStats = new JButton("📊 Rapports & statistiques");
        JButton btnLogout = new JButton("🚪 Déconnexion");

        for (JButton btn : new JButton[]{
                btnAfficher, btnAdd, btnEdit, btnDelete, btnDroits,
                btnSauvegarde, btnRestaurer, btnStats, btnLogout
        }) {
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            menuPanel.add(btn);
        }

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("Zone de travail - Administrateur"));

        add(menuPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // --- Button Actions ---
        btnAfficher.addActionListener(e -> AdministrateurService.afficherUtilisateurs());
        btnAdd.addActionListener(e -> AdministrateurService.ajouterUtilisateur());
        btnEdit.addActionListener(e -> AdministrateurService.modifierUtilisateur());
        btnDelete.addActionListener(e -> AdministrateurService.supprimerUtilisateur());
        btnDroits.addActionListener(e -> AdministrateurService.modifierDroitsAcces(admin));
        btnSauvegarde.addActionListener(e -> AdministrateurService.sauvegarderDonnees());
        btnRestaurer.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int result = fc.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                AdministrateurService.restaurerDonnees(fc.getSelectedFile().getAbsolutePath());
            }
        });
        btnStats.addActionListener(e -> AdministrateurService.genererRapportsEtStatistiques());
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });


        setVisible(true);
    }
}
