package library;
public class integer {
	public int value = 0;

	public integer()
	{
	    value = 0;
	}
	public integer(int value) {
		this.value = value;
	}

	// self = other
	public void operator_assignment(integer other) {
		this.value = other.value;
	}

	// self + other
	public integer operator_plus(integer other) {
		return new integer(this.value += other.value);
	}

	// self - other
	public integer operator_minus(integer other) {
		return new integer(this.value - other.value);
	}

	// self * other
	public integer operator_star(integer other) {
		return new integer(this.value * other.value);
	}

	// self / other
	public integer operator_slash(integer other) {
		return new integer(value / other.value);
	}
  
  public integer operator_mod(integer other)
  {
    return new integer(value % other.value);
  }

  public static integer unary_minus(integer i)
  {
    return new integer(-i.value);
  }
  
	// integer(string(x))
	public static integer operator_parenthesis(string str) {
		return new integer(Integer.parseInt(str.value));
	}

	// integer(real(x))
	public static integer operator_parenthesis(real real) {
		return new integer((int) real.value);
	}

	// integer(bool(x))
	public static integer operator_parenthesis(bool bool) {
		return new integer(bool.value);
	}

	// int1 == int2
	public bool operator_equals(integer int2) {
		return new bool(value == int2.value);
	}

	// int1 != int2
	public bool operator_not_equals(integer int2) {
		return new bool(value != int2.value);
	}

	// int1 <= int2
	public bool operator_less_equals(integer int2) {
		return new bool(value <= int2.value);
	}

	// int1 >= int2
	public bool operator_greater_equals(integer int2) {
		return new bool(value >= int2.value);
	}

	// int1 < int2
	public bool operator_less_than(integer int2) {
		return new bool(value < int2.value);
	}

	// int1 > int2
	public bool operator_greater_than(integer int2) {
		return new bool(value > int2.value);
	}

	@Override
	public String toString() {
		return Integer.toString(value);
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
		integer other = (integer) obj;
		if (value != other.value)
			return false;
		return true;
	}

}
