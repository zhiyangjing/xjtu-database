CREATE TABLE IF NOT EXISTS student
(
    student_id   CHAR(10) PRIMARY KEY,
    student_name VARCHAR(50),
    gender       CHAR(1),
    birth_date   DATE,
    height_cm    DECIMAL(5, 2),
    dorm_number  VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS course
(
    course_id     CHAR(10) PRIMARY KEY,
    course_name   VARCHAR(50),
    period_hours  INT,
    credit_points DECIMAL(3, 1),
    teacher_name  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS studentcourse
(
    course_id  CHAR(10),
    student_id CHAR(10),
    grade      DECIMAL(5, 2),
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES student (student_id),
    FOREIGN KEY (course_id) REFERENCES course (course_id)
);
