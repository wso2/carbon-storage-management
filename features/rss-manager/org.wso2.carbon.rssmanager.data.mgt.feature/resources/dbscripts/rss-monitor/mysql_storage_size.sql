SELECT
    table_schema "DATABASE_NAME",
    (sum( data_length + index_length ) / 1024 / 1024) "DISK_USAGE",
    (sum(data_free)/ 1024 / 1024) "FREE_SPACE"
FROM information_schema.TABLES
GROUP BY table_schema;