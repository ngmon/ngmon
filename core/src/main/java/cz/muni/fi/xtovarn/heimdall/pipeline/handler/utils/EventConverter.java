package cz.muni.fi.xtovarn.heimdall.pipeline.handler.utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import cz.muni.fi.publishsubscribe.countingtree.Attribute;
import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
import cz.muni.fi.publishsubscribe.countingtree.Event;
import cz.muni.fi.publishsubscribe.countingtree.EventImpl;

public class EventConverter {

	private enum TYPE {
		INTEGER, STRING, DATE
	}

	public <T1 extends Comparable<T1>> Event ngmonEventToPubsubEvent(
			cz.muni.fi.xtovarn.heimdall.commons.entity.Event ngmonEvent) throws IntrospectionException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		EventImpl pubSubEvent = new EventImpl();

		for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(
				cz.muni.fi.xtovarn.heimdall.commons.entity.Event.class, Object.class).getPropertyDescriptors()) {
			// propertyEditor.getReadMethod() exposes the getter
			// btw, this may be null if you have a write-only property
			Method method = propertyDescriptor.getReadMethod();
			if (method == null)
				continue;

			String methodName = method.getName();
			if (methodName.equals("getPayload"))
				continue;

			Class<?> resultClass = method.getReturnType();
			Object result = method.invoke(ngmonEvent);
			// remove if null is a valid attribute value
			if (result == null)
				continue;
			String attributeName = methodName.substring(3);
			if (attributeName.isEmpty())
				continue;

			TYPE resultType;
			// String, Date, long, int
			if (resultClass.equals(Integer.TYPE) || resultClass.equals(Integer.class) || resultClass.equals(Long.TYPE)
					|| resultClass.equals(Long.class)) {
				resultType = TYPE.INTEGER;
			} else if (resultClass.equals(String.class)) {
				resultType = TYPE.STRING;
			} else if (resultClass.equals(Date.class)) {
				resultType = TYPE.DATE;
			} else {
				throw new IllegalArgumentException("Type " + resultClass + " is not supported");
			}

			// ugly unchecked casts
			pubSubEvent.addAttribute(new Attribute<>(attributeName, new AttributeValue<>((T1) result,
					(Class<T1>) typeToClass(resultType))));

			System.out.println("Method name = " + methodName);
			System.out.println("Method return type = " + resultClass);
			System.out.println("Method value = " + result);
		}

		return pubSubEvent;
	}

	private Class<? extends Comparable<?>> typeToClass(TYPE type) {
		switch (type) {
		case INTEGER:
			return Long.class;
		case STRING:
			return String.class;
		case DATE:
			return Date.class;
		default:
			throw new IllegalArgumentException(type + " is not supported");
		}
	}
}
