EMPTY = '.'
PAPER_ROLL = '@'
Y_MASK = [-1,-1,-1,0,0,1,1,1]
X_MASK = [-1,0,1,-1,1,-1,0,1]

def find_reachable(board):
	y_size = len(board)
	x_size = len(board[0])
	reachable = []

	for y, line in enumerate(board):
		for x, v in enumerate(line):
			if v != PAPER_ROLL:
				continue

			neighbors = 0
			for k in range(0, len(Y_MASK)):
				ny = Y_MASK[k]
				nx = X_MASK[k]
				i = y + ny
				j = x + nx
				if -1 < i < y_size and -1 < j < x_size:
					if board[i][j] == PAPER_ROLL:
						neighbors += 1

			if neighbors < 4:
				reachable.append((x, y))
	return reachable

with open('input.txt', 'r') as file:
	board = [list(line) for line in file]
	
	print("Part 1:", len(find_reachable(board)))

	removed = 0

	while (True):
		reachable = find_reachable(board)

		if len(reachable) == 0:
			break
		for x, y in reachable:
			board[y][x] = EMPTY
			removed += 1

	print("Part 2", removed)

