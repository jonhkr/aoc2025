
record Button(Set<Integer> lights) {}
record Machine(Set<Integer> lights, Set<Button> buttons, List<Integer> joltage) {}

Set<Integer> setXor(Set<Integer> a, Set<Integer> b) {
    var out = new HashSet<Integer>();
    for (Integer i : a) {
        if (!b.contains(i)) {
            out.add(i);
        }
    }

    for (Integer i : b) {
        if (!a.contains(i)) {
            out.add(i);
        }
    }
    return out;
}

void testSetXor() {
    var r = setXor(Set.of(1, 2, 3), Set.of(0,3,4));
    if (!Set.of(0,1,2,4).equals(r)) {
        throw new AssertionError();
    }
}

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
    var r = Set.of(new Button(Set.of(1, 2)), new Button(Set.of(0,3)), new Button(Set.of(3)));
    var c = combinations(r, 2);
    if (!Set.of(
            Set.of(new Button(Set.of(1, 2)), new Button(Set.of(0, 3))),
            Set.of(new Button(Set.of(1, 2)), new Button(Set.of(3))),
            Set.of(new Button(Set.of(0, 3)), new Button(Set.of(3)))
    ).equals(c)) {
        throw new AssertionError();
    }
}

void main() {
    testSetXor();
    testCombinations();

    var in = new BufferedReader(new InputStreamReader(System.in));
    var machines = parseInput(in);
    var total = 0L;
    for (var m : machines) {
        done:
        for (var c : IntStream.range(1, m.buttons().size() + 1).toArray()) {
            for (var attempt : combinations(m.buttons(), c)) {
                var lights = Set.<Integer>of();
                for(var button : attempt) {
                    lights = setXor(lights, button.lights);
                }

                if (lights.equals(m.lights)) {
                    total += c;
                    break done;
                }
            }
        }
    }
    print("Part 1:", total);
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
                var l = IntStream.range(0, lights.length())
                        .filter(i -> lights.charAt(i) == '#')
                        .boxed()
                        .collect(Collectors.toSet());

                var buttons = matcher.group(2);
                var b = Arrays.stream(buttons.split(" "))
                        .map(b1 -> new Button(
                                Arrays.stream(b1.substring(1, b1.length()-1)
                                                .split(","))
                                        .mapToInt(Integer::parseInt)
                                        .boxed()
                                        .collect(Collectors.toSet())))
                        .collect(Collectors.toSet());
                var j = Arrays.stream(matcher.group(3).split(",")).mapToInt(Integer::parseInt)
                        .boxed().toList();
                return new Machine(l, b, j);
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
