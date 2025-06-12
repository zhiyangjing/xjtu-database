package db.cr

import kotlinx.coroutines.*
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.atomic.AtomicInteger


data class Student(
    val id: String,
    val name: String,
    val gender: String,
    val birth: String,
    val height: String,
    val dorm: String
)

data class Course(val id: String, val name: String, val period: String, val credit: String, val teacher: String)
data class Enrollment(val studentId: String, val courseId: String, val grade: String?)

val config = listOf<Any>("gamma", "data_5000.csv", "course1000.csv", "enrollments200000.csv")
// val config = listOf<Any>("alpha", "data_1000.csv", "course100.csv", "enrollments.csv")

fun getConnection(): Connection {
    val url = "jdbc:postgresql://localhost:15432/${config[0]}"
    val user = "zackary"
    val password = "zackary@123"
    return DriverManager.getConnection(url, user, password)
}

fun loadStudents(path: String): List<Student> =
    File(path).readLines().drop(1).map { it.split(",") }
        .map { Student(it[0], it[1], it[2], it[4], it[3], it[5]) }

fun loadCourses(path: String): List<Course> =
    File(path).readLines().drop(1).map { it.split(",") }
        .map { Course(it[0], it[1], it[2], it[3], it[4]) }

fun loadEnrollments(path: String): List<Enrollment> =
    File(path).readLines().drop(1).map { it.split(",") }
        .map { Enrollment(it[0], it[1], it[2]) }

suspend fun <T> parallelInsert(
    data: List<T>,
    threadCount: Int = 4,
    insertFunc: suspend (List<T>, Connection) -> Unit,
    onInsertChunk: (suspend (Int) -> Unit)? = null
) = coroutineScope {
    val chunked = data.chunked((data.size + threadCount - 1) / threadCount)
    val jobs = chunked.mapIndexed { index, chunk ->
        async(Dispatchers.IO) {
            println("Coroutine $index started on thread ${Thread.currentThread().name}, processing chunk size ${chunk.size}")
            getConnection().use { conn ->
                insertFunc(chunk, conn)
            }
            println("Coroutine $index finished on thread ${Thread.currentThread().name}")
            // 在每个协程插入完自己的chunk后，执行回调（比如启动删除）
            onInsertChunk?.let { it(index) }
        }
    }
    jobs.awaitAll()
}

// 插入学生
suspend fun insertStudents(data: List<Student>, conn: Connection) {
    val sql = """
        MERGE INTO student AS t
        USING (SELECT ? AS student_id, ? AS student_name, ? AS gender, ? AS birth_date, ? AS height_cm, ? AS dorm_number) AS vals
        ON t.student_id = vals.student_id
        WHEN MATCHED THEN UPDATE SET
            student_name = vals.student_name,
            gender = vals.gender,
            birth_date = vals.birth_date,
            height_cm = vals.height_cm,
            dorm_number = vals.dorm_number
        WHEN NOT MATCHED THEN INSERT (student_id, student_name, gender, birth_date, height_cm, dorm_number)
            VALUES (vals.student_id, vals.student_name, vals.gender, vals.birth_date, vals.height_cm, vals.dorm_number)
    """.trimIndent()

    conn.prepareStatement(sql).use { stmt ->
        data.forEach {
            stmt.setString(1, it.id.trim())
            stmt.setString(2, it.name)
            stmt.setString(3, it.gender)
            stmt.setDate(4, java.sql.Date.valueOf(it.birth))
            stmt.setBigDecimal(5, it.height.toBigDecimal())
            stmt.setString(6, it.dorm)
            stmt.executeUpdate()
        }
    }
}

// 插入课程
suspend fun insertCourses(data: List<Course>, conn: Connection) {
    val sql = """
        MERGE INTO course AS t
        USING (SELECT ? AS course_id, ? AS course_name, ? AS period_hours, ? AS credit_points, ? AS teacher_name) AS vals
        ON t.course_id = vals.course_id
        WHEN MATCHED THEN UPDATE SET
            course_name = vals.course_name,
            period_hours = vals.period_hours,
            credit_points = vals.credit_points,
            teacher_name = vals.teacher_name
        WHEN NOT MATCHED THEN INSERT (course_id, course_name, period_hours, credit_points, teacher_name)
            VALUES (vals.course_id, vals.course_name, vals.period_hours, vals.credit_points, vals.teacher_name)
    """.trimIndent()

    conn.prepareStatement(sql).use { stmt ->
        data.forEach {
            stmt.setString(1, it.id.trim())
            stmt.setString(2, it.name)
            stmt.setInt(3, it.period.toInt())
            stmt.setBigDecimal(4, it.credit.toBigDecimal())
            stmt.setString(5, it.teacher)
            stmt.executeUpdate()
        }
    }
}

// 插入选课记录
suspend fun insertEnrollments(data: List<Enrollment>, conn: Connection) {
    val sql = """
        MERGE INTO studentcourse AS t
        USING (SELECT ? AS student_id, ? AS course_id, ? AS grade) AS vals
        ON t.student_id = vals.student_id AND t.course_id = vals.course_id
        WHEN MATCHED THEN UPDATE SET
            grade = vals.grade
        WHEN NOT MATCHED THEN INSERT (student_id, course_id, grade)
            VALUES (vals.student_id, vals.course_id, vals.grade)
    """.trimIndent()

    conn.prepareStatement(sql).use { stmt ->
        data.forEach {
            stmt.setString(1, it.studentId.trim())
            stmt.setString(2, it.courseId.trim())

            if (it.grade == null) {
                stmt.setNull(3, java.sql.Types.NUMERIC)
            } else {
                stmt.setBigDecimal(3, it.grade.toBigDecimal())
            }
            stmt.executeUpdate()
        }
    }
}


fun deleteOneLowGrade(conn: Connection): Boolean {
    // PostgreSQL：ORDER BY RANDOM() 随机选一条
    val pickSql = """
      SELECT student_id, course_id
        FROM studentcourse
       WHERE (grade < 60 OR grade IS NULL)
       ORDER BY RANDOM()
       LIMIT 1
    """.trimIndent()

    conn.prepareStatement(pickSql).use { pickStmt ->
        pickStmt.executeQuery().use { rs ->
            if (!rs.next()) return false
            val sid = rs.getString("student_id")
            val cid = rs.getString("course_id")

            // 打印一下被选中的
            println("准备删除：student_id=$sid, course_id=$cid")

            val delSql = """
              DELETE FROM studentcourse
               WHERE student_id = ? AND course_id = ?
            """.trimIndent()

            conn.prepareStatement(delSql).use { delStmt ->
                delStmt.setString(1, sid)
                delStmt.setString(2, cid)
                val deleted = delStmt.executeUpdate()
                if (deleted > 0) {
                    println("已删除：student_id=$sid, course_id=$cid")
                    return true
                }
            }
        }
    }
    return false
}

/**
 * 并行插入 + 并发随机删除 200 条低成绩记录
 */
suspend fun insertAndRandomDeleteLowGrades(
    data: List<Enrollment>,
    insertThreads: Int = 10,
    deleteTarget: Int = 200
) = coroutineScope {
    val deletedCount = AtomicInteger(0)

    val insertJob = launch(Dispatchers.IO) {
        println("插入协程组启动，共 ${data.size} 条记录")
        parallelInsert(data, insertThreads, ::insertEnrollments) { chunkIndex ->
            println("Chunk $chunkIndex 插入完毕")
        }
        println("插入协程组全部完成")
    }

    val deleteJob = launch(Dispatchers.IO) {
        getConnection().use { conn ->
            println("删除协程启动，目标删除 $deleteTarget 条低成绩记录")
            while (deletedCount.get() < deleteTarget) {
                val success = deleteOneLowGrade(conn)
                if (success) {
                    val now = deletedCount.incrementAndGet()
                    println("删除进度：$now / $deleteTarget")
                } else {
                    if (insertJob.isActive) {
                        delay(100)
                        continue
                    } else {
                        println("插入已完成，且当前无可删低成绩记录，退出删除循环")
                        break
                    }
                }
            }
            println("删除协程完成，总共删除 ${deletedCount.get()} 条")
        }
    }
    joinAll(insertJob, deleteJob)
}

fun main() = runBlocking {
    val base = "src/gen_data"
    val students = loadStudents("$base/probability_based_students/datasource/${config[1]}")
    val courses = loadCourses("$base/spider_based_courses/course_data/${config[2]}")
    val enrollments = loadEnrollments("$base/student_courses/enrollments_data/${config[3]}")

    println("开始插入学生 ${students.size} 条")
    parallelInsert(students, 4, ::insertStudents)
    println("学生插入完成")

    println("开始插入课程 ${courses.size} 条")
    parallelInsert(courses, 4, ::insertCourses)
    println("课程插入完成")

    println("开始插入并行插入 & 删除选课记录 ${enrollments.size} 条，同时删除 200 条历史数据")
    insertAndRandomDeleteLowGrades(enrollments, insertThreads = 10, 200)
    println("并行插入 & 删除完成")
}
