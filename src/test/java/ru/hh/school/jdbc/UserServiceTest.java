package ru.hh.school.jdbc;

import org.hibernate.SessionFactory;
import org.hibernate.TransientObjectException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class UserServiceTest {

  private static UserService userService;
  
  @BeforeClass
  public static void setUp() {
    SessionFactory sessionFactory = createSessionFactory();
    
    userService = new UserService(
      sessionFactory, 
      new UserDaoHiberImpl(sessionFactory)
    );
  }

  private static SessionFactory createSessionFactory() {
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
      .loadProperties("hibernate.properties")
      .build();

    Metadata metadata = new MetadataSources(serviceRegistry)
      // ToDo add entity class
      .addAnnotatedClass(User.class)
      .buildMetadata();

    return metadata.buildSessionFactory();
  }


  @Before
  public void cleanUpDb() {
    userService.deleteAll();
  }

  @Test
  public void getAllUsersShouldReturnEmptySet() {
    assertEquals(Collections.emptySet(), userService.getAll());
  }

  @Test
  public void saveNewUserShouldInsertDbRow() {
    User user = User.newUser("John", "Lennon");
    userService.saveNew(user);
    assertEquals(Set.of(user), userService.getAll());
  }

  @Test(expected = PersistenceException.class)
  public void savingOfExistingUserShouldBePrevented() {
    User user = User.newUser("John", "Lennon");
    userService.saveNew(user);
    userService.saveNew(user);

    fail();
  }

  @Test
  public void updateFirstNameShouldSucceed() {
    User user = User.newUser("John", "Lennon");
    userService.saveNew(user);
    userService.changeFirstName(user.getId(), "Paul");

    assertEquals(
      "Paul",
      userService.getBy(user.getId()).map(User::getFirstName).get()
    );
  }

  @Test
  public void getByIdShouldReturnUserIfRowExists() {
    User user = User.newUser("John", "Lennon");
    userService.saveNew(user);

    Optional<User> extractedUser = userService.getBy(user.getId());

    assertTrue(extractedUser.isPresent());
    assertEquals(user, extractedUser.get());
  }


  @Test
  public void getByIdShouldReturnEmptyIfRowDoesntExist() {
    assertFalse(userService.getBy(-1).isPresent());
  }

  @Test
  public void deleteUserShouldDeleteDbRow() {
    User user = User.newUser("John", "Lennon");
    userService.saveNew(user);

    Optional<User> extractedUser = userService.getBy(user.getId());
    assertTrue(extractedUser.isPresent());

    userService.deleteBy(user.getId());

    extractedUser = userService.getBy(user.getId());
    assertFalse(extractedUser.isPresent());
  }

  @Test(expected = TransientObjectException.class)
  public void updateShouldThrowExceptionForNewUsers() {
    User user = User.newUser("John", "Lennon");
    userService.update(user);

    fail();
  }

  @Test
  public void updateShouldUpdateDbRowOfExistingUser() {
    User user = User.newUser("Ringo", "Lennon");
    userService.saveNew(user);

    user.setFirstName("John");
    userService.update(user);

    assertEquals(
      "John",
      userService.getBy(user.getId()).map(User::getFirstName).orElse(null)
    );
  }
  
}
