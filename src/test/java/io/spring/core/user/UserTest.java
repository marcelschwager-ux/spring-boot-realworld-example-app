package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_generated_id() {
    User user = new User("test@test.com", "test", "password", "bio", "image");
    assertThat(user.getId(), notNullValue());
    assertThat(user.getEmail(), is("test@test.com"));
    assertThat(user.getUsername(), is("test"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_update_user_fields_when_not_empty() {
    User user = new User("old@test.com", "old", "old", "old", "old");
    user.update("new@test.com", "", "new", "", "new");
    assertThat(user.getEmail(), is("new@test.com"));
    assertThat(user.getUsername(), is("old"));
    assertThat(user.getPassword(), is("new"));
    assertThat(user.getBio(), is("old"));
    assertThat(user.getImage(), is("new"));
  }

  @Test
  public void should_not_update_user_fields_when_empty() {
    User user = new User("old@test.com", "old", "old", "old", "old");
    user.update("", "", "", "", "");
    assertThat(user.getEmail(), is("old@test.com"));
    assertThat(user.getUsername(), is("old"));
    assertThat(user.getPassword(), is("old"));
    assertThat(user.getBio(), is("old"));
    assertThat(user.getImage(), is("old"));
  }

  @Test
  public void should_update_only_provided_fields() {
    User user = new User("old@test.com", "old", "old", "old", "old");
    user.update("new@test.com", "newuser", "", "newbio", "");
    assertThat(user.getEmail(), is("new@test.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("old"));
    assertThat(user.getBio(), is("newbio"));
    assertThat(user.getImage(), is("old"));
  }
}
