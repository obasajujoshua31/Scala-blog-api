# Articles schema

# --- !Ups
CREATE TABLE IF NOT EXISTS `scala_blog_api`.`comments` (
                                                           `id` VARCHAR (255) NOT NULL,
                                                           `content` VARCHAR(255) NOT NULL,
                                                           `reviewer_id` VARCHAR (255) NOT NULL,
                                                           `article_id` VARCHAR(255) NOT NULL,
                                                           `no_of_likes` INTEGER DEFAULT 0,
                                                           PRIMARY KEY (`id`),
    FOREIGN KEY (`reviewer_id`) REFERENCES `scala_blog_api`.`users`(`id`))

    DEFAULT CHARACTER SET = utf8;

# --- !Downs
drop table `scala_blog_api`.`comments`;