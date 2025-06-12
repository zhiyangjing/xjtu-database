CREATE VIEW male_students_east18 AS
SELECT student_id, student_name, birth_date, height_cm
FROM student
WHERE gender = '男' AND dorm_number LIKE '东18舍%';
