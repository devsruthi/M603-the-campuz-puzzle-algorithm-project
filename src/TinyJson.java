import java.util.ArrayList;
import java.util.HashMap;

public class TinyJson {
    private String s;
    private int i;

    TinyJson(String s) {
        this.s = s;
        this.i = 0;
    }

    Object read() {
        skip();
        char c = s.charAt(i);
        if (c == '{') {
            return readObj();
        }
        if (c == '[') {
            return readArr();
        }
        if (c == '"') {
            return readStr();
        }
        return readNum();
    }

    private HashMap<String, Object> readObj() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        i++;
        skip();
        while (s.charAt(i) != '}') {
            String key = readStr();
            skip();
            i++; // :
            Object val = read();
            map.put(key, val);
            skip();
            if (s.charAt(i) == ',') {
                i++;
                skip();
            }
        }
        i++;
        return map;
    }

    private ArrayList<Object> readArr() {
        ArrayList<Object> list = new ArrayList<Object>();
        i++;
        skip();
        while (s.charAt(i) != ']') {
            list.add(read());
            skip();
            if (s.charAt(i) == ',') {
                i++;
                skip();
            }
        }
        i++;
        return list;
    }

    private String readStr() {
        i++;
        StringBuilder sb = new StringBuilder();
        while (s.charAt(i) != '"') {
            sb.append(s.charAt(i));
            i++;
        }
        i++;
        return sb.toString();
    }

    private Object readNum() {
        int start = i;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '-' || (c >= '0' && c <= '9')) {
                i++;
            } else {
                break;
            }
        }
        return Long.valueOf(s.substring(start, i));
    }

    private void skip() {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                i++;
            } else {
                break;
            }
        }
    }
}
