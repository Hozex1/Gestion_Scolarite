package ui;

import models.Utilisateur;
import services.ChefProgrammeService;

import javax.swing.*;
import java.awt.*;

public class ChefProgrammeDashboard extends JFrame {

    public ChefProgrammeDashboard(Utilisateur chef) {
        setTitle("Espace Chef de Programme - " + chef.getNom());
        setSize(750, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblWelcome = new JLabel("Bienvenue " + chef.getPrenom() + " " + chef.getNom(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel panel = new JPanel(new GridLayout(12, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        // === Buttons ===
        JButton btnAjouterProgramme = new JButton("➕ Ajouter un programme");
        JButton btnModifierProgramme = new JButton("✏️ Modifier un programme");
        JButton btnSupprimerProgramme = new JButton("🗑️ Supprimer un programme");
        JButton btnAfficherMatieres = new JButton("📚 Voir matières d’un programme");
        JButton btnAjouterMatiere = new JButton("📘 Ajouter une matière");
        JButton btnAjouterMatiereExistante = new JButton("🧩 Ajouter une matière existante à un programme");
        JButton btnModifierMatiere = new JButton("✏️ Modifier une matière");
        JButton btnSupprimerMatiere = new JButton("🗑️ Supprimer une matière");
        JButton btnPrerequis = new JButton("🔗 Définir prérequis entre programmes");
        JButton btnPonderations = new JButton("⚖️ Définir coefficients matières / épreuves");
        JButton btnAssignerEns = new JButton("👨‍🏫 Assigner / modifier un enseignant");
        JButton btnValiderStatuts = new JButton("✅ Valider moyennes et statuts");
        JButton btnQuitter = new JButton("🚪 Déconnexion");

        // === Add buttons to panel ===
        panel.add(btnAjouterProgramme);
        panel.add(btnModifierProgramme);
        panel.add(btnSupprimerProgramme);
        panel.add(btnAfficherMatieres);
        panel.add(btnAjouterMatiere);
        panel.add(btnAjouterMatiereExistante);
        panel.add(btnModifierMatiere);
        panel.add(btnSupprimerMatiere);
        panel.add(btnPrerequis);
        panel.add(btnPonderations);
        panel.add(btnAssignerEns);
        panel.add(btnValiderStatuts);
        panel.add(btnQuitter);

        add(lblWelcome, BorderLayout.NORTH);
        add(new JScrollPane(panel), BorderLayout.CENTER);

        // === Actions ===
        btnAjouterProgramme.addActionListener(e -> ChefProgrammeService.ajouterProgramme(chef));
        btnModifierProgramme.addActionListener(e -> ChefProgrammeService.modifierProgramme(chef));
        btnSupprimerProgramme.addActionListener(e -> ChefProgrammeService.supprimerProgramme(chef));
        btnAfficherMatieres.addActionListener(e -> ChefProgrammeService.afficherMatieresParProgramme());
        btnAjouterMatiere.addActionListener(e -> ChefProgrammeService.ajouterMatiere(chef));
        btnAjouterMatiereExistante.addActionListener(e -> ChefProgrammeService.ajouterMatiereExistanteAProgramme());
        btnModifierMatiere.addActionListener(e -> ChefProgrammeService.modifierMatiere(chef));
        btnSupprimerMatiere.addActionListener(e -> ChefProgrammeService.supprimerMatiere(chef));
        btnPrerequis.addActionListener(e -> ChefProgrammeService.definirPrerequisProgramme(chef));
        btnPonderations.addActionListener(e -> ChefProgrammeService.definirPonderationsMatieres(chef));
        btnAssignerEns.addActionListener(e -> ChefProgrammeService.assignerOuModifierEnseignantMatiere(chef));
        btnValiderStatuts.addActionListener(e -> ChefProgrammeService.validerMoyennesEtStatuts(chef));

        btnQuitter.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }
}
