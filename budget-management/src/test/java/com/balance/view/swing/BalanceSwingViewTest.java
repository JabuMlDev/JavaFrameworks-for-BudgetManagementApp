package com.balance.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.balance.controller.BalanceController;
import com.balance.model.Client;
import com.balance.model.Invoice;
import com.balance.utils.DateTestsUtil;

@RunWith(GUITestRunner.class)
public class BalanceSwingViewTest extends AssertJSwingJUnitTestCase{
	
	private FrameFixture window;
	
	private BalanceSwingView balanceSwingView;
	
	@Mock
	private BalanceController balanceController;
	
	private static final int CURRENT_YEAR=Calendar.getInstance().get(Calendar.YEAR);
	private static final int YEAR_FIXTURE=2019;
	
	@Override
	protected void onSetUp() {
		MockitoAnnotations.initMocks(this);
		GuiActionRunner.execute(() -> {
			balanceSwingView = new BalanceSwingView();
			balanceSwingView.setBalanceController(balanceController);
			return balanceSwingView;
		});
		window = new FrameFixture(robot(), balanceSwingView);
		window.show();
	}
	
	@Test @GUITest
	public void testControlInitialStates() {
		window.label(JLabelMatcher.withText("CLIENTI")); 
		window.list("clientsList");
		window.table("invoicesTable").requireColumnCount(3);
		window.table("invoicesTable").requireColumnNamed("Cliente");
		window.table("invoicesTable").requireColumnNamed("Data");
		window.table("invoicesTable").requireColumnNamed("Importo (???)");
		window.label("revenueLabel");
		window.comboBox("yearsCombobox");
		window.textBox("paneClientErrorMessage").requireText("");
		window.textBox("paneInvoiceErrorMessage").requireText("");
		window.button(JButtonMatcher.withText(Pattern.compile(".*Vedi tutte.*le fatture.*")))
			.requireNotVisible();
		window.label(JLabelMatcher.withText("NUOVO CLIENTE"));
		window.label(JLabelMatcher.withText("Identificativo"));
		window.textBox("textField_clientName").requireEnabled();
		window.button(JButtonMatcher.withText("Aggiungi cliente")).requireDisabled();
		window.button(JButtonMatcher.withText("Rimuovi cliente")).requireDisabled();
		window.label(JLabelMatcher.withText("NUOVA FATTURA"));
		window.label(JLabelMatcher.withText("Cliente"));
		window.comboBox("clientsCombobox");
		window.label(JLabelMatcher.withText("Data"));
		window.textBox("textField_dayOfDateInvoice").requireEnabled();
		window.textBox("textField_monthOfDateInvoice").requireEnabled();
		window.textBox("textField_yearOfDateInvoice").requireEnabled();
		window.label(JLabelMatcher.withText("Importo"));
		window.label(JLabelMatcher.withText("???"));
		window.textBox("textField_revenueInvoice").requireEnabled();
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		window.button(JButtonMatcher.withText(Pattern.compile(".*Rimuovi.*fattura.*"))).requireDisabled();
	}
	
	@Test @GUITest
	public void testShowAllClientsShouldAddClientsInOrderToTheClientsListAndCombobox(){
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		GuiActionRunner.execute(() -> 
			balanceSwingView.showClients(Arrays.asList(client1, client2)) 
		);
		String[] clientListContents=window.list("clientsList").contents();
		assertThat(clientListContents).containsExactly(client1.toString(), client2.toString());
		String[] clientComboboxContents=window.comboBox("clientsCombobox").contents();
		assertThat(clientComboboxContents).containsExactly(client1.toString(), client2.toString());
	}
	
	@Test @GUITest
	public void testShowAllClientsShouldAddClientsInOrderToTheClientsListAndComboboxAndResetPrevious(){ 
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		GuiActionRunner.execute(() -> {
				balanceSwingView.getClientListModel().add(0,client1);
				balanceSwingView.getComboboxClientsModel().addElement(client1);
				balanceSwingView.showClients(Arrays.asList(client1,client2));
			}
		);
		String[] clientListContents=window.list("clientsList").contents();
		assertThat(clientListContents).containsExactly(client1.toString(), client2.toString());
		String[] clientComboboxContents=window.comboBox("clientsCombobox").contents();
		assertThat(clientComboboxContents).containsExactly(client1.toString(), client2.toString());
	}
	
	@Test @GUITest
	public void testShowAllInvoicesShouldAddInvoicesToTheInvoicesTable(){
		Client client=new Client("1","test identifier 1"); 
		Invoice invoice1=new Invoice("1",client, new Date(), 10);
		Invoice invoice2=new Invoice("2",client, new Date(), 20);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> yearsCombobox=balanceSwingView.getComboboxYearsModel();
				yearsCombobox.addElement(CURRENT_YEAR);
				yearsCombobox.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(invoice1, invoice2));
			}
		);
		window.table("invoicesTable").requireRowCount(2);
		String[][] tableContents = window.table("invoicesTable").contents();
		assertThat(tableContents[0]).containsExactly(invoice1.getClient().getIdentifier(),
				invoice1.getDateInString(),invoice1.getRevenueInString());
		assertThat(tableContents[1]).containsExactly(invoice2.getClient().getIdentifier(),
				invoice2.getDateInString(),invoice2.getRevenueInString());
	}
	
	@Test @GUITest
	public void testShowAllInvoicesShouldAddInvoicesToTheInvoicesTableAndResetPrevious(){
		Client client=new Client("1","test identifier 1"); 
		Invoice invoice1=new Invoice("1",client, new Date(), 10);
		Invoice invoice2=new Invoice("2",client, new Date(), 20);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> yearsCombobox=balanceSwingView.getComboboxYearsModel();
				yearsCombobox.addElement(CURRENT_YEAR);
				yearsCombobox.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.getInvoiceTableModel().addElement(invoice1);
				balanceSwingView.showInvoices(Arrays.asList(invoice2));
			}
		);
		window.table("invoicesTable").requireRowCount(1);
		String[][] tableContents = window.table("invoicesTable").contents();
		assertThat(tableContents[0]).containsExactly(invoice2.getClient().getIdentifier(),
				invoice2.getDateInString(),invoice2.getRevenueInString());
	}
	
	@Test @GUITest
	public void testShowAllInvoicesShouldAddInvoicesInOrderToTheInvoicesTableAndResetPrevious(){
		Client client=new Client("1","test identifier 1"); 
		Invoice invoicePreviousDate=new Invoice(client, DateTestsUtil.getDate(2, 10, CURRENT_YEAR),20);
		Invoice invoiceNextDate=new Invoice(client, DateTestsUtil.getDate(3, 10, CURRENT_YEAR),10);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> yearsCombobox=balanceSwingView.getComboboxYearsModel();
				yearsCombobox.addElement(CURRENT_YEAR);
				yearsCombobox.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.getInvoiceTableModel().addElement(invoicePreviousDate);
				balanceSwingView.showInvoices(Arrays.asList(invoiceNextDate,invoicePreviousDate));
			}
		);
		window.table("invoicesTable").requireRowCount(2);
		String[][] tableContents = window.table("invoicesTable").contents();
		assertThat(tableContents[0]).containsExactly(invoicePreviousDate.getClient().getIdentifier(),
				invoicePreviousDate.getDateInString(),invoicePreviousDate.getRevenueInString());
		assertThat(tableContents[1]).containsExactly(invoiceNextDate.getClient().getIdentifier(),
				invoiceNextDate.getDateInString(),invoiceNextDate.getRevenueInString());
	}
	
	
	@Test @GUITest
	public void testSetChoiceYearInvoicesInOrderAndResetWhenThereIsCurrentYear() {
		GuiActionRunner.execute(() -> {
				balanceSwingView.setChoiceYearInvoices(Arrays.asList(YEAR_FIXTURE,YEAR_FIXTURE-1,
						CURRENT_YEAR));
			}
		);
		String[] yearsComboboxContents=window.comboBox("yearsCombobox").contents();
		assertThat(yearsComboboxContents).containsExactly(
				""+CURRENT_YEAR,""+(YEAR_FIXTURE),""+(YEAR_FIXTURE-1));
		window.comboBox("yearsCombobox").requireSelection(Pattern.compile(""+CURRENT_YEAR));
	}
	
	@Test @GUITest
	public void testSetChoiceYearInvoicesAndResetWhenThereIsNotCurrentYear() {
		GuiActionRunner.execute(() -> {
				balanceSwingView.setChoiceYearInvoices(Arrays.asList(YEAR_FIXTURE, YEAR_FIXTURE-1));
			}
		);
		String[] yearsComboboxContents=window.comboBox("yearsCombobox").contents();
		assertThat(yearsComboboxContents).containsExactly(
				""+CURRENT_YEAR,""+(YEAR_FIXTURE),""+(YEAR_FIXTURE-1));
		window.comboBox("yearsCombobox").requireSelection(Pattern.compile(""+CURRENT_YEAR));
	}
	
	@Test @GUITest
	public void testSelectYearShouldDelegateToControllerFindYearsInvoices() {
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> listYearsModel =balanceSwingView.getComboboxYearsModel();
			listYearsModel.addElement(CURRENT_YEAR-1);
			listYearsModel.addElement(CURRENT_YEAR);
			}
		);
		window.comboBox("yearsCombobox").selectItem(0);
		verify(balanceController).allInvoicesByYear(CURRENT_YEAR-1);
	}
	
	@Test @GUITest
	public void testSelectYearWhenAClientIsSelectedShouldDelegateToControllerFindClientInvoices() {
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> listYearsModel=balanceSwingView.getComboboxYearsModel();
			listYearsModel.addElement(CURRENT_YEAR-1);
			listYearsModel.addElement(CURRENT_YEAR);
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(client1);
			listClientsModel.addElement(client2);
			}
		);
		window.list("clientsList").selectItem(Pattern.compile(client2.toString()));
		window.comboBox("yearsCombobox").selectItem(Pattern.compile(""+(CURRENT_YEAR-1)));
		verify(balanceController).allInvoicesByClientAndYear(client2, CURRENT_YEAR-1);
	}	
	
	@Test @GUITest
	public void testClientRemovedShouldRemoveTheClientFromTheListAndComboboxAndClearSelectionList(){
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(client1);
			listClientsModel.addElement(client2);
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(client1);
			comboboxClientsModel.addElement(client2);
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute( () -> balanceSwingView.clientRemoved(
				new Client("1", "test identifier 1")));
		String[] clientListContents=window.list("clientsList").contents();
		assertThat(clientListContents).containsOnly(client2.toString());
		String[] clientComboboxContents=window.comboBox("clientsCombobox").contents();
		assertThat(clientComboboxContents).containsOnly(client2.toString());
		window.list("clientsList").requireNoSelection();
	}
	
	@Test @GUITest
	public void testClientRemovedShouldRemoveTheClientWhenThereIsAnotherClientWithSameIdentifier(){
		Client clientToRemove=new Client("1","test identifier");
		Client clientWithEqualName=new Client("2","test identifier");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(clientToRemove);
			listClientsModel.addElement(clientWithEqualName);
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(clientToRemove);
			comboboxClientsModel.addElement(clientWithEqualName);
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute( () -> balanceSwingView.clientRemoved(
				new Client("1", "test identifier")));
		String[] clientListContents=window.list("clientsList").contents();
		assertThat(clientListContents).containsOnly(clientWithEqualName.toString());
		String[] clientComboboxContents=window.comboBox("clientsCombobox").contents();
		assertThat(clientComboboxContents).containsOnly(clientWithEqualName.toString());
		window.list("clientsList").requireNoSelection();
	}
	
	@Test @GUITest
	public void testShowErrorClientShouldShowTheMessageInTheClientErrorLabel() {
		Client client=new Client("1","test identifier");
		GuiActionRunner.execute(
				() -> balanceSwingView.showClientError("error message", client) );
		window.textBox("paneClientErrorMessage").requireText("error message: " + 
				client.toString());
	}
	
	@Test @GUITest
	public void testShowAllInvoicesButtonShouldBeVisibleOnlyWhenAClientIsSelected() {
		GuiActionRunner.execute(() -> balanceSwingView.getClientListModel()
				.addElement(new Client("1","test identifier"))); 
		window.list("clientsList").selectItem(0); 
		JButtonFixture showInvoicesButton = 
				window.button(JButtonMatcher.withText(Pattern.compile(".*Vedi tutte.*le fatture.*")));
		showInvoicesButton.requireVisible();
		window.list("clientsList").clearSelection(); 
		showInvoicesButton.requireNotVisible();
	}
	
	@Test @GUITest
	public void testShowAllInvoicesShouldDelegateToControllerFindAllInvoicesAndRevenue() {
		GuiActionRunner.execute(() -> {
			balanceSwingView.getClientListModel().addElement(new Client("1","test identifier"));
			balanceSwingView.getComboboxYearsModel().addElement(CURRENT_YEAR);
			balanceSwingView.getComboboxYearsModel().addElement(YEAR_FIXTURE);
		}); 
		window.list("clientsList").selectItem(0);
		window.comboBox("yearsCombobox").selectItem(1);
		window.button(JButtonMatcher.withText(Pattern.compile(".*Vedi tutte.*le fatture.*"))).click();
		verify(balanceController).allInvoicesByYear(YEAR_FIXTURE);
		window.list("clientsList").requireNoSelection();
		window.button(JButtonMatcher.withText(".*Vedi tutte.*le fatture.*")).requireNotVisible();
	}
	
	@Test @GUITest
	public void testWhenIdentifierAreNonEmptyThenAddButtonShouldBeEnabled() {
		JTextComponentFixture identifierTextBox = window.textBox("textField_clientName");
		identifierTextBox.enterText("test");
		window.button("btnAddClient").requireEnabled();
		identifierTextBox.setText("");
		identifierTextBox.enterText(" ");
		window.button("btnAddClient").requireDisabled();
	}
	
	@Test @GUITest
	public void testClientAddedShouldAddTheClientToTheListAndComboboxAndResetTheErrorLabelAndTextFieldName(){
		Client client=new Client("1","test identifier");
		GuiActionRunner.execute(() -> balanceSwingView.clientAdded(new Client("1","test identifier")) ); 
		String[] clientListContents=window.list("clientsList").contents();
		assertThat(clientListContents).contains(client.toString());
		String[] clientComboboxContents=window.comboBox("clientsCombobox").contents();
		assertThat(clientComboboxContents).contains(client.toString());
		window.textBox("paneClientErrorMessage").requireText("");
		window.textBox(("textField_clientName")).requireText("");
		window.button("btnAddClient").requireDisabled();
	}
	

	@Test @GUITest
	public void testClientAddedShouldAddTheClientInOrderToListAndComboboxResetTheErrorLabelAndTextFieldName(){
		Client client1=new Client("1", "test identifier 1");
		Client client2=new Client("2", "test identifier 2");
		GuiActionRunner.execute(() -> {
			balanceSwingView.getClientListModel().add(0,client2);
			balanceSwingView.getComboboxClientsModel().addElement(client2);
		});
		window.list("clientsList").selectItem(0);
		window.comboBox("clientsCombobox").selectItem(0);
		GuiActionRunner.execute(() -> balanceSwingView.clientAdded(client1) );
		String[] clientListContents=window.list("clientsList").contents();
		assertThat(clientListContents).containsExactly(client1.toString(),client2.toString());
		String[] clientComboboxContents=window.comboBox("clientsCombobox").contents();
		assertThat(clientComboboxContents).containsExactly(client1.toString(),client2.toString());
		window.list("clientsList").requireSelection(Pattern.compile(client2.getIdentifier()));
		window.comboBox("clientsCombobox").requireSelection(Pattern.compile(client2.getIdentifier()));
		window.textBox("paneClientErrorMessage").requireText("");
		window.textBox(("textField_clientName")).requireText("");
		window.button("btnAddClient").requireDisabled();
	}

	
	@Test @GUITest
	public void testAddClientButtonShouldDelegateToBalanceControllerNewClientAndResetClientErrorLabel() {
		window.textBox("textField_clientName").enterText("test identifier 1");
		window.button(JButtonMatcher.withText("Aggiungi cliente")).click();
		verify(balanceController).newClient(new Client("test identifier 1"));
	}
	
	@Test @GUITest
	public void testDeleteClientButtonShouldBeEnabledOnlyWhenAClientIsSelected() {
		GuiActionRunner.execute(() -> 
			balanceSwingView.getClientListModel().addElement(new Client("1", "test identifier"))); 
		window.list("clientsList").selectItem(0); 
		JButtonFixture deleteClientButton = window.button(JButtonMatcher.withText("Rimuovi cliente"));
		deleteClientButton.requireEnabled();
		window.list("clientsList").clearSelection(); 
		deleteClientButton.requireDisabled(); 
	}
	
	@Test @GUITest
	public void testDeleteClientButtonShouldDelegateToBalanceControllerDeleteClient() {
		Client client1=new Client("1", "test identifier 1");
		Client client2=new Client("2", "test identifier 2");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(client1);
			listClientsModel.addElement(client2);
			}
		);
		window.list("clientsList").selectItem(0);
		window.button(JButtonMatcher.withText("Rimuovi cliente")).click();
		verify(balanceController).deleteClient(new Client("1",client1.getIdentifier()));
	}
	
	@Test @GUITest
	public void testRefreshAllInvoicesByYearWhenAnyClientIsSelected(){
		Client client1=new Client("1", "test identifier 1");
		Client client2=new Client("2", "test identifier 2");
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearsModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearsModel.addElement(CURRENT_YEAR-1);
			comboboxYearsModel.addElement(CURRENT_YEAR);
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(client1);
			listClientsModel.addElement(client2);
			}
		);
		window.comboBox("yearsCombobox").selectItem(0);
		window.list("clientsList").selectItem(0);
		window.list("clientsList").clearSelection();
		verify(balanceController,times(2)).allInvoicesByYear(CURRENT_YEAR-1);
	}
	
	@Test @GUITest
	public void testInvoiceAddedWhenInvoiceIsOfTheYearSelectedAndResetErrorLabel() {
		Client client=new Client("1", "test identifier");
		Invoice invoiceToAdd=new Invoice(client, DateTestsUtil.getDateFromYear(YEAR_FIXTURE),10);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearsModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearsModel.addElement(YEAR_FIXTURE);
			comboboxYearsModel.addElement(CURRENT_YEAR);
			comboboxYearsModel.setSelectedItem(YEAR_FIXTURE);
			balanceSwingView.invoiceAdded(invoiceToAdd);
			}
		);
		window.table("invoicesTable").requireRowCount(1);
		String[][] tableContents = window.table("invoicesTable").contents(); 
		assertThat(tableContents[0]).containsExactly(invoiceToAdd.getClient().getIdentifier(),
				invoiceToAdd.getDateInString(),invoiceToAdd.getRevenueInString());
		window.textBox("paneInvoiceErrorMessage").requireText("");
	}
	
	@Test @GUITest
	public void testInvoiceAddedInOrderWhenInvoiceIsOfTheYearSelectedAndResetErrorLabel() {
		Client client=new Client("1", "test identifier");
		Invoice invoiceExistingPrevious=new Invoice(
				client, DateTestsUtil.getDate(2, 10, CURRENT_YEAR),20);
		Invoice invoiceExistingNext=new Invoice(
				client, DateTestsUtil.getDate(4, 10, CURRENT_YEAR),10);
		Invoice invoiceToAdd=new Invoice(
				client, DateTestsUtil.getDate(3, 10, CURRENT_YEAR),10);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearsModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearsModel.addElement(YEAR_FIXTURE);
				comboboxYearsModel.addElement(CURRENT_YEAR);
				comboboxYearsModel.setSelectedItem(CURRENT_YEAR);
				InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
				listInvoiceModel.addElement(invoiceExistingPrevious);
				listInvoiceModel.addElement(invoiceExistingNext);
			}
		);
		window.table("invoicesTable").selectRows(1);
		GuiActionRunner.execute(() -> balanceSwingView.invoiceAdded(invoiceToAdd));
		window.table("invoicesTable").requireRowCount(3);
		String[][] tableContents = window.table("invoicesTable").contents();
		assertThat(tableContents[0]).containsExactly(invoiceExistingPrevious.getClient().getIdentifier(),
				invoiceExistingPrevious.getDateInString(),invoiceExistingPrevious.getRevenueInString());
		assertThat(tableContents[1]).containsExactly(invoiceToAdd.getClient().getIdentifier(),
				invoiceToAdd.getDateInString(),invoiceToAdd.getRevenueInString());
		assertThat(tableContents[2]).containsExactly(invoiceExistingNext.getClient().getIdentifier(),
				invoiceExistingNext.getDateInString(),invoiceExistingNext.getRevenueInString());
		window.table("invoicesTable").requireSelectedRows(2);
		window.textBox("paneInvoiceErrorMessage").requireText("");
	}
	
	
	@Test @GUITest
	public void testInvoiceAddedWhenInvoiceIsNotOfTheYearSelectedAndResetErrorLabel() {
		Client client=new Client("1", "test identifier");
		Invoice invoiceToAdd=new Invoice(
				client, DateTestsUtil.getDateFromYear(YEAR_FIXTURE),10);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearsModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearsModel.addElement(CURRENT_YEAR);
			comboboxYearsModel.addElement(YEAR_FIXTURE-1);
			comboboxYearsModel.addElement(YEAR_FIXTURE-2);
			comboboxYearsModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.invoiceAdded(invoiceToAdd);
			}
		);
		String[][] tableContents = window.table("invoicesTable").contents();
		assertThat(tableContents).doesNotContain(
				new String[] {invoiceToAdd.getClient().getIdentifier(),
					invoiceToAdd.getDateInString(),invoiceToAdd.getRevenueInString()});
		String[] yearsComboboxContents = window.comboBox("yearsCombobox").contents();
		assertThat(yearsComboboxContents).containsExactly(
				""+CURRENT_YEAR,""+YEAR_FIXTURE,""+(YEAR_FIXTURE-1),""+(YEAR_FIXTURE-2));
		window.comboBox("yearsCombobox").requireSelection(Pattern.compile(""+CURRENT_YEAR));
		window.textBox("paneInvoiceErrorMessage").requireText("");
	}
	
	@Test @GUITest
	public void testInsertOnlyCorrectNumberInDateAndRevenueTextFields() {
		window.textBox("textField_dayOfDateInvoice").enterText("text");
		window.textBox("textField_dayOfDateInvoice").requireEmpty();
		window.textBox("textField_dayOfDateInvoice").enterText("203");
		window.textBox("textField_dayOfDateInvoice").requireText("20");
		window.textBox("textField_dayOfDateInvoice").setText("");
		window.textBox("textField_dayOfDateInvoice").enterText("2 3");
		window.textBox("textField_dayOfDateInvoice").requireText("23");
		window.textBox("textField_dayOfDateInvoice").setText("");
		window.textBox("textField_dayOfDateInvoice").enterText("20");
		window.textBox("textField_dayOfDateInvoice").requireText("20");
		
		window.textBox("textField_monthOfDateInvoice").enterText("text");
		window.textBox("textField_monthOfDateInvoice").requireEmpty();
		window.textBox("textField_monthOfDateInvoice").enterText("203");
		window.textBox("textField_monthOfDateInvoice").requireText("20");
		window.textBox("textField_monthOfDateInvoice").setText("");
		window.textBox("textField_monthOfDateInvoice").enterText("2 3");
		window.textBox("textField_monthOfDateInvoice").requireText("23");
		window.textBox("textField_monthOfDateInvoice").setText("");
		window.textBox("textField_monthOfDateInvoice").enterText("20");
		window.textBox("textField_monthOfDateInvoice").requireText("20");
		
		window.textBox("textField_yearOfDateInvoice").enterText("text");
		window.textBox("textField_yearOfDateInvoice").requireEmpty();
		window.textBox("textField_yearOfDateInvoice").enterText("20200");
		window.textBox("textField_yearOfDateInvoice").requireText("2020");
		window.textBox("textField_yearOfDateInvoice").setText("");
		window.textBox("textField_yearOfDateInvoice").enterText("2 0 2 0");
		window.textBox("textField_yearOfDateInvoice").requireText("2020");
		window.textBox("textField_yearOfDateInvoice").setText("");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.textBox("textField_yearOfDateInvoice").requireText("2020");
		
		window.textBox("textField_revenueInvoice").enterText("text");
		window.textBox("textField_revenueInvoice").requireEmpty();
		window.textBox("textField_revenueInvoice").enterText("5 00, 20");
		window.textBox("textField_revenueInvoice").requireText("500,20");
		window.textBox("textField_revenueInvoice").setText("");
		window.textBox("textField_revenueInvoice").enterText("500,2012");
		window.textBox("textField_revenueInvoice").requireText("500,20");
		window.textBox("textField_revenueInvoice").setText("");
		window.textBox("textField_revenueInvoice").enterText("500,2,0");
		window.textBox("textField_revenueInvoice").requireText("500,20");
		window.textBox("textField_revenueInvoice").setText("");
		window.textBox("textField_revenueInvoice").enterText("500,20");
		window.textBox("textField_revenueInvoice").requireText("500,20");
		
		
	}
	
	@Test @GUITest
	public void testWhenAClientIsSelectedAndTextFieldsIsNotEmptyThenAddInvoiceButtonShouldBeEnabled() {
		Client client=new Client("1", "test identifier");
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(client);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireEnabled();
		
		window.comboBox("clientsCombobox").clearSelection();
		window.textBox("textField_dayOfDateInvoice").enterText("");
		window.textBox("textField_monthOfDateInvoice").enterText("");
		window.textBox("textField_yearOfDateInvoice").enterText("");
		window.textBox("textField_revenueInvoice").enterText("");
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.comboBox("clientsCombobox").selectItem(0);
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireEnabled();
	}
	
	@Test @GUITest
	public void testWhenNoClientSelectedOrTextFieldsIsEmptyThenAddInvoiceButtonShouldBeDisabled() {
		Client client=new Client("1", "test identifier");
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(client);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.textBox("textField_revenueInvoice").enterText(" ");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		
		window.textBox("textField_yearOfDateInvoice").setText("");
		window.textBox("textField_revenueInvoice").setText("");
		window.textBox("textField_yearOfDateInvoice").enterText(" ");
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		
		window.textBox("textField_monthOfDateInvoice").setText("");
		window.textBox("textField_yearOfDateInvoice").setText("");
		window.textBox("textField_monthOfDateInvoice").enterText(" ");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		
		window.textBox("textField_dayOfDateInvoice").setText("");
		window.textBox("textField_monthOfDateInvoice").setText("");
		window.textBox("textField_dayOfDateInvoice").enterText(" ");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		
		window.comboBox("clientsCombobox").clearSelection();
		window.textBox("textField_dayOfDateInvoice").setText("");
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
	}
	

	@Test @GUITest
	public void testAddInvoiceBtnShouldDelegateToControllerNewInvoiceWhenDateIsCorrectAndResetTextField() {
		Client client=new Client("1", "test identifier");
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(client);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText(""+YEAR_FIXTURE);
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).click();
		verify(balanceController).newInvoice(new Invoice(
				client, DateTestsUtil.getDate(1, 5, YEAR_FIXTURE), 10.20));
		window.textBox("paneInvoiceErrorMessage").requireText("");
		window.textBox("textField_dayOfDateInvoice").requireEmpty();
		window.textBox("textField_monthOfDateInvoice").requireEmpty();
		window.textBox("textField_yearOfDateInvoice").requireEmpty();
		window.textBox("textField_revenueInvoice").requireEmpty();
		window.comboBox("clientsCombobox").requireNoSelection();
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
	}

	@Test @GUITest
	public void testAddInvoiceBtnShouldDelegateToControllerNewInvoiceWhenYearIsNotCorrect() {
		Client client=new Client("1", "test identifier");
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(client);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText(""+(CURRENT_YEAR+1));
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).click();
		
		window.textBox("textField_dayOfDateInvoice").requireEmpty();
		window.textBox("textField_monthOfDateInvoice").requireEmpty();
		window.textBox("textField_yearOfDateInvoice").requireEmpty();
		window.textBox("paneInvoiceErrorMessage").requireText(
				"La data 1/5/"+(CURRENT_YEAR+1)+" non ?? corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
		
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText(""+(CURRENT_YEAR-101));
		window.button(JButtonMatcher.withText("Aggiungi fattura")).click();
		window.textBox("paneInvoiceErrorMessage").requireText(
				"La data 1/5/"+(CURRENT_YEAR-101)+" non ?? corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
	}
	
	@Test @GUITest
	public void testAddInvoiceBtnShouldDelegateToControllerNewInvoiceWhenMonthIsNotCorrect() {
		Client client=new Client("1", "test identifier");
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(client);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("13");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).click();
		
		window.textBox("textField_dayOfDateInvoice").requireEmpty();
		window.textBox("textField_monthOfDateInvoice").requireEmpty();
		window.textBox("textField_yearOfDateInvoice").requireEmpty();
		window.textBox("paneInvoiceErrorMessage").requireText("La data 1/13/2020 non ?? corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
	}
	
	@Test @GUITest
	public void testAddInvoiceBtnShouldDelegateToControllerNewInvoiceWhenDayIsNotCorrect() {
		Client client=new Client("1", "test identifier");
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(client);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("31");
		window.textBox("textField_monthOfDateInvoice").enterText("2");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).click();
		window.textBox("textField_dayOfDateInvoice").requireEmpty();
		window.textBox("textField_monthOfDateInvoice").requireEmpty();
		window.textBox("textField_yearOfDateInvoice").requireEmpty();
		window.textBox("paneInvoiceErrorMessage").requireText("La data 31/2/2020 non ?? corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenShowAllInvoicesAndAnyClientIsSelected() {
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		Invoice invoice1=new Invoice("1",client1, new Date(), 10);
		Invoice invoice2=new Invoice("2",client2, new Date(), 20);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(invoice1,invoice2));
			}
		);
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoice1.getRevenue()+
								invoice2.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenShowAllInvoicesAndAClientIsSelected() {
		Client client1=new Client("1","test identifier 1"); 
		Invoice invoice1=new Invoice("1",client1, new Date(), 10);
		Invoice invoice2=new Invoice("2",client1, new Date(), 20);
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(client1);
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(invoice1,invoice2));
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(invoice1,invoice2));
				}
			);
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ client1.getIdentifier()+" nel "
						+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoice1.getRevenue()+
								invoice2.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceOfTheYearSelectedIsAddedAndAnyClientIsSelected() {
		Client client=new Client("1","test identifier 1"); 
		Invoice invoiceOfCurrentYear1=new Invoice(
				"1",client, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		Invoice invoiceOfCurrentYear2=new Invoice(
				"2",client, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 20);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(invoiceOfCurrentYear1));
				balanceSwingView.invoiceAdded(invoiceOfCurrentYear2);
			}
		);
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoiceOfCurrentYear1.getRevenue()+
								invoiceOfCurrentYear2.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceNotOfTheYearSelectedIsAddedAndAnyClientIsSelected() {
		Client client=new Client("1","test identifier 1"); 
		Invoice invoiceOfCurrentYear=new Invoice(
				"1",client, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		Invoice invoiceOfYearFixture=new Invoice(
				"2",client, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 20);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(invoiceOfCurrentYear));
				balanceSwingView.invoiceAdded(invoiceOfYearFixture);
			}
		);
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoiceOfCurrentYear.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceOfTheYearSelectedAndOfTheClientSelectedIsAdded() {
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		Invoice invoiceOfClient2=new Invoice(
				"2",client2, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 20);
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(client1);
				listClientModel.addElement(client2);
			}
		);
		window.list("clientsList").selectItem(1);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.invoiceAdded(invoiceOfClient2);
			}
	    );
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ client2.getIdentifier()+" nel "
						+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoiceOfClient2.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceNotOfTheYearSelectedAndOfTheClientSelectedIsAdded() {
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		Invoice invoiceOfCurrentYearClient1=new Invoice(
				"1",client1, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		Invoice invoiceOfYearFixtureClient1=new Invoice(
				"2",client1, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 20);
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(client1);
				listClientModel.addElement(client2);
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.showInvoices(Arrays.asList(invoiceOfCurrentYearClient1));
			balanceSwingView.invoiceAdded(invoiceOfYearFixtureClient1);
			}
		);
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ client1.getIdentifier()+" nel "
						+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoiceOfCurrentYearClient1.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceNotOfTheYearSelectedAndNotOfTheClientSelectedIsAdded() {
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		Invoice invoiceOfClient1CurrentYear=new Invoice(
				"1",client1, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		Invoice invoiceOfClient2YearFixture=new Invoice(
				"2",client2, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 20);
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(client1);
				listClientModel.addElement(client2);
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.showInvoices(Arrays.asList(invoiceOfClient1CurrentYear));
			balanceSwingView.invoiceAdded(invoiceOfClient2YearFixture);
		}
	);
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ client1.getIdentifier()+" nel "
						+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoiceOfClient1CurrentYear.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testResetShowInvoiceOfCurrentYearWhenShowInvoiceIsCalledWithEmptyArgumentAndAnyClientIsSelected() {
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
				balanceSwingView.showInvoices(Arrays.asList());
			}
		);
		verify(balanceController).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testNotResetShowInvoiceWhenIsCalledWithEmptyArgumentClientIsSelectedAndCurrentYearIsSelected() {
		Client client=new Client("1","test identifier"); 
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(client);				
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> balanceSwingView.showInvoices(Arrays.asList()) );
		window.label("revenueLabel").requireText("Non sono presenti fatture del "+YEAR_FIXTURE+
				" per il cliente "+client.getIdentifier());
		verify(balanceController, never()).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testNotResetShowInvoiceOfCurrentYearWhenIsCalledWithEmptyArgumentAndCurrentYearIsSelected() {
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);			
				balanceSwingView.showInvoices(Arrays.asList());
			}
		);
		window.label("revenueLabel").requireText("Non sono presenti fatture per il "+CURRENT_YEAR);
		verify(balanceController, never()).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testRemoveInvoicesOfAClientWhenInvoicesRemanentInListIsNotEmptyAndUpdateTotalRevenue() {
		Client client1=new Client("1","test identifier 1");
		Client client2=new Client("2","test identifier 2");
		Invoice invoiceOfClient1=new Invoice("1",client1, new Date(), 10);
		Invoice invoiceOfClient2=new Invoice("1",client2, new Date(), 20);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(invoiceOfClient1);
			listInvoiceModel.addElement(invoiceOfClient2);
			balanceSwingView.removeInvoicesOfClient(client1);
			}
		);
		String[][] tableContents=window.table("invoicesTable").contents();
		assertThat(tableContents).containsOnly(
				new String[] {invoiceOfClient2.getClient().getIdentifier(),
						invoiceOfClient2.getDateInString(),invoiceOfClient2.getRevenueInString()});
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoiceOfClient2.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testRemoveInvoicesOfAClientWhenInvoicesRemanentInListIsEmptyAndAnyClientIsSelected() {
		Client client=new Client("1","test identifier");
		Invoice invoice1=new Invoice("1",client, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 10);
		Invoice invoice2=new Invoice("2",client, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 20);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(invoice1);
			listInvoiceModel.addElement(invoice2);
			balanceSwingView.removeInvoicesOfClient(new Client("1","test identifier"));
			}
		);
		String[][] tableContents=window.table("invoicesTable").contents();
		assertThat(tableContents).isEmpty();
		verify(balanceController).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testRemoveInvoicesOfAClientWhenInvoicesRemanentInListIsEmptyAndAnOtherClientIsSelected() {
		Client client1=new Client("1","test identifier 1"); 
		Client client2=new Client("2","test identifier 2"); 
		Invoice invoiceOfClient1CurrentYear=new Invoice(
				"1",client1, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		Invoice invoiceOfClient1YearFixture=new Invoice(
				"2",client1, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 20);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
			listClientModel.addElement(client1);
			listClientModel.addElement(client2);
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(invoiceOfClient1CurrentYear);
			listInvoiceModel.addElement(invoiceOfClient1YearFixture);
			}
		);
		window.list("clientsList").selectItem(1);
		GuiActionRunner.execute(() ->
				balanceSwingView.removeInvoicesOfClient(new Client(client1.getIdentifier()))
			);
		String[][] tableContents=window.table("invoicesTable").contents();
		assertThat(tableContents).isEmpty();
		verify(balanceController,never()).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testDeleteInvoiceButtonShouldBeEnabledOnlyWhenAnInvoiceIsSelected() {
		Client client=new Client("1","test identifier"); 
		Invoice invoice=new Invoice("1",client, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		GuiActionRunner.execute(() -> balanceSwingView.getInvoiceTableModel().addElement(invoice)); 
		window.table("invoicesTable").selectRows(0); 
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText(
										Pattern.compile(".*Rimuovi.*fattura.*")));
		deleteButton.requireEnabled();
		window.table("invoicesTable").unselectRows(0); 
		deleteButton.requireDisabled(); 
	}
	
	@Test @GUITest
	public void testDeleteInvoiceButtonShouldDelegateToBalanceControllerDeleteInvoice() {
		Client client=new Client("1","test identifier"); 
		Invoice invoice1=new Invoice(
				"1",client, DateTestsUtil.getDate(1, 2, CURRENT_YEAR), 10);
		Invoice invoice2=new Invoice(
				"2",client, DateTestsUtil.getDate(1, 4, CURRENT_YEAR), 20);
		GuiActionRunner.execute(() -> {
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(invoice1);
			listInvoiceModel.addElement(invoice2);
			}
		);
		window.table("invoicesTable").selectRows(0); 
		window.button(JButtonMatcher.withText(Pattern.compile(".*Rimuovi.*fattura.*"))).click();
		verify(balanceController).deleteInvoice(new Invoice(client, invoice1.getDate(), 10));
	}
	
	@Test @GUITest
	public void testRemoveInvoiceWhenInvoicesRemanentInTableIsNotEmptyAndUpdateTotalRevenue() {
		Client client=new Client("1","test identifier"); 
		Invoice invoice1=new Invoice(
				"1",client, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		Invoice invoice2=new Invoice(
				"2",client, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 20);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(invoice1);
			listInvoiceModel.addElement(invoice2);
			balanceSwingView.invoiceRemoved(new Invoice(client,
					invoice1.getDate(), 10));
			}
		);
		String[][] tableContents=window.table("invoicesTable").contents();
		assertThat(tableContents).containsOnly(
				new String[] {invoice2.getClient().getIdentifier(),
						invoice2.getDateInString(),invoice2.getRevenueInString()});
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" ?? di "
						+String.format("%.2f", invoice2.getRevenue())+"???");
	}
	
	@Test @GUITest
	public void testRemoveInvoiceWhenInvoicesRemanentInListIsEmpty() {
		Client client=new Client("1","test identifier"); 
		Invoice invoice=new Invoice(
				"1",client, DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
			balanceSwingView.getInvoiceTableModel().addElement(invoice);
			balanceSwingView.invoiceRemoved(new Invoice(client,
					invoice.getDate(),10));
			}
		);
		assertThat(window.table("invoicesTable").contents()).isEmpty();
		verify(balanceController).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testShowErrorInvoiceShouldShowTheMessageInTheInvoiceErrorLabel() {
		Invoice invoice=new Invoice("1",new Client("1","test identifier"), 
							DateTestsUtil.getDateFromYear(CURRENT_YEAR), 10);
		GuiActionRunner.execute(
				() -> balanceSwingView.showInvoiceError("error message", invoice) );
		window.textBox("paneInvoiceErrorMessage").requireText("error message: " + 
				invoice.toString());
	}
	
}
