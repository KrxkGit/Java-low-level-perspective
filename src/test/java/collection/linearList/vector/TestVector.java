package collection.linearList.vector;

import org.junit.Test;

import java.util.Vector;

public class TestVector {
    @Test
    public void testVector() {
        System.out.println("testVector");
        Vector<Integer> vector = new Vector<Integer>();
        vector.insertElementAt(2, 0);
    }

}
