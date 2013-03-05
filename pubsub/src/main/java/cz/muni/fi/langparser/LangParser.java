package cz.muni.fi.langparser;

import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.LongRange;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LangParser {

    private final String attributeName;
    private final String input;
    private int currentPos = 0;
    private char current;
    private String alreadyRead = "";

    public LangParser(String attributeName, String input) {
        this.attributeName = attributeName;
        this.input = input;
    }

    public Constraint<?> parse() throws ParseException, IndexOutOfBoundsException {
        AttributeValue<?> av = null;
        Operator operator = null;

        switch (readString()) {
            case "#lt":
                operator = Operator.LESS_THAN;
                av = argNum();
                break;
            case "#le":
                operator = Operator.LESS_THAN_OR_EQUAL_TO;
                av = argNum();
                break;
            case "#gt":
                operator = Operator.GREATER_THAN;
                av = argNum();
                break;
            case "#ge":
                operator = Operator.GREATER_THAN_OR_EQUAL_TO;
                av = argNum();
                break;
            case "#eq":
                operator = Operator.EQUALS;
                av = argAny();
                break;
            case "#rng":
                operator = Operator.RANGE;
                av = argNum_argNum();
                break;
            case "#pref":
                operator = Operator.PREFIX;
                av = argString();
                break;
            //TODO case "#suff"

            default:
                throw new ParseException("Unsupported Operator", currentPos);
        }

        return createConstraint(av, operator);
    }

    private AttributeValue<?> argNum() throws ParseException {
        if (! acceptSpace()) {
            throw new ParseException("Exactly one space expected", currentPos);
        }

        alreadyRead = "";

        current = readChar();
        if (current == '\'') { //expect quoted String -> if acceptable, argAny will take care of it
            throw new ParseException("Number expected", currentPos);
        }

        if (current == '-') { //expect negative number
            alreadyRead += current;
            while ((!isLastArg()) && (readCharIfNotSpace())) {
                alreadyRead += current;
            }

            return createNumericAttributeValue();
        }

        int dateEndsAt = 0;
        int year = 0;
        int month = 0;
        int day = 0;
        int hour;
        int min;
        int sec;

        alreadyRead += current;
        while ((!isLastArg()) && (readCharIfNotSpace())) {
            if (current == '-') { //expect date (y-m-dTh:m:s)
                year = getInteger(alreadyRead);
                month = readIntegerUntil('-');
                day = readIntegerUntil('T');
                dateEndsAt = alreadyRead.length() + 1;
            }

            if (current == ':') { //expect time (h:m:s)
                hour = getInteger(alreadyRead.substring(dateEndsAt));
                min = readIntegerUntil(':');
                alreadyRead += current;
                String seconds = readString();
                alreadyRead += seconds;

                if (isLastArg()) {
                    sec = getInteger(seconds);
                    if (dateEndsAt == 0) {
                        //TODO AttributeValue<Time>...
                        throw new UnsupportedOperationException("Time not supported yet");
                    } else {
                        Calendar dateTime = new GregorianCalendar(year, month, day, hour, min, sec);
                        return new AttributeValue<>(dateTime.getTime(), Date.class);
                    }
                }
            }
            alreadyRead += current;
        }

        //else expect positive integer
        return createNumericAttributeValue();
    }

    private AttributeValue<?> argAny() throws ParseException {
        AttributeValue<?> av = null;
        try {
            av = argNum();
        } catch (ParseException | UnsupportedOperationException e) {
            //TODO
        }

        if (av == null) {
            if (alreadyRead.equals("")) { //only possible if the first char was a space or '
                switch (current) {
                    case ' ':
                        throw new ParseException("No space expected", currentPos - 1);
                    case '\'':
                        av = readQuotedString();
                }
            } else {
                alreadyRead += readString();

                if (alreadyRead.trim().length() == 0) {
                    throw new ParseException("No space expected", currentPos - 1);
                }

                av = new AttributeValue<>(alreadyRead, String.class);
            }
        }

        return av;
    }

    private AttributeValue<?> argString() throws ParseException {
        if (!acceptSpace()) {
            throw new ParseException("Exactly one space expected", currentPos);
        }

        AttributeValue<String> av = null;
        alreadyRead = "";
        current = readChar();

        switch (current) {
            case ' ':
                throw new ParseException("No space expected", currentPos - 1);
            case '\'':
                av = readQuotedString();
                break;
            default:
                alreadyRead += current + readString();
                if (isLastArg()) {
                    av = new AttributeValue<>(alreadyRead, String.class);
                } else {
                    throw new ParseException("No space expected", currentPos);
                }
        }

        return av;
    }

    private AttributeValue<?> argNum_argNum() throws ParseException {
        AttributeValue<?> av1 = argNum();

        if (av1.getType() == Date.class) {
            AttributeValue<?> av2 = argNum();

            if (av2.getType() == Date.class) {
                //TODO return new AttributeValue<DateRange>...
                throw new UnsupportedOperationException("DateRange not supported yet");
            } else {
                throw new ParseException("Type mismatch", currentPos);
            }
        }

        //TODO time

        if (av1.getType() == Double.class) {
            AttributeValue<?> av2 = argNum();

            if ((av2.getType() == Double.class) || (av2.getType() == Long.class)) {
                //TODO return new AttributeValue<DoubleRange>...
                throw new UnsupportedOperationException("DoubleRange not supported yet");
            } else {
                throw new ParseException("Type mismatch", currentPos);
            }
        }

        if (av1.getType() == Long.class) {
            AttributeValue<?> av2 = argNum();

            if (av2.getType() == Long.class) {
                return new AttributeValue<>(new LongRange((Long)av1.getValue(), (Long)av2.getValue()), LongRange.class);
            } else {
                if (av2.getType() == Double.class) {
                    //TODO return new AttributeValue<DoubleRange>...
                    throw new UnsupportedOperationException("DoubleRange not supported yet");
                } else {
                    throw new ParseException("Type mismatch", currentPos);
                }
            }
        }

        throw new ParseException(av1.getType() + " not supported", currentPos);
    }

    private char readChar() {
        char next = input.charAt(currentPos);
        currentPos++;
        return next;
    }

    private boolean readCharIfNotSpace() {
        if (input.charAt(currentPos) == ' ') {
            return false;
        } else {
            current = readChar();
            return true;
        }
    }
    
    private String readString() {
        String s = "";
        while ((!isLastArg()) && (readCharIfNotSpace())) {
            s += current;
        }

        return s;
    }

    private AttributeValue<String> readQuotedString() throws ParseException {
        while ((!isLastArg()) && ((current = readChar()) != '\'')) {
            alreadyRead += current;
        }
        if (current == '\'') {
            return new AttributeValue<>(alreadyRead, String.class);
        } else {
            throw new ParseException("Closing quote expected", currentPos);
        }
    }

    private boolean accept(char c) {
        if (input.charAt(currentPos) == c) {
            currentPos++;
            return true;
        } else {
            return false;
        }
    }

    private boolean acceptSpace() { //exactly one space
        if (accept(' ')) {
            if (!isLastArg()) {
                if (input.charAt(currentPos) == ' ') {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private AttributeValue<?> createNumericAttributeValue() throws ParseException {
        AttributeValue<?> av;

        try {
            Double num = Double.valueOf(alreadyRead);
            if (num.longValue() == num) {
                av = new AttributeValue<>(num.longValue(), Long.class);
            } else {
//                av = new AttributeValue<>(num, Double.class);
                //TODO
                throw new UnsupportedOperationException("Non-integer values not supported yet + \"" + alreadyRead + "\"");
            }
        } catch (NumberFormatException e) {
            throw new ParseException("Number expected", currentPos);
        }

        return av;
    }

    private int getInteger(String s) throws ParseException { //expect non-negative integer
        int num;
        try {
            num = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            throw new ParseException("Integer expected", currentPos);
        }
        return num;
    }

    private int readIntegerUntil(char until) throws ParseException {
        alreadyRead += current;
        String string = "";
        while ((!isLastArg()) && (readCharIfNotSpace()) && (current != until)) {
            string += current;
            alreadyRead += current;
        }

        if ((isLastArg()) || (current == ' ')) {
            throw new ParseException("Unexpected end of input", currentPos);
        }

        return getInteger(string);
    }

    private Constraint<?> createConstraint(AttributeValue<?> av, Operator operator) throws ParseException {
        if (! isLastArg()) {
            throw new ParseException("End of input expected", currentPos);
        }

        if (av == null) {
            throw new ParseException("Failed to parse \'" + alreadyRead + "\'", currentPos);
        }

        if (operator == null) {
            throw new ParseException("Unsupported Operator", currentPos);
        }

        return new Constraint<>(attributeName, av, operator);
    }

    private boolean isLastArg() {
        if (currentPos < input.length()) {
            return false;
        }
        return true;
    }
}