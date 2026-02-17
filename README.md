# GamesUP - Installation

![image](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![image](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)


## Prérequis
- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Installation

### 1. Cloner le projet
```bash
git clone <votre-repo>
cd gamesUP
```

### 2. Configuration de la base de données
```sql
CREATE DATABASE gamesUP CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'gamesup_admin'@'localhost' IDENTIFIED BY 'votre_password';
GRANT ALL PRIVILEGES ON gamesUP.* TO 'gamesup_admin'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuration des variables d'environnement
```bash
# Copier le fichier exemple
cp .env.example .env

# Générer un secret JWT
openssl rand -base64 64

# Éditer .env et remplacer les valeurs
nano .env
```

### 4. Lancer l'application
```bash
mvn clean install
mvn spring-boot:run
```

### 5. Accéder à l'application
- API : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui.html

## Tests
```bash
mvn clean test
mvn jacoco:report
```

Le rapport de couverture sera disponible dans `target/site/jacoco/index.html`

## Compte admin par défaut
- Username: `admin`
- Password: `admin123`
