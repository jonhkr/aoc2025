
void main() throws IOException {
    final var lines = Files.readAllLines(Path.of("./sample-input.txt")).stream().toList();
    final var opsLine = lines.getLast() + ' ';
    final var ops = Arrays.stream(opsLine.split(" +")).toList();
    final var digitLines = lines.subList(0, lines.size() -1);
    final var digitRows = digitLines.stream()
            .map(s -> Arrays.stream(s.trim().split(" +"))
                    .map(Long::parseLong)
                    .toList())
            .toList();
    final var columnSizes = Arrays.stream(opsLine.split("([+*])"))
            .map(String::length)
            .dropWhile(l -> l <= 0)
            .toList();

    var part1 = 0L;
    var part2 = 0L;
    var lastColumnX = 0;
    for (int i = 0; i < ops.size(); i++) {
        var op = ops.get(i);

        var part1Digits = LongStream.builder();
        for (List<Long> row : digitRows) {
            part1Digits.accept(row.get(i));
        }
        part1 += applyOp(op, part1Digits.build());

        var columnSize = columnSizes.get(i);
        var part2Digits = LongStream.builder();
        for (int x = 0; x < columnSize; x++) {
            StringBuilder d = new StringBuilder();
            for (String digitLine : digitLines) {
                d.append(digitLine.charAt(x + lastColumnX));
            }
            part2Digits.accept(Long.parseLong(d.toString().trim()));
        }

        lastColumnX += columnSize + 1;
        part2 += applyOp(op, part2Digits.build());
    }

    System.out.println("Part 1: " + part1);
    System.out.println("Part 2: " + part2);
    System.out.println("Part 2: " + part2Alternative(lines));
}

private long part2Alternative(List<String> lines) {
    var width = lines.getLast().length();
    var stack = new Stack<Long>();
    var part2 = 0L;
    for (int w = width-1; w >= 0; w--) {
        int finalW = w;
        var data = lines.stream()
                .map(l -> l.charAt(finalW))
                .map(String::valueOf)
                .collect(Collectors.joining());

        if (data.trim().isEmpty()) {
            continue;
        }

        if (data.matches(".*[*+]$")) {
            var op = data.charAt(data.length()-1);
            var lastNum = Long.parseLong(data.substring(0, data.length()-1).trim());
            stack.push(lastNum);
            part2 += applyOp(op, stack.stream().mapToLong(Long::longValue));
            stack.clear();
        } else {
            stack.push(Long.parseLong(data.trim()));
        }
    }

    return part2;
}

private long applyOp(String op, LongStream values) {
    return switch (op) {
        case "+" -> values.sum();
        case "*" -> values.reduce(1L, (left, right) -> left * right);
        default -> throw new IllegalArgumentException("Invalid op: " + op);
    };
}

private long applyOp(char op, LongStream values) {
    return applyOp(String.valueOf(op), values);
}
