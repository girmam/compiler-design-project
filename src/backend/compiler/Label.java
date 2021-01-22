package backend.compiler;

public class Label
{
    private static int index = 0;  // index for generating label strings
    private String label;          // the label string

    /**
     * Constructor.
     */
    public Label() { this.label = "L" + String.format("%03d", ++index); }

    /**
     * Generate the label string. 
     * @return the string.
     */
    public String toString() { return this.label; }
}