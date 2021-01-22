package library;

public class real {

	public double value;

	public real()
	{
	    value = 0;
	}
	
	public real(double value) {
		this.value = value;
	}

	// self = other
	public void operator_assignment(real other) {
		this.value = other.value;
	}

	// self += other
	public real operator_plus(real other) {
		return new real(this.value + other.value);
	}

	// self -= other
	public real operator_minus(real other) {
		return new real(this.value - other.value);
	}

	// self *= other
	public real operator_star(real other) {
		return new real(this.value * other.value);
	}

	// self /= other
	public real operator_slash(real other) {
		return new real(this.value / other.value);
	}
  
  public static real unary_minus(real other)
  {
    return new real(- other.value);
  }

	// real(string(x))
	public static real operator_parenthesis(string str) {
		return new real(Double.parseDouble(str.value));
	}

	// real(integer(x))
	public static real operator_parenthesis(integer integer) {
		return new real(integer.value);
	}

	// real(bool(x))
	public static real operator_parenthesis(bool bool) {
		return new real(bool.value);
	}

	// r1 == r2
	public bool operator_equals(real r2) {
		return new bool(value == r2.value);
	}

	// r1 != r2
	public bool operator_not_equals(real r2) {
		return new bool(value != r2.value);
	}

	// r1 <= r2
	public bool operator_less_equals(real r2) {
		return new bool(value <= r2.value);
	}

	// r1 >= r2
	public bool operator_greater_equals(real r2) {
		return new bool(value >= r2.value);
	}

	// r1 < r2
	public bool operator_less_than(real r2) {
		return new bool(value < r2.value);
	}

	// r1 > r2
	public bool operator_greater_than(real r2) {
		return new bool(value > r2.value);
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		real other = (real) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}

}
