import java.io.*;
import java.util.*;

public class Main {

    String filenameIn = "text.txt";
    String filenameTable = "table.txt";
    String filenameEncoded = "encoded_text.bin";
    String filenameDeoded = "decoded_text.txt";


    void dfs(Node n, BitSeq b) {
        if (n.c != 0)
            map.put(n.c, b);
        else {
            dfs(n.zero, b.add(false));
            dfs(n.one, b.add(true));
        }

    }

    HashMap<Character, BitSeq> map = new HashMap<>();
    HashMap<Character, BitSeq> table = new HashMap<>();
    Node root;


    void buildtable() throws Exception {
        Scanner scan = new Scanner(new File(filenameIn));
        PrintWriter out = new PrintWriter(filenameTable);
        String s = "";
        while (scan.hasNext()) {
            s += scan.nextLine() + "\n";
        }
        HashMap<Character, Long> freqs = new HashMap<>();
        for (char c : s.toCharArray()) {
            if (!freqs.containsKey(c))
                freqs.put(c, 0l);
            freqs.put(c, freqs.get(c) + 1);
        }
        PriorityQueue<Node> queue = new PriorityQueue<>();
        for (Map.Entry<Character, Long> pair : freqs.entrySet()) {
            queue.add(new Node(pair.getValue().intValue(), pair.getKey()));
        }
        while (queue.size() > 1) {
            Node n1 = queue.poll();
            Node n2 = queue.poll();
            Node nn = new Node(n1.freq + n2.freq, (char) 0);
            nn.zero = n1;
            nn.one = n2;
            queue.add(nn);
        }
        dfs(queue.poll(), new BitSeq());
        for (Map.Entry<Character, BitSeq> e : map.entrySet()) {
            Character key = e.getKey();
            if (key == '\n')
                key = 1;
            out.println(key.toString() + " " + e.getValue().toString());
        }
        out.close();
    }

    void readTable() throws Exception {
        Scanner tab = new Scanner(new File(filenameTable));
        root = new Node(0, (char) 0);
        while (tab.hasNext()) {
            String s = tab.nextLine();
            Character c = s.charAt(0);
            if (c == 1)
                c = '\n';
            BitSeq b = new BitSeq(s.substring(2));
            Node cur = root;
            while (!b.deq.isEmpty()) {
                boolean nxt = b.deq.pop();
                if (nxt) {
                    if (cur.one == null)
                        cur.one = new Node(0, (char) 0);
                    cur = cur.one;
                } else {
                    if (cur.zero == null)
                        cur.zero = new Node(0, (char) 0);
                    cur = cur.zero;
                }
            }
            cur.c = c;
            b = new BitSeq(s.substring(2));
            table.put(c, b);
        }
    }

    void endcode() throws Exception {
        BufferedWriter out = new BufferedWriter(new FileWriter(filenameEncoded));
        Scanner scan = new Scanner(new File(filenameIn));
        String s = "";
        while (scan.hasNext()) {
            s += scan.nextLine() + "\n";
        }
        int lastbyte = 0;
        for (char c : s.toCharArray()) {
            lastbyte = (lastbyte + table.get(c).size) % 8;
        }
        out.write((8 - lastbyte) % 8);
        ArrayDeque<Boolean> deq = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            for (Boolean b : table.get(c).deq)
                deq.add(b);
            while (deq.size() >= 8) {
                int cur = 0;
                for (int i = 0; i < 8; i++) {
                    boolean b = deq.pop();
                    cur <<= 1;
                    if (b)
                        cur++;

                }
                out.write(cur);
            }
        }
        if (deq.size() > 0) {
            int cur = 0;
            int cnt = 0;
            while (!deq.isEmpty()) {
                cnt++;
                boolean b = deq.pop();
                cur <<= 1;
                if (b)
                    cur++;
            }
            while (cnt < 8) {
                cur <<= 1;
                cnt++;
            }
            out.write(cur);
        }
        out.close();
    }

    void decode() throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(filenameEncoded));
        PrintWriter out = new PrintWriter(filenameDeoded);
        String s = "";
        int lastbyte = in.read();
        ArrayDeque<Boolean> deq = new ArrayDeque<>();
        while (in.ready()) {
            int nxt = in.read();
            int curstep = 128;
            for (int i = 0; i < 8; i++) {
                deq.add((curstep & nxt) > 0);
                curstep >>= 1;
            }

            while (deq.size() > 320) {
                Node cur = root;
                while (cur.c == 0) {
                    boolean bool = deq.pop();
                    if (bool)
                        cur = cur.one;
                    else
                        cur = cur.zero;
                }
                s += Character.toString(cur.c);
            }

        }
        for (int i = 0; i < lastbyte; i++) {
            deq.removeLast();
        }

        while (deq.size() > 0) {
            Node cur = root;
            while (deq.size() > 0 && cur.c == 0) {
                boolean bool = deq.pop();
                if (bool)
                    cur = cur.one;
                else
                    cur = cur.zero;
            }
            s += Character.toString(cur.c);
        }

        out.print(s);
        out.close();

    }

    public static void main(String[] argc) throws Exception {
        Main m = new Main();
        if (argc.length == 0 || argc[0].equals("table")) {
            m.buildtable();
        } else {

            if (argc[0].equals("encode")) {
                if (argc.length >= 2)
                    m.filenameIn = argc[1];
                if (argc.length >= 3)
                    m.filenameEncoded = argc[2];
                File f = new File("table.txt");
                m.buildtable();
                m.readTable();


                m.endcode();
            } else if (argc[0].equals("decode")) {
                if (argc.length >= 2)
                    m.filenameEncoded = argc[1];
                if (argc.length >= 3)
                    m.filenameDeoded = argc[2];


                File f = new File("table.txt");
                m.buildtable();
                m.readTable();
                m.decode();


            }
        }

        System.out.print("Done");
    }
}


class Node implements Comparable<Node> {
    Node one;
    Node zero;
    int freq;
    char c;

    Node(int freq, char c) {
        this.freq = freq;
        this.c = c;
    }

    @Override
    public int compareTo(Node o) {
        return Integer.compare(freq, o.freq);
    }
}

class BitSeq {
    ArrayDeque<Boolean> deq;
    int size;

    BitSeq() {
        deq = new ArrayDeque<>();
        size = 0;
    }

    BitSeq(String s) {
        deq = new ArrayDeque<>();
        size = 0;
        for (char c : s.toCharArray()) {
            deq.add(c == '1');
            size++;
        }
    }

    public BitSeq add(boolean b) {
        BitSeq nxt = new BitSeq();
        for (Boolean b2 : deq)
            nxt.deq.add(b2);
        nxt.deq.add(b);
        nxt.size = size + 1;
        return nxt;
    }

    @Override
    public String toString() {
        String s = "";
        for (Boolean b : deq)
            if (b)
                s += "1";
            else
                s += "0";
        return s;
    }


}

