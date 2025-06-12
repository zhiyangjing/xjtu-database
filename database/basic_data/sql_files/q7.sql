WITH CS_Courses AS (
    SELECT course_id FROM course WHERE course_id LIKE 'CS-%'
),
     CS_Student_Course_Count AS (
         SELECT student_id, COUNT(DISTINCT course_id) AS cs_course_count
         FROM studentcourse
         WHERE course_id LIKE 'CS-%'
         GROUP BY student_id
     ),
     Total_CS_Courses AS (
         SELECT COUNT(*) AS total_cs_courses FROM CS_Courses
     )
SELECT s.student_name, COALESCE(SUM(c.credit_points), 0) AS total_credits
FROM student s
         JOIN CS_Student_Course_Count cs_count ON s.student_id = cs_count.student_id
         JOIN studentcourse sc ON s.student_id = sc.student_id
         JOIN course c ON sc.course_id = c.course_id
WHERE cs_count.cs_course_count = (SELECT total_cs_courses FROM Total_CS_Courses)
GROUP BY s.student_name;
