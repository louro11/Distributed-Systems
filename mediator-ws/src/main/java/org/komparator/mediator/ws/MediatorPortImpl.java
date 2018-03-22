package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import pt.ulisboa.tecnico.sdis.ws.cli.*;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;


@WebService(
	endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
	wsdlLocation = "mediator.1_0.wsdl", 
	name = "MediatorWebService", 
	portName = "MediatorPort", 
	targetNamespace = "http://ws.mediator.komparator.org/", 
	serviceName = "MediatorService"
)

@HandlerChain(file = "/mediator-ws_handler-chain.xml")

public class MediatorPortImpl implements MediatorPortType{

	/** Map of purchases. Also uses concurrent hash table implementation. */
	private ConcurrentMap<String, CartView> carts = new ConcurrentHashMap<>();
	private List<ShoppingResultView> history = new ArrayList<ShoppingResultView>();
	private Date timestamp;
	
	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	
	// Main operations -------------------------------------------------------

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		// check item id
		if (productId == null)
			throwInvalidItemId("Item identifier cannot be null!");
		productId = productId.trim();
		if (productId.length() == 0)
			throwInvalidItemId("Item identifier cannot be empty or whitespace!");

		Collection<String> urls;
		SupplierClient client = null;
		
		List<ItemView> itemvs = new ArrayList<ItemView>();
		ProductView prod = new ProductView();

	 	String uddiURL = endpointManager.getUddiURL();
	 	UDDINaming uddiNaming = null;

		try {
			uddiNaming = new UDDINaming(uddiURL);
			urls = uddiNaming.list("A67_Supplier%");
			String supplierID="";
			for(String url: urls) {
				client = new SupplierClient(url);
				supplierID = urlToSupplierID(url);
				prod = client.getProduct(productId);
				if(prod != null)
					itemvs.add(newItemView(prod, supplierID)); //AQUI ESTAVA URL
		 	}
		
			Collections.sort(itemvs, new Comparator<ItemView>() {
		        public int compare(ItemView iv1, ItemView iv2) {
		        	//compareTo returns a int, <0 if it comes first 
		        	int val = 0;
	        		if(iv1.getPrice()<iv2.getPrice()){
	        			val = -1;
	        		}else if(iv1.getPrice()<iv2.getPrice()){
	        			val = 1;
	        		}else{
	        			val = iv1.getItemId().getSupplierId().compareTo(iv2.getItemId().getSupplierId());
	        		}
		        	return val;
		        }
			});	
			
		} catch (UDDINamingException e) {
			e.printStackTrace();
		} catch (SupplierClientException e) {
			System.out.print("Could not contact supplier");
			e.printStackTrace();
		} catch (BadProductId_Exception e) {
			throwInvalidItemId(e.getMessage());
		}
		
		return itemvs;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		// check description text
		if (descText == null)
			throwInvalidText("Item description cannot be null!");
		descText = descText.trim();
		if (descText.length() == 0)
			throwInvalidText("Item description cannot be empty or whitespace!");
		
		List<ProductView> products = new ArrayList<ProductView>();
		ItemView item = new ItemView();
		List<ItemView> ivList = new ArrayList<ItemView>();
		Collection<String> urls;
		SupplierClient client = null;
		
	 	String uddiURL = endpointManager.getUddiURL();
	 	UDDINaming uddiNaming;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			urls = uddiNaming.list("A67_Supplier%");
			String supplierID="";
			for(String url: urls){
				client = new SupplierClient(url);
				supplierID = urlToSupplierID(url);
				products = client.searchProducts(descText);
				for(ProductView prod : products){
					item = newItemView(prod, supplierID); //changes here
					ivList.add(item);
				}
		 	}
	
		} catch (UDDINamingException e) {
			e.printStackTrace();
		} catch (SupplierClientException e) {
			System.out.print("Could not contact supplier at" + client.getWsURL());
		} catch (BadText_Exception e) {
			throwInvalidText(e.getMessage());
		}
		
		Collections.sort(ivList, new Comparator<ItemView>() {

	        public int compare(ItemView iv1, ItemView iv2) {
	        	//compareTo returns a int, <0 if it comes first 
	        	int val = iv1.getItemId().getProductId().compareTo(iv2.getItemId().getProductId());
	        	if(val==0){
	        		if(iv1.getPrice()<iv2.getPrice()){
	        			val = -1;
	        		}else{
	        			val = 1;
	        		}
	        	}
	        	
	        	return val;
	        }
	    });	
		return ivList;
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// check cart id
		if (cartId == null)
			throwInvalidCartId("Cart identifier cannot be null!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
		// check item id view
		if (itemId == null)
			throwInvalidItemId("Item identifier view cannot be null!");
		if (itemId.getSupplierId() == null)
			throwInvalidItemId("Supplier identifier cannot be null!");
		String supId = itemId.getSupplierId().trim();
		if (supId.length() == 0)
			throwInvalidItemId("Suppier identifier cannot be empty or whitespace!");
		
		SupplierClient client = null;
		ProductView product = null;
		
		String uddiURL = endpointManager.getUddiURL();
	 	UDDINaming uddiNaming;
	 	String supplierURL="";
		
		try {
			uddiNaming = new UDDINaming(uddiURL);
			supplierURL = uddiNaming.lookup(itemId.getSupplierId());
			client = new SupplierClient(supplierURL);
			product = client.getProduct(itemId.getProductId());
			
			// The itemId of inexisting item
			if(product == null)
				throwInvalidItemId("Item identifier of inexisting Product");
			
			// check quantity
			if (itemQty <= 0)
				throwInvalidQuantity("Quantity must be a positive number!");
			
			// The demanded quantity must be confirmed with the suppliers available quantity
			if(product.getQuantity() < itemQty)
				throwNotEnoughItems("Not enough items available!");
			
			CartView cart = new CartView();
			
			// If Cart with cartId does not exist, one must be created
			if(!carts.containsKey(cartId)) {
				cart.setCartId(cartId);
				addItemToCart(cart, itemId.getSupplierId(), product, itemQty);
				carts.put(cartId, cart);
				updateSecondaryCarts();
			// Cart already exists
			} else {
				cart = carts.get(cartId);
				List<CartItemView> cartItemList = new ArrayList<CartItemView>();
				cartItemList = cart.getItems();
				// If Cart already has that item, only the quantity of that item must be added
				for( CartItemView cartItem: cartItemList ) {
					if(cartItem.getItem().getItemId().equals(itemId)) {
						System.out.println("Has ITEM!!!!");
						if(product.getQuantity() < itemQty + cartItem.getQuantity()){
							throwNotEnoughItems("Not enough items available!");
						}
						
						cartItem.setQuantity(itemQty + cartItem.getQuantity());
						updateSecondaryCarts();
						return;
					}
				}
				// Cart doesn't have that item
				addItemToCart(cart, itemId.getSupplierId(), product, itemQty);
				updateSecondaryCarts();
			}
		} catch (SupplierClientException e) {
			System.out.print("Could not contact supplier");
		} catch (BadProductId_Exception e) {
			throwInvalidItemId(e.getMessage());
		} catch (UDDINamingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr)
			throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		// check cart id
		if (cartId == null)
			throwInvalidCartId("Cart identifier cannot be null!");
		cartId = cartId.trim();
		if (cartId.length() == 0)
			throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
		if(!carts.containsKey(cartId))
			throwInvalidCartId("Cart identifier doesn't belong to any available Cart!");
		CartView cart = carts.get(cartId);
		if(cart.getItems().size() == 0)
			throwEmptyCart("Cart identifier belongs to an Empty Cart!");
			
		//check credit card number
		if (creditCardNr == null)
			throwInvalidCreditCard("Credit card number cannot be null!");
		creditCardNr = creditCardNr.trim();
		if (creditCardNr.length() == 0)
			throwInvalidCreditCard("Cart identifier cannot be empty or whitespace!");
		if(creditCardNr.equals("	"))
			throwInvalidCreditCard("Credit card number cannot be empty tab!");
		if(creditCardNr.equals("\n"))
			throwInvalidCreditCard("Credit card number cannot be newline!");
		
		try {
	 		String wsURL = endpointManager.getUddiNaming().lookup("CreditCard");
			CreditCardClient creditcard = new CreditCardClient(wsURL);
			if(!creditcard.validateNumber(creditCardNr))
				throwInvalidCreditCard("Invalid credit card number!");
		} catch(CreditCardClientException e) {
			System.out.print("Unabled to contact cc-ws!");
		} catch (UDDINamingException e) {
			e.printStackTrace();
		}
		
		ShoppingResultView result = new ShoppingResultView();
		result.setTotalPrice(0);
		result.setId("begin");
		SupplierClient client = null;
		
		for (CartItemView cartItem: cart.getItems()){
			boolean drop = false;
			try {
				synchronized(this){
					client = new SupplierClient(cartItem.getItem().getItemId().getSupplierId());
					result.setId(result.getId() + client.buyProduct(cartItem.getItem().getItemId().getProductId(), cartItem.getQuantity()));
					if(!drop){
						result.getPurchasedItems().add(cartItem);
						result.setTotalPrice(result.getTotalPrice() + cartItem.getQuantity());
					}
				}
				
			} catch (Exception e) {
				result.getDroppedItems().add(cartItem);
				result.setId(result.getId() + "drop");
				drop = true;
				continue;
			}
			
		}

		result.setResult(Result.PARTIAL);
		
		if (result.getDroppedItems().size() == 0) {
			result.setResult(Result.COMPLETE);
		} else if (result.getPurchasedItems().size() == 0)
			result.setResult(Result.EMPTY);
		
		history.add(result);
		updateSecondaryHistory();
		return result;
	}
	
	
	// Auxiliary operations --------------------------------------------------	
	
	@Override
	public String ping(String arg0) {
		System.out.println("Receiving ping request");
		StringBuilder builder = new StringBuilder();
		Collection<String> urls;
		SupplierClient client = null;
		/*Consultar o UDDI para pesquisar fornecedores (suppliers) */
		
	 	String uddiURL = endpointManager.getUddiURL();
	 	UDDINaming uddiNaming;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			urls = uddiNaming.list("A67_Supplier%");
			for(String url: urls){
				System.out.println("Pinging supplier at " + url);
				client = new SupplierClient(url);
				builder.append(client.ping(arg0)).append("\n");
		 	}
			
		} catch (UDDINamingException e) {
			e.printStackTrace();
		} catch (SupplierClientException e) {
			System.out.print("Could not contact supplier at" + client.getWsURL());
		}
		return builder.toString();
	}

	@Override
	public void clear() {
		Collection<String> urls;
		SupplierClient client = null;
	 	String uddiURL = endpointManager.getUddiURL();
	 	UDDINaming uddiNaming;
	 	
		try {
			uddiNaming = new UDDINaming(uddiURL);
			urls = uddiNaming.list("A67_Supplier%");
			for(String url: urls){
				client = new SupplierClient(url);
				client.clear();
		 	}
			carts.clear();
			history.clear();
			
		} catch (UDDINamingException e) {
			e.printStackTrace();
		} catch (SupplierClientException e) {
			System.out.print("Could not contact supplier");
		}	
	}

	@Override
	public List<CartView> listCarts() {
		List<CartView> cartsList = new ArrayList<CartView>();
		for (String key : carts.keySet()){
			cartsList.add(carts.get(key));
		}
		return cartsList;
	}
	
	@Override
	public List<ShoppingResultView> shopHistory() {
		List<ShoppingResultView> clone = new ArrayList<ShoppingResultView>(history);
		Collections.reverse(clone);
		
		return clone;
	}
	
	private void addItemToCart(CartView cart, String supplierId, ProductView product, int qty) {
		CartItemView cartItem = new CartItemView();
		cartItem.setItem(newItemView(product, supplierId));
		cartItem.setQuantity(qty);
		
		cart.getItems().add(cartItem);
	}
	
	
	private String urlToSupplierID(String url){
		String aux = url.replaceFirst("^http://localhost:808", "");
		aux = aux.substring(0, 1);
		String supplierID = "A67_Supplier" + aux;
		
		return supplierID;
	}
	
	private void updateSecondaryCarts() {
		MediatorClient sec_client;
		try {
			sec_client = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
			sec_client.updateCart();
		} catch (MediatorClientException e) {
			System.out.println("Error while creating a Secondary Mediator Client for update");
		}
	}
	
	private void updateSecondaryHistory() {
		MediatorClient sec_client;
		try {
			sec_client = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
			sec_client.updateShopHistory();
		} catch (MediatorClientException e) {
			System.out.println("Error while creating a Secondary Mediator Client for update");
		}
	}
	

	
	// View helpers -----------------------------------------------------
	
	private ItemView newItemView(ProductView product, String supplierId) {
		ItemIdView itemId = new ItemIdView();
		itemId.setProductId(product.getId());
		itemId.setSupplierId(supplierId);

		ItemView item = new ItemView();
		item.setItemId(itemId);
		item.setDesc(product.getDesc());
		item.setPrice(product.getPrice());

		return item;
	}

	
	// Exception helpers -----------------------------------------------------

	/** Helper method to throw new EmptyCart exception */
	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}
	
	/** Helper method to throw new InvalidCartId exception */
	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}
	
	/** Helper method to throw new InvalidCreditCard exception */
	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}
	
	/** Helper method to throw new InvalidItemId exception */
	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}
	
	/** Helper method to throw new InvalidQuantity exception */
	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.message = message;
		throw new InvalidQuantity_Exception(message, faultInfo);
	}
	
	/** Helper method to throw new InvalidText exception */
	private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}
	
	/** Helper method to throw new NotEnoughItems exception */
	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.message = message;
		throw new NotEnoughItems_Exception(message, faultInfo);
	}


	@Override
	public void imAlive() {
		String wsid = endpointManager.getWsURL();
		wsid = wsid.replaceFirst("^http://localhost:807", "");
		wsid = wsid.substring(0, 1);
		if(wsid.equals("1")){
			System.out.println("[Primary mediator] imAlive");
			return;
		}else{
			timestamp = new Date();
			System.out.println("[Backup mediator] primary mediator is alive at" + timestamp.toString());
		}
	}


	public Date getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	@Override
	public void updateShopHistory() {
		MediatorClient prim_client;
		try {
			prim_client = new MediatorClient("http://localhost:8071/mediator-ws/endpoint");
			history = prim_client.shopHistory();
			System.out.println("\n\n[Updated shop history]\n\n");
		} catch (MediatorClientException e) {
			System.out.println("Error while creating a Primary Mediator Client for secondary shop history update");
		}
	}


	@Override
	public void updateCart() {
		MediatorClient prim_client;
		try {
			prim_client = new MediatorClient("http://localhost:8071/mediator-ws/endpoint");
			carts.clear();
			for(CartView cart : prim_client.listCarts()){
				carts.put(cart.getCartId(), cart);
			}
			System.out.println("\n\n[Updated Cart List]\n\n");
		} catch (MediatorClientException e) {
			System.out.println("Error while creating a Primary Mediator Client for secondary cart update");
		}
	}	

}
