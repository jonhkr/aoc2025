import com.microsoft.z3.*;

import java.util.regex.Pattern;

record Button(Set<Integer> lights, int bits) {}
record Machine(int lightBits, List<Button> buttons, List<Integer> joltage) {}
record Tuple<A, B>(A a, B b) {}

void main() {
    var in = new BufferedReader(new InputStreamReader(System.in));
    var machines = parseInput(in);
    var part1 = 0L;
    var part2 = 0L;
    for (var m : machines) {

        part1 += configureLights(m);
        part2 += solveWithLA(m);
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

private double[][] buildAugmentedMatrix(Machine m) {
    var rows = m.joltage.size();
    var cols = m.buttons.size() + 1;
    var augmented = new double[rows][cols];
    for (var col = 0; col < cols -1; col++) {
        var button = m.buttons.get(col);
        for (var b : button.lights) {
            augmented[b][col] = 1.0;
        }
    }
    for (var row = 0; row < rows; row++) {
        augmented[row][cols - 1] = (double) m.joltage.get(row);
    }
    return augmented;
}

static double EPSILON = 1e-9;

private int solveWithLA(Machine m) {
    var original = buildAugmentedMatrix(m);
    var rows = original.length;
    var cols = original[0].length -1;

    var augmented = new double[rows][cols];
    for (var row = 0; row < rows; row++) {
        augmented[row] = original[row].clone();
    }

    var freeVars = new ArrayList<Integer>();

    var row = 0;
    var col = 0;
    while (col < cols && row < rows) {
        var pivot = row;
        for (var i = row + 1; i < rows; i++) {
            if (Math.abs(augmented[i][col])
                    > Math.abs(augmented[pivot][col])) {
                pivot = i;
            }
        }

        if (Math.abs(augmented[pivot][col]) < EPSILON) {
            freeVars.add(col);
            col++;
            continue;
        }

        var temp = augmented[row];
        augmented[row] = augmented[pivot];
        augmented[pivot] = temp;

        var pivotVal = augmented[row][col];
        for (var i = col; i <= cols; i++) {
            augmented[row][i] /= pivotVal;
        }

        for (var i = 0; i < rows; i++) {
            if (i != row) {
                var factor = augmented[i][col];
                for (var j = col; j <= cols; j++) {
                    augmented[i][j] -= factor * augmented[row][j];
                }
            }
        }

        row++;
        col++;
    }

    for (var i = col; i < cols; i++) {
        freeVars.add(i);
    }

    var max = m.joltage.stream().mapToInt(i -> i).max().orElseThrow() + 1;
    var varValues = findMissingVariables(augmented, new double[cols], 0, freeVars, max);

    return sum(varValues);
}

double[] findMissingVariables(double[][] augmented, double[] varValues, int depth, List<Integer> missingVars, int max) {
    if (missingVars.size() == depth) {
        return calculateVarValues(augmented, varValues);
    }

    var min = Integer.MAX_VALUE;
    double[] minValues = null;
    for (int i = 0; i < max; i++) {
        varValues[missingVars.get(depth)] = i;
        var vals = findMissingVariables(augmented, varValues, depth + 1, missingVars, max);
        if (vals != null) {
            var S = sum(vals);
            if (min > S) {
                min = S;
                minValues = vals;
                // todo: need to optimize this
//                break;
            }
        }
    }
    return minValues;
}

double[] calculateVarValues(double[][] augmented, double[] varValues) {
    var rows = augmented.length;
    var cols = augmented[0].length - 1;
    var vals = Arrays.copyOf(varValues, varValues.length);

    for (var i = rows - 1; i >= 0; i--) {
        var pivot = -1;
        for (int j = 0; j < cols; j++) {
            if (Math.abs(augmented[i][j]) > EPSILON) {
                pivot = j;
                break;
            }
        }
        if (pivot != -1) {
            vals[pivot] = augmented[i][cols];
            for (int j = pivot + 1; j < cols; j++) {
                vals[pivot] -= vals[j] * augmented[i][j];
            }
            if (!valid(vals[pivot])) {
                return null;
            }
        }
    }

    return vals;
}

boolean valid(double val) {
    if (val < -EPSILON) {
        return false;
    }

    var r = Math.round(val);
    return !(Math.abs(val - r) > EPSILON);
}

int sum(double[] values) {
    double sum = 0D;
    for (var v : values) {
        sum += v;
    }
    return (int) sum;
}


int solveWithZ3(Machine m) {
    HashMap<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");

    try (var ctx = new Context(cfg)) {
        var o = ctx.mkOptimize();
        var vars = new ArrayList<IntExpr>();
        var buttons = new ArrayList<>(m.buttons());
        for (int i = 0; i < buttons.size(); i++) {
            var v = ctx.mkIntConst("B" + i);
            o.Add(ctx.mkGe(v, ctx.mkInt(0)));
            vars.add(v);
        }
        for (int i = 0; i < m.joltage().size(); i++) {
            var joltage = m.joltage().get(i);
            var equation = new ArrayList<IntExpr>();
            for (int j = 0; j < buttons.size(); j++) {
                var button = buttons.get(j);
                if (button.lights.contains(i)) {
                    equation.add(vars.get(j));
                }
            }
            var e = ctx.mkAdd(equation.toArray(IntExpr[]::new));
            o.Add(ctx.mkEq(ctx.mkAdd(e), ctx.mkInt(joltage)));
        }
        var min = o.MkMinimize(ctx.mkAdd(vars.toArray(IntExpr[]::new)));
        if (Status.UNSATISFIABLE.equals(o.Check())) {
            throw new AssertionError();
        }

        return Integer.parseInt(min.toString());
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