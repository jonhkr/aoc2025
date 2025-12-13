import com.microsoft.z3.*;

import java.util.regex.Pattern;

record Button(Set<Integer> lights, int bits) {}
record Machine(int lightBits, Set<Button> buttons, List<Integer> joltage) {}

Set<Set<Button>> combinations(Set<Button> buttons, int count) {
    var list = new ArrayList<>(buttons);
    var result = new HashSet<Set<Button>>();
    backtrack(list, count, 0, new LinkedHashSet<>(), result);
    return result;
}

private void backtrack(List<Button> buttons, int count,
                       int index, LinkedHashSet<Button> current,
                       HashSet<Set<Button>> result) {

    if (current.size() == count) {
        result.add(Set.copyOf(current));
        return;
    }

    if (index == buttons.size()) return;

    current.add(buttons.get(index));
    backtrack(buttons, count, index + 1, current, result);

    current.remove(buttons.get(index));
    backtrack(buttons, count, index + 1, current, result);
}

void testCombinations() {
    var r = Set.of(new Button(Set.of(1, 2),0), new Button(Set.of(0,3),0), new Button(Set.of(3),0));
    var c = combinations(r, 2);
    if (!Set.of(
            Set.of(new Button(Set.of(1, 2),0), new Button(Set.of(0, 3),0)),
            Set.of(new Button(Set.of(1, 2),0), new Button(Set.of(3),0)),
            Set.of(new Button(Set.of(0, 3),0), new Button(Set.of(3),0))
    ).equals(c)) {
        throw new AssertionError();
    }
}

void main() {
    testCombinations();

    var in = new BufferedReader(new InputStreamReader(System.in));
    var machines = parseInput(in);
    var total = 0L;
    var part2 = 0L;
    for (var m : machines) {
        done:
        for (var c : IntStream.range(1, m.buttons().size() + 1).toArray()) {
            for (var attempt : combinations(m.buttons(), c)) {
                var lightBits = 0L;
                for(var button : attempt) {
                    lightBits ^= button.bits;
                }
                if (m.lightBits == lightBits) {
                    total += c;
                    break done;
                }
            }
        }

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
                    if (button.lights().contains(i)) {
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

            part2 += Integer.parseInt(min.toString());
        }
    }

    print("Part 1:", total);

    /*
     * Requires Z3 lib
     * Download from https://github.com/Z3Prover/z3/releases
     * Copy the bin here
     * run: $ java -cp com.microsoft.z3.jar code.java < input.txt
     */
    print("Part 2:", part2);
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
                        .map(i -> lights.charAt(i) == '#' ? Math.powExact(2, i) : 0).sum();

                var buttons = matcher.group(2);
                var b = Arrays.stream(buttons.split(" "))
                        .map(b1 -> {
                            var ints = Arrays.stream(b1.substring(1, b1.length()-1)
                                            .split(","))
                                    .mapToInt(Integer::parseInt)
                                    .boxed()
                                    .collect(Collectors.toSet());
                            var bits = ints.stream().mapToInt(i -> Math.powExact(2, i)).sum();
                            return new Button(ints, bits);
                        })
                        .collect(Collectors.toSet());
                var j = Arrays.stream(matcher.group(3).split(",")).mapToInt(Integer::parseInt)
                        .boxed().toList();
                return new Machine(lightBits, b, j);
            })
            .toList();
}

void print(Object... values) {
    var sb = new StringBuilder();
    for (Object value : values) {
        sb.append(value);
        sb.append(" ");
    }
    System.out.println(sb);
}
