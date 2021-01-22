type TestType:
	integer i
	string name
	
	def printName:
		print(name)
		i = i + 1
	end printName
end TestType

TestType t
t.name = "Jordan, who is the best there ever was"

def do_thrice(routine r):
	r()
	r()
	r()
end do_thrice

def printHello:
	print("Hello")
end printHello

do_thrice(printHello)
do_thrice(t.printName())


