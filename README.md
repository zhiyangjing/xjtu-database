## switch to omm 

```shell
su - omm
```


### Connection to database locally

> ![Note] login from other host:
>  ```shell
>  gsql -d postgres -U gaussdb -W'zackary@123' -h localhost -p5432
>  ```


> login from host :
> ```shell
> gsql -d postgres -p 5432 -r
> ```
> -r means raw output 
> 
> ```shell
> gsql -d beta -U zackary -W 'zackary@123' -p 5432
> ```
> choose one of the database
> ```shell
> gsql -u <user> -p <pswd> -g <dbname>
> ```


## Commands
```sql
\l -- 列出数据库
\dt -- 列出table
\q -- 退出
\i -- 执行某个文件
\c  db_name -- enter a db
\du  -- show all the user
\dn -- show all schema
\d db_name -- show info about db
\d+ db_name -- show more info

-- 创建数据库
CREATE DATABASE db_name;

-- 移交所有权
ALTER DATABASE db_name OWNER TO user_name;
```


## Backup and Restore
backup:
```shell
gsql -d gamma -U zackary -W 'zackary@123' -p 5432
gs_dump -U zackary -W 'zackary@123' -p 5432 -h 127.0.0.1 -f /sql_files/backup/backup200000.sql gamma
```

restore
```shell
gsql -d postgres -U zackary -W 'zackary@123' -p 5432 -h 127.0.0.1 -c "CREATE DATABASE backupdb;"
gsql -d backupdb -U zackary -W 'zackary@123' -p 5432 -h 127.0.0.1 -f /sql_files/backup/backup_other.sql
```


