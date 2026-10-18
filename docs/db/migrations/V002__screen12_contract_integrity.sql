-- V002: Screen12-compatible checks, indexes and service-owned update triggers.

-- Service-owned helper; does not replace another service's generic trigger function.
CREATE OR REPLACE FUNCTION public.dts_result_set_updated_at()
RETURNS trigger LANGUAGE plpgsql AS $body$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$body$;


CREATE OR REPLACE FUNCTION pg_temp.dts_schema_index(
  index_name text, target_table text, columns_expected text[], is_unique boolean,
  predicate text DEFAULT NULL
) RETURNS void LANGUAGE plpgsql AS $body$
DECLARE info record; columns_sql text;
BEGIN
  SELECT i.indrelid,i.indisunique,i.indisvalid,i.indisready,i.indpred IS NOT NULL AS partial,
    ARRAY(SELECT a.attname::text FROM unnest(i.indkey) WITH ORDINALITY k(attnum,ordinal)
      JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=k.attnum ORDER BY k.ordinal) AS columns_actual
    INTO info FROM pg_index i WHERE i.indexrelid=to_regclass('public.'||quote_ident(index_name));
  IF FOUND THEN
    IF info.indrelid<>to_regclass('public.'||quote_ident(target_table)) OR info.indisunique<>is_unique
      OR NOT info.indisvalid OR NOT info.indisready OR info.columns_actual<>columns_expected
      OR info.partial<>(predicate IS NOT NULL) THEN
      RAISE EXCEPTION 'Existing index % has an incompatible definition; migration stopped',index_name;
    END IF;
    RETURN;
  END IF;
  SELECT string_agg(quote_ident(value),', ') INTO columns_sql FROM unnest(columns_expected) value;
  EXECUTE format('CREATE %s INDEX %I ON public.%I (%s)%s',CASE WHEN is_unique THEN 'UNIQUE' ELSE '' END,
    index_name,target_table,columns_sql,CASE WHEN predicate IS NULL THEN '' ELSE ' WHERE '||predicate END);
END;
$body$;

ALTER TABLE public.learning_results DROP CONSTRAINT IF EXISTS ck_learning_results_source_type;
ALTER TABLE public.learning_results ADD CONSTRAINT ck_learning_results_source_type CHECK (source_type IN ('EXAM_SESSION', 'PRACTICE_SESSION', 'LESSON_SESSION', 'ASSIGNMENT_SESSION'));

ALTER TABLE public.learning_results DROP CONSTRAINT IF EXISTS ck_learning_results_target_type;
ALTER TABLE public.learning_results ADD CONSTRAINT ck_learning_results_target_type CHECK (target_type IN ('EXAM', 'PRACTICE_SET', 'CHAPTER', 'LESSON', 'LEARNING_PROGRAM'));

ALTER TABLE public.learning_results DROP CONSTRAINT IF EXISTS ck_learning_results_result;
ALTER TABLE public.learning_results ADD CONSTRAINT ck_learning_results_result CHECK (result IN ('PASSED', 'FAILED', 'COMPLETED', 'ABANDONED', 'EXPIRED', 'SUBMITTED'));

ALTER TABLE public.learning_summaries DROP CONSTRAINT IF EXISTS ck_learning_summaries_target_type;
ALTER TABLE public.learning_summaries ADD CONSTRAINT ck_learning_summaries_target_type CHECK (target_type IN ('EXAM', 'PRACTICE_SET', 'CHAPTER', 'LESSON', 'LEARNING_PROGRAM'));

ALTER TABLE public.learning_summaries DROP CONSTRAINT IF EXISTS ck_learning_summaries_status;
ALTER TABLE public.learning_summaries ADD CONSTRAINT ck_learning_summaries_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'));

SELECT pg_temp.dts_schema_index('uq_learning_results_source','learning_results',ARRAY['source_id','source_type'],true,NULL);

SELECT pg_temp.dts_schema_index('uq_learning_results_user_target_attempt','learning_results',ARRAY['user_id','target_type','target_id','attempt_no'],true,NULL);

SELECT pg_temp.dts_schema_index('ix_learning_results_user_id','learning_results',ARRAY['user_id'],false,NULL);

SELECT pg_temp.dts_schema_index('uq_learning_summaries_user_target','learning_summaries',ARRAY['user_id','target_type','target_id'],true,NULL);

SELECT pg_temp.dts_schema_index('ix_learning_summaries_user_id','learning_summaries',ARRAY['user_id'],false,NULL);

SELECT pg_temp.dts_schema_index('ix_learning_summaries_status','learning_summaries',ARRAY['status'],false,NULL);

DROP TRIGGER IF EXISTS trg_learning_results_updated_at ON public.learning_results;
CREATE TRIGGER trg_learning_results_updated_at BEFORE UPDATE ON public.learning_results
FOR EACH ROW EXECUTE FUNCTION public.dts_result_set_updated_at();

DROP TRIGGER IF EXISTS trg_learning_summaries_updated_at ON public.learning_summaries;
CREATE TRIGGER trg_learning_summaries_updated_at BEFORE UPDATE ON public.learning_summaries
FOR EACH ROW EXECUTE FUNCTION public.dts_result_set_updated_at();
