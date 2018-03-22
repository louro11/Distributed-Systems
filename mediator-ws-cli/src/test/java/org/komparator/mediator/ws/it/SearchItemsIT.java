package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.*;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

/**
 * Test suite
 */
public class SearchItemsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	
	@BeforeClass
	public static void oneTimeSetUp() throws InvalidText_Exception, SupplierClientException, BadProductId_Exception, BadProduct_Exception {
		mediatorClient.clear();
		
		SupplierClient sc1 = new SupplierClient(testProps.getProperty("uddi.url"), "A67_Supplier1");
		SupplierClient sc2 = new SupplierClient(testProps.getProperty("uddi.url"), "A67_Supplier2");
		
		{	
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Baskteball items");
			product.setPrice(20);
			product.setQuantity(10);
			sc1.createProduct(product);
		
		}
		{	
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			sc1.createProduct(product);
			
		}
		{	
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball items");
			product.setPrice(20);
			product.setQuantity(20);
			sc2.createProduct(product);
			
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(15);
			product.setQuantity(30);
			sc2.createProduct(product);
			
			
		}
		{
			
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(20);
			product.setQuantity(30);
			sc1.createProduct(product);
			
			
		}
		{
			
			ProductView product = new ProductView();
			product.setId("A3");
			product.setDesc("Soccer ball");
			product.setPrice(20);
			product.setQuantity(30);
			sc1.createProduct(product);
		}
		{
			
			ProductView product = new ProductView();
			product.setId("A4");
			product.setDesc("Swimming items");
			product.setPrice(10);
			product.setQuantity(30);
			sc1.createProduct(product);
		}
		{
			
			ProductView product = new ProductView();
			product.setId("A4");
			product.setDesc("Swimming items");
			product.setPrice(20);
			product.setQuantity(30);
			sc2.createProduct(product);
		}
		{
			
			ProductView product = new ProductView();
			product.setId("A5");
			product.setDesc("Ballet slippers");
			product.setPrice(20);
			product.setQuantity(30);
			sc2.createProduct(product);
		}
		{
			
			ProductView product = new ProductView();
			product.setId("B6");
			product.setDesc("Ballet slippers");
			product.setPrice(20);
			product.setQuantity(30);
			sc2.createProduct(product);
		}
		
		
	}

	@AfterClass
	public static void oneTimeTearDown() {
		mediatorClient.clear();
	}
	
	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}


	// public List<ItemView> searchItems(String descText) throws InvalidText_Exception

	// bad input tests
	@Test(expected = InvalidText_Exception.class)
	public void nullTextTest() throws InvalidText_Exception{
		mediatorClient.searchItems(null);
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void whitespaceTextText() throws InvalidText_Exception{
		mediatorClient.searchItems(" ");
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void emptyTextTest() throws InvalidText_Exception{
		mediatorClient.searchItems("");
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void newlineTextTest() throws InvalidText_Exception{
		mediatorClient.searchItems("\n");
	}
	
	@Test(expected = InvalidText_Exception.class)
	public void tabTextTest() throws InvalidText_Exception{
		mediatorClient.searchItems("\t");
	}
	
	// main tests
	
	//test when there is one item found with the same exact description
	@Test
	public void searchExistingItemDescriptionTest() throws InvalidText_Exception{
		List<ItemView> items = mediatorClient.searchItems("Baskteball items");
		
		assertEquals(1, items.size());
		
		//do asserts for the item view
		
	}
	
	//test when there is one item found and the description given is contained in the products description
	@Test
	public void searchExistingItemDescriptionContainedTest() throws InvalidText_Exception{
		List<ItemView> items = mediatorClient.searchItems("Baskteball");
		
		assertEquals(1, items.size());
		
		//do asserts for the item view
		
	}
	
	
	//test when 2 items are found with the same productID and the same price
	@Test
	public void searchExistingItemsDescriptionTest() throws InvalidText_Exception{
		List<ItemView> items = mediatorClient.searchItems("Baseball");
		
		assertEquals(2, items.size());
	
	}
	
	//test when 2 items are found with the same productID and different prices
	@Test
	public void searchExistingItemsDescriptionDifferentPricesTest() throws InvalidText_Exception{
		List<ItemView> items = mediatorClient.searchItems("Swimming items");
		
		assertEquals(2, items.size());
		
		assertEquals(10, items.get(0).getPrice());
		assertEquals(20, items.get(1).getPrice());
		
	}
	
	//test when 2 items are found with different productIDs and with the same price
	@Test
	public void searchExistingItemsDescriptionDifferentIDTest() throws InvalidText_Exception{
		List<ItemView> items = mediatorClient.searchItems("Ballet slippers");
		
		assertEquals(2, items.size());
		
		assertEquals("A5", items.get(0).getItemId().getProductId());
		assertEquals("B6", items.get(1).getItemId().getProductId());
		
	}
	
	//test when 3 items are found, 2 with different productIDs and 2 with different prices
	@Test
	public void searchExistingItemsDescriptionDifferentEverythingTest() throws InvalidText_Exception{
		List<ItemView> items = mediatorClient.searchItems("Soccer ball");
		
		assertEquals(3, items.size());
		
		assertEquals("A3", items.get(0).getItemId().getProductId());
		assertEquals(20, items.get(0).getPrice());
		
		assertEquals("Z3", items.get(1).getItemId().getProductId());
		assertEquals(15, items.get(1).getPrice());
		
		assertEquals("Z3", items.get(2).getItemId().getProductId());
		assertEquals(20, items.get(2).getPrice());
		
	}
	
	
	//test when there is no item with such a description found
	@Test
	public void searchInexistentItemDescriptionTest() throws InvalidText_Exception{
		List<ItemView> items = mediatorClient.searchItems("Volleyball");
		
		//is this method like searchProducts?
		assertNotNull(items);
		assertEquals(0, items.size());
	}
	
	/*@Test 
	public void searchItemsLowercaseNotExistsTest() throws InvalidText_Exception {
		List<ItemView> items = mediatorClient.searchItems("baseball"); does this even make sense
		assertNull(items);
	}*/

}