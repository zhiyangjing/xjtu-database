WITH WangTaoAvg AS (
    SELECT AVG(sc.grade) AS avg_grade
    FROM student s
             JOIN studentcourse sc ON s.student_id = sc.student_id
    WHERE s.student_name = '王涛'
)
SELECT s.student_id, s.student_name, AVG(sc.grade) AS avg_grade
FROM student s
         JOIN studentcourse sc ON s.student_id = sc.student_id
GROUP BY s.student_id, s.student_name
HAVING AVG(sc.grade) > (SELECT avg_grade FROM WangTaoAvg)
ORDER BY s.student_id DESC;
