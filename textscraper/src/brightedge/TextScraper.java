package brightedge;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class TextScraper {
	
	private static final boolean DEBUG = true;
	
	private static final String HOST = "http://www.walmart.com/";
	private static final String LINK = "search/search-ng.do?";
	private static final String SQ = "search_query";
	private static final String IC = "ic";
	private static final String FILENAME = "/tmp/temp.html";
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
		
		/* sample queries and conclusion
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
				//System.out.println("document = " + doc);
				printToFile(doc, FILENAME);
			}
			
			/*
			totalResult = doc.select(".numTotalResults").text();
			if (pageNumber.length() == 0) {
				totalResult = totalResult.substring(totalResult.indexOf("of") + 3);
			} // end of if
			
			Elements resultContainer = doc.select("div[class^=gridItemBtm]");
			if (DEBUG)
				printToFile(resultContainer, FILENAME);
			
			Iterator<Element> it = resultContainer.iterator();
			Element gridItem;
			
			while (it.hasNext()) {
					
				// get the details of each product
				Item item = new Item();
				gridItem = it.next();
				
				// populate the product bean object with the extracted data
				populateItem(item, gridItem);
				
				// add the product to the result
				result.addItem(item);
				//itemList.add(item);
				
			} // end of while
            */
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
	
	private void populateItem(Item it, Element gridItem) {
		
		Element moreInfo;
		Elements link, nameSpan, priceSpan, shipping, vendorElem;			
		List<String> vendorList = new ArrayList<String>();
		int cnt = 0;
		String href = "";
		String vendor = "";
		
		if (gridItem != null || 
				gridItem.text() != "" ||
				gridItem.text().length() != 0) {
	
			cnt++;
			vendorList.clear();
			
			//	get the product info from the DOM
			link = gridItem.getElementsByAttributeValueStarting("class", "productName");
			nameSpan = gridItem.getElementsByAttributeValueStarting("id", "nameQA");
			priceSpan = gridItem.getElementsByAttributeValueStarting("class", "productPrice");
			shipping = gridItem.getElementsByAttributeValueStarting("class", "taxShippingArea");					
			if (shipping.text() == "" || shipping.text().length() == 0) {
				shipping = gridItem.getElementsByAttributeValueStarting("class", "freeShip");
			}
			
			href = link.attr("href").toString();
			if (!href.startsWith("javascript")) {				
				//	valid href
				vendorElem = gridItem.getElementsByAttributeValueStarting("class", "buyAtTxt");
				vendor = vendorElem.text();
				
				/*
				// this is not part of the assignment though. Option for enhancement
				// we navigate to next page to get the vendor list
				Document storeDoc = Jsoup.connect(HOST + href.substring(1)).post();
				if (DEBUG)
					printToFile(storeDoc, FILENAME2);
				*/
			} else {
				// if no href and we have javascript, then we can get vendor detail
				// in the same page
				moreInfo = gridItem.nextElementSibling();
				vendorElem = moreInfo.getElementsByAttributeValueStarting("id", "DCTmerchLogo");
				vendor = vendorElem.attr("title");						
			}
			vendorList.add(vendor);
			
			// update the product and product list with above info
			//if (link != null) { 
				//p.setName(link.attr("title"));
			if (nameSpan != null) {
				it.setName(nameSpan.text());
			}
			if (priceSpan != null) {
				it.setPrice(priceSpan.text());
			}
			if (shipping != null) {
				if (shipping.text() == "" || shipping.text().length() == 0) {
					it.setShippingPrice(NO_SHIPPING_INFO);						
				} else {
					it.setShippingPrice(shipping.text());
				}
			}
			if (vendor.length() != 0 || !vendor.isEmpty()) {
				it.setVendors(vendorList);
			}						
		} // end of if		
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