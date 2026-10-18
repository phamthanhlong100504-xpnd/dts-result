-- V001: baseline entity columns for a new or existing service database.
-- No DELETE/UPDATE/backfill of existing business rows; incompatible legacy data causes rollback.


CREATE OR REPLACE FUNCTION pg_temp.dts_schema_column(
  target_table text, column_name text, declaration text, expected_type regtype,
  required boolean, default_expression text
) RETURNS void LANGUAGE plpgsql AS $body$
DECLARE actual_type oid; is_required boolean; type_modifier integer; width integer;
BEGIN
  SELECT a.atttypid,a.attnotnull,a.atttypmod INTO actual_type,is_required,type_modifier FROM pg_attribute a
    WHERE a.attrelid=to_regclass('public.'||quote_ident(target_table))
      AND a.attname=column_name AND a.attnum>0 AND NOT a.attisdropped;
  IF actual_type IS NULL THEN
    EXECUTE format('ALTER TABLE public.%I ADD COLUMN %I %s',target_table,column_name,declaration);
    SELECT a.atttypid,a.attnotnull,a.atttypmod INTO actual_type,is_required,type_modifier FROM pg_attribute a
      WHERE a.attrelid=to_regclass('public.'||quote_ident(target_table)) AND a.attname=column_name AND NOT a.attisdropped;
  END IF;
  -- Never silently convert a differently typed live column or shrink existing text.
  IF actual_type<>expected_type::oid AND NOT(expected_type::oid='varchar'::regtype::oid AND actual_type='text'::regtype::oid) THEN
    RAISE EXCEPTION 'Schema mismatch %.%: unexpected column type; review before migration',target_table,column_name;
  END IF;
  width := substring(declaration from '(?i)VARCHAR\(([0-9]+)\)')::integer;
  IF actual_type='varchar'::regtype::oid AND width IS NOT NULL AND type_modifier>=4 AND type_modifier-4<width THEN
    EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I TYPE varchar(%s)',target_table,column_name,width);
  END IF;
  IF required AND NOT is_required THEN
    EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I SET NOT NULL',target_table,column_name);
  ELSIF NOT required AND is_required THEN
    EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I DROP NOT NULL',target_table,column_name);
  END IF;
  IF default_expression IS NOT NULL THEN
    EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I SET DEFAULT %s',target_table,column_name,default_expression);
  END IF;
END;
$body$;

CREATE TABLE IF NOT EXISTS public.learning_results (
  id UUID NOT NULL DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  source_id UUID NOT NULL,
  target_id UUID NOT NULL,
  source_type VARCHAR(30) NOT NULL,
  target_type VARCHAR(30) NOT NULL,
  attempt_no INT NOT NULL,
  score NUMERIC,
  max_score NUMERIC,
  progress NUMERIC NOT NULL DEFAULT 0,
  duration_seconds INT NOT NULL DEFAULT 0,
  result VARCHAR(30) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ,
  result_snapshot JSONB,
  metadata JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SELECT pg_temp.dts_schema_column('learning_results','id','UUID NOT NULL DEFAULT gen_random_uuid()','uuid'::regtype,true,'gen_random_uuid()');

SELECT pg_temp.dts_schema_column('learning_results','user_id','UUID NOT NULL','uuid'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','source_id','UUID NOT NULL','uuid'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','target_id','UUID NOT NULL','uuid'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','source_type','VARCHAR(30) NOT NULL','varchar'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','target_type','VARCHAR(30) NOT NULL','varchar'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','attempt_no','INT NOT NULL','integer'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','score','NUMERIC','numeric'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_results','max_score','NUMERIC','numeric'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_results','progress','NUMERIC NOT NULL DEFAULT 0','numeric'::regtype,true,'0');

SELECT pg_temp.dts_schema_column('learning_results','duration_seconds','INT NOT NULL DEFAULT 0','integer'::regtype,true,'0');

SELECT pg_temp.dts_schema_column('learning_results','result','VARCHAR(30) NOT NULL','varchar'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','started_at','TIMESTAMPTZ NOT NULL','timestamptz'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_results','completed_at','TIMESTAMPTZ','timestamptz'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_results','result_snapshot','JSONB','jsonb'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_results','metadata','JSONB','jsonb'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_results','created_at','TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP','timestamptz'::regtype,true,'CURRENT_TIMESTAMP');

SELECT pg_temp.dts_schema_column('learning_results','updated_at','TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP','timestamptz'::regtype,true,'CURRENT_TIMESTAMP');

DO $body$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM pg_constraint WHERE conrelid='public.learning_results'::regclass AND contype='p') THEN
   ALTER TABLE public.learning_results ADD CONSTRAINT pk_learning_results PRIMARY KEY(id);
 ELSIF NOT EXISTS(SELECT 1 FROM pg_constraint c JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attname='id'
   WHERE c.conrelid='public.learning_results'::regclass AND c.contype='p' AND c.conkey=ARRAY[a.attnum]::smallint[]) THEN
   RAISE EXCEPTION 'Unexpected primary key on learning_results';
 END IF;
END;
$body$;

CREATE TABLE IF NOT EXISTS public.learning_summaries (
  id UUID NOT NULL DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  target_id UUID NOT NULL,
  last_result_id UUID,
  target_type VARCHAR(30) NOT NULL,
  attempt_count INT NOT NULL DEFAULT 0,
  completion_count INT NOT NULL DEFAULT 0,
  best_score NUMERIC,
  latest_score NUMERIC,
  average_score NUMERIC,
  progress NUMERIC NOT NULL DEFAULT 0,
  total_duration_seconds INT NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
  last_activity_at TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  summary_snapshot JSONB,
  metadata JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SELECT pg_temp.dts_schema_column('learning_summaries','id','UUID NOT NULL DEFAULT gen_random_uuid()','uuid'::regtype,true,'gen_random_uuid()');

SELECT pg_temp.dts_schema_column('learning_summaries','user_id','UUID NOT NULL','uuid'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','target_id','UUID NOT NULL','uuid'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','last_result_id','UUID','uuid'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','target_type','VARCHAR(30) NOT NULL','varchar'::regtype,true,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','attempt_count','INT NOT NULL DEFAULT 0','integer'::regtype,true,'0');

SELECT pg_temp.dts_schema_column('learning_summaries','completion_count','INT NOT NULL DEFAULT 0','integer'::regtype,true,'0');

SELECT pg_temp.dts_schema_column('learning_summaries','best_score','NUMERIC','numeric'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','latest_score','NUMERIC','numeric'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','average_score','NUMERIC','numeric'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','progress','NUMERIC NOT NULL DEFAULT 0','numeric'::regtype,true,'0');

SELECT pg_temp.dts_schema_column('learning_summaries','total_duration_seconds','INT NOT NULL DEFAULT 0','integer'::regtype,true,'0');

SELECT pg_temp.dts_schema_column('learning_summaries','status','VARCHAR(30) NOT NULL DEFAULT ''NOT_STARTED''','varchar'::regtype,true,'''NOT_STARTED''');

SELECT pg_temp.dts_schema_column('learning_summaries','last_activity_at','TIMESTAMPTZ','timestamptz'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','completed_at','TIMESTAMPTZ','timestamptz'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','summary_snapshot','JSONB','jsonb'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','metadata','JSONB','jsonb'::regtype,false,NULL);

SELECT pg_temp.dts_schema_column('learning_summaries','created_at','TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP','timestamptz'::regtype,true,'CURRENT_TIMESTAMP');

SELECT pg_temp.dts_schema_column('learning_summaries','updated_at','TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP','timestamptz'::regtype,true,'CURRENT_TIMESTAMP');

DO $body$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM pg_constraint WHERE conrelid='public.learning_summaries'::regclass AND contype='p') THEN
   ALTER TABLE public.learning_summaries ADD CONSTRAINT pk_learning_summaries PRIMARY KEY(id);
 ELSIF NOT EXISTS(SELECT 1 FROM pg_constraint c JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attname='id'
   WHERE c.conrelid='public.learning_summaries'::regclass AND c.contype='p' AND c.conkey=ARRAY[a.attnum]::smallint[]) THEN
   RAISE EXCEPTION 'Unexpected primary key on learning_summaries';
 END IF;
END;
$body$;
