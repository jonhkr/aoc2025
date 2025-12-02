import re

PART_1_PATTERN = re.compile(r"^(.+)\1$")
PART_2_PATTERN = re.compile(r"^(.+)\1+$")

def valid_p1(_id):
	return PART_1_PATTERN.match(_id) is None

def valid_p2(_id):
	return PART_2_PATTERN.match(_id) is None

with open('input.txt', 'r') as i:
	ranges = i.read().split(',')
	invalids_p1 = 0
	invalids_p2 = 0
	for r in ranges:
		[a, b] = r.split('-')
		for i in range(int(a), int(b) + 1):
			if not valid_p1(str(i)):
				invalids_p1 += i
			if not valid_p2(str(i)):
				invalids_p2 += i
	
	print("Part 1:", invalids_p1)
	print("Part 2:", invalids_p2)
