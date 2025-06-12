CREATE VIEW zhangming_courses_avg AS
SELECT c.course_id, c.course_name, AVG(sc.grade) AS avg_grade
FROM course c
         LEFT JOIN studentcourse sc ON c.course_id = sc.course_id
WHERE c.teacher_name = '张明'
GROUP BY c.course_id, c.course_name;
