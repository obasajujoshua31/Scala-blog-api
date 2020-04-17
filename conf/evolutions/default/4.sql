# Articles schema

# --- !Ups
CREATE TABLE IF NOT EXISTS `scala_blog_api`.`likes` (
                                                           `id` VARCHAR (255) NOT NULL,
                                                           `article_id` VARCHAR(255),
                                                           `comment_id` VARCHAR (255),
                                                           `reactor_id` VARCHAR(255) NOT NULL,
                                                           PRIMARY KEY (`id`),
    FOREIGN KEY (`reactor_id`) REFERENCES `scala_blog_api`.`users`(`id`))
    DEFAULT CHARACTER SET = utf8;

# --- !Downs
drop table `scala_blog_api`.`likes`