package db.rm
import kotlinx.coroutines.*
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

data class EnrollmentKey(val studentId: String, val courseId: String)

fun getConnection(): Connection {
    val url = "jdbc:postgresql://localhost:15432/gamma"
    val user = "zackary"
    val password = "zackary@123"
    return DriverManager.getConnection(url, user, password)
}

suspend fun loadLowGradeEnrollments(limit: Int): List<EnrollmentKey> = withContext(Dispatchers.IO) {
    val sql = """
        SELECT student_id, course_id 
        FROM studentcourse 
        WHERE grade IS NULL OR grade < 60
        """.trimIndent();
    val all = mutableListOf<EnrollmentKey>()
    getConnection().use { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(sql)
            while (rs.next()) {
                all += EnrollmentKey(rs.getString("student_id").trim(), rs.getString("course_id").trim())
            }
        }
    }

    all.shuffled().take(limit)
}

suspend fun deleteEnrollmentsParallel(
    keys: List<EnrollmentKey>,
    threadCount: Int = 4
) = coroutineScope {
    val chunks = keys.chunked((keys.size + threadCount - 1) / threadCount)

    val jobs = chunks.mapIndexed { index, chunk ->
        async(Dispatchers.IO) {
            println("Coroutine $index deleting ${chunk.size} records")
            getConnection().use { conn ->
                conn.prepareStatement("""
                    DELETE FROM studentcourse 
                    WHERE student_id = ? AND course_id = ?
                """.trimIndent()).use { stmt ->
                    chunk.forEach { key ->
                        stmt.setString(1, key.studentId)
                        stmt.setString(2, key.courseId)
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }
            }
            println("Coroutine $index finished")
        }
    }
    jobs.awaitAll()
}

fun main() = runBlocking {
    println("加载低分或空成绩选课记录...")
    val toDelete = loadLowGradeEnrollments(100)
    println("准备删除 ${toDelete.size} 条记录")

    deleteEnrollmentsParallel(toDelete, threadCount = 4)
    println("删除完成")
}
