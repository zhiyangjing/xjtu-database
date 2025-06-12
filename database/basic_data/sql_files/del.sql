DELETE FROM student
WHERE student_id IN (
    SELECT sc.student_id
    FROM studentcourse sc
             JOIN course c ON sc.course_id = c.course_id
    GROUP BY sc.student_id
    HAVING SUM(c.credit_points) > 60
);
