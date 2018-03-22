package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		client.clear();
		
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
	}


	@After
	public void tearDown() {
		client.clear();
	}

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests
	@Test(expected = BadProductId_Exception.class)
	public void nullProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null, 1);	
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void emptyProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("", 1);
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void whitespaceProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct(" ", 1);
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void newlineProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("\n", 1);	
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void tabProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("\t", 1);
	}
	
	// main tests
	@Test
	public void equalQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		String pid = client.buyProduct("X1", 10);
		ProductView product = client.getProduct("X1");
		assertEquals(0, product.getQuantity());
		assertNotNull(pid);
	}
	
	@Test
	public void lessRequestedQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		String pid = client.buyProduct("X1", 1);
		ProductView product = client.getProduct("X1");
		assertEquals(9, product.getQuantity());
		assertNotNull(pid);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void moreRequestedQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		 client.buyProduct("X1", 11);
		
	}
	
	@Test(expected = BadQuantity_Exception.class)
	public void noQuantityRequestedTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("X1", 0);
	}
	
	@Test(expected = BadQuantity_Exception.class)
	public void nonsensicalQuantityRequestedTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("X1", -1);
		
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void noSuchProductAvailableTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		client.buyProduct("X2", 1);
	}
		
}