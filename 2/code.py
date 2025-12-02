
def valid_p1(_id):
	if len(_id) == 1:
		return True

	if len(_id) == 2:
		return _id[0] != _id[1]

	if len(_id) % 2 == 1:
		return True

	mid = len(_id) // 2
	a = _id[:mid]
	b = _id[mid:]
	return a != b

def valid_p2(_id):
	if len(_id) == 1:
		return True

	if len(_id) == 2:
		return _id[0] != _id[1]

	mid = len(_id) // 2
	for i in range(1, mid + 1):
		w = _id[0:i]
		if _id.count(w) == len(_id) / len(w):
			return False
	return True

with open('input.txt', 'r') as i:
	ranges = i.read().split(',')
	invalids_p1 = []
	invalids_p2 = []
	for r in ranges:
		[a, b] = r.split('-')
		for i in range(int(a), int(b) + 1):
			if not valid_p1(str(i)):
				invalids_p1.append(i)
			if not valid_p2(str(i)):
				invalids_p2.append(i)
	
	print("Part 1:", sum(invalids_p1))
	print("Part 2:", sum(invalids_p2))
