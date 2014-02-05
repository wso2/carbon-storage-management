select DISTINCT FIRST_VALUE(owner) OVER (PARTITION BY DISK_USAGE ORDER BY owner)as DATABASE_NAME ,DISK_USAGE
from (SELECT sum(total_table_meg)over (partition by owner) as DISK_USAGE, realsize,owner FROM (
  SELECT
     owner, object_name, object_type, table_name, ROUND(bytes)/1024/1024 AS meg,bytes/1024/1024 as realSize,
    tablespace_name, extents, initial_extent,
    Sum(bytes/1024/1024) OVER (PARTITION BY table_name,owner) AS total_table_meg
  FROM (
    -- Tables
    SELECT owner, segment_name AS object_name, 'TABLE' AS object_type,
          segment_name AS table_name, bytes,
          tablespace_name, extents, initial_extent
    FROM   dba_segments
    WHERE  segment_type IN ('TABLE', 'TABLE PARTITION', 'TABLE SUBPARTITION')
    UNION ALL
    -- Indexes
    SELECT i.owner, i.index_name AS object_name, 'INDEX' AS object_type,
          i.table_name, s.bytes,
          s.tablespace_name, s.extents, s.initial_extent
    FROM   dba_indexes i, dba_segments s
    WHERE  s.segment_name = i.index_name
    AND    s.owner = i.owner
    AND    s.segment_type IN ('INDEX', 'INDEX PARTITION', 'INDEX SUBPARTITION')
    -- LOB Segments
    UNION ALL
    SELECT l.owner, l.column_name AS object_name, 'LOB_COLUMN' AS object_type,
          l.table_name, s.bytes,
          s.tablespace_name, s.extents, s.initial_extent
    FROM   dba_lobs l, dba_segments s
    WHERE  s.segment_name = l.segment_name
    AND    s.owner = l.owner
    AND    s.segment_type = 'LOBSEGMENT'
    -- LOB Indexes
    UNION ALL
    SELECT l.owner, l.column_name AS object_name, 'LOB_INDEX' AS object_type,
          l.table_name, s.bytes,
          s.tablespace_name, s.extents, s.initial_extent
    FROM   dba_lobs l, dba_segments s
    WHERE  s.segment_name = l.index_name
    AND    s.owner = l.owner
    AND    s.segment_type = 'LOBINDEX'
  )
  --WHERE owner = UPPER('am140_reg')
)
--AND total_table_meg > 10
ORDER BY owner,total_table_meg DESC, meg DESC
)
--where ROWNUM <=50
