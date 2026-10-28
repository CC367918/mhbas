-- 一次性迁移脚本：将旧版 t_* 表迁移为文档定义的简洁表结构。
-- 执行前会保留 v1 数据快照；确认迁移无误后可自行删除 backup_v1_* 表。

CREATE TABLE IF NOT EXISTS backup_v1_user AS SELECT * FROM t_user;
CREATE TABLE IF NOT EXISTS backup_v1_step_record AS SELECT * FROM t_step_record;
CREATE TABLE IF NOT EXISTS backup_v1_fitness_record AS SELECT * FROM t_fitness_record;
CREATE TABLE IF NOT EXISTS backup_v1_sleep_record AS SELECT * FROM t_sleep_record;
CREATE TABLE IF NOT EXISTS backup_v1_water_record AS SELECT * FROM t_water_record;
CREATE TABLE IF NOT EXISTS backup_v1_meal_record AS SELECT * FROM t_meal_record;
CREATE TABLE IF NOT EXISTS backup_v1_goal AS SELECT * FROM t_goal;

RENAME TABLE
    t_user TO `user`,
    t_step_record TO step_record,
    t_fitness_record TO fitness_record,
    t_sleep_record TO sleep_record,
    t_water_record TO water_record,
    t_meal_record TO meal_record,
    t_goal TO goal;

ALTER TABLE `user`
    DROP COLUMN gender,
    DROP COLUMN age,
    DROP COLUMN height_cm,
    DROP COLUMN occupation,
    DROP COLUMN city,
    DROP COLUMN phone,
    DROP COLUMN email,
    DROP COLUMN bio,
    DROP COLUMN created_at,
    DROP COLUMN updated_at,
    ADD COLUMN wx_openid VARCHAR(50) COMMENT '微信 OpenID，用于未来微信小程序登录' AFTER avatar_url,
    ADD UNIQUE KEY uk_user_wx_openid (wx_openid);

ALTER TABLE step_record
    DROP COLUMN updated_at;

ALTER TABLE fitness_record
    DROP COLUMN created_at;

ALTER TABLE sleep_record
    DROP COLUMN created_at;

ALTER TABLE water_record
    DROP COLUMN created_at;

ALTER TABLE meal_record
    DROP COLUMN meal_time,
    DROP COLUMN recorded;

-- 保留最早创建的目标，清理完全重复的历史目标后再添加唯一约束。
DELETE duplicate_goal
FROM goal duplicate_goal
JOIN goal kept_goal
  ON duplicate_goal.user_id = kept_goal.user_id
 AND duplicate_goal.goal_type = kept_goal.goal_type
 AND duplicate_goal.target_value = kept_goal.target_value
 AND duplicate_goal.period = kept_goal.period
 AND duplicate_goal.id > kept_goal.id;

ALTER TABLE goal
    ADD UNIQUE KEY uk_goal_user_type_target_period
        (user_id, goal_type, target_value, period);
