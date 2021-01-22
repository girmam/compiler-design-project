
def root(real x) returns real:
	real r, prev, diff
	
	r = 1.0
	prev = 0.0
	diff = 1.0
	
	while diff > 0.0000001:
		r = ( x / r + r ) / 2.0
		diff = r - prev
		 
		if diff < 0.0:
			diff = -diff
		end if
		
	end while
	
	return r
end root

def printRoot(integer n, real root):
	print("The square root of " + string(n) + " is " + string(root))
end printRoot

integer number = 1

while number <= 25:
	printRoot(number, root(real(number)))
end while