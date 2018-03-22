package org.komparator.mediator.ws.it;


import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//import org.komparator.mediator.ws.ItemIdView;

import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.it.BaseIT;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class GetItemsIT extends BaseIT {
	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws InvalidItemId_Exception, SupplierClientException, BadProductId_Exception, BadProduct_Exception {
	// clear remote service state before all tests
		mediatorClient.clear();
		
		SupplierClient sc1 = new SupplierClient(testProps.getProperty("uddi.url"), "A67_Supplier1");
		SupplierClient sc2 = new SupplierClient(testProps.getProperty("uddi.url"), "A67_Supplier2");

	// fill-in test products
	// (since getItems is read-only the initialization below
	// can be done once for all tests in this suite)
		{	
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
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
			product.setDesc("Baseball");
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
			sc1.createProduct(product);
			
		}
		{
	
		
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(20);
			product.setQuantity(30);
			sc2.createProduct(product);
		}
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// clear remote service state after all tests
		mediatorClient.clear();
	}
	
	// members

		// initialization and clean-up for each test
		@Before
		public void setUp() {
		}

		@After
		public void tearDown() {
		}
	
	// public List<ItemView> getItems(String productId) throws InvalidItemId_Exception
		
	// bad input tests

		@Test(expected = InvalidItemId_Exception.class)
		public void getItemsNullTest() throws InvalidItemId_Exception {
			mediatorClient.getItems(null);
		}

		@Test(expected = InvalidItemId_Exception.class)
		public void getItemsEmptyTest() throws InvalidItemId_Exception {
			mediatorClient.getItems("");
		}

		@Test(expected = InvalidItemId_Exception.class)
		public void getItemsWhitespaceTest() throws InvalidItemId_Exception {
			mediatorClient.getItems(" ");
		}

		@Test(expected = InvalidItemId_Exception.class)
		public void getItemsTabTest() throws InvalidItemId_Exception {
			mediatorClient.getItems("\t");
		}

		@Test(expected = InvalidItemId_Exception.class)
		public void getItemsNewlineTest() throws InvalidItemId_Exception {
			mediatorClient.getItems("\n");
		}
		
		
		//main tests
		
		@Test
		public void getItemsItemExistsTest() throws InvalidItemId_Exception {
			List<ItemView> item = mediatorClient.getItems("X1");
			
			assertEquals(1, item.size());
			
			assertEquals("X1", item.get(0).getItemId().getProductId());
			assertEquals("A67_Supplier1", item.get(0).getItemId().getSupplierId());
			assertEquals("Basketball", item.get(0).getDesc());
			assertEquals(10, item.get(0).getPrice());
			
		}

		@Test
		public void getItemsTwoItemsWithTheSamePricesExistTest() throws InvalidItemId_Exception {
			List<ItemView> items = mediatorClient.getItems("Y2");
			
			assertEquals(2, items.size());
			
			assertEquals("Y2", items.get(0).getItemId().getProductId());
			assertEquals("A67_Supplier1", items.get(0).getItemId().getSupplierId());
			assertEquals("Baseball", items.get(0).getDesc());
			assertEquals(20, items.get(0).getPrice());
		
			assertEquals("Y2", items.get(1).getItemId().getProductId());
			assertEquals("A67_Supplier2", items.get(1).getItemId().getSupplierId());
			assertEquals("Baseball", items.get(1).getDesc());
			assertEquals(20, items.get(1).getPrice());
			
		}
		
		@Test
		public void getItemsTwoItemsWithDifferentPricesExistTest() throws InvalidItemId_Exception {
			List<ItemView> items = mediatorClient.getItems("Z3");
			
			assertEquals(2, items.size());
			
			assertEquals("Z3", items.get(0).getItemId().getProductId());
			assertEquals("A67_Supplier1", items.get(0).getItemId().getSupplierId());
			assertEquals("Soccer ball", items.get(0).getDesc());
			assertEquals(15, items.get(0).getPrice());
			
			assertEquals("Z3", items.get(1).getItemId().getProductId());
			assertEquals("A67_Supplier2", items.get(1).getItemId().getSupplierId());
			assertEquals("Soccer ball", items.get(1).getDesc());
			assertEquals(20, items.get(1).getPrice());
		}

		
		@Test
		public void getItemsNotExistsTest() throws InvalidItemId_Exception {
			// when product does not exist, null should be returned
			//I have no idea, but it makes sense
			List<ItemView> item = mediatorClient.getItems("A0");
			assertNotNull(item);
			assertEquals(0, item.size());
		}
		
		@Test
		public void getItemsLowerCaseNotExistsTest() throws InvalidItemId_Exception {
			// product identifiers are case sensitive,
			// so "x1" is not the same as "X1"
			List<ItemView> items = mediatorClient.getItems("x1");
			assertNotNull(items);
			assertEquals(0, items.size());
		}

}