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

---

### 👨‍🏫 **Enseignant**
- Create / Modify / Delete examinations  
- Enter student grades  
- Validate and calculate final grades  
- View statistics and exam results for their classes  

---

### 🧑‍💼 **Secrétaire**
- Add / Modify / Delete users  
- Modify & delete student inscriptions  
- Display all students  
- Manage administrative records  

---

### 🧑‍💼 **Chef de Programme**
- Manage programs (add / edit / delete)  
- Manage modules within a program  
- Assign teachers  
- Define coefficients and prerequis  
- Validate yearly results  

---

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

## ▶️ How to Run (Development Mode)

### **Prerequisites**
✔ Install Java 17+  
✔ Install XAMPP (MySQL)  
✔ Import the SQL file into phpMyAdmin  
✔ Add JAR dependencies (JFreeChart etc.) in IntelliJ  

### **Run**
Simply run:

```java
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}

