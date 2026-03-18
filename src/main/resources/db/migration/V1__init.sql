CREATE SCHEMA IF NOT EXISTS testdb;

SET search_path TO testdb;

-- Roles
CREATE TABLE IF NOT EXISTS roles (
  role_key VARCHAR(50) PRIMARY KEY,
  role_name VARCHAR(100) NOT NULL,
  use_yn BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Orgs
CREATE TABLE IF NOT EXISTS orgs (
  org_id BIGSERIAL PRIMARY KEY,
  parent_id BIGINT NULL,
  name VARCHAR(200) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  use_yn BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Users
CREATE TABLE IF NOT EXISTS users (
  user_id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(200) NOT NULL,
  name VARCHAR(200) NOT NULL,
  role_key VARCHAR(50) NOT NULL REFERENCES roles(role_key),
  org_id BIGINT NULL REFERENCES orgs(org_id),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Menus (Tree)
CREATE TABLE IF NOT EXISTS menus (
  menu_id BIGSERIAL PRIMARY KEY,
  parent_id BIGINT NULL REFERENCES menus(menu_id) ON DELETE CASCADE,
  name VARCHAR(200) NOT NULL,
  path VARCHAR(300),
  icon VARCHAR(100),
  sort_order INT NOT NULL DEFAULT 0,
  use_yn BOOLEAN NOT NULL DEFAULT TRUE,
  menu_type VARCHAR(20) NOT NULL DEFAULT 'MENU', -- MENU/GROUP/BOARD
  board_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS menu_roles (
  menu_id BIGINT NOT NULL REFERENCES menus(menu_id) ON DELETE CASCADE,
  role_key VARCHAR(50) NOT NULL REFERENCES roles(role_key) ON DELETE CASCADE,
  PRIMARY KEY (menu_id, role_key)
);

-- Boards
CREATE TABLE IF NOT EXISTS boards (
  board_id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  use_yn BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Posts
CREATE TABLE IF NOT EXISTS posts (
  post_id BIGSERIAL PRIMARY KEY,
  board_id BIGINT NOT NULL REFERENCES boards(board_id) ON DELETE CASCADE,
  title VARCHAR(300) NOT NULL,
  content TEXT NOT NULL,
  author_id BIGINT NOT NULL REFERENCES users(user_id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_posts_board_id ON posts(board_id);

-- Comments
CREATE TABLE IF NOT EXISTS comments (
  comment_id BIGSERIAL PRIMARY KEY,
  post_id BIGINT NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
  author_id BIGINT NOT NULL REFERENCES users(user_id),
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id);

-- Files
CREATE TABLE IF NOT EXISTS files (
  file_id BIGSERIAL PRIMARY KEY,
  original_name VARCHAR(500) NOT NULL,
  saved_name VARCHAR(500) NOT NULL,
  content_type VARCHAR(200),
  size_bytes BIGINT NOT NULL,
  storage_path VARCHAR(1000) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS post_files (
  post_id BIGINT NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
  file_id BIGINT NOT NULL REFERENCES files(file_id) ON DELETE CASCADE,
  PRIMARY KEY (post_id, file_id)
);

-- Common Codes
CREATE TABLE IF NOT EXISTS code_groups (
  group_key VARCHAR(100) PRIMARY KEY,
  group_name VARCHAR(200) NOT NULL,
  use_yn BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS codes (
  group_key VARCHAR(100) NOT NULL REFERENCES code_groups(group_key) ON DELETE CASCADE,
  code VARCHAR(100) NOT NULL,
  name VARCHAR(200) NOT NULL,
  value VARCHAR(500),
  sort_order INT NOT NULL DEFAULT 0,
  use_yn BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (group_key, code)
);

-- Seed data (idempotent-ish by UPSERT)
INSERT INTO roles(role_key, role_name, use_yn)
VALUES ('ADMIN','Administrator',TRUE), ('USER','User',TRUE)
ON CONFLICT (role_key) DO UPDATE SET role_name = EXCLUDED.role_name, use_yn = EXCLUDED.use_yn;

INSERT INTO orgs(org_id, parent_id, name, sort_order, use_yn)
VALUES (1, NULL, 'Company', 0, TRUE)
ON CONFLICT (org_id) DO UPDATE SET name = EXCLUDED.name, use_yn = EXCLUDED.use_yn;

SELECT setval(pg_get_serial_sequence('orgs','org_id'), (SELECT MAX(org_id) FROM orgs));

INSERT INTO users(user_id, username, password_hash, name, role_key, org_id, enabled)
VALUES
  (1, 'admin', '$2b$10$vwB9r1Xj08LFtfI8ni5NgOGqkbg1qo98YTXpU1aLer4Jt1f2/.TqC', 'Admin', 'ADMIN', 1, TRUE),
  (2, 'user',  '$2b$10$MirmxKckS0TwaZEPaIScr.Hj1iN9crkfXcd8uyye03Vfse90JD42y',  'User',  'USER',  1, TRUE)
ON CONFLICT (user_id) DO UPDATE SET
  username = EXCLUDED.username,
  password_hash = EXCLUDED.password_hash,
  name = EXCLUDED.name,
  role_key = EXCLUDED.role_key,
  org_id = EXCLUDED.org_id,
  enabled = EXCLUDED.enabled,
  updated_at = NOW();

SELECT setval(pg_get_serial_sequence('users','user_id'), (SELECT MAX(user_id) FROM users));

-- Base menus
INSERT INTO menus(menu_id, parent_id, name, path, icon, sort_order, use_yn, menu_type)
VALUES
  (1, NULL, 'Dashboard', '/dashboard', 'layout-dashboard', 0, TRUE, 'MENU'),
  (2, NULL, 'Boards', NULL, 'file-text', 10, TRUE, 'GROUP'),
  (3, NULL, 'Admin', NULL, 'settings', 20, TRUE, 'GROUP'),
  (4, 3, 'Common Codes', '/admin/codes', 'code', 0, TRUE, 'MENU'),
  (5, 3, 'Boards', '/admin/boards', 'boards', 10, TRUE, 'MENU'),
  (6, 3, 'Users', '/admin/users', 'users', 20, TRUE, 'MENU'),
  (7, 3, 'Orgs', '/admin/orgs', 'orgs', 30, TRUE, 'MENU'),
  (8, 3, 'Menus', '/admin/menus', 'menus', 40, TRUE, 'MENU'),
  (9, 3, 'Roles', '/admin/roles', 'roles', 50, TRUE, 'MENU'),
  (10, NULL, 'My Info', '/me', 'user', 30, TRUE, 'MENU')
ON CONFLICT (menu_id) DO UPDATE SET
  parent_id = EXCLUDED.parent_id,
  name = EXCLUDED.name,
  path = EXCLUDED.path,
  icon = EXCLUDED.icon,
  sort_order = EXCLUDED.sort_order,
  use_yn = EXCLUDED.use_yn,
  menu_type = EXCLUDED.menu_type,
  updated_at = NOW();

SELECT setval(pg_get_serial_sequence('menus','menu_id'), (SELECT MAX(menu_id) FROM menus));

-- Admin sees all
INSERT INTO menu_roles(menu_id, role_key)
SELECT menu_id, 'ADMIN' FROM menus
ON CONFLICT DO NOTHING;

-- User sees dashboard + boards + my info
INSERT INTO menu_roles(menu_id, role_key) VALUES
  (1,'USER'), (2,'USER'), (10,'USER')
ON CONFLICT DO NOTHING;

-- Default board + linked menu
INSERT INTO boards(board_id, name, description, use_yn)
VALUES (1, '공지사항', '기본 게시판', TRUE)
ON CONFLICT (board_id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  use_yn = EXCLUDED.use_yn,
  updated_at = NOW();

SELECT setval(pg_get_serial_sequence('boards','board_id'), (SELECT MAX(board_id) FROM boards));

INSERT INTO menus(menu_id, parent_id, name, path, icon, sort_order, use_yn, menu_type, board_id)
VALUES (11, 2, '공지사항', '/boards/1', 'clipboard-list', 0, TRUE, 'BOARD', 1)
ON CONFLICT (menu_id) DO UPDATE SET
  parent_id = EXCLUDED.parent_id,
  name = EXCLUDED.name,
  path = EXCLUDED.path,
  icon = EXCLUDED.icon,
  sort_order = EXCLUDED.sort_order,
  use_yn = EXCLUDED.use_yn,
  menu_type = EXCLUDED.menu_type,
  board_id = EXCLUDED.board_id,
  updated_at = NOW();

SELECT setval(pg_get_serial_sequence('menus','menu_id'), (SELECT MAX(menu_id) FROM menus));

INSERT INTO menu_roles(menu_id, role_key) VALUES (11,'ADMIN'),(11,'USER')
ON CONFLICT DO NOTHING;



-- Sample common codes
INSERT INTO code_groups(group_key, group_name, use_yn)
VALUES ('YN','Yes/No',TRUE)
ON CONFLICT (group_key) DO UPDATE SET group_name = EXCLUDED.group_name, use_yn = EXCLUDED.use_yn, updated_at = NOW();

INSERT INTO codes(group_key, code, name, value, sort_order, use_yn)
VALUES
  ('YN','Y','Yes','Y',0,TRUE),
  ('YN','N','No','N',10,TRUE)
ON CONFLICT (group_key, code) DO UPDATE SET
  name = EXCLUDED.name,
  value = EXCLUDED.value,
  sort_order = EXCLUDED.sort_order,
  use_yn = EXCLUDED.use_yn,
  updated_at = NOW();
