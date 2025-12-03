import re

def joltage_loop(bats, n):
	result = ''
	last_index = -1
	for i in range(0, n):
		selected = 0
		s = last_index + 1
		e = len(bats) - (n - i) + 1
		for j in range(s, e):
			v = int(bats[j])
			if v > selected:
				selected = v
				last_index = j
		result += str(selected)

	return result


def joltage_recursive(bats, n):
	bat_d = '0'
	bat_v = 0
	bat_i = 0
	for i, d in enumerate(bats):
		v = int(d)
		
		if v > bat_v:
			bat_d = d
			bat_v = v
			bat_i = i

		if i + n == len(bats):
			if n == 1:
				return bat_d
			else:
				return bat_d + joltage_recursive(bats[bat_i+1:], n-1)


with open('input.txt', 'r') as file:
	part_1 = 0
	part_2 = 0
	for line in file:
		line = line.strip()
		
		jolt_2 = joltage_loop(line, 2)
		jolt_12 = joltage_loop(line, 12)

		# jolt_2 = joltage_recursive(line, 2)
		# jolt_12 = joltage_recursive(line, 12)
		
		part_1 += int(jolt_2)
		part_2 += int(jolt_12)
	
	print("Part 1:", part_1)
	print("Part 2:", part_2)		
		
