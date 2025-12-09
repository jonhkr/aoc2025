
record Connection(Point p, Point q) implements Comparable<Connection> {
    @Override
    public int compareTo(Connection o) {
        return Double.compare(this.distance(), o.distance());
    }

    public double distance() {
        return p.distance(q);
    }

    public Set<Point> points() {
        return Set.of(p, q);
    }

    @Override
    public String toString() {
        return p + " -> " + q + " = " + distance();
    }
}

record Point(int x, int y, int z) {

    Point(List<Integer> c) {
        this(c.get(0), c.get(1), c.get(2));
    }

    double distance(Point other) {
        return Math.sqrt(Math.pow(other.x - x, 2)
                + Math.pow(other.y - y, 2)
                + Math.pow(other.z - z, 2));
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }
}

static class SetUtils {
    public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>();
        result.addAll(set1);
        result.addAll(set2);
        return result;
    }
}

void main() throws IOException {
    final var coords = Files.readAllLines(Path.of("./input.txt"))
            .stream()
            .map(line -> new Point(Arrays
                    .stream(line.split(","))
                    .map(Integer::parseInt)
                    .toList()))
            .toList();

    var distances = new TreeSet<Connection>();
    for (int i = 0; i < coords.size(); i++) {
        var p = coords.get(i);
        for (int j = i + 1; j < coords.size(); j++) {
            var q = coords.get(j);
            distances.add(new Connection(p, q));
        }
    }

    var part1 = 1L;
    var connections = 0;
    var circuitId = 0;
    final var circuitMap = new HashMap<Point, Integer>();
    final var circuitSets = new HashMap<Integer, Set<Point>>();

    Connection lastConnection = null;
    for (Connection d : distances) {
        if (connections == 1000) {
            part1 = circuitSets.values()
                    .stream()
                    .map(Set::size)
                    .sorted((a, b) -> b - a)
                    .limit(3)
                    .reduce((a, b) -> a * b)
                    .orElse(0);
        }
        connections++;

        var pId = circuitMap.getOrDefault(d.p, -1);
        var qId = circuitMap.getOrDefault(d.q, -1);

        if (pId > -1 && qId > -1) {
            if (pId.equals(qId)) {
                continue;
            }

            if (circuitSets.size() == 2) {
                lastConnection = d;
            }

            circuitSets.merge(pId, circuitSets.get(qId), SetUtils::union);
            circuitSets.get(qId).forEach(c -> circuitMap.put(c, pId));
            circuitSets.remove(qId);
        } else {
            if (circuitSets.size() == 1) {
                lastConnection = d;
            }
            if (Objects.equals(pId, qId) && pId == -1) {
                circuitMap.put(d.p, circuitId);
                circuitMap.put(d.q, circuitId);
                circuitSets.put(circuitId, new HashSet<>(d.points()));
                circuitId++;
            } else {
                var id = Math.max(pId, qId);
                circuitMap.put(d.p, id);
                circuitMap.put(d.q, id);
                circuitSets.merge(id, d.points(), SetUtils::union);
            }
        }
    }

    System.out.println("Part 1: " + part1);

    assert lastConnection != null;
    System.out.println("Part 2: " + (long) lastConnection.p.x * lastConnection.q.x);
}
