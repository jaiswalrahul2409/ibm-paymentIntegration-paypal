package com.ibm.spring.paypal.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;

@Controller
public class PaypalController {

	@Autowired
	PaypalService service;

	public static final String SUCCESS_URL = "pay/success";
	public static final String CANCEL_URL = "pay/cancel";

	@GetMapping("/")
	public String home() {
		return "home";
	}

	
	
	  @PostMapping("/pay") public String payment(@ModelAttribute("order") Order
	  order) {
	 
	 
	
	
	/*
	 * @RequestMapping(method=RequestMethod.POST, value="/pay") public String
	 * payment(@RequestBody Order order) {
	 */
	 
		try {
			Payment payment = service.createPayment(order.getPrice(), order.getCurrency(), order.getMethod(),
					order.getIntent(), order.getDescription(), "http://localhost:8080/" + CANCEL_URL,
					"http://localhost:8080/" + SUCCESS_URL);
			for(Links link:payment.getLinks()) {
				if(link.getRel().equals("approval_url")) {
					return "redirect:"+link.getHref();
				}
			}
			
		} catch (PayPalRESTException e) {
		
			e.printStackTrace();
		}
		return "redirect:/";
	}
	
	 @GetMapping(value = CANCEL_URL)
	    public String cancelPay() {
	        return "cancel";
	    }

		/*
		 * @GetMapping(value = SUCCESS_URL) public String
		 * successPay(@RequestParam("paymentId") String
		 * paymentId, @RequestParam("PayerID") String payerId) { try { Payment payment =
		 * service.executePayment(paymentId, payerId);
		 * System.out.println(payment.toJSON()); if
		 * (payment.getState().equals("approved")) { return "success"; } } catch
		 * (PayPalRESTException e) { System.out.println(e.getMessage()); } return
		 * "redirect:/"; }
		 */
	 
	 @RequestMapping(value = SUCCESS_URL)
	    public ModelAndView successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
	     ModelAndView model = new ModelAndView("success");  
		 try {
	            Payment payment = service.executePayment(paymentId, payerId);
	            PayerInfo payerInfo = payment.getPayer().getPayerInfo();
	            Transaction transaction = payment.getTransactions().get(0);
	            
	           
	            System.out.println(payment.toJSON());
	            if (payment.getState().equals("approved")) {
	            	model.addObject("transactionId",payment.getId());
	            	model.addObject("time",payment.getCreateTime());
	            	model.addObject("payer",payerInfo.getFirstName());
	            	model.addObject("total",transaction.getAmount().getTotal());
	            
	                return model;
	            }
	        } catch (PayPalRESTException e) {
	         System.out.println(e.getMessage());
	        }
	        return model;
	    }

}
