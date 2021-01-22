type TestType:
	string name
	static string dname = "general kenobi"
	
	def printName:
		print(name)
	end printName
	
	static def alsoPrintName:
		print("hello there")
	end alsoPrintName
end TestType

TestType t
t.name = string(1)
t.printName()

TestType.alsoPrintName()
print(TestType.dname)