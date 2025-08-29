package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@Import({UserService.class, MyBatisUserRepository.class})
public class UserServiceTest extends DbTestBase {

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @MockBean private PasswordEncoder passwordEncoder;

  @Test
  public void should_create_user_with_encoded_password() {
    when(passwordEncoder.encode("password")).thenReturn("encoded");
    RegisterParam param = new RegisterParam("test@test.com", "test", "password");

    User user = userService.createUser(param);

    assertThat(user.getEmail(), is("test@test.com"));
    assertThat(user.getUsername(), is("test"));
    assertThat(user.getPassword(), is("encoded"));
    assertThat(user.getId(), notNullValue());
  }

  @Test
  public void should_update_user_success() {
    User user = new User("old@test.com", "old", "old", "old", "old");
    userRepository.save(user);

    UpdateUserParam param = new UpdateUserParam("new@test.com", "new", "new", "new", "new");
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertThat(user.getEmail(), is("new@test.com"));
    assertThat(user.getUsername(), is("new"));
    assertThat(user.getPassword(), is("new"));
    assertThat(user.getBio(), is("new"));
    assertThat(user.getImage(), is("new"));
  }
}
