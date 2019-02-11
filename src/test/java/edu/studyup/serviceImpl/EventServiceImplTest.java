package edu.studyup.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.studyup.entity.Event;
import edu.studyup.entity.Location;
import edu.studyup.entity.Student;
import edu.studyup.util.DataStorage;
import edu.studyup.util.StudyUpException;

class EventServiceImplTest {

	EventServiceImpl eventServiceImpl;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		eventServiceImpl = new EventServiceImpl();
		//Create Student
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);

		//Create Event1 in the past
		Event event1 = new Event();
		event1.setEventID(1);
		event1.setDate(new Date(0));
		event1.setName("Event 1");
		event1.setLocation(new Location(-122, 37));
		event1.setStudents(new ArrayList<>());
		event1.getStudents().add(student);
		DataStorage.eventData.put(event1.getEventID(), event1);

		//Create Event2 in the distant future
		Event event2 = new Event();
		event2.setEventID(2);
		event2.setDate(new Date(java.sql.Date.valueOf("8099-01-01").getTime()));
		event2.setName("Event 2");
		event2.setLocation(new Location(-122, 37));
		event2.setStudents(new ArrayList<>());
		event2.getStudents().add(student);
		DataStorage.eventData.put(event2.getEventID(), event2);
	}

	@AfterEach
	void tearDown() throws Exception {
		DataStorage.eventData.clear();
	}

	@Test
	void testUpdateEventName_GoodCase() throws StudyUpException {
		int eventID = 1;
		eventServiceImpl.updateEventName(eventID, "Renamed Event 1");
		assertEquals("Renamed Event 1", DataStorage.eventData.get(eventID).getName());
	}

	@Test
	void testUpdateEvent_WrongEventID_badCase() {
		int eventID = 3;
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, "Renamed Event 3");
		  });
	}

	@Test
	void testUpdateEventName_LengthCheckGoodCase() throws StudyUpException {
		// Up to 20 characters should be allowed for the length
		final int eventID = 1;
		String testStr = "";
		for (int i = 0; i <= 20; i++) {
			Event event = eventServiceImpl.updateEventName(eventID, testStr);
			assertEquals(testStr, event.getName());
			testStr += "*";
		}
	}

	@Test
	void testUpdateEventName_LengthCheckBadCase() {
		// Up to 20 characters should be allowed for the length
		// Pass a string of length 21 and ensure it throws an exception
		final int eventID = 1;
		Assertions.assertThrows(StudyUpException.class, () -> {
			final String testStr = "123456789abcdef123456";
			assertEquals(testStr.length(), 21);
			eventServiceImpl.updateEventName(eventID, testStr);
		  });
	}

	@Test
	void testGetActiveEvents_badCase() {
		// There are two events total: one in the year 1970 and one in 8099
		// Ensure that only the 8099 event is returned
		// (i.e., event date is in the future)
		List<Event> activesList = new ArrayList<>();
		activesList = eventServiceImpl.getActiveEvents();
		Assertions.assertTrue(activesList.get(0).getDate().after(new Date()));
		assertEquals(activesList.size(), 1);
	}

	@Test
	void testAddStudentToEvent_GoodCase() throws StudyUpException {
		int eventID = 1;
		Student student = new Student();
		student.setFirstName("Daniel");
		student.setLastName("Alsawaf");
		student.setEmail("DanielAlsawaf@email.com");
		student.setId(2);
		eventServiceImpl.addStudentToEvent(student, eventID);
		assertEquals(DataStorage.eventData.get(eventID).getStudents().size(), 2);
	}

	@Test
	void testAddStudentToEvent_BadNumberCase() throws StudyUpException {
		// Add two students to the second event and check if
		// there can be >2 students in an Event
		int eventID = 2;
		Student student = new Student();
		student.setFirstName("Student");
		student.setLastName("Two");
		student.setEmail("SecondStudent@email.com");
		student.setId(2);
		eventServiceImpl.addStudentToEvent(student, eventID);

		Student student2 = new Student();
		student2.setFirstName("Student");
		student2.setLastName("Three");
		student2.setEmail("ThirdStudent@email.com");
		student2.setId(3);
		eventServiceImpl.addStudentToEvent(student, eventID);
		assertEquals(DataStorage.eventData.get(eventID).getStudents().size(), 3);
	}
	
	@Test
	void testAddStudentToEvent_addStudentToEmptyEvent() throws StudyUpException {
		int eventID = 3;
		Event event3 = new Event();
		event3.setEventID(eventID);
		event3.setDate(new Date(0));
		event3.setName("Event 3");
		event3.setLocation(new Location(-122, 37));
		DataStorage.eventData.put(event3.getEventID(), event3);
		
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);
		eventServiceImpl.addStudentToEvent(student, eventID);
		assertEquals(DataStorage.eventData.get(eventID).getStudents().size(), 1);
	}

	@Test
	void testAddStudentToEvent_unknownEventCase() throws StudyUpException {
		int eventID = 3;
		Assertions.assertThrows(StudyUpException.class, () -> {
			Student student = new Student();
			student.setFirstName("Daniel");
			student.setLastName("Alsawaf");
			student.setEmail("DanielAlsawaf@email.com");
			student.setId(2);
			eventServiceImpl.addStudentToEvent(student, eventID);
		  });
	}

	@Test
	void testDeleteEvent_goodCase() {
		// Delete the second Event and make sure it's gone
		int eventID = 2;
		eventServiceImpl.deleteEvent(eventID);
		assertNull(DataStorage.eventData.get(eventID));
	}
	
	@Test
	void testGetPastEvents_goodCase() {
		List<Event> pastList = new ArrayList<>();
		pastList = eventServiceImpl.getPastEvents();
		Assertions.assertTrue(pastList.get(0).getDate().before(new Date()));
		assertEquals(pastList.size(), 1);
	}
}
