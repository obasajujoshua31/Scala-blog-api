# Articles schema

# --- !Ups
CREATE TABLE IF NOT EXISTS `scala_blog_api`.`articles` (
   `id` VARCHAR (255) NOT NULL,
   `content` VARCHAR(255) NOT NULL,
   `title` VARCHAR (255) NOT NULL,
   `created_at` VARCHAR(255) NOT NULL,
   `author_id` VARCHAR (255) NOT NULL ,
   `no_of_comments` INTEGER DEFAULT 0,
   `no_of_likes` INTEGER DEFAULT 0,
   PRIMARY KEY (`id`)
   )

   DEFAULT CHARACTER SET = utf8;

     ALTER TABLE `scala_blog_api`.`articles`
     ADD FOREIGN KEY (`author_id`) REFERENCES `scala_blog_api`.`users`(id);

   # --- !Downs
   drop table `scala_blog_api`.`articles`;