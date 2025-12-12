# 🎓 Gestion Scolarité – Java Swing Application

A complete school management desktop application built in **Java Swing**, featuring user authentication, dashboards for every role, note management, programme management, and administrative tools such as backup/restore and statistics generation.

This project was created to simulate a real university/college management system with multiple user roles, each having their own permissions and dedicated UI.

---

## 🚀 Features

### 🔐 **Authentication**
- Secure login via email + password  
- Redirects user to their respective dashboard  
- Validation & error handling  

---

## 👥 User Roles & Dashboards

### 👨‍🎓 **Étudiant**
- View notes for each module  
- View semester/year average  
- View status (admis / redoublant / exclu)  
- View personal info  
- Display full bulletin  

### 👨‍🏫 **Enseignant**
- Create / Modify / Delete examinations  
- Enter student grades  
- Validate and calculate final grades  
- View statistics and exam results for their classes  

### 🧑‍💼 **Secrétaire**
- Add / Modify / Delete users  
- Modify & delete student inscriptions  
- Display all students  
- Manage administrative records  

### 🧑‍💼 **Chef de Programme**
- Manage programs (add / edit / delete)  
- Manage modules within a program  
- Assign teachers  
- Define coefficients and prerequis  
- Validate yearly results  

### 🛠️ **Administrateur**
- Full user CRUD  
- Modify access rights  
- Generate reports & statistics  
- Backup database (.sql)  
- Restore database (.sql)  

---

## 🛠 Technologies Used

- **Java 17+**
- **Swing (UI)**
- **MySQL (XAMPP)**  
- **JDBC**
- **JFreeChart** (statistics & charts)
- **Mysqldump** (backup)
- **ProcessBuilder** (backup & restore execution)

---
```text
## 📁 Project Structure
Gestion Scolarite/
├── src/
│ ├── db/
│ │ └── DatabaseConnection.java
│ ├── models/
│ │ ├── Administrateur.java
│ │ ├── ChefProgramme.java
│ │ ├── Enseignant.java
│ │ ├── Etudiant.java
│ │ ├── Secretaire.java
│ │ └── Utilisateur.java
│ ├── services/
│ │ ├── AdministrateurService.java
│ │ ├── AuthService.java
│ │ ├── ChefProgrammeService.java
│ │ ├── ComboDataLoader.java
│ │ ├── EnseignantService.java
│ │ ├── EtudiantService.java
│ │ └── SecretaireService.java
│ ├── ui/
│ │ ├── AdminDashboard.java
│ │ ├── ChefProgrammeDashboard.java
│ │ ├── EnseignantDashboard.java
│ │ ├── EtudiantDashboard.java
│ │ ├── LoginFrame.java
│ │ └── SecretaireDashboard.java
│ ├── ClientMain.java
│ └── ServerMain.java
├── lib/ # JAR dependencies (JFreeChart, etc.)
├── out/ # Compiled classes
└── 
```

---

## ⚙️ Setup & Installation

### Prerequisites
- **Java 17+** installed and in PATH
- **XAMPP** with MySQL running
- **Git** (optional, for cloning)

### 1. Clone the Repository
```bash
git clone <your-repository-url>
cd "Gestion Scolarite"
```
Edit src/services/AdministrateurService.java:
- Change from:
- D:\xampp\mysql\bin\mysql.exe
- To:
- C:\xampp\mysql\bin\mysql.exe

## How to run :
# Navigate to project directory
```bash
cd "path\to\Gestion Scolarite"
```

# Create out folder if not existing
```bash
mkdir out 2>nul
```
# Compile with ALL libraries from lib folder
```bash
javac -d out -cp "lib\*" -encoding UTF-8 src\*.java src\db\*.java src\models\*.java src\services\*.java src\ui\*.java
```
```bash
cd "path\to\Gestion Scolarite"
java -cp "out;lib\*" ServerMain
```
```bash
cd "path\to\Gestion Scolarite"
java -cp "out;lib\*" ClientMain
```
