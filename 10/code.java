import java.util.regex.Pattern;

record Button(Set<Integer> lights, int bits) {}
record Machine(int lightBits, List<Button> buttons, List<Integer> joltage) {}
record Tuple<A, B>(A a, B b) {}

void main() {
    var in = new BufferedReader(new InputStreamReader(System.in));
    var machines = parseInput(in);
    var part1 = 0L;
    var part2 = 0L;
    var solver = new Part2Solver();
    for (var m : machines) {
        part1 += configureLights(m);
        part2 += solver.solve(m);
    }

    print("Part 1:", part1);
    print("Part 2:", part2);
}

private int configureLights(Machine m) {
    var result = new ArrayDeque<Tuple<Integer, Integer>>();
    result.push(new Tuple<>(0, 0));

    var seen = new HashSet<Integer>();
    seen.add(0);

    for (var r = result.poll(); r != null; r = result.poll()) {
        if (r.a == m.lightBits) {
            return r.b;
        }

        for (var button : m.buttons) {
            var lights = r.a ^ button.bits;
            if (seen.add(lights)) {
                result.add(new Tuple<>(lights, r.b + 1));
            }
        }
    }

    throw new AssertionError("Could not solve");
}

static class Part2Solver {
    private static final double EPSILON = 1e-9;
    private List<Integer> freeVars = null;
    private List<Integer> dependentVars = null;
    private Integer cols = null;

    private int solve(Machine m) {
        var A = buildAugmentedMatrix(m);
        runGaussianElimination(A);
        return minimize(A, new double[freeVars.size()], 0, max(m.joltage) + 1);
    }

    private void runGaussianElimination(double[][] A) {
        var rows = A.length;
        cols = A[0].length - 1;
        freeVars = new ArrayList<>();
        dependentVars = new ArrayList<>();

        var row = 0;
        var col = 0;
        while (col < cols && row < rows) {
            var pivot = row;
            for (var i = row + 1; i < rows; i++) {
                if (Math.abs(A[i][col]) > Math.abs(A[pivot][col])) {
                    pivot = i;
                }
            }

            if (Math.abs(A[pivot][col]) < EPSILON) {
                freeVars.add(col);
                col++;
                continue;
            }

            var temp = A[row];
            A[row] = A[pivot];
            A[pivot] = temp;
            dependentVars.add(col);

            var pivotVal = A[row][col];
            for (var i = col; i <= cols; i++) {
                A[row][i] /= pivotVal;
            }

            for (var i = 0; i < rows; i++) {
                if (i != row) {
                    var factor = A[i][col];
                    for (var j = col; j <= cols; j++) {
                        A[i][j] -= factor * A[row][j];
                    }
                }
            }

            row++;
            col++;
        }

        for (var i = col; i < cols; i++) {
            freeVars.add(i);
        }
    }

    private double[][] buildAugmentedMatrix(Machine m) {
        var rows = m.joltage.size();
        var cols = m.buttons.size() + 1;
        var A = new double[rows][cols];
        for (var col = 0; col < cols - 1; col++) {
            var button = m.buttons.get(col);
            for (var b : button.lights) {
                A[b][col] = 1.0;
            }
        }

        for (var row = 0; row < rows; row++) {
            A[row][cols - 1] = (double) m.joltage.get(row);
        }

        return A;
    }

    int minimize(double[][] A, double[] freeValues, int depth, int max) {
        if (freeVars.size() == depth) {
            var total = sum(freeValues);

            for (int row = 0; row < dependentVars.size(); row++) {
                var c = A[row][cols];
                for (var j = 0; j < freeVars.size(); j++) {
                    var col = freeVars.get(j);
                    c -= A[row][col] * freeValues[j];
                }

                if (invalid(c)) {
                    return Integer.MAX_VALUE;
                }

                total += c;
            }

            return (int) total;
        }

        var min = Integer.MAX_VALUE;
        for (var i = 0; i < max; i++) {
            freeValues[depth] = i;
            var res = minimize(A, freeValues, depth + 1, max);
            min = Math.min(min, res);
        }

        return min;
    }

    /**
     * 'You have to push each button an integer number of times;
     *  there's no such thing as "0.5 presses" (nor can you push
     *  a button a negative number of times)'
     * A value is considered invalid in the following cases:
     * - the value is lower than zero (negative)
     * - the value is not a whole int
     * @param val the variable value to check
     * @return true if the given value is invalid, false otherwise
     */
    boolean invalid(double val) {
        return val < -EPSILON || Math.abs(val - Math.rint(val)) > EPSILON;
    }

    double sum(double[] values) {
        double sum = 0D;
        for (var v : values) {
            sum += v;
        }
        return sum;
    }

    int max(List<Integer> values) {
        int max = Integer.MIN_VALUE;
        for (var v : values) {
            max = Math.max(max, v);
        }
        return max;
    }
}

List<Machine> parseInput(BufferedReader in) {
    // [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
    var pattern = Pattern.compile("^\\[([.#]+)] ([()\\d, ]+) \\{([\\d,]+)}$");
    return in.lines()
            .map(line -> {
                var matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException();
                }
                var lights = matcher.group(1);
                var lightBits = IntStream.range(0, lights.length())
                        .map(i -> lights.charAt(i) == '#' ? 1 << i : 0).sum();

                var buttons = matcher.group(2);
                var b = Arrays.stream(buttons.split(" "))
                        .map(b1 -> {
                            var ints = Arrays.stream(b1.substring(1, b1.length()-1)
                                            .split(","))
                                    .mapToInt(Integer::parseInt)
                                    .boxed().collect(Collectors.toSet());
                            var bits = ints.stream().mapToInt(i -> 1 << i).sum();
                            return new Button(ints, bits);
                        }).toList();
                var j = Arrays.stream(matcher.group(3).split(",")).mapToInt(Integer::parseInt)
                        .boxed().toList();
                return new Machine(lightBits, b, j);
            })
            .toList();
}

static void print(Object... values) {
    var sb = new StringBuilder();
    for (Object value : values) {
        switch (value) {
            case int[][] v -> print(v);
            case int[] v -> print(v);
            case double[][] v -> print(v);
            case double[] v -> print(v);
            case null, default -> {
                sb.append(value);
                sb.append(" ");
            }
        }
    }
    System.out.println(sb);
}

static void print(int[] ints) {
    System.out.println(Arrays.toString(ints));
}

static void print(int[][] ints) {
    for (var row : ints) {
        print(row);
    }
}

static void print(double[][] nss) {
    for (var row : nss) {
        print(row);
    }
}

static void print(double[] ns) {
    System.out.println(Arrays.toString(ns));
}