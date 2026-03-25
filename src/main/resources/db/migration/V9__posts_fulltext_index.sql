-- Add GIN index for full-text search on posts (title + content)
CREATE INDEX IF NOT EXISTS idx_posts_fulltext
ON posts USING GIN (to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, '')));
