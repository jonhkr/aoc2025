import re

with open('input.txt', 'r') as file:
	total_joltage = 0
	for line in file:
		line = line.strip()
		bat_1 = 0
		bat_2 = 0
		l = len(line)
		
		for i in range(0, l): 
			d = int(line[i])
			if d > bat_1 and i+1 != l:
				bat_1 = d
				bat_2 = int(line[i+1])
			else:
				if d > bat_2:
					bat_2 = d
		
		joltage = int(str(bat_1) + str(bat_2))
		total_joltage += joltage
		print(line, bat_1, bat_2, joltage, total_joltage)
		
		
