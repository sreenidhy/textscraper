package brightedge;

import java.util.Iterator;
import java.util.List;

public class Request {
	
	private static final boolean DEBUG = true;
	
	private static void printUsage() {
		System.out.println("");
		System.out.println("\t\tUsage : Request <keyword> [page number]");
		System.out.println("");
	}
	
	public static void main(String[] args) {
		
		TextScraper scraper = new TextScraper();
		Result res = new Result();
		boolean query1 = false, query2 = false;
		String searchString = "";
		String pageNumber = "1"; //default page number is 1
		
		/*
		 *  Parse the number of arguments 
		 */
		if (args.length > 2 || args.length < 1) {
			printUsage();
			System.exit(1);
			
		} else if (args.length == 2) {
			// set query 2 to be true
			query2 = true;			
			
		} else if  (args.length == 1) {
			// set query 1 to be true
			query1 = true;			
		}

		System.out.println("");
		/*
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "] = " + args[i]);
		}
		*/		
		
		searchString = args[0];
		scraper.setSearchString(searchString);		
		if (query2) {
			// throw exception if not number and if it is 0
			pageNumber = args[1];
			scraper.setPageNumber(pageNumber);
		}
		
		// start scrapping the webpage for requested data
		System.out.println("Searching for requested information...");
		
		try {
			scraper.start();
		
			String total = scraper.getTotalResult();
			if (total.length() != 0) {
				System.out.println("\nTotal Result for search item [" + 
						searchString + "] = " + scraper.getTotalResult() + "\n");
				if (query2) {
					res = scraper.getResult();
					List<Item> itemList = res.getItemList();
					Iterator<Item> itemIt = itemList.iterator();
					Item it = new Item();
					int cnt = 1;

					System.out.println("\n-------------Product Details-------------");			
					while (itemIt.hasNext()) {
						it = itemIt.next();
						System.out.println("\nItem no. " + cnt);
						System.out.println(it);
						cnt++;
					}
					System.out.println("\n-------------Product Details-------------");
				}
			} else {
				System.out.println("Invalid Request or Page Not Found");
			}
			
		} catch (Exception ex) {
			if (DEBUG)
				ex.printStackTrace();
		}
		
		System.out.println("");
	}
}