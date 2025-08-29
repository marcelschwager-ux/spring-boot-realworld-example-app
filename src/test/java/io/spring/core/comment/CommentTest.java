package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_generated_id() {
    Comment comment = new Comment("test body", "user123", "article456");
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is("test body"));
    assertThat(comment.getUserId(), is("user123"));
    assertThat(comment.getArticleId(), is("article456"));
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_create_comment_with_current_timestamp() {
    Comment comment = new Comment("test body", "user123", "article456");
    assertThat(comment.getCreatedAt(), notNullValue());
  }
}
