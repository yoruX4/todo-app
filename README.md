# TaskFlow – JavaFX To-Do List Application

A full-featured desktop To-Do List application built with **JavaFX 21**, **MySQL**, and **Maven**, following the **MVC architecture pattern**.

---

## ✨ Features

- **5 screens**: Login · Register · Dashboard · Task Manager · Profile
- **Secure authentication** with BCrypt password hashing
- **Full CRUD** for tasks (Create, Read, Update, Delete)
- **Task filtering** by status (Pending / In Progress / Completed)
- **Keyword search** across title, description, and category
- **Priority levels** (Low / Medium / High) with colour coding
- **Due dates** with overdue detection
- **No hardcoded credentials** – all secrets loaded from environment variables
- Clean, dark-sidebar UI with responsive layout

## 🗂 Project Structure

```
todo-app/
├── src/
│   └── main/
│       ├── java/com/todoapp/
│       │   ├── MainApp.java              # Entry point
│       │   ├── config/
│       │   │   └── AppConfig.java        # Env var loader
│       │   ├── controller/
│       │   │   ├── LoginController.java
│       │   │   ├── RegisterController.java
│       │   │   ├── DashboardController.java
│       │   │   ├── TasksController.java
│       │   │   └── ProfileController.java
│       │   ├── dao/
│       │   │   ├── UserDAO.java
│       │   │   └── TaskDAO.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   └── Task.java
│       │   └── util/
│       │       ├── DatabaseManager.java  # HikariCP pool
│       │       ├── PasswordUtil.java     # BCrypt
│       │       ├── SessionManager.java
│       │       └── AlertUtil.java
│       └── resources/
│           ├── fxml/                     # JavaFX layouts
│           ├── css/styles.css            # Stylesheet
│           └── logback.xml               # Logging config
├── sql/
│   ├── 01_create_database.sql
│   └── 02_sample_data.sql
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

---

## ⚙️ Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17 or 21 |
| Maven | 3.8+ |
| MySQL | 8.0+ |

---

## 🛠 Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/todo-app.git
cd todo-app
```

### 2. Set up the database

```bash
# Log in to MySQL
mysql -u root -p

# Run the creation script
mysql -u root -p < sql/01_create_database.sql

# (Optional) Load sample data
mysql -u root -p todoapp < sql/02_sample_data.sql
```

### 3. Configure environment variables

```bash
# Copy the template
cp .env.example .env

# Edit .env and fill in your credentials
nano .env   # or open with any editor
```

Your `.env` should look like:

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=todoapp
DB_USER=your_mysql_user
DB_PASSWORD=your_mysql_password
```

> ⚠️ **Never commit `.env` to Git.** It is already listed in `.gitignore`.

### 4. Build the project

```bash
mvn clean package -DskipTests
```

### 5. Run the application

```bash
mvn javafx:run
```

Or run the fat JAR:

```bash
java -jar target/todo-app-1.0.0.jar
```

---

## 👤 Sample Credentials (after loading `02_sample_data.sql`)

| Username | Password |
|----------|----------|
| alice | password123 |
| bob | mypassword |
| charlie | charlie123 |


---

## 📄 License

MIT License – see [LICENSE](LICENSE) for details.
