import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

// Stage 2: Graph Engine (Conflict Graph + Coloring)
// classes that share a professor or a student group cannot sit in the same hour.

public class GraphEngine {

    DataLoad data;
    int n;
    boolean[][] edge;
    int[] degree;
    int[] colour; // -1 = no free timeslot left

    GraphEngine(DataLoad data) {
        this.data = data;
        this.n = data.courses.size();
        this.edge = new boolean[n][n];
        this.degree = new int[n];
        this.colour = new int[n];
        for (int i = 0; i < n; i++) {
            colour[i] = -1;
        }
        build();
        greedyColour();
    }

    void build() {
        HashMap<String, Integer> idx = new HashMap<String, Integer>();
        for (int i = 0; i < n; i++) {
            idx.put(data.courses.get(i).class_id, i);
        }

        // same professor
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (data.courses.get(i).professor_id.equals(data.courses.get(j).professor_id)) {
                    add(i, j);
                }
            }
        }

        // each student group is a complete subgraph
        for (String g : data.groups.keySet()) {
            ArrayList<String> ids = data.groups.get(g);
            for (int a = 0; a < ids.size(); a++) {
                Integer ia = idx.get(ids.get(a));
                if (ia == null) {
                    continue;
                }
                for (int b = a + 1; b < ids.size(); b++) {
                    Integer ib = idx.get(ids.get(b));
                    if (ib == null) {
                        continue;
                    }
                    add(ia, ib);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            int d = 0;
            for (int j = 0; j < n; j++) {
                if (edge[i][j]) {
                    d++;
                }
            }
            degree[i] = d;
        }
    }

    private void add(int a, int b) {
        if (a == b) {
            return;
        }
        edge[a][b] = true;
        edge[b][a] = true;
    }

    // greedy colouring, (highest degree first)
    void greedyColour() {
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }
        Collections.sort(order, new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                if (degree[b] != degree[a]) {
                    return degree[b] - degree[a];
                }
                return data.courses.get(a).class_id.compareTo(data.courses.get(b).class_id);
            }
        });

        int maxCol = data.slots.size();
        for (int k = 0; k < order.size(); k++) {
            int v = order.get(k);
            boolean[] used = new boolean[maxCol + 1];
            for (int u = 0; u < n; u++) {
                if (edge[v][u] && colour[u] >= 0 && colour[u] < used.length) {
                    used[colour[u]] = true;
                }
            }
            int c = 0;
            while (c < maxCol && used[c]) {
                c++;
            }
            if (c >= maxCol) {
                colour[v] = -1;
            } else {
                colour[v] = c;
            }
        }
    }

    String slotOf(int i) {
        if (colour[i] < 0 || colour[i] >= data.slots.size()) {
            return "NO-SLOT";
        }
        return data.slots.get(colour[i]);
    }

    int edgeCount() {
        int e = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (edge[i][j]) {
                    e++;
                }
            }
        }
        return e;
    }

    void print() {
        System.out.println();
        System.out.println("STEP 2 :**************** CONFLICT GRAPH + GREEDY COLOURING *******************");
        System.out.println();
        System.out.println("graph: " + n + " classes, " + edgeCount() + " conflict edges");
        System.out.println();

        ArrayList<Integer> byName = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            byName.add(i);
        }
        Collections.sort(byName, new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                return data.courses.get(a).class_id.compareTo(data.courses.get(b).class_id);
            }
        });

        System.out.println("1) CONFLICT GRAPH");
        System.out.println("--------------------------------");
        for (int k = 0; k < byName.size(); k++) {
            int i = byName.get(k);
            ArrayList<String> names = new ArrayList<String>();
            for (int j = 0; j < n; j++) {
                if (edge[i][j]) {
                    names.add(data.courses.get(j).class_id);
                }
            }
            Collections.sort(names);
            String list = names.size() == 0 ? "(none)" : join(names);
            System.out.println(pad(data.courses.get(i).class_id, 12) + "  deg " + degree[i] + "  ->  " + list);
        }
        System.out.println();
        System.out.println();
        System.out.println("2) GREEDY COLOURING");
        System.out.println("--------------------------------");
        System.out.println("hours (colour -> timeslot):");
        int usedColours = 0;
        for (int i = 0; i < n; i++) {
            if (colour[i] + 1 > usedColours) {
                usedColours = colour[i] + 1;
            }
        }
        for (int c = 0; c < usedColours; c++) {
            System.out.println("  colour " + c + "  =  " + data.slots.get(c));
        }

        System.out.println();
        System.out.println("class            colour");
        System.out.println();
        int ok = 0;
        for (int k = 0; k < byName.size(); k++) {
            int i = byName.get(k);
            if (colour[i] >= 0) {
                ok++;
            }
            String col = colour[i] < 0 ? "-" : String.valueOf(colour[i]);
            System.out.println(pad(data.courses.get(i).class_id, 16) + "  " + col);
        }

        System.out.println();
        System.out.println();
        System.out.println("3) TIMESLOT ALLOCATION");
        System.out.println("--------------------------------");
        for (int k = 0; k < byName.size(); k++) {
            int i = byName.get(k);
            System.out.println(pad(data.courses.get(i).class_id, 16) + "  " + slotOf(i));
        }
        System.out.println();
        System.out.println("Unscheduled classes: " + (n - ok));
        System.out.println();
        System.out.println("Result: conflict free hours for (" + ok + " / " + n + ") classes");
        System.out.println();
    }

    static String join(ArrayList<String> names) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(names.get(i));
        }
        return sb.toString();
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
