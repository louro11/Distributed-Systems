package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialisation and clean-up
	
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		client.clear();
		
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Basketball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		
	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}
	
	// Initialisation and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}


	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	// bad input tests
	@Test(expected = BadText_Exception.class)
	public void nullTextTest() throws BadText_Exception{
		client.searchProducts(null);
	}
	
	@Test(expected = BadText_Exception.class)
	public void whitespaceTextText() throws BadText_Exception{
		client.searchProducts(" ");
	}
	
	@Test(expected = BadText_Exception.class)
	public void emptyTextTest() throws BadText_Exception{
		client.searchProducts("");
	}
	
	@Test(expected = BadText_Exception.class)
	public void newlineTextTest() throws BadText_Exception{
		client.searchProducts("\n");
	}
	
	@Test(expected = BadText_Exception.class)
	public void tabTextTest() throws BadText_Exception{
		client.searchProducts("\t");
	}
	
	// main tests
	@Test
	public void searchExistingProductDescriptionTest() throws BadText_Exception{
		List<ProductView> products = client.searchProducts("Basketball");
		assertEquals(2, products.size());
		assertTrue(!products.isEmpty());
	}
	
	@Test
	public void searchAnotherExistingProductDescriptionTest() throws BadText_Exception{
		List<ProductView> products = client.searchProducts("Baseball");
		assertEquals(1, products.size());
		assertTrue(!products.isEmpty());
		
		ProductView product = products.get(0);
		assertEquals("Z2", product.getId());
		assertEquals(20, product.getPrice());
		assertEquals(20, product.getQuantity());
		assertEquals("Baseball", product.getDesc());
	}
	

	@Test
	public void searchInexistentProductDescriptionTest() throws BadText_Exception{
		List<ProductView> products = client.searchProducts("Soccer ball");
		assertNotNull(products);
		assertEquals(0, products.size());
	}

}
