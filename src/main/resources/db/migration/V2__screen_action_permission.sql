-- 화면 테이블
CREATE TABLE screens (
  screen_id   SERIAL PRIMARY KEY,
  screen_key  VARCHAR(100) UNIQUE NOT NULL,
  screen_name VARCHAR(200) NOT NULL,
  use_yn      BOOLEAN NOT NULL DEFAULT TRUE
);

-- 화면별 액션 테이블
CREATE TABLE screen_actions (
  action_id   SERIAL PRIMARY KEY,
  screen_id   INT NOT NULL REFERENCES screens(screen_id) ON DELETE CASCADE,
  action_key  VARCHAR(100) NOT NULL,
  action_name VARCHAR(200) NOT NULL,
  use_yn      BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (screen_id, action_key)
);

-- 역할-액션 매핑 테이블
CREATE TABLE role_actions (
  role_key  VARCHAR(100) NOT NULL REFERENCES roles(role_key) ON DELETE CASCADE,
  action_id INT NOT NULL REFERENCES screen_actions(action_id) ON DELETE CASCADE,
  PRIMARY KEY (role_key, action_id)
);

-- 기본 화면 데이터
INSERT INTO screens (screen_key, screen_name) VALUES
  ('BOARD_POST',    '게시글'),
  ('BOARD_COMMENT', '댓글'),
  ('ADMIN_USERS',   '사용자 관리'),
  ('ADMIN_ROLES',   '역할 관리'),
  ('ADMIN_BOARDS',  '게시판 관리'),
  ('ADMIN_ORGS',    '조직 관리'),
  ('ADMIN_MENUS',   '메뉴 관리'),
  ('ADMIN_CODES',   '코드 관리'),
  ('ADMIN_SCREENS', '화면/액션 권한 관리');

-- 기본 액션 데이터
INSERT INTO screen_actions (screen_id, action_key, action_name) VALUES
  ((SELECT screen_id FROM screens WHERE screen_key='BOARD_POST'),    'CREATE', '게시글 작성'),
  ((SELECT screen_id FROM screens WHERE screen_key='BOARD_POST'),    'EDIT',   '게시글 수정'),
  ((SELECT screen_id FROM screens WHERE screen_key='BOARD_POST'),    'DELETE', '게시글 삭제'),
  ((SELECT screen_id FROM screens WHERE screen_key='BOARD_COMMENT'), 'CREATE', '댓글 작성'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_USERS'),   'CREATE', '사용자 추가'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_USERS'),   'EDIT',   '사용자 수정'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_USERS'),   'DELETE', '사용자 삭제'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_ROLES'),   'CREATE', '역할 추가'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_ROLES'),   'EDIT',   '역할 수정'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_ROLES'),   'DELETE', '역할 삭제'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_BOARDS'),  'CREATE', '게시판 추가'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_BOARDS'),  'EDIT',   '게시판 수정'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_BOARDS'),  'DELETE', '게시판 삭제'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_ORGS'),    'CREATE', '조직 추가'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_ORGS'),    'EDIT',   '조직 수정'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_ORGS'),    'DELETE', '조직 삭제'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_MENUS'),   'CREATE', '메뉴 추가'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_MENUS'),   'EDIT',   '메뉴 수정'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_MENUS'),   'DELETE', '메뉴 삭제'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_CODES'),   'CREATE', '코드 추가'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_CODES'),   'EDIT',   '코드 수정'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_CODES'),   'DELETE', '코드 삭제'),
  ((SELECT screen_id FROM screens WHERE screen_key='ADMIN_SCREENS'), 'MANAGE', '화면/액션 권한 관리');

-- ADMIN 역할에 모든 액션 부여
INSERT INTO role_actions (role_key, action_id)
SELECT 'ADMIN', action_id FROM screen_actions;
