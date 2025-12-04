import re

def joltage_loop(bats, n):
	L = len(bats)
	result = 0
	last_index = -1
	for i in range(0, n):
		selected = 0
		s = last_index + 1
		e = L - (n - i) + 1
		for j in range(s, e):
			v = bats[j]
			if v > selected:
				selected = v
				last_index = j
				if v == 9:
					break
		result = result * 10 + selected

	return result


def joltage_recursive(bats, n):
	bat_v = 0
	bat_i = 0
	for i, v in enumerate(bats):
		if v > bat_v:
			bat_v = v
			bat_i = i

		if i + n == len(bats) or bat_v == 9:
			if n == 1:
				return bat_v
			else:
				return bat_v * (10 ** (n -1)) + joltage_recursive(bats[bat_i+1:], n-1)


with open('input.txt', 'r') as file:
	part_1 = 0
	part_2 = 0
	for line in file:
		line = line.strip()
		bats = list(map(int, line))
		
		jolt_2 = joltage_loop(bats, 2)
		jolt_12 = joltage_loop(bats, 12)

		# jolt_2 = joltage_recursive(bats, 2)
		# jolt_12 = joltage_recursive(bats, 12)
		
		part_1 += jolt_2
		part_2 += jolt_12
	
	print("Part 1:", part_1)
	print("Part 2:", part_2)		
		
