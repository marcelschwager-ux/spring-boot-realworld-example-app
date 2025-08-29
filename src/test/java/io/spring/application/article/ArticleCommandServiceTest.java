package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ArticleCommandService.class, MyBatisArticleRepository.class})
public class ArticleCommandServiceTest extends DbTestBase {

  @Autowired private ArticleCommandService articleCommandService;
  @Autowired private ArticleRepository articleRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "test", "123", "", "");
  }

  @Test
  public void should_create_article_success() {
    NewArticleParam param =
        new NewArticleParam("title", "desc", "body", Arrays.asList("java", "spring"));

    Article article = articleCommandService.createArticle(param, user);

    assertThat(article.getTitle(), is("title"));
    assertThat(article.getDescription(), is("desc"));
    assertThat(article.getBody(), is("body"));
    assertThat(article.getUserId(), is(user.getId()));
    assertThat(article.getId(), notNullValue());
    assertThat(article.getSlug(), is("title"));
  }

  @Test
  public void should_update_article_success() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    UpdateArticleParam param = new UpdateArticleParam("new title", "new body", "new desc");
    Article updatedArticle = articleCommandService.updateArticle(article, param);

    assertThat(updatedArticle.getTitle(), is("new title"));
    assertThat(updatedArticle.getDescription(), is("new desc"));
    assertThat(updatedArticle.getBody(), is("new body"));
    assertThat(updatedArticle.getSlug(), is("new-title"));
  }

  @Test
  public void should_update_article_with_partial_fields() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    UpdateArticleParam param = new UpdateArticleParam("new title", "", "");
    Article updatedArticle = articleCommandService.updateArticle(article, param);

    assertThat(updatedArticle.getTitle(), is("new title"));
    assertThat(updatedArticle.getDescription(), is("old desc"));
    assertThat(updatedArticle.getBody(), is("old body"));
  }
}
