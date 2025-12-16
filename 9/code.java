record Area(Point p, Point q) implements Comparable<Area> {
    @Override
    public int compareTo(Area o) {
        return Long.compare(this.area(), o.area());
    }

    public long area() {
        return (long) (Math.abs(this.p.x - this.q.x) + 1) * (Math.abs(this.p.y - this.q.y) + 1);
    }

    public Set<Point> points() {
        return Set.of(p, q);
    }

    @Override
    public String toString() {
        return p + " -> " + q + " = " + area();
    }
}

record Point(int x, int y) implements Comparable<Point> {

    Point(List<Integer> c) {
        this(c.get(0), c.get(1));
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public int compareTo(Point o) {
        if (x == o.x) {
            return Long.compare(y, o.y);
        } else {
            return Long.compare(x, o.x);
        }
    }

    public Set<Point> around() {
        return Set.of(
                new Point(x - 1, y),
                new Point(x + 1, y),
                new Point(x, y - 1),
                new Point(x, y + 1));
    }
}

record SortedPair<T extends Comparable<T>>(T a, T b) {
    SortedPair(T a, T b) {
        if (a.compareTo(b) <= 0) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
    }

    @Override
    public String toString() {
        return "(" + a + "," + b + ")";
    }
}

void main() {
    var input = new BufferedReader(new InputStreamReader(System.in));
    final var points = input.lines()
            .map(line -> new Point(Arrays
                    .stream(line.split(","))
                    .map(Integer::parseInt)
                    .toList()))
            .toList();

    List<Area> areas = new ArrayList<>();
    for (int i = 0; i < points.size(); i++) {
        var p = points.get(i);
        for (int j = i + 1; j < points.size(); j++) {
            var q = points.get(j);
            areas.add(new Area(p, q));
        }
    }

    areas = areas.stream().sorted().toList();
    var largest = areas.getLast();

    System.out.println("Part 1: " + largest.area());

    // find all distinct xs and ys to build compressed array
    var xs = points.stream().mapToInt(p -> p.x).distinct().sorted().boxed().toList();
    var ys = points.stream().mapToInt(p -> p.y).distinct().sorted().boxed().toList();

    var grid = buildCompressedArray(points, xs, ys);
    floodFillInPlace(grid);
    var psa = buildPrefixSumArray(grid);

    Area largestFitting = areas.reversed().stream()
            .filter(area -> valid(psa, area.p, area.q, xs, ys))
            .findFirst().orElseThrow();

    System.out.println("Part 2: " + Objects.requireNonNull(largestFitting).area());
}

/**
 * Builds a prefix sum array to check if a rectangle is inside the polygon.
 * The resulting array is of size(x+1, y+1)
 * eg:
 * input:
 * [0, 1, 1]
 * [1, 1, 1]
 * output:
 * [0, 0, 0, 0]
 * [0, 0, 1, 2]
 * [0, 1, 3, 5]
 *
 */
private long[][] buildPrefixSumArray(byte[][] baseGrid) {
    var psa = new long[baseGrid.length + 1][baseGrid[0].length + 1];
    for (int x = 1; x < psa.length; x++) {
        for (int y = 1; y < psa[0].length; y++) {
            var left = psa[x - 1][y];
            var top = psa[x][y - 1];
            var topLeft = psa[x - 1][y - 1];
            psa[x][y] = left + top - topLeft + baseGrid[x - 1][y - 1];
        }
    }
    return psa;
}

/**
 * Checks if the rectangle formed by the two points p and q is valid
 * by firstly compressing the coordinates and then looking if the
 * area is filled with 1s.
 * The algorithm uses a prefix sum array to check how many 1s
 * are in the area of the rectangle.
 */
private boolean valid(long[][] psa, Point p, Point q, List<Integer> xs, List<Integer> ys) {
    var cxs = new SortedPair<>(xs.indexOf(p.x) * 2, xs.indexOf(q.x) * 2);
    var cys = new SortedPair<>(ys.indexOf(p.y) * 2, ys.indexOf(q.y) * 2);
    var left = psa[cxs.a][cys.b + 1];
    var top = psa[cxs.b + 1][cys.a];
    var topLeft = psa[cxs.a][cys.a];
    var count = psa[cxs.b + 1][cys.b + 1] - left - top + topLeft;
    return count == (long) (cxs.b - cxs.a + 1) * (cys.b - cys.a + 1);
}

/**
 * fills the inside of the polygon boundary with 1s
 * the algorithm searches for all outside points
 * and then fills with 1s the points inside
 */
private void floodFillInPlace(byte[][] grid) {
    var queue = new ArrayDeque<Point>();
    queue.add(new Point(-1, -1));

    var outside = new HashMap<Point, Boolean>();

    while (!queue.isEmpty()) {
        var p = queue.pop();
        for (var np : p.around()) {
            // if out of bounds
            if (np.x < -1 || np.y < -1 || np.x > grid.length || np.y > grid[0].length) {
                continue;
            }

            // if at polygon boundary
            if (np.x >= 0 && np.x < grid.length
                    && np.y >= 0 && np.y < grid[0].length
                    && grid[np.x][np.y] == 1) {
                continue;
            }

            // if already visited
            if (outside.containsKey(np)) {
                continue;
            }

            outside.put(np, true);
            queue.push(np);
        }
    }

    for (int x = 0; x < grid.length; x++) {
        for (int y = 0; y < grid[x].length; y++) {
            if (!outside.containsKey(new Point(x, y))) {
                grid[x][y] = 1;
            }
        }
    }
}

/**
 * builds a compressed array using the indexes of xs and ys
 * a gap is added to every span to keep the overall shape of the original matrix
 * eg
 *  [x1,  gap   , x2]
 *  [90, 91..101, 102]
 * result:
 *  [0, 1, 2]
 * where
 *  0 = 90
 *  1 = 91..101
 *  2 = 102
 */
byte[][] buildCompressedArray(List<Point> points, List<Integer> xs, List<Integer> ys) {
    var grid = new byte[xs.size() * 2 - 1][ys.size() * 2 - 1];
    for (int i = 0; i < points.size(); i++) {
        var p = points.get(i);
        var q = points.get((i + 1) % points.size());

        var cxs = new SortedPair<>(xs.indexOf(p.x) * 2, xs.indexOf(q.x) * 2);
        var cys = new SortedPair<>(ys.indexOf(p.y) * 2, ys.indexOf(q.y) * 2);

        for (int cx = cxs.a; cx < cxs.b + 1; cx++) {
            for (int cy = cys.a; cy < cys.b + 1; cy++) {
                grid[cx][cy] = 1;
            }
        }
    }
    return grid;
}

void print(Object... values) {
    var sb = new StringBuilder();
    for (Object value : values) {
        sb.append(value);
        sb.append(" ");
    }
    System.out.println(sb);
}

void print(byte[][] grid) {
    for (byte[] bytes : grid) {
        System.out.println(Arrays.toString(bytes));
    }
}

void print(long[][] grid) {
    for (long[] longs : grid) {
        System.out.println(Arrays.toString(longs));
    }
}
