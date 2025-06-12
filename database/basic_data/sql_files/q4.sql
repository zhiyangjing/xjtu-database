SELECT s.student_id, s.student_name, COALESCE(SUM(c.credit_points), 0) AS total_credits
FROM student s
         LEFT JOIN studentcourse sc ON s.student_id = sc.student_id
         LEFT JOIN course c ON sc.course_id = c.course_id
GROUP BY s.student_id, s.student_name;
