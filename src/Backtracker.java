import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

// Stage 4: try leftover classes on any free hour/room.
// if it still cannot sit, we keep the best partial timetable and flag them.
public class Backtracker {

    DataLoad data;
    ArrayList<Booking> current;
    ArrayList<Course> left;
    HashMap<String, ArrayList<String>> groupsOf;
    int nodes;
    int nodeCap;
    int bestCount;
    ArrayList<Booking> best;

    Backtracker(DataLoad data, ArrayList<Booking> already, int nodeCap) {
        this.data = data;
        this.nodeCap = nodeCap;
        this.current = copy(already);
        this.groupsOf = invertGroups(data);
        this.left = new ArrayList<Course>();
        HashMap<String, Boolean> done = new HashMap<String, Boolean>();
        for (int i = 0; i < already.size(); i++) {
            done.put(already.get(i).course.class_id, Boolean.TRUE);
        }
        for (int i = 0; i < data.courses.size(); i++) {
            if (!done.containsKey(data.courses.get(i).class_id)) {
                left.add(data.courses.get(i));
            }
        }
        Collections.sort(left, new Comparator<Course>() {
            public int compare(Course a, Course b) {
                return b.number_of_enrolled_students - a.number_of_enrolled_students;
            }
        });
        this.nodes = 0;
        this.bestCount = already.size();
        this.best = copy(already);
    }

    ArrayList<Booking> run() {
        search(0);
        return best;
    }

    private void search(int i) {
        nodes++;
        if (nodes > nodeCap) {
            return;
        }
        if (current.size() > bestCount) {
            bestCount = current.size();
            best = copy(current);
        }
        if (i >= left.size()) {
            return;
        }
        if (current.size() + (left.size() - i) <= bestCount) {
            return;
        }

        Course c = left.get(i);
        for (int s = 0; s < data.slots.size(); s++) {
            String slot = data.slots.get(s);
            for (int h = 0; h < data.rooms.size(); h++) {
                Room room = data.rooms.get(h);
                if (!canPut(current, c, slot, room)) {
                    continue;
                }
                int waste = room.seating_capacity - c.number_of_enrolled_students;
                current.add(new Booking(c, slot, room, waste));
                search(i + 1);
                current.remove(current.size() - 1);
                if (nodes > nodeCap) {
                    return;
                }
            }
        }
        search(i + 1);
    }

    boolean canPut(ArrayList<Booking> booked, Course c, String slot, Room room) {
        if (room.seating_capacity < c.number_of_enrolled_students) {
            return false;
        }
        for (int i = 0; i < booked.size(); i++) {
            Booking b = booked.get(i);
            if (!b.slot.equals(slot)) {
                continue;
            }
            if (b.room.room_id.equals(room.room_id)) {
                return false;
            }
            if (b.course.professor_id.equals(c.professor_id)) {
                return false;
            }
            if (shareGroup(c.class_id, b.course.class_id)) {
                return false;
            }
        }
        return true;
    }

    String whyLeft(Course c) {
        int fitRooms = 0;
        for (int i = 0; i < data.rooms.size(); i++) {
            if (data.rooms.get(i).seating_capacity >= c.number_of_enrolled_students) {
                fitRooms++;
            }
        }
        boolean someHourWorks = false;
        for (int s = 0; s < data.slots.size() && !someHourWorks; s++) {
            for (int h = 0; h < data.rooms.size(); h++) {
                if (canPut(best, c, data.slots.get(s), data.rooms.get(h))) {
                    someHourWorks = true;
                    break;
                }
            }
        }
        if (fitRooms == 0) {
            return "no hall is large enough";
        }
        if (!someHourWorks) {
            return "insufficient rooms: only " + fitRooms
                    + " hall(s) seat " + c.number_of_enrolled_students
                    + " students, and those halls are taken in every timeslot";
        }
        return "no legal hour+room left (group/professor clash)";
    }

    private boolean shareGroup(String a, String b) {
        ArrayList<String> ga = groupsOf.get(a);
        ArrayList<String> gb = groupsOf.get(b);
        if (ga == null || gb == null) {
            return false;
        }
        for (int i = 0; i < ga.size(); i++) {
            if (gb.contains(ga.get(i))) {
                return true;
            }
        }
        return false;
    }

    static HashMap<String, ArrayList<String>> invertGroups(DataLoad data) {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        for (String g : data.groups.keySet()) {
            ArrayList<String> ids = data.groups.get(g);
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                if (!map.containsKey(id)) {
                    map.put(id, new ArrayList<String>());
                }
                map.get(id).add(g);
            }
        }
        return map;
    }

    static ArrayList<Booking> copy(ArrayList<Booking> src) {
        ArrayList<Booking> out = new ArrayList<Booking>();
        for (int i = 0; i < src.size(); i++) {
            out.add(src.get(i));
        }
        return out;
    }

    static ArrayList<Course> stillLeft(DataLoad data, ArrayList<Booking> placed) {
        HashMap<String, Boolean> done = new HashMap<String, Boolean>();
        for (int i = 0; i < placed.size(); i++) {
            done.put(placed.get(i).course.class_id, Boolean.TRUE);
        }
        ArrayList<Course> left = new ArrayList<Course>();
        for (int i = 0; i < data.courses.size(); i++) {
            if (!done.containsKey(data.courses.get(i).class_id)) {
                left.add(data.courses.get(i));
            }
        }
        return left;
    }
}
