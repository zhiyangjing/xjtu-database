package db.set_null

import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager

data class EnrollmentKey(val studentId: String, val courseId: String)


fun getConnection(): Connection {
    val url = "jdbc:postgresql://localhost:15432/gamma"
    val user = "zackary"
    val password = "zackary@123"
    return DriverManager.getConnection(url, user, password)
}


suspend fun randomizeNullGrades(proportion: Double = 0.1) = withContext(Dispatchers.IO) {
    val selectSql = "SELECT student_id, course_id FROM studentcourse WHERE grade IS NOT NULL"
    val toNullify = mutableListOf<EnrollmentKey>()

    getConnection().use { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(selectSql)
            while (rs.next()) {
                toNullify += EnrollmentKey(rs.getString("student_id").trim(), rs.getString("course_id").trim())
            }
        }
    }

    val selected = toNullify.shuffled().take((toNullify.size * proportion).toInt())
    println("将 ${selected.size} 条记录的成绩设为 NULL")

    getConnection().use { conn ->
        conn.prepareStatement(
            """
            UPDATE studentcourse
            SET grade = NULL
            WHERE student_id = ? AND course_id = ?
        """.trimIndent()
        ).use { stmt ->
            selected.forEach {
                stmt.setString(1, it.studentId)
                stmt.setString(2, it.courseId)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    println("空成绩设置完成")
}

fun main() = runBlocking {
    randomizeNullGrades(0.01)
}