
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

    print("Part 1:", count(deviceMap, "you", "out", new HashMap<>()));
    print("Part 2:", (
              count(deviceMap, "svr", "dac", new HashMap<>())
            * count(deviceMap, "dac", "fft", new HashMap<>())
            * count(deviceMap, "fft", "out", new HashMap<>()) + (
              count(deviceMap, "svr", "fft", new HashMap<>())
            * count(deviceMap, "fft", "dac", new HashMap<>())
            * count(deviceMap, "dac", "out", new HashMap<>()))));
}

long count(Map<String, List<String>> devices, String from, String to, Map<String, Long> visited) {
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
        total += count(devices, option, to, visited);
    }

    visited.put(from, total);
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
