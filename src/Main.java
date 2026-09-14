import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = "data/constraints.json";
        if (args.length > 0) {
            path = args[0];
        }

        DataLoad data = DataLoad.fromFile(path);
        System.out.println("loaded " + data.courses.size() + " classes, "
                + data.rooms.size() + " rooms, "
                + data.slots.size() + " slots");
        System.out.println();
        System.out.println("STEP 1 : GREEDY ALGORITHM IMPLEMENTATION ******");
        System.out.println("(sorted by - class size, placing the biggest classes first)");
        System.out.println();

        ArrayList<Booking> booked = GreedySolver.run(data);
        int wasteTotal = 0;
        for (int i = 0; i < booked.size(); i++) {
            Booking b = booked.get(i);
            wasteTotal += b.waste;
            System.out.println(pad(b.course.class_id, 12)
                    + "  " + pad(b.slot, 16)
                    + "  " + pad(b.room.room_id, 6)
                    + "  waste " + b.waste);
        }

        ArrayList<Course> left = GreedySolver.notBooked(data, booked);
        for (int i = 0; i < left.size(); i++) {
            Course c = left.get(i);
            System.out.println(pad(c.class_id, 12) + "  LEFT   (no room/slot big enough)");
        }

        System.out.println();
        System.out.println("booked " + booked.size() + " / " + data.courses.size()
                + "   empty seats total " + wasteTotal);
        if (left.size() > 0) {
            System.out.println("unscheduled: " + left.size() + " classes");
        }
    }

    static String pad(String s, int w) {
        if (s.length() >= w) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < w) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
