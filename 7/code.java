
static class Matrix<T> {
    final private int width;
    final private int height;
    final private List<List<T>> data;

    public Matrix(List<List<T>> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("empty data");
        }

        this.data = new ArrayList<>();
        for (List<T> datum : data) {
            this.data.add(new ArrayList<>(datum));
        }

        this.width = data.getFirst().size();
        this.height = data.size();
    }

    public Matrix(int width, int height, T initial) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("invalid width or height");
        var data = new ArrayList<List<T>>();

        for (int i = 0; i < height; i++) {
            var row = new ArrayList<T>();
            for (int j = 0; j < width; j++) {
                row.add(initial);
            }
            data.add(row);
        }

        this(data);
    }

    public void update(int x, int y, Function<T, T> function) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }

        update(x, y, function.apply(valueAt(x, y)));
    }

    public void update(int x, int y, T value) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }

        data.get(y).set(x, value);
    }

    public T valueAt(int x, int y) {
        return data.get(y).get(x);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                sb.append(valueAt(j, i));
                sb.append(" ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public List<T> lastRow() {
        return data.getLast();
    }
}

void main() throws IOException {
    final var lines = Files.readAllLines(Path.of("./input.txt"))
            .stream()
            .map(line -> line.chars()
                    .mapToObj(c -> (char) c)
                    .toList())
            .toList();


    var splits = 0;

    final var matrix = new Matrix<>(lines);
    final var matrix2 = new Matrix<>(matrix.width, matrix.height, 0L);

    for (int y = 1; y < matrix.height; y++) {
        for (int x = 0; x < matrix.width; x++) {
            final var v1 = matrix.valueAt(x, y - 1);
            final var v2 = matrix.valueAt(x, y);
            final var beans = matrix2.valueAt(x, y - 1);

            if (v1 == 'S' || v1 == '|') {
                if (v2 == '^') {
                    matrix2.update(x - 1, y, v -> v + beans);
                    matrix2.update(x + 1, y, v -> v + beans);

                    matrix.update(x - 1, y, '|');
                    matrix.update(x + 1, y, '|');
                    splits++;
                } else {
                    if (v1 == 'S') {
                        matrix2.update(x, y, 1L);
                    } else {
                        matrix2.update(x, y, v -> v + beans);
                    }

                    matrix.update(x, y, '|');
                }
            }
        }
    }

    var timelines = matrix2.lastRow().stream().reduce(Long::sum).orElse(0L);

    System.out.println("Part 1: " + splits);
    System.out.println("Part 2: " + timelines);
}
