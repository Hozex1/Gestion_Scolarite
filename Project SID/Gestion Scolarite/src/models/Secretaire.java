package models;

public class Secretaire extends Utilisateur {
    private String bureau;

    public Secretaire(int id, String nom, String prenom, String email, String motDePasse, String role, String bureau) {
        super(id, nom, prenom, email, motDePasse, role);
        this.bureau = bureau;
    }

    public String getBureau() { return bureau; }
    public void setBureau(String bureau) { this.bureau = bureau; }
}
