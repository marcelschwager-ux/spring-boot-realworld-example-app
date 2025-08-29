package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_allow_article_author_to_write() {
    User user = new User("test@test.com", "test", "123", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList(), user.getId());

    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_deny_non_author_to_write_article() {
    User user1 = new User("test1@test.com", "test1", "123", "", "");
    User user2 = new User("test2@test.com", "test2", "123", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList(), user1.getId());

    assertFalse(AuthorizationService.canWriteArticle(user2, article));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "123", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "123", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList(), articleAuthor.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "123", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "123", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList(), articleAuthor.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  public void should_deny_non_author_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "123", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "123", "", "");
    User otherUser = new User("other@test.com", "other", "123", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList(), articleAuthor.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(otherUser, article, comment));
  }
}
