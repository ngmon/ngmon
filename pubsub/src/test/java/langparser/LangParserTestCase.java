package langparser;

import cz.muni.fi.langparser.LangParser;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.LongRange;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
import java.text.ParseException;
import static org.junit.Assert.*;
import org.junit.Test;

public class LangParserTestCase { //TODO test the rest
    
    private static final String ATTRIBUTE_NAME = "attribute";
    
    @Test
    public void testParseLTLE() throws ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt 42");
        Constraint<?> constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.LESS_THAN, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#lt -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.LESS_THAN, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#le -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.LESS_THAN_OR_EQUAL_TO, constraint.getOperator());
    }
    
    @Test(expected = ParseException.class)
    public void testFailNotNumberLT() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt -42a");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailTooManyArgsLT() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt 10 20");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailStringArgLT() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt blabla");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailNoSpaceLT() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt_5");
        parser.parse();
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testFailNoArgsLT() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt ");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailSpaceAtTheEndLT() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt 0 ");
        parser.parse();
    }
    
    @Test
    public void testParseGTGE() throws ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#gt 42");
        Constraint<?> constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.GREATER_THAN, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#gt -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.GREATER_THAN, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#ge -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.GREATER_THAN_OR_EQUAL_TO, constraint.getOperator());
    }
    
    @Test(expected = ParseException.class)
    public void testFailNotNumberGT() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#gt -42a");
        parser.parse();
    }
    
    @Test
    public void testParseEQ() throws ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#eq 42");
        Constraint<?> constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#eq -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#eq bazinga");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("bazinga", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#eq 42a");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("42a", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());

        parser = new LangParser(ATTRIBUTE_NAME, "#eq 00:00:00:00");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("00:00:00:00", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#eq 'this is a quoted String'");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("this is a quoted String", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
    }
    
    @Test(expected = ParseException.class)
    public void testFailNoArgsButSpaceEQ() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#eq  ");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailNoClosingQuoteEQ() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#eq 'no closing quote");
        parser.parse();
    }
    
    @Test
    public void testParsePREF() throws ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#pref abc");
        Constraint<?> constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("abc", constraint.getAttributeValue().getValue());
        assertEquals(Operator.PREFIX, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#pref 'quoted String'");
        constraint = parser.parse();
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("quoted String", constraint.getAttributeValue().getValue());
    }
    
    @Test(expected = ParseException.class)
    public void testFailSpaceAtTheEndPref() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#pref space ");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailNoClosingQuotePref() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#pref 'no closing quote");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailOpMisspelledPREF() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#pre abc");
        parser.parse();
    }
    
    @Test
    public void testParseRNG() throws ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#rng 10 20");
        Constraint<?> constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(LongRange.class, constraint.getAttributeValue().getType());
        assertEquals(Operator.RANGE, constraint.getOperator());
        assertSame(10L, ((LongRange)(constraint.getAttributeValue().getValue())).getStart());
        assertSame(20L, ((LongRange)(constraint.getAttributeValue().getValue())).getEnd());
    }
    
    @Test(expected = ParseException.class)
    public void testFailTypeMismatchRNG() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#rng 10 dvanast");
        parser.parse();
    }
    
    @Test(expected = ParseException.class)
    public void testFailUnexpSpaceRNG() throws IndexOutOfBoundsException, UnsupportedOperationException, ParseException {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#rng 10  30");
        parser.parse();
    }
}
