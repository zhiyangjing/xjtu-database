SELECT
    att.attname AS column_name,           -- 列名
    typ.typname AS data_type,              -- 数据类型
    att.attlen AS length,                  -- 字节长度
    att.atttypmod AS typmod,               -- 类型修饰符（长度等）
    att.attnotnull AS not_null,            -- 是否 NOT NULL
    dsc.description AS column_comment     -- 列注释（如果有）
FROM pg_attribute att
         JOIN pg_class cls ON att.attrelid = cls.oid
         JOIN pg_type typ ON att.atttypid = typ.oid
         LEFT JOIN pg_description dsc ON dsc.objoid = att.attrelid AND dsc.objsubid = att.attnum
WHERE cls.relname = 'student'
  AND att.attnum > 0
  AND NOT att.attisdropped
ORDER BY att.attnum;

SELECT
    att.attname AS column_name,           -- 列名
    typ.typname AS data_type,              -- 数据类型
    att.attlen AS length,                  -- 字节长度
    att.atttypmod AS typmod,               -- 类型修饰符（长度等）
    att.attnotnull AS not_null,            -- 是否 NOT NULL
    dsc.description AS column_comment     -- 列注释（如果有）
FROM pg_attribute att
         JOIN pg_class cls ON att.attrelid = cls.oid
         JOIN pg_type typ ON att.atttypid = typ.oid
         LEFT JOIN pg_description dsc ON dsc.objoid = att.attrelid AND dsc.objsubid = att.attnum
WHERE cls.relname = 'course'
  AND att.attnum > 0
  AND NOT att.attisdropped
ORDER BY att.attnum;

SELECT
    att.attname AS column_name,           -- 列名
    typ.typname AS data_type,              -- 数据类型
    att.attlen AS length,                  -- 字节长度
    att.atttypmod AS typmod,               -- 类型修饰符（长度等）
    att.attnotnull AS not_null,            -- 是否 NOT NULL
    dsc.description AS column_comment     -- 列注释（如果有）
FROM pg_attribute att
         JOIN pg_class cls ON att.attrelid = cls.oid
         JOIN pg_type typ ON att.atttypid = typ.oid
         LEFT JOIN pg_description dsc ON dsc.objoid = att.attrelid AND dsc.objsubid = att.attnum
WHERE cls.relname = 'studentcourse'
  AND att.attnum > 0
  AND NOT att.attisdropped
ORDER BY att.attnum;
