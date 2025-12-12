
record Device(String name, List<String> devices) {
}


void main() {
    var in = new BufferedReader(new InputStreamReader(System.in));
    var deviceMap = in.lines()
            .map(l -> {
                var s = l.split(":");
                var name = s[0];
                var devices = s[1].trim().split(" ");
                return new Device(name, Arrays.stream(devices).toList());
            })
            .collect(Collectors.toMap(Device::name, Device::devices));

    print("Part 1:", part1(deviceMap, "you", new HashMap<>()));
    print("Part 2:", (
              part2(deviceMap, "svr", "dac", new HashMap<>())
            * part2(deviceMap, "dac", "fft", new HashMap<>())
            * part2(deviceMap, "fft", "out", new HashMap<>()) + (
              part2(deviceMap, "svr", "fft", new HashMap<>())
            * part2(deviceMap, "fft", "dac", new HashMap<>())
            * part2(deviceMap, "dac", "out", new HashMap<>()))));
}

long part2(Map<String, List<String>> devices, String from, String to, Map<String, Long> visited) {
    if (from.equals(to)) {
        return 1L;
    }

    if (visited.containsKey(from)) {
        return visited.get(from);
    }

    var options = devices.get(from);
    if (options == null || options.isEmpty()) {
        visited.put(from, 0L);
        return 0L;
    }
    var total = 0L;
    for (var option : options) {
        total += part2(devices, option, to, visited);
    }

    visited.put(from, total);
    return total;
}

long part1(Map<String, List<String>> devices, String name, Map<String, Long> visited) {
    if (visited.containsKey(name)) {
        return visited.get(name);
    }

    var options = devices.get(name);
    if (options.contains("out")) {
        return 1;
    }
    var total = 0L;
    for (var option : options) {
        var n = part1(devices, option, visited);
        visited.put(option, n);
        total += n;
    }

    return total;
}

void print(Object... values) {
    var sb = new StringBuilder();
    for (Object value : values) {
        sb.append(value);
        sb.append(" ");
    }
    System.out.println(sb);
}
