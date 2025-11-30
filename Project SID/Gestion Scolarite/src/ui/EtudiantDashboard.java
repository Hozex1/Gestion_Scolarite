package ui;

import models.Utilisateur;
import services.EtudiantService;
import javax.swing.*;
import java.awt.*;

public class EtudiantDashboard extends JFrame {

    public EtudiantDashboard(Utilisateur etudiant) {
        setTitle("Espace Étudiant - " + etudiant.getNom());
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblWelcome = new JLabel("Bienvenue " + etudiant.getPrenom() + " " + etudiant.getNom(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        JButton btnNotes = new JButton("📘 Voir mes notes");
        JButton btnMoyenne = new JButton("📊 Voir ma moyenne");
        JButton btnStatut = new JButton("🎓 Voir mon statut");
        JButton btnInfos = new JButton("🧍 Mes informations");
        JButton btnBulletin = new JButton("🪪 Afficher bulletin");
        JButton btnQuitter = new JButton("🚪 Déconnexion");

        panel.add(btnNotes);
        panel.add(btnMoyenne);
        panel.add(btnStatut);
        panel.add(btnInfos);
        panel.add(btnBulletin);
        panel.add(btnQuitter);

        add(lblWelcome, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        btnNotes.addActionListener(e -> EtudiantService.afficherNotes(etudiant));
        btnMoyenne.addActionListener(e -> EtudiantService.afficherMoyenne(etudiant));
        btnStatut.addActionListener(e -> EtudiantService.afficherStatut(etudiant));
        btnInfos.addActionListener(e -> EtudiantService.afficherInfosPerso(etudiant));
        btnBulletin.addActionListener(e -> EtudiantService.afficherBulletin(etudiant));
        btnQuitter.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }
}
