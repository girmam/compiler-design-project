type Dinosaur:
	string name
	
	def printName:
		print(name)
		name = "Utahraptor"
		print(name)
	end printName
	
end Dinosaur

Dinosaur d
d.name = "spinosaurus"

def print_twice(routine r):
	r()
	r()
end print_twice

print_twice(d.printName)