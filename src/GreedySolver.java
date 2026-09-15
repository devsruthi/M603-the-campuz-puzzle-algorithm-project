import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

// Stage 1: Greedy algorithm to schedule the biggest classes first.

public class GreedySolver {

    static ArrayList<Booking> run(DataLoader data) {
        ArrayList<Course> order = new ArrayList<Course>(data.courses);
        Collections.sort(order, new Comparator<Course>() {
            public int compare(Course a, Course b) {
                return b.number_of_enrolled_students - a.number_of_enrolled_students;
            }
        });

        HashSet<String> taken = new HashSet<String>();
        ArrayList<Booking> booked = new ArrayList<Booking>();

        for (int i = 0; i < order.size(); i++) {
            Course c = order.get(i);
            boolean ok = false;
            for (int s = 0; s < data.slots.size() && !ok; s++) {
                String slot = data.slots.get(s);
                for (int r = 0; r < data.rooms.size(); r++) {
                    Room room = data.rooms.get(r);
                    if (room.seating_capacity < c.number_of_enrolled_students) {
                        continue;
                    }
                    String key = room.room_id + "@" + slot;
                    if (taken.contains(key)) {
                        continue;
                    }
                    int waste = room.seating_capacity - c.number_of_enrolled_students;
                    booked.add(new Booking(c, slot, room, waste));
                    taken.add(key);
                    ok = true;
                    break;
                }
            }
            if (!ok) {
               // System.out.println("No room found for class " + c.class_id);
            }
        }
        return booked;
    }

    static ArrayList<Course> notBooked(DataLoader data, ArrayList<Booking> booked) {
        HashSet<String> done = new HashSet<String>();
        for (int i = 0; i < booked.size(); i++) {
            done.add(booked.get(i).course.class_id);
        }
        ArrayList<Course> left = new ArrayList<Course>();
        for (int i = 0; i < data.courses.size(); i++) {
            if (!done.contains(data.courses.get(i).class_id)) {
                left.add(data.courses.get(i));
            }
        }
        return left;
    }
}
