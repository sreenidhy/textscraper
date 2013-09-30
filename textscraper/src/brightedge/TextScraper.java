package brightedge;

import java.io.*;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class TextScraper {
	
	private static final boolean DEBUG = false;
	
	private static final String HOST = "http://www.walmart.com/";
	private static final String LINK = "search/search-ng.do?";
	private static final String SQ = "search_query";
	private static final String IC = "ic";
	private static final String FILENAME_RESPONSE = "./response.html";
	private static final String FILENAME_ITEMS = "./items.html";
	private static final String NO_SHIPPING_INFO = "No Shipping Information";
	private static final int FACTOR = 16;
	
	String totalResult;
	String link;
	String searchString;
	String pageNumber;
	Result result;
	
	TextScraper() {
		totalResult = "";
		link = "";
		searchString = "";
		pageNumber = "";
		result = new Result();
	}
	
	TextScraper(String searchStr) {
		this.searchString = searchStr;
	}
	
	TextScraper(String searchStr, String pageNum) {
		this.searchString = searchStr;
		this.pageNumber = pageNum;
	}
	
	public void setSearchString(String str) {
		this.searchString = str;
	}
	
	public void setPageNumber(String pageNum) {
		this.pageNumber = pageNum;
	}
	
	public Result getResult() {
		return this.result;
	}
	
	public String getTotalResult() {
		return this.totalResult;
	}

	/*
	 * start will first try to connect to the HOST site 
	 * and fetch the requested information. It can fail
	 * with UnknownHostException or IOException. On 
	 * success, it stores the result in a Document object
	 * which will be traversed using Jsoup apis.
	 * It builds the list of Items based on the document
	 * object for the specified request.
	 */
	public void start()
			throws UnknownHostException, IOException {
		
		/* 
		 * sample queries and conclusion
		 * query string has lot of additional information
		 * http://www.walmart.com/search/search-ng.do?ic=16_0&Find=Find&search_query=digital+camera&Find=Find&search_constraint=0
		 * http://www.walmart.com/search/search-ng.do?tab_value=all&search_query=digital+camera&search_constraint=0&Find=Find&pref_store=3775&ss=false&ic=16_32&_mm=
		 * http://www.walmart.com/search/search-ng.do?tab_value=all&search_query=slr+camera&search_constraint=0&Find=Find&pref_store=3775&ss=false&ic=16_32&_mm=
		
   		 * verified that earch_query and ic sufficient to do a text scraping of all the results
		 * they have a strange key-value pair with key "ic" and its value looks 16_0 / 16_16 / 16_32... so on.
		 * 16_0 represents page 1, 16_16 represents page 2 and so on. This can be substituted with 15_0, 15_15, 15_30 so on.
		 * http://www.walmart.com/search/search-ng.do?search_query=digital+camera&ic=16_16
		 */
		
		String searchQuery = searchString.replace(" ", "+");
		String pgNum = "16_"; 
		int num = 0; // default is first page
		if (pageNumber.length() != 0) {
			num = Integer.parseInt(pageNumber);
			num--;
			num *= FACTOR;
		}
		
		pgNum += num;
		link = LINK + SQ + "=" + searchQuery + "&" + IC + "=" + pgNum;
		if (DEBUG) {
			System.out.println("search string = " + searchQuery + ", page number = " + pgNum);
			System.out.println("url = " + HOST + link);
		}
		
		try {
			
			Connection conn = Jsoup.connect(HOST + link);
			conn.userAgent("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36");
			//Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.76 Safari/537.36
			Document doc = conn.get();

			if (DEBUG) {
				printToFile(doc, FILENAME_RESPONSE);
			}
			
			/*
			 * Number of results is of the form
			 * 1234 Results
			 * Extract just the numerical part from this
			 */
			totalResult = doc.select(".numResults").text().split(" ")[0];
			if (DEBUG) {
				System.out.println("totalResult = " + totalResult);
			}

			Elements resultContainer = doc.select("div[class=prodInfo]");
			if (DEBUG)
				printToFile(resultContainer, FILENAME_ITEMS);
			
			Iterator<Element> it = resultContainer.iterator();
			Element prodInfo;
			
			while (it.hasNext()) {
					
				// get the details of each product
				Item item = new Item();
				prodInfo = it.next();

				// populate the product bean(item) object with the extracted data
				populateItem(item, prodInfo);
				
				// add the product to the result
				result.addItem(item);
				
			} // end of while
            
		} catch (UnknownHostException uhe) {
			System.out.println("Unable to reach the host " + HOST);
			if (DEBUG)
				uhe.printStackTrace();
			throw uhe;
		} catch (IOException ioe) {
			System.out.println("Failed to perform operation");
			if (DEBUG)
				ioe.printStackTrace();
			throw ioe;
		} 
	} // start
	
	private void populateItem(Item it, Element prodInfo) {
		
		Elements gridItemLink, bigPriceText, smallPriceText;
		String itemName, itemPrice;
		
		if (null != prodInfo) {
			/*
			 * prodInfo has gridItem and listItem elements for the same product.
			 * using gridItem to get the required info
			 */
			gridItemLink = prodInfo.getElementsByAttributeValue("class", "prodLink GridItemLink");		
			bigPriceText = prodInfo.getElementsByAttributeValue("class", "bigPriceText2");
			if (bigPriceText.text() == "" || bigPriceText.text().length() == 0) {
				bigPriceText = prodInfo.getElementsByAttributeValue("class", "bigPriceTextOutStock2");
			}
		
			smallPriceText = prodInfo.getElementsByAttributeValue("class", "smallPriceText2");	
			if (smallPriceText.text() == "" || smallPriceText.text().length() == 0) {
				smallPriceText = prodInfo.getElementsByAttributeValue("class", "smallPriceTextOutStock2");
			}
		
			// we always have 1 product information in this function, can safely use get(0)
			// get the title from the href element
			//itemName = gridItemLink.get(0).text();
			itemName = gridItemLink.get(0).attr("title");
			itemPrice = bigPriceText.text() + smallPriceText.text();
			if (DEBUG) {
				System.out.println("itemName = " + itemName);
				System.out.println("itemPrice = " + itemPrice);
			}
		
			// update the item with above info
			if (null != itemName  && itemName.length() != 0) {
				it.setName(itemName);
			}
			if (null != itemPrice && itemPrice.length() != 0) {
				it.setPrice(itemPrice);
			}
		}
	} // populateProduct
	
	// for debugging purpose only
	private void printToFile(Object obj, String fname) {
		// temp
		File tmp = new File(fname);
		try {
			if (tmp.exists()) {
				tmp.delete();
				/*
				tmp.createNewFile();
				FileWriter fw =  new FileWriter(fname);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(obj.toString());
				
			} else {
				System.out.println("file not exists");
				*/
			}
			
			tmp.createNewFile();
			FileWriter fw =  new FileWriter(fname);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(obj.toString());

		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

}