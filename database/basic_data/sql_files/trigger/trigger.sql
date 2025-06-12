CREATE OR REPLACE FUNCTION public.fn_bk_studentcourse_delete()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.bk_studentcourse(student_id, course_id, grade)
    VALUES (OLD.student_id, OLD.course_id, OLD.grade);

    RETURN OLD;
EXCEPTION
    WHEN OTHERS THEN
        -- 万一备份出错，仍让删除继续；并写日志到 server log
        RAISE WARNING '归档失败：% / % / %', OLD.student_id, OLD.course_id, SQLERRM;
        RETURN OLD;
END;
$$ LANGUAGE plpgsql;
