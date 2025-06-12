CREATE TABLE public.bk_studentcourse
(
    student_id CHARACTER(10) NOT NULL,
    course_id  CHARACTER(10) NOT NULL,
    grade      NUMERIC(5, 2),
    deleted_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT bk_sc_pk PRIMARY KEY (student_id, course_id, deleted_at),
    CONSTRAINT bk_sc_student_fk FOREIGN KEY (student_id)
        REFERENCES public.student (student_id) ON UPDATE CASCADE,
    CONSTRAINT bk_sc_course_fk FOREIGN KEY (course_id)
        REFERENCES public.course (course_id) ON UPDATE CASCADE
);
