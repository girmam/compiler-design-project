package library;

public class bool {

	public int value;
	
	public bool()
	{
	    value = 0;
	}
	
	// Constructor for int
	public bool(int value) {
		this.value = value;
	}

	// Constructor for boolean
	public bool(boolean value) {
		if (value)
			this.value = 1;
		else
			this.value = 0;
	}

	// self = other
	public void operator_assignment(bool other) {
		this.value = other.value;
	}
 
	public bool operator_equals(bool bool2) {
		if(value == bool2.value)
      return new bool(1);
    else
      return new bool(0);
	}

	// bool1 != bool2
	public bool operator_not_equals(bool bool2) {
    if(value == bool2.value)
      return new bool(0);
    else
      return new bool(1);
	}
  
  public static bool operator_parenthesis(string str)
  {
    if(str.value.equals("true"))
      return new bool(1);
    else
      return new bool(0);
  }
  
  public static bool operator_not(bool bool1)
  {
    if(bool1.value == 0)
      return new bool(1);
    else
      return new bool(0);
  }
  
  public bool operator_or(bool bool1)
  {
    if(value + bool1.value == 1 || value + bool1.value == 2)
      return new bool(1);
    else
      return new bool(0);
  }
  
  public bool operator_and(bool bool1)
  {
    if(value + bool1.value == 2)
      return new bool(1);
    else
      return new bool(0);
  }

	@Override
	public String toString() {
		if(value == 0)
      return "false";
    else
      return "true";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		bool other = (bool) obj;
		if (value != other.value)
			return false;
		return true;
	}

}
