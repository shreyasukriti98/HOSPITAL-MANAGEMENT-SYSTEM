ITWINGS - HOSPITAL MANAGEMENT SYSTEM (Java + MySQL)

FEATURES
- Patient records: add, view, delete
- Doctor records: add, view, delete
- Appointment booking and list
- Patient billing and bill list
- Swing desktop GUI with MySQL database

REQUIREMENTS
- JDK 17 or newer
- MySQL Server (MySQL Workbench is optional)
- Maven

SETUP
1. Start MySQL Server.
2. Open MySQL Workbench or phpMyAdmin (if configured for MySQL).
3. Run the complete database.sql script.
4. Open src/main/java/com/itwings/hospital/DB.java.
5. Set USER and PASSWORD to your local MySQL credentials.
   Default in this sample: user=root and blank password.
6. Open a terminal in the project folder and run:
      mvn clean package
      mvn exec:java -Dexec.mainClass="com.itwings.hospital.Main"
   If Maven Exec Plugin is not available, run the main class from IntelliJ IDEA / Eclipse after importing as a Maven project.
7. The Hospital Management System window should open.

IMPORT INTO IDE
- IntelliJ IDEA: File > Open > select the folder containing pom.xml.
- Eclipse: File > Import > Existing Maven Projects > select project folder.

NOTES
- This is a learning/demo project, not a production hospital system.
- Do not use real patient data in a demo installation.
- Database connection settings are in DB.java.
