import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

fun main() {
    val sysDbUrl = "jdbc:postgresql://localhost:15432/postgres"
    val username = "zackary"
    val password = "zackary@123"

    try {
        val sysConnection: Connection = DriverManager.getConnection(sysDbUrl,username,password)
        val sysStatement: Statement = sysConnection.createStatement()

//        val dbName = "alpha"
        val dbName = "gamma"
        val checkDbQuery = "SELECT 1 FROM pg_database WHERE datname = '$dbName'"
        val rs = sysStatement.executeQuery(checkDbQuery)

        if (!rs.next()) {
            sysStatement.executeUpdate("CREATE DATABASE $dbName")
            println("Database $dbName created successfully")
        } else {
            println("Database $dbName already exists, skipping creation")
        }

        rs.close()

        sysStatement.close()
        sysConnection.close()

        val dbUrl = "jdbc:postgresql://localhost:15432/$dbName"
        val dbConnection: Connection = DriverManager.getConnection(dbUrl,username,password)
        val dbStatement: Statement = dbConnection.createStatement()

        dbStatement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS student (
                student_id CHAR(10) PRIMARY KEY,
                student_name VARCHAR(50),
                gender CHAR(1),
                birth_date DATE,
                height_cm DECIMAL(5,2),
                dorm_number VARCHAR(20)
            );
        """.trimIndent())
        println("Table 'student' created successfully")

        dbStatement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS course (
                course_id CHAR(10) PRIMARY KEY,
                course_name VARCHAR(50),
                period_hours INT,
                credit_points DECIMAL(3,1),
                teacher_name VARCHAR(50)
            );
        """.trimIndent())
        println("Table 'course' created successfully")

        dbStatement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS studentcourse (
                course_id CHAR(10),
                student_id CHAR(10),
                grade DECIMAL(5,2),
                PRIMARY KEY (student_id,course_id),
                FOREIGN KEY (student_id) REFERENCES student(student_id),
                FOREIGN KEY (course_id) REFERENCES course(course_id)
            );
        """.trimIndent())
        println("Table 'studentcourse' created successfully")

        dbStatement.close()
        dbConnection.close()

    }  catch (e: Exception) {
        println("Execution failed : ${e.message}")
        e.printStackTrace()
    }
}