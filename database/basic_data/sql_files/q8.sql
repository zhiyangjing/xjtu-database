WITH StudentCourseCounts AS (SELECT student_id, COUNT(course_id) AS course_count
                             FROM studentcourse
                             GROUP BY student_id
                             HAVING COUNT(course_id) >= 3),
     StudentAvgGrades AS (SELECT sc.student_id, AVG(sc.grade) AS avg_grade
                          FROM studentcourse sc
                                   JOIN StudentCourseCounts scc ON sc.student_id = scc.student_id
                          GROUP BY sc.student_id),
     MaxAvgGrade AS (SELECT MAX(avg_grade) AS max_avg
                     FROM StudentAvgGrades)
SELECT s.student_id, s.student_name
FROM StudentAvgGrades sag
         JOIN student s ON sag.student_id = s.student_id
         JOIN MaxAvgGrade mag ON sag.avg_grade = mag.max_avg;
