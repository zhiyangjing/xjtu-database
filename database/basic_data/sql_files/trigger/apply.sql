CREATE TRIGGER trg_bk_studentcourse_delete
    BEFORE DELETE
    ON public.studentcourse
    FOR EACH ROW
EXECUTE PROCEDURE public.fn_bk_studentcourse_delete();
