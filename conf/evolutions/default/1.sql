# Users schema

# --- !Ups
CREATE TABLE IF NOT EXISTS `scala_blog_api`.`users` (
   `id` VARCHAR (255) NOT NULL,
   `email` VARCHAR(255) NOT NULL,
   `password` VARCHAR (255) NOT NULL,
   `name` VARCHAR(255) NOT NULL,
   PRIMARY KEY (`id`))

   DEFAULT CHARACTER SET = utf8;
   CREATE INDEX email_index ON `scala_blog_api`.`users` (email);

   # --- !Downs
   ALTER TABLE `scala_blog_api`.`users`
   DROP INDEX email_index;

   drop table `scala_blog_api`.`users`;