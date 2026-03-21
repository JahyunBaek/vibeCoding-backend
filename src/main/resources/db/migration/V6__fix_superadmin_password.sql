SET search_path TO testdb;

-- superadmin 비밀번호를 Admin1234! 로 재설정
UPDATE users
   SET password_hash = '$2b$10$7F2HgEo2xtMgAQZBE.aB7ulqihtZ.W0s/x6k9o93mOewy1WFpgBeq'
 WHERE username = 'superadmin';
