public class Booking {
    Course course;
    String slot;
    Room room;
    int waste;

    Booking(Course course, String slot, Room room, int waste) {
        this.course = course;
        this.slot = slot;
        this.room = room;
        this.waste = waste;
    }
}
