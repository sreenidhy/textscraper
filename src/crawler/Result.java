package crawler;

import java.util.ArrayList;
import java.util.List;

public class Result {

	List<Item> itemList;
	
	Result() {
		itemList = new ArrayList<Item>();
	}
	
	public void setItemList(List<Item> productList) {
		this.itemList = productList;
	}
	
	public List<Item> getItemList() {
		return this.itemList;
	}
	
	public void addItem(Item item) {
		itemList.add(item);
	}
}