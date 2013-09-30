package brightedge;

public class Item {
	
	String name;
	String price;
	
	Item() {
		name = "";
		price = "";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPrice(String price) {
		this.price = price;
	}

	public String getName() {
		return this.name;
	}

	public String getPrice() {
		return this.price;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		
		str.append("Item Name: " + this.name + "\n");
		str.append("Item Price: " + this.price + "\n");

		return str.toString();
		
	}
}
