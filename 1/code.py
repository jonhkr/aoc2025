def parse(c):
	d = c[0]
	n = int(c[1:])
	return [d, n]


with open('input.txt', 'r') as i:
	p = 50
	p1 = 0
	p2 = 0
	for line in i:
		l = line.strip()
		[d, n] = parse(l)

		p2 += n // 100
		n = n % 100
		if d == 'L':
			n = n * (-1)

		if p + n >= 100 or p + n <= 0:
			if p != 0:
				p2 += 1

		p = (p + n) % 100

		if p == 0:
			p1 += 1
	print(p1, p2)
