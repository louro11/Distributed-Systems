package org.komparator.mediator.ws.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.komparator.mediator.ws.*;

public class MediatorClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

        // Create client
        MediatorClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new MediatorClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new MediatorClient(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

        System.out.println("Invoke ping()...");
        String result = client.ping("client");
        client.imAlive();
        System.out.println(result);

        // remote invocations by demand
        // run "mvn install -DskipTests" in console to skip tests

        System.out.println("Awaiting instructiuons");

        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final String prompt = ">>> ";
        final String helperMsg = "Enter an option [clear, ping, searchItems, getItems, listCarts, addToCart, buyCart, shopHistory or exit]";

        System.out.println(helperMsg);
        System.out.print(prompt);

        String input;
        while (!(input = in.readLine()).equals("exit")) {
            try {
                if (input.equals("clear")) {
                    client.clear();
                    System.out.println("Cleared previous state.");
                    
                } else if (input.equals("ping")) {
                    System.out.println(client.ping("Client"));
                
                } else if (input.equals("searchItems")) {
                    System.out.println("Enter the description: ");
                    System.out.print(prompt);
                    input = in.readLine();
                    System.out.println("Items list: ");
                    client.searchItems(input).stream().forEach(MediatorClientApp::printItemView);
                
                } else if (input.equals("getItems")) {
                    System.out.println("Enter the product Id: ");
                    System.out.print(prompt);
                    input = in.readLine();
                    System.out.println("Items list: ");
                    client.getItems(input).stream().forEach(MediatorClientApp::printItemView);
                    
                } else if (input.equals("listCarts")) {
                    System.out.println("Carts list: ");
                    client.listCarts().stream().forEach(MediatorClientApp::printCartView);
                
                } else if (input.equals("addToCart")) {
                    System.out.println("Enter the Cart Id: ");
                    System.out.print(prompt);
                    final String cartId = in.readLine();
                    System.out.println("Enter the product Id: ");
                    System.out.print(prompt);
                    final String prodId = in.readLine();
                    System.out.println("Enter the suppiler Id: ");
                    System.out.print(prompt);
                    final String supId = in.readLine();
                    System.out.println("Enter the quantity: ");
                    System.out.print(prompt);
                    final String itemQty = in.readLine();
                    ItemIdView itemId = new ItemIdView();
                    itemId.setProductId(prodId);
                    itemId.setSupplierId(supId);
                    client.addToCart(cartId, itemId, Integer.parseInt(itemQty));;
                    System.out.println("Item added to Cart.");
                    
                } else if (input.equals("buyCart")) {
                    System.out.println("Enter the Cart Id: ");
                    System.out.print(prompt);
                    final String cartId = in.readLine();
                    System.out.println("Enter the Credit Card Number: ");
                    System.out.print(prompt);
                    final String cardNumber = in.readLine();
                    client.buyCart(cartId, cardNumber);
                    System.out.println("Cart successfully bought.");
                    
                } else if (input.equals("shopHistory")) {
                    System.out.println("Shooping History: ");
                    client.shopHistory().stream().forEach(MediatorClientApp::printShopView);
                    
                } else {
                    if (input != "")
                        System.out.println("Unknown option.");
                }
                System.out.println(helperMsg);
                System.out.print(prompt);
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        
    }

	private static void printItemView(ItemView item) {
		System.out.printf("ProductId: %s; SupplierId: %s; Price: %s; Description: %s%n",
                item.getItemId().getProductId(),
                item.getItemId().getSupplierId(),
                item.getPrice(),
                item.getDesc());
	}
	private static void printCartView(CartView cart) {
		System.out.printf("CartId: %s; Item List:%n",
                cart.getCartId().toString());
		for(CartItemView c : cart.getItems()) {
			System.out.printf("Quantity: %s;", c.getQuantity());
			printItemView(c.getItem());
		}
	}
	private static void printShopView(ShoppingResultView result) {
		System.out.printf("ShoopingResultId: %s; Total Price: %s; Result: %s%n",
                result.getId(), result.getTotalPrice(), result.getResult().toString());
	}
}

