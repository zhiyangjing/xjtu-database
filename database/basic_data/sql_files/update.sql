UPDATE course
SET period_hours = 36,
    credit_points = credit_points + 1
WHERE teacher_name = '张明'
  AND course_name = '数字电子技术';
