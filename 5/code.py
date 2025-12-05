def merge_overlapping(ranges):
    sorted_ranges = sorted(ranges, key=lambda x: x[0])
    L = len(sorted_ranges)
    result = []
    overlapping = None

    for i, r in enumerate(sorted_ranges):
        current = overlapping or r

        if i == L - 1:
            result.append(current)
            break

        other = sorted_ranges[i + 1]

        a, b = current
        k, l = other
        if b - k >= 0:
            overlapping = (min(a, k), max(b, l))
        else:
            overlapping = None
            result.append(current)

    return result


def in_any_fresh_range(ingredient, ranges):
    for a, b in ranges:
        if b >= ingredient >= a:
            return True
    return False


def part1(ranges, ingredients):
    fresh = 0
    for ing in ingredients:
        if in_any_fresh_range(ing, ranges):
            fresh += 1
    return fresh


def part2(ranges):
    result = 0
    for a, b in ranges:
        result += b - a + 1
    return result


with open('input.txt', 'r') as file:
    fresh_ranges = []
    for line in file:
        line = line.strip()
        if line == '':
            break
        fresh_ranges.append(tuple(map(int, line.split("-"))))

    fresh_ranges = merge_overlapping(fresh_ranges)

    print("Part 1:", part1(fresh_ranges, [int(line.strip()) for line in file]))
    print("Part 2:", part2(fresh_ranges))
