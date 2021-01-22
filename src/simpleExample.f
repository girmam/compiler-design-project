
if true:
	print("This should be printed")
end if
else:
	print("This shouldn't be printed")
end else

type Dinosaur:
	static string status = "extinct"
	
	string name
	
	def printName:
		print(name)
	end printName
	
	static def revive:
		Dinosaur.status = "alive"
	end revive
end Dinosaur

Dinosaur d
d.name = "T.rex"

print(Dinosaur.status)
