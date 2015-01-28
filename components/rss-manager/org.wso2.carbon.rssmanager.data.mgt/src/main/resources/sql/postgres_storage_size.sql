SELECT pg_database.datname AS DATABASE_NAME,  
       pg_size_pretty(pg_database_size(pg_database.datname)) AS DISK_USAGE  
  FROM pg_database;