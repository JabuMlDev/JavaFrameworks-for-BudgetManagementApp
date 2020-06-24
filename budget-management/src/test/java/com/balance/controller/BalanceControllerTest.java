package com.balance.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.balance.controller.BalanceController;
import com.balance.model.Client;
import com.balance.model.Invoice;
import com.balance.service.ClientService;
import com.balance.service.InvoiceService;
import com.balance.view.BalanceView;


public class BalanceControllerTest {
	@Mock
	private ClientService clientService;
	
	@Mock
	private InvoiceService invoiceService;
	
	@Mock
	private BalanceView balanceView;
	
	@InjectMocks
	private BalanceController balanceController;
	
	private static final int CURRENT_YEAR=Calendar.getInstance().get(Calendar.YEAR);
	private static final int YEAR_FIXTURE=2019;
	private static final double TOTAL_REVENUE_FIXTURE=50.6;
	private static final Client CLIENT_FIXTURE=new Client("test identifier");
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test @GUITest
	public void testInitializeView() {
		List<Client> clients = Arrays.asList(new Client());
		when(clientService.findAllClients()).thenReturn(clients);
		List<Integer> yearsOfTheinvoices=Arrays.asList(CURRENT_YEAR);
		when(invoiceService.findYearsOfTheInvoices()).thenReturn(yearsOfTheinvoices);
		balanceController.initializeView();
		verify(balanceView).showClients(clients);
		verify(balanceView).setChoiceYearInvoices(yearsOfTheinvoices);
		verify(balanceView).setYearSelected(CURRENT_YEAR);
	}
	
	@Test
	public void testAllClients() {
		List<Client> clients = Arrays.asList(new Client());
		when(clientService.findAllClients()).thenReturn(clients);
		balanceController.allClients();
		verify(balanceView).showClients(clients);
	}
	
	@Test
	public void testAllInvoicesByYear() {
		List<Invoice> invoices = Arrays.asList(new Invoice());
		when(invoiceService.findAllInvoicesByYear(YEAR_FIXTURE)).thenReturn(invoices);
		balanceController.allInvoicesByYear(YEAR_FIXTURE);
		verify(balanceView).showInvoices(invoices);
	}
	
	@Test
	public void testAnnualRevenue() {
		when(invoiceService.getTotalRevenueOfAnYear(YEAR_FIXTURE))
			.thenReturn(TOTAL_REVENUE_FIXTURE);
		balanceController.annualRevenue(YEAR_FIXTURE);
		verify(balanceView).setAnnualTotalRevenue(YEAR_FIXTURE,TOTAL_REVENUE_FIXTURE);
	}
	
	@Test
	public void testYearsOfTheInvoices() {
		List<Integer> yearsOfTheinvoices=Arrays.asList(YEAR_FIXTURE);
		when(invoiceService.findYearsOfTheInvoices()).thenReturn(yearsOfTheinvoices);
		balanceController.yearsOfTheInvoices();
		verify(balanceView).setChoiceYearInvoices(yearsOfTheinvoices);
	}
	
	@Test
	public void testInvoicesByClientAndYear() {
		List<Invoice> invoiceofClientAndYear = Arrays.asList(new Invoice());
		when(invoiceService.findInvoicesByClientAndYear(CLIENT_FIXTURE,YEAR_FIXTURE))
			.thenReturn(invoiceofClientAndYear);
		balanceController.allInvoicesByClientAndYear(CLIENT_FIXTURE, YEAR_FIXTURE);
		verify(balanceView).showInvoices(invoiceofClientAndYear);
	}
	
	@Test
	public void testAnnualClientRevenue() {
		when(invoiceService.getAnnualClientRevenue(CLIENT_FIXTURE, YEAR_FIXTURE))
			.thenReturn(TOTAL_REVENUE_FIXTURE);
		balanceController.annualClientRevenue(CLIENT_FIXTURE, YEAR_FIXTURE);
		verify(balanceView).setAnnualClientRevenue(
				CLIENT_FIXTURE, YEAR_FIXTURE, TOTAL_REVENUE_FIXTURE);
	}
	
	
}
