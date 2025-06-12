SELECT sc.student_id, sc.course_id, sc.grade
FROM studentcourse sc
         JOIN student s ON sc.student_id = s.student_id
WHERE s.gender = '女'
  AND sc.student_id NOT IN (SELECT student_id
                            FROM studentcourse
                            WHERE course_id = 'CS-02');