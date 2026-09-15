import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = "data/constraints.json";
        if (args.length > 0) {
            path = args[0];
        }

        DataLoad data = DataLoad.fromFile(path);
        System.out.println("Loaded Data (json constraints) : " + data.courses.size() + " classes, "
                + data.rooms.size() + " rooms, "
                + data.slots.size() + " slots");
        System.out.println();
        System.out.println("STEP 1 : **************** GREEDY BASELINE ***********************");
        System.out.println();
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
                    + "  wasted : " + b.waste);
        }

        ArrayList<Course> left = GreedySolver.notBooked(data, booked);
        for (int i = 0; i < left.size(); i++) {
            Course c = left.get(i);
            System.out.println(pad(c.class_id, 12) + "  LEFT   (no room/slot big enough)");
        }

        System.out.println();
        System.out.println("Total classes: " + data.courses.size() + " , " + "booked classes: " + booked.size() + " , " 
                + "total empty seats: " + wasteTotal);
        if (left.size() > 0) {
            System.out.println();
            System.out.println("unscheduled classes: " + left.size());
        }

        GraphEngine graph = new GraphEngine(data);
        graph.print();
        System.out.println();
        System.out.println("STEP 3 : **************** ROOM ALLOCATION (min waste ) ***********************");
        System.out.println();
        System.out.println("assigning rooms to classes innorder to minimize the empty seats");
        System.out.println();

        ArrayList<Booking> rooms = Optimizer.run(data, graph);
        int waste3 = 0;
        ArrayList<Booking> show = new ArrayList<Booking>(rooms);
        Collections.sort(show, new Comparator<Booking>() {
            public int compare(Booking a, Booking b) {
                return a.course.class_id.compareTo(b.course.class_id);
            }
        });
        for (int i = 0; i < show.size(); i++) {
            Booking b = show.get(i);
            waste3 += b.waste;
            System.out.println(pad(b.course.class_id, 12)
                    + "  " + pad(b.slot, 16)
                    + "  " + pad(b.room.room_id, 6)
                    + "  wasted : " + b.waste);
        }
        ArrayList<Course> left3 = Optimizer.notPlaced(data, rooms);
        for (int i = 0; i < left3.size(); i++) {
            System.out.println(pad(left3.get(i).class_id, 12) + "  UNSCHEDULED   (no Room/hall left in that hour)");
        }
        System.out.println();
        if (left3.size() > 0) {
            System.out.println("Resolved classes : " + rooms.size()+ "   |   " + "Unscheduled classes : " + left3.size());
            System.out.println();
        }
        System.out.println("empty seats before Room Optimization (step 1): " + wasteTotal);
        System.out.println();
        System.out.println("Result : Total empty seats after Room Optimization (step 3): " + waste3);
        System.out.println();

        Backtracker hunt = new Backtracker(data, rooms, 4000);
        ArrayList<Booking> finalBooked = hunt.run();
        ArrayList<Course> unresolved = Backtracker.stillLeft(data, finalBooked);

        System.out.println();
        System.out.println("STEP 4 : **************** BEST-EFFORT BACKTRACKING ***********************");
        System.out.println();
        System.out.println("try leftover classes on any free hour/room; stop at the best partial timetable");
        System.out.println();

        ArrayList<Booking> show4 = new ArrayList<Booking>(finalBooked);
        Collections.sort(show4, new Comparator<Booking>() {
            public int compare(Booking a, Booking b) {
                return a.course.class_id.compareTo(b.course.class_id);
            }
        });

        System.out.println(pad("class", 12) + "  " + pad("timeslot", 16) + "  " + pad("room", 6) + "  waste");
        System.out.println("----------------------------------------------------------");
        for (int i = 0; i < show4.size(); i++) {
            Booking b = show4.get(i);
            System.out.println(pad(b.course.class_id, 12)
                    + "  " + pad(b.slot, 16)
                    + "  " + pad(b.room.room_id, 6)
                    + "  wasted : " + b.waste);
        }
        for (int i = 0; i < unresolved.size(); i++) {
            Course c = unresolved.get(i);
            System.out.println(pad(c.class_id, 12) + "  " + pad("UNSCHEDULED", 16)
                    + "  " + pad("N/A", 6) + "  wasted : N/A");
        }
        System.out.println();
        System.out.println("Resolved classes : " + finalBooked.size() + "   |   "
                + "Unscheduled classes : " + unresolved.size());
        System.out.println();
        System.out.println("---------------- CONFLICT REPORT -----------------------------------------");
        System.out.println();
        for (int i = 0; i < show4.size(); i++) {
            Booking b = show4.get(i);
            System.out.println("Scheduled    " + pad(b.course.class_id, 10)
                    + "  " + pad(b.slot, 12)
                    + "  " + pad(b.room.room_id, 6)
                    + "  " + fitText(b.waste));
        }
        for (int i = 0; i < unresolved.size(); i++) {
            Course c = unresolved.get(i);
            System.out.println("Unscheduled  " + pad(c.class_id, 10)
                    + "  " + pad("N/A", 12)
                    + "  " + pad("N/A", 6));
        }
        if (unresolved.size() > 0) {
            System.out.println();
            for (int i = 0; i < unresolved.size(); i++) {
                Course c = unresolved.get(i);
                System.out.println(c.class_id + " : " + hunt.whyLeft(c));
            }
            System.out.println();
            System.out.println("Manual intervention is required to schedule these classes");
        }
        System.out.println();
        double pct = 100.0 * finalBooked.size() / data.courses.size();
        System.out.println("Result : Classes scheduled: " + finalBooked.size() + " / " + data.courses.size()
                + "  (" + String.format("%.1f", pct) + "%)");
        System.out.println();
    }

    static String fitText(int waste) {
        if (waste == 0) {
            return "Perfect Fit";
        }
        return "Wasted " + waste + " seats";
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
