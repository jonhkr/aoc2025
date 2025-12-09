
record Area(Point p, Point q) implements Comparable<Area> {
    @Override
    public int compareTo(Area o) {
        return Long.compare(this.area(), o.area());
    }

    public long area() {
        return (long) Math.abs(this.p.x - this.q.x + 1) * Math.abs(this.p.y - this.q.y + 1);
    }

    public Set<Point> points() {
        return Set.of(p, q);
    }

    @Override
    public String toString() {
        return p + " -> " + q + " = " + area();
    }
}

record Point(int x, int y) {

    Point(List<Integer> c) {
        this(c.get(0), c.get(1));
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
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

    var areas = new TreeSet<Area>();
    for (int i = 0; i < coords.size(); i++) {
        var p = coords.get(i);
        for (int j = i + 1; j < coords.size(); j++) {
            var q = coords.get(j);
            areas.add(new Area(p, q));
        }
    }

    System.out.println("Part 1: " + areas.last().area());

}
