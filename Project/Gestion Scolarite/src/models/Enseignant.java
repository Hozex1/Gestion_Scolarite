package models;

public class Enseignant extends Utilisateur {
    private String grade;

    public Enseignant(int id, String nom, String prenom, String email, String motDePasse, String role, String grade) {
        super(id, nom, prenom, email, motDePasse, role);
        this.grade = grade;
    }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
