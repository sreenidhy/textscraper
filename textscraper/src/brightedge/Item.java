package brightedge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Item {
	
	String name;
	String price;
	//String shippingPrice;
	//List<String> vendors;
	
	Item() {
		name = "";
		price = "";
		//shippingPrice = "";
		//vendors = new ArrayList<String>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPrice(String price) {
		this.price = price;
	}

	/*
	public void setShippingPrice(String shippingPrice) {
		this.shippingPrice = shippingPrice;
	}

	public void setVendors(List<String> vendors) {
		this.vendors = vendors;
	}
	*/
	
	public String getName() {
		return this.name;
	}

	public String getPrice() {
		return this.price;
	}

	/*
	public String getShippingPrice() {
		return this.shippingPrice;
	}

	public List<String> getVendors() {
		return this.vendors;
	}
	*/
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		
		str.append("Item Name: " + this.name + "\n");
		str.append("Item Price: " + this.price + "\n");
		//str.append("Item Shipping Price: " + this.shippingPrice + "\n");
		//Iterator<String> vIt = this.vendors.iterator();
		//while (vIt.hasNext()) {
			//str.append("Item Vendor Information: " + vIt.next() + "\n");
		//}
		
		return str.toString();
		
	}
}
