import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataLoader {
    ArrayList<Course> courses = new ArrayList<Course>();
    ArrayList<Room> rooms = new ArrayList<Room>();
    ArrayList<String> slots = new ArrayList<String>();
    HashMap<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();

    static DataLoader fromFile(String path) throws Exception {
        String text = Files.readString(Path.of(path));
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) new JsonParser(text).read();

        DataLoader d = new DataLoader();

        List<Object> classList = (List<Object>) root.get("classes");
        for (int i = 0; i < classList.size(); i++) {
            Map<String, Object> c = (Map<String, Object>) classList.get(i);
            d.courses.add(new Course(
                    String.valueOf(c.get("class_id")),
                    ((Number) c.get("number_of_enrolled_students")).intValue(),
                    String.valueOf(c.get("professor_id"))));
        }

        List<Object> roomList = (List<Object>) root.get("rooms");
        for (int i = 0; i < roomList.size(); i++) {
            Map<String, Object> r = (Map<String, Object>) roomList.get(i);
            d.rooms.add(new Room(
                    String.valueOf(r.get("room_id")),
                    ((Number) r.get("seating_capacity")).intValue()));
        }

        List<Object> slotList = (List<Object>) root.get("timeslots");
        for (int i = 0; i < slotList.size(); i++) {
            d.slots.add(String.valueOf(slotList.get(i)));
        }

        Map<String, Object> sg = (Map<String, Object>) root.get("student_groups");
        for (String name : sg.keySet()) {
            List<Object> ids = (List<Object>) sg.get(name);
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < ids.size(); i++) {
                list.add(String.valueOf(ids.get(i)));
            }
            d.groups.put(name, list);
        }
        return d;
    }
}
