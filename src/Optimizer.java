import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

// Stage 3: Room Optimization
// assign rooms to classes in a way that minimizes the empty seats.
public class Optimizer {

    static ArrayList<Booking> run(DataLoad data, GraphEngine graph) {
        ArrayList<Booking> placed = new ArrayList<Booking>();
        for (int s = 0; s < data.slots.size(); s++) {
            ArrayList<Course> here = new ArrayList<Course>();
            for (int i = 0; i < data.courses.size(); i++) {
                if (graph.colour[i] == s) {
                    here.add(data.courses.get(i));
                }
            }
            if (here.isEmpty()) {
                continue;
            }
            oneHour(data, data.slots.get(s), here, placed);
        }
        return placed;
    }

    private static void oneHour(DataLoad data, String slot, ArrayList<Course> here,
            ArrayList<Booking> placed) {
        int n = here.size();
        int m = data.rooms.size();
        if (m > 16 || n > 18) {
            tightGreedy(data, slot, here, placed);
            return;
        }

        int[][] memo = new int[n + 1][1 << m];
        int[][] take = new int[n + 1][1 << m];
        for (int i = 0; i <= n; i++) {
            for (int k = 0; k < (1 << m); k++) {
                memo[i][k] = -1;
                take[i][k] = -2;
            }
        }
        go(0, 0, here, data.rooms, memo, take);

        int used = 0;
        for (int i = 0; i < n; i++) {
            int h = take[i][used];
            if (h >= 0) {
                Room room = data.rooms.get(h);
                Course c = here.get(i);
                int waste = room.seating_capacity - c.number_of_enrolled_students;
                placed.add(new Booking(c, slot, room, waste));
                used = used | (1 << h);
            }
        }
    }

    private static int go(int i, int used, ArrayList<Course> here, ArrayList<Room> halls,
            int[][] memo, int[][] take) {
        if (i == here.size()) {
            return 0;
        }
        if (memo[i][used] != -1) {
            return memo[i][used];
        }
        int best = 100000 + go(i + 1, used, here, halls, memo, take);
        int bestH = -1;
        Course c = here.get(i);
        for (int h = 0; h < halls.size(); h++) {
            if ((used & (1 << h)) != 0) {
                continue;
            }
            Room room = halls.get(h);
            if (room.seating_capacity < c.number_of_enrolled_students) {
                continue;
            }
            int waste = room.seating_capacity - c.number_of_enrolled_students;
            int v = waste + go(i + 1, used | (1 << h), here, halls, memo, take);
            if (v < best) {
                best = v;
                bestH = h;
            }
        }
        take[i][used] = bestH;
        memo[i][used] = best;
        return best;
    }

    private static void tightGreedy(DataLoad data, String slot, ArrayList<Course> here,
            ArrayList<Booking> placed) {
        ArrayList<Course> order = new ArrayList<Course>(here);
        Collections.sort(order, new Comparator<Course>() {
            public int compare(Course a, Course b) {
                return b.number_of_enrolled_students - a.number_of_enrolled_students;
            }
        });
        boolean[] taken = new boolean[data.rooms.size()];
        for (int i = 0; i < order.size(); i++) {
            Course c = order.get(i);
            int best = -1;
            int bestWaste = Integer.MAX_VALUE;
            for (int h = 0; h < data.rooms.size(); h++) {
                if (taken[h]) {
                    continue;
                }
                Room room = data.rooms.get(h);
                if (room.seating_capacity < c.number_of_enrolled_students) {
                    continue;
                }
                int w = room.seating_capacity - c.number_of_enrolled_students;
                if (w < bestWaste) {
                    bestWaste = w;
                    best = h;
                }
            }
            if (best >= 0) {
                taken[best] = true;
                placed.add(new Booking(c, slot, data.rooms.get(best), bestWaste));
            }
        }
    }

    static ArrayList<Course> notPlaced(DataLoad data, ArrayList<Booking> placed) {
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
