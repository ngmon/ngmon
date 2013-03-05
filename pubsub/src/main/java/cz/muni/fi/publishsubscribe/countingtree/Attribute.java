package cz.muni.fi.publishsubscribe.countingtree;

/**
 * An attribute with a name and a value (events will use this)
 * 
 * @param <T1> The type of the value that will be stored in this attribute
 */
public class Attribute<T1 extends Comparable<T1>> {

		private String name;

		private AttributeValue<T1> value;

		public Attribute(String name, AttributeValue<T1> value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public AttributeValue<T1> getValue() {
			return value;
		}

	}