package library;

public class string {
	public String value;

	public string()
	{
	    value = "";
	}
	
	public string(String value) {
		this.value = value;
	}

	// self = other
	public void operator_assignment(string other) {
		this.value = other.value;
	}

	// self += other
	public string operator_plus(string other) {
		return new string(this.value + other.value);
	}
	
	// substring(str1, x, y)
	public string substring(integer beginIndex, integer endIndex) {
		return new string(value.substring(beginIndex.value, endIndex.value));
	}

	// string(real(x))
	public static string operator_parenthesis(real real) {
		return new string(real.toString());
	}

	// string(integer(x))
	public static string operator_parenthesis(integer integer) {
		return new string(integer.toString());
	}

	// string(bool(x))
	public static string operator_parenthesis(bool bool) {
		return new string(bool.toString());
	}

	// str1 == str2
	public bool operator_equals(string str2) {
		return new bool(value.equals(str2.value));
	}

	public bool operator_not_equals(string str2) {
		return new bool(!value.equals(str2.value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		string other = (string) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
