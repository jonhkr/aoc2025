
record Shape(char[][] grid) {
    int size() {
        return grid.length * grid[0].length;
    }
}

record Region(int row, int col) {
    int size() {
        return row * col;
    }
}

void main() {
    var in = new BufferedReader(new InputStreamReader(System.in));
    var allLines = in.lines().toList();

    var shapes = new ArrayList<Shape>();
    var regions = new ArrayList<Region>();
    var shapeCounts = new ArrayList<List<Integer>>();
    for (int i = 0; i < allLines.size(); i++) {
        var line = allLines.get(i);
        if (line.matches("^\\d:$")) {
            var grid = new char[3][3];
            for (int x = 0; x < 3; x++) {
                i++;
                grid[x] = allLines.get(i + 1 + x).toCharArray();
            }
            shapes.add(new Shape(grid));
        }
        if (line.matches("^\\d{1,2}x\\d{1,2}:.*$")) {
            var parts = line.split(":");
            var regionSize = parts[0].split("x");
            regions.add(new Region(Integer.parseInt(regionSize[0]), Integer.parseInt(regionSize[1])));
            var counts = Arrays.stream(parts[1].strip().split(" ")).map(Integer::parseInt).toList();
            shapeCounts.add(counts);
        }
    }

    var fits = 0L;
    for (int i = 0; i < regions.size(); i++) {
        var region = regions.get(i);
        var counts = shapeCounts.get(i);
        var total = 0L;
        for (int j = 0; j < counts.size(); j++) {
            var shape = shapes.get(j);
            var count = counts.get(j);
            total += (long) shape.size() * count;
        }

        if (total <= region.size()) {
            fits++;
        }
    }

    print("Fits:", fits);
}

void print(Object... values) {
    var sb = new StringBuilder();
    for (Object value : values) {
        sb.append(value);
        sb.append(" ");
    }
    System.out.println(sb);
}
