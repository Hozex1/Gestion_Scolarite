package models;

public class Administrateur extends Utilisateur {
    private String niveauAcces;

    public Administrateur(int id, String nom, String prenom, String email, String motDePasse, String role, String niveauAcces) {
        super(id, nom, prenom, email, motDePasse, role);
        this.niveauAcces = niveauAcces;
    }

    public String getNiveauAcces() { return niveauAcces; }
    public void setNiveauAcces(String niveauAcces) { this.niveauAcces = niveauAcces; }
}
