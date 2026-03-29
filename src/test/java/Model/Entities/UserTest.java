package Model.Entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        List<String> interests = List.of("Technology", "Gaming");
        List<String> eventTypes = List.of("Workshops", "Meetups");
        user = new User(
                1,
                "John Doe", // Combined first and last name into a single name
                "john.doe@example.com",
                "password123",
                "New York",
                interests,
                eventTypes,
                "A software developer passionate about new tech."
        );
    }

    @Test
    void testFullConstructor() {
        assertEquals(1, user.getId());
        assertEquals("John Doe", user.getName()); // Changed to getName()
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("New York", user.getLocation());
        assertEquals(2, user.getInterests().size());
        assertTrue(user.getInterests().contains("Technology"));
        assertEquals(2, user.getEventTypes().size());
        assertTrue(user.getEventTypes().contains("Meetups"));
        assertEquals("A software developer passionate about new tech.", user.getBio());
        assertEquals("/src/main/resources/images/missing-image.png", user.getProfileImagePath());
    }

    @Test
    void testEmptyConstructorAndSetters() {
        User emptyUser = new User();
        List<String> interests = List.of("Art");
        List<String> eventTypes = List.of("Art Exhibitions");

        emptyUser.setId(2);
        emptyUser.setName("Alice Smith"); // Changed to setName()
        emptyUser.setEmail("alice@example.com");
        emptyUser.setPassword("alicePass");
        emptyUser.setLocation("London");
        emptyUser.setInterests(interests);
        emptyUser.setEventTypes(eventTypes);
        emptyUser.setBio("An artist from London.");
        emptyUser.setProfileImagePath("new/path.png");

        assertEquals(2, emptyUser.getId());
        assertEquals("Alice Smith", emptyUser.getName()); // Changed to getName()
        assertEquals("alice@example.com", emptyUser.getEmail());
        assertEquals("alicePass", emptyUser.getPassword());
        assertEquals("London", emptyUser.getLocation());
        assertEquals(interests, emptyUser.getInterests());
        assertEquals(eventTypes, emptyUser.getEventTypes());
        assertEquals("An artist from London.", emptyUser.getBio());
        assertEquals("new/path.png", emptyUser.getProfileImagePath());
    }

    @Test
    void testSettersAllowNull() {
        user.setName(null); // Changed to setName()
        user.setEmail(null);
        user.setPassword(null);
        user.setLocation(null);
        user.setInterests(null);
        user.setEventTypes(null);
        user.setBio(null);
        user.setProfileImagePath(null);

        assertNull(user.getName()); // Changed to getName()
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getLocation());
        assertNull(user.getInterests());
        assertNull(user.getEventTypes());
        assertNull(user.getBio());
        assertNull(user.getProfileImagePath());
    }

    @Test
    void testToStringContainsFields() {
        String result = user.toString();
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='John Doe'")); // Changed to name
        assertTrue(result.contains("email='john.doe@example.com'"));
        assertTrue(result.contains("location='New York'"));
        assertTrue(result.contains("interests=[Technology, Gaming]"));
        assertTrue(result.contains("eventTypes=[Workshops, Meetups]"));
        assertTrue(result.contains("bio='A software developer passionate about new tech.'"));
        assertTrue(result.contains("profileImagePath='/src/main/resources/images/missing-image.png'"));
    }
}
