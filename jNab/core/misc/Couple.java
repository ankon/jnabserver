package jNab.core.misc;

/**
 * Utility class handling a generic couple of elements.
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 * 
 * @param <A> Type of the first element of the couple.
 * @param <B> Type of the second element of the couple.
 */

public class Couple<A, B>
{
    /**
     * First element of the couple.
     */
    private A firstElement;

    /**
     * Second element of the couple.
     */
    private B secondElement;

    /**
     * Creating a new couple instance with specified elements.
     * 
     * @param first the first element of the couple.
     * @param second the second element of the couple.
     */
    public Couple(A first, B second)
    {
	this.firstElement = first;
	this.secondElement = second;
    }

    /**
     * Getting the first element of the couple.
     * 
     * @return the first element of the couple.
     */
    public A getFirstElement()
    {
	return this.firstElement;
    }

    /**
     * Setting the first element of the couple.
     * 
     * @param element the first element of the couple.
     */
    public void setFirstElement(A element)
    {
	this.firstElement = element;
    }

    /**
     * Getting the second element of the couple.
     * 
     * @return the second element of the couple.
     */
    public B getSecondElement()
    {
	return this.secondElement;
    }

    /**
     * Setting the second element of the couple.
     * 
     * @param element the second element of the couple.
     */
    public void setSecondElement(B element)
    {
	this.secondElement = element;
    }
}
