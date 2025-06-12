CREATE VIEW ai_course_students AS
SELECT s.student_id, s.student_name, sc.grade
FROM student s
         JOIN studentcourse sc ON s.student_id = sc.student_id
         JOIN course c ON sc.course_id = c.course_id
WHERE c.course_name = '人工智能';
