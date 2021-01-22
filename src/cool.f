bool cool = true
bool not_cool = false

integer i = 0

def cool_routine returns routine returns string:

	routine returns string return_routine
	
	if cool:
		i = 5
		
		type cool_type:
			string name
			
			def getName returns string:
				return name
			end getName
		end cool_type
		
		cool_type ct
		ct.name = "The best"
		
		return_routine = ct.getName
	
	else:
		i = 6
		
		def not_cool_routine returns string:
				return "Not cool man"
		end not_cool_routine
		
		return_routine = not_cool_routine
	end if
	
	return return_routine
end cool_routine

