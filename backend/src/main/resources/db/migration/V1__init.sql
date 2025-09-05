-- =========================================================
-- BoardCraft — V1__init.sql (MySQL 8)
-- 관리 방침: JPA DDL 자동생성 금지, Flyway로만 관리
-- PK는 UUID 문자열(CHAR(36)) 사용
-- =========================================================

-- 공통: 기본 문자셋/콜레이션을 테이블마다 지정
-- ---------------------------------------------------------

-- USERS
CREATE TABLE IF NOT EXISTS users
(
    id            CHAR(36)     NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname      VARCHAR(50)  NOT NULL,
    enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ROLES
CREATE TABLE IF NOT EXISTS roles
(
    name VARCHAR(32) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- USER_ROLES (USER—ROLE M:N)
CREATE TABLE IF NOT EXISTS user_roles
(
    user_id   CHAR(36)    NOT NULL,
    role_name VARCHAR(32) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_name) REFERENCES roles (name) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- CATEGORIES (단일 카테고리 연결)
CREATE TABLE IF NOT EXISTS categories
(
    id   CHAR(36)     NOT NULL,
    name VARCHAR(80)  NOT NULL,
    path VARCHAR(160) NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_path UNIQUE (path)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- POSTS
CREATE TABLE IF NOT EXISTS posts
(
    id           CHAR(36)     NOT NULL,
    author_id    CHAR(36)     NOT NULL,
    slug         VARCHAR(200) NOT NULL,
    title        VARCHAR(160) NOT NULL,
    content_md   MEDIUMTEXT   NOT NULL,
    category_id  CHAR(36)     NULL,
    is_published TINYINT(1)   NOT NULL DEFAULT 0,
    published_at DATETIME(3)  NULL,
    is_hidden    TINYINT(1)   NOT NULL DEFAULT 0,
    is_deleted   TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_posts PRIMARY KEY (id),
    CONSTRAINT uk_posts_slug UNIQUE (slug),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories (id),
    INDEX idx_posts_slug (slug),
    INDEX idx_posts_publist (is_published, published_at, created_at),
    INDEX idx_posts_category (category_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- TAGS
CREATE TABLE IF NOT EXISTS tags
(
    id   CHAR(36)    NOT NULL,
    name VARCHAR(60) NOT NULL,
    CONSTRAINT pk_tags PRIMARY KEY (id),
    CONSTRAINT uk_tags_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- POST_TAGS (POST—TAG M:N)
CREATE TABLE IF NOT EXISTS post_tags
(
    post_id CHAR(36) NOT NULL,
    tag_id  CHAR(36) NOT NULL,
    CONSTRAINT pk_post_tags PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE,
    INDEX idx_post_tags_post (post_id),
    INDEX idx_post_tags_tag (tag_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- COMMENTS (1-레벨 대댓글: 애플리케이션 검증으로 제한)
CREATE TABLE IF NOT EXISTS comments
(
    id         CHAR(36)    NOT NULL,
    post_id    CHAR(36)    NOT NULL,
    author_id  CHAR(36)    NOT NULL,
    parent_id  CHAR(36)    NULL,
    content_md MEDIUMTEXT  NOT NULL,
    is_hidden  TINYINT(1)  NOT NULL DEFAULT 0,
    is_deleted TINYINT(1)  NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments (id),
    INDEX idx_comments_post_created (post_id, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ATTACHMENTS
CREATE TABLE IF NOT EXISTS attachments
(
    id            CHAR(36)     NOT NULL,
    post_id       CHAR(36)     NOT NULL,
    key_path      VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_attachments PRIMARY KEY (id),
    CONSTRAINT fk_attachments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    INDEX idx_attachments_post (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- REPORTS (신고)
CREATE TABLE IF NOT EXISTS reports
(
    id           CHAR(36)                NOT NULL,
    reporter_id  CHAR(36)                NOT NULL,
    subject_id   CHAR(36)                NOT NULL,
    subject_type ENUM ('POST','COMMENT') NOT NULL,
    reason       VARCHAR(255)            NOT NULL,
    created_at   DATETIME(3)             NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_reports PRIMARY KEY (id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_reports_subject (subject_type, subject_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- USER_RESTRICTIONS (블럭/제한)
CREATE TABLE IF NOT EXISTS user_restrictions
(
    id               CHAR(36)     NOT NULL,
    user_id          CHAR(36)     NOT NULL,
    restricted_until DATETIME(3)  NOT NULL,
    reason           VARCHAR(255) NULL,
    created_by       CHAR(36)     NOT NULL, -- MOD/ADMIN
    created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_user_restrictions PRIMARY KEY (id),
    CONSTRAINT fk_user_restrictions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_restrictions_actor FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_user_restrictions_user (user_id, restricted_until)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- (선택) AUDIT_LOGS — 운영 감사 로그가 필요하면 주석 해제
-- CREATE TABLE IF NOT EXISTS audit_logs (
--   id          CHAR(36)    NOT NULL,
--   actor_id    CHAR(36)    NOT NULL,
--   action      VARCHAR(64) NOT NULL,
--   target_id   CHAR(36)    NULL,
--   target_type VARCHAR(32) NULL,
--   metadata    JSON        NULL,
--   created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
--   CONSTRAINT pk_audit_logs PRIMARY KEY (id),
--   CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id),
--   INDEX idx_audit_actor_created (actor_id, created_at)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 초기 데이터: ROLES 시드
INSERT IGNORE INTO roles(name)
VALUES ('USER'),
       ('MOD'),
       ('ADMIN');
