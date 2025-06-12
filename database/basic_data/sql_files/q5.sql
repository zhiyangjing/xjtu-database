WITH RankedGrades AS (
    SELECT student_id, grade,
           DENSE_RANK() OVER (ORDER BY grade DESC) AS rank
    FROM studentcourse
    WHERE course_id = 'CS-01'
)
SELECT student_id
FROM RankedGrades
WHERE rank = 2;
