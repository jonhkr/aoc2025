void main() throws IOException {
    final var lines = Files.readAllLines(Path.of("./input.txt"))
            .stream()
            .toList();

    final var splits = new AtomicInteger();

    final var beanMap = new HashMap<Integer, Long>() {{
        put(lines.getFirst().indexOf('S'), 1L);
    }};

    lines.stream().skip(1).forEach(l -> {
        new ArrayList<>(beanMap.keySet()).forEach(x -> {
            if (l.charAt(x) == '^') {
                splits.getAndIncrement();
                final var beans = beanMap.get(x);
                beanMap.compute(x - 1, (_, v) -> v == null ? beans : v + beans);
                beanMap.compute(x + 1, (_, v) -> v == null ? beans : v + beans);
                beanMap.remove(x);
            }
        });
    });

    System.out.println("Part 1: " + splits.get());
    System.out.println("Part 2: " + beanMap.values().stream().mapToLong(Long::longValue).sum());
}
