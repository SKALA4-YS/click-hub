-- 관리자 전용 ID/PW 로그인을 OAuth 사용자와 구분하기 위한 로컬 인증 제공자.
-- PostgreSQL은 ALTER TYPE으로 추가한 enum 값을 같은 트랜잭션에서 바로 사용할 수 없으므로,
-- 컬럼과 제약조건 변경은 다음 버전(V5)에서 수행한다.
ALTER TYPE social_login_provider ADD VALUE IF NOT EXISTS 'LOCAL';
