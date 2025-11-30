package ui;

import models.Utilisateur;
import services.SecretaireService;

import javax.swing.*;
import java.awt.*;

public class SecretaireDashboard extends JFrame {

    private final Utilisateur secretaire;
    private final JPanel contentPanel;

    public SecretaireDashboard(Utilisateur secretaire) {
        this.secretaire = secretaire;

        setTitle("📋 Espace Secrétaire - " + secretaire.getNom());
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Sidebar menu
        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton btnAddEtudiant = new JButton("➕ Ajouter étudiant");
        JButton btnModifierUser = new JButton("✏️ Modifier utilisateur");
        JButton btnSupprimerUser = new JButton("🗑️ Supprimer utilisateur");
        JButton btnModifierInscription = new JButton("🔄 Modifier inscription");
        JButton btnSupprimerInscription = new JButton("❌ Supprimer inscription");
        JButton btnAfficher = new JButton("📖 Afficher étudiants");
        JButton btnLogout = new JButton("🚪 Déconnexion");

        for (JButton btn : new JButton[]{
                btnAddEtudiant, btnModifierUser, btnSupprimerUser,
                btnModifierInscription, btnSupprimerInscription,
                btnAfficher, btnLogout
        }) {
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            menuPanel.add(btn);
        }

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("Zone de travail - Secrétaire"));

        add(menuPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // --- Button actions ---
        btnAddEtudiant.addActionListener(e -> SecretaireService.ajouterEtudiant());
        btnModifierUser.addActionListener(e -> SecretaireService.modifierUtilisateur());
        btnSupprimerUser.addActionListener(e -> SecretaireService.supprimerUtilisateur());
        btnModifierInscription.addActionListener(e -> SecretaireService.modifierInscription());
        btnSupprimerInscription.addActionListener(e -> SecretaireService.supprimerInscription());
        btnAfficher.addActionListener(e -> SecretaireService.showUsers());
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }
}
