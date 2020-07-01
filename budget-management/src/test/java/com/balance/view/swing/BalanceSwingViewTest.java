package com.balance.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.awt.Dimension;
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
	
	private static final Client CLIENT_FIXTURE_1=
			new Client("test identifier 1"); 
	private static final Client CLIENT_FIXTURE_2=
			new Client("test identifier 2"); 
	
	private static final Invoice INVOICE_FIXTURE_1=
			new Invoice(CLIENT_FIXTURE_1, new Date(), 10);
	
	private static final Invoice INVOICE_FIXTURE_2=
			new Invoice(CLIENT_FIXTURE_2, new Date(), 20);
	
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
		window.table("invoicesTable").requireColumnNamed("Importo (€)");
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
		window.label(JLabelMatcher.withText("€"));
		window.textBox("textField_revenueInvoice").requireEnabled();
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		window.button(JButtonMatcher.withText(Pattern.compile(".*Rimuovi.*fattura.*"))).requireDisabled();
	}
	
	@Test @GUITest
	public void testShowAllClientsShouldAddClientsToTheClientsListAndCombobox(){ 
		GuiActionRunner.execute(() -> 
			balanceSwingView.showClients(Arrays.asList(CLIENT_FIXTURE_1, CLIENT_FIXTURE_2)) 
		);
		assertThat(window.list("clientsList").contents())
			.containsExactly(CLIENT_FIXTURE_1.toString(), CLIENT_FIXTURE_2.toString());
		assertThat(window.comboBox("clientsCombobox").contents())
			.containsExactly(CLIENT_FIXTURE_1.toString(), CLIENT_FIXTURE_2.toString());
	}
	
	@Test @GUITest
	public void testShowAllClientsShouldAddClientsInOrderToTheClientsListAndComboboxAndResetPrevious(){ 
		GuiActionRunner.execute(() -> {
				balanceSwingView.getClientListModel().add(0,CLIENT_FIXTURE_1);
				balanceSwingView.getComboboxClientsModel().addElement(CLIENT_FIXTURE_1);
				balanceSwingView.showClients(Arrays.asList(new Client("test identifier 2"),
						new Client("test identifier 1")));
			}
		);
		assertThat( window.list("clientsList").contents())
			.containsExactly(new Client("test identifier 1").toString(),
					new Client("test identifier 2").toString());
		assertThat(window.comboBox("clientsCombobox").contents())
			.containsExactly(new Client("test identifier 1").toString(),
					new Client("test identifier 2").toString()
		);
	}
	
	
	@Test @GUITest
	public void testShowAllInvoicesShouldAddInvoicesToTheInvoicesTable(){ 
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> yearsCombobox=balanceSwingView.getComboboxYearsModel();
				yearsCombobox.addElement(CURRENT_YEAR);
				yearsCombobox.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1, INVOICE_FIXTURE_2));
			}
		);
		String[][] tableContents = window.table("invoicesTable").contents(); 
		assertThat(tableContents[0]).containsExactly(INVOICE_FIXTURE_1.getClient().getIdentifier(),
				INVOICE_FIXTURE_1.getDateInString(),INVOICE_FIXTURE_1.getRevenueInString());
		assertThat(tableContents[1]).containsExactly(INVOICE_FIXTURE_2.getClient().getIdentifier(),
				INVOICE_FIXTURE_2.getDateInString(),INVOICE_FIXTURE_2.getRevenueInString());
	}
	
	@Test @GUITest
	public void testShowAllInvoicesShouldAddInvoicesToTheInvoicesTableAndResetPrevious(){ 
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> yearsCombobox=balanceSwingView.getComboboxYearsModel();
				yearsCombobox.addElement(CURRENT_YEAR);
				yearsCombobox.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.getInvoiceTableModel().addElement(INVOICE_FIXTURE_1);
				balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_2));
			}
		);
		String[][] tableContents = window.table("invoicesTable").contents(); 
		assertThat(tableContents[0]).containsExactly(INVOICE_FIXTURE_2.getClient().getIdentifier(),
				INVOICE_FIXTURE_2.getDateInString(),INVOICE_FIXTURE_2.getRevenueInString());
	}
	
	@Test @GUITest
	public void testShowAllInvoicesShouldAddInvoicesInOrderToTheInvoicesTableAndResetPrevious(){
		Invoice invoicePrevious=new Invoice(CLIENT_FIXTURE_1, DateTestsUtil.getDate(2, 10, CURRENT_YEAR)
				,20);
		Invoice invoiceNext=new Invoice(CLIENT_FIXTURE_1, DateTestsUtil.getDate(3, 10, CURRENT_YEAR),10);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> yearsCombobox=balanceSwingView.getComboboxYearsModel();
				yearsCombobox.addElement(CURRENT_YEAR);
				yearsCombobox.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.getInvoiceTableModel().addElement(INVOICE_FIXTURE_1);
				balanceSwingView.showInvoices(Arrays.asList(invoiceNext,invoicePrevious));
			}
		);
		String[][] tableContents = window.table("invoicesTable").contents(); 
		assertThat(tableContents[0]).containsExactly(invoicePrevious.getClient().getIdentifier(),
				invoicePrevious.getDateInString(),invoicePrevious.getRevenueInString());
		assertThat(tableContents[1]).containsExactly(invoiceNext.getClient().getIdentifier(),
				invoiceNext.getDateInString(),invoiceNext.getRevenueInString());
	}
	
	
	@Test @GUITest
	public void testSetChoiceYearInvoicesInOrderAndResetWhenThereIsCurrentYear() {
		GuiActionRunner.execute(() -> {
				balanceSwingView.setChoiceYearInvoices(Arrays.asList(YEAR_FIXTURE,YEAR_FIXTURE-1,
						CURRENT_YEAR));
			}
		);
		assertThat(window.comboBox("yearsCombobox").contents()).containsExactly(""+CURRENT_YEAR
				,""+(YEAR_FIXTURE),""+(YEAR_FIXTURE-1));
		window.comboBox("yearsCombobox").requireSelection(Pattern.compile(""+CURRENT_YEAR));
	}
	
	@Test @GUITest
	public void testSetChoiceYearInvoicesAndResetWhenThereIsNotCurrentYear() {
		GuiActionRunner.execute(() -> {
				balanceSwingView.setChoiceYearInvoices(Arrays.asList(YEAR_FIXTURE, YEAR_FIXTURE-1));
			}
		);
		assertThat(window.comboBox("yearsCombobox").contents()).containsExactly(""+CURRENT_YEAR
				,""+(YEAR_FIXTURE),""+(YEAR_FIXTURE-1));
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
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> listYearsModel=balanceSwingView.getComboboxYearsModel();
			listYearsModel.addElement(CURRENT_YEAR-1);
			listYearsModel.addElement(CURRENT_YEAR);
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(CLIENT_FIXTURE_1);
			listClientsModel.addElement(CLIENT_FIXTURE_2);
			}
		);
		window.list("clientsList").selectItem(1);
		window.comboBox("yearsCombobox").selectItem(0);
		verify(balanceController).allInvoicesByClientAndYear(CLIENT_FIXTURE_2, CURRENT_YEAR-1);
	}	
	
	@Test @GUITest
	public void testClientRemovedShouldRemoveTheClientFromTheListAndComboboxAndClearSelectionList(){
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(CLIENT_FIXTURE_1);
			listClientsModel.addElement(CLIENT_FIXTURE_2);
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(CLIENT_FIXTURE_1);
			comboboxClientsModel.addElement(CLIENT_FIXTURE_2);
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute( () -> balanceSwingView.clientRemoved(
				CLIENT_FIXTURE_1)
			);
		assertThat(window.list("clientsList").contents())
			.containsExactly(CLIENT_FIXTURE_2.toString());
		assertThat(window.comboBox("clientsCombobox").contents())
			.containsExactly(CLIENT_FIXTURE_2.toString());
		window.list("clientsList").requireNoSelection();
	}
	
	@Test @GUITest
	public void testClientRemovedShouldRemoveTheClientWhenThereIsAnotherClientWithSameIdentifier(){
		Client client1=new Client("1","test identifier");
		Client client2=new Client("2","test identifier");
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
				client1)
			);
		assertThat(window.list("clientsList").contents())
			.containsExactly(client2.toString());
		assertThat(window.comboBox("clientsCombobox").contents())
			.containsExactly(client2.toString());
		window.list("clientsList").requireNoSelection();
	}
	
	@Test @GUITest
	public void testShowErrorClientShouldShowTheMessageInTheClientErrorLabel() {
		GuiActionRunner.execute(
				() -> balanceSwingView.showClientError("error message", CLIENT_FIXTURE_1) );
		window.textBox("paneClientErrorMessage").requireText("error message: " + 
				CLIENT_FIXTURE_1.getIdentifier());
	}
	
	@Test @GUITest
	public void testShowAllInvoicesButtonShouldBeVisibleOnlyWhenAClientIsSelected() {
		GuiActionRunner.execute(() -> balanceSwingView.getClientListModel()
				.addElement(CLIENT_FIXTURE_1)); 
		window.list("clientsList").selectItem(0); 
		JButtonFixture deleteButton = 
				window.button(JButtonMatcher.withText(Pattern.compile(".*Vedi tutte.*le fatture.*")));
		deleteButton.requireVisible();
		window.list("clientsList").clearSelection(); 
		deleteButton.requireNotVisible();
	}
	
	@Test @GUITest
	public void testShowAllInvoicesShouldDelegateToControllerFindAllInvoicesAndRevenue() {
		GuiActionRunner.execute(() -> {
			balanceSwingView.getClientListModel().addElement(CLIENT_FIXTURE_1);
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
		JTextComponentFixture nameTextBox = window.textBox("textField_clientName");
		nameTextBox.enterText("test");
		window.button("btnAddClient").requireEnabled();
		nameTextBox.setText("");
		nameTextBox.enterText(" ");
		window.button("btnAddClient").requireDisabled();
	}
	
	@Test @GUITest
	public void testClientAddedShouldAddTheClientToTheListAndComboboxAndResetTheErrorLabelAndTextFieldName(){
		GuiActionRunner.execute(() -> balanceSwingView.clientAdded(CLIENT_FIXTURE_1) ); 
		assertThat(window.list("clientsList").contents())
			.contains(CLIENT_FIXTURE_1.toString());
		assertThat(window.comboBox("clientsCombobox").contents())
			.contains(CLIENT_FIXTURE_1.toString());
		window.textBox("paneClientErrorMessage").requireText("");
		window.textBox(("textField_clientName")).requireText("");
		window.button("btnAddClient").requireDisabled();
	}
	

	@Test @GUITest
	public void testClientAddedShouldAddTheClientInOrderToListAndComboboxResetTheErrorLabelAndTextFieldName(){
		Client client1=new Client("test identifier 1");
		Client client2=new Client("test identifier 2");
		GuiActionRunner.execute(() -> {
			balanceSwingView.getClientListModel().add(0,client2);
			balanceSwingView.getComboboxClientsModel().addElement(client2);
		});
		window.list("clientsList").selectItem(0);
		window.comboBox("clientsCombobox").selectItem(0);
		GuiActionRunner.execute(() -> balanceSwingView.clientAdded(client1) );
		assertThat(window.list("clientsList").contents())
			.containsExactly(client1.toString(),client2.toString());
		window.list("clientsList").requireSelection(Pattern.compile(client2.getIdentifier()));
		assertThat(window.comboBox("clientsCombobox").contents())
			.containsExactly(client1.toString(),client2.toString());
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
		GuiActionRunner.execute(() -> balanceSwingView.getClientListModel().addElement(CLIENT_FIXTURE_1)); 
		window.list("clientsList").selectItem(0); 
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Rimuovi cliente"));
		deleteButton.requireEnabled();
		window.list("clientsList").clearSelection(); 
		deleteButton.requireDisabled(); 
	}
	
	@Test @GUITest
	public void testDeleteClientButtonShouldDelegateToBalanceControllerDeleteClient() {
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(CLIENT_FIXTURE_1);
			listClientsModel.addElement(CLIENT_FIXTURE_2);
			}
		);
		window.list("clientsList").selectItem(0);
		window.button(JButtonMatcher.withText("Rimuovi cliente")).click();
		verify(balanceController).deleteClient(new Client(CLIENT_FIXTURE_1.getIdentifier()));
	}
	
	@Test @GUITest
	public void testRefreshAllInvoicesByYearWhenAnyClientIsSelected(){
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearsModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearsModel.addElement(CURRENT_YEAR-1);
			comboboxYearsModel.addElement(CURRENT_YEAR);
			DefaultListModel<Client> listClientsModel =balanceSwingView.getClientListModel();
			listClientsModel.addElement(CLIENT_FIXTURE_1);
			listClientsModel.addElement(CLIENT_FIXTURE_2);
			}
		);
		window.comboBox("yearsCombobox").selectItem(0);
		window.list("clientsList").selectItem(0);
		window.list("clientsList").clearSelection();
		verify(balanceController,times(2)).allInvoicesByYear(CURRENT_YEAR-1);
	}
	
	@Test @GUITest
	public void testInvoiceAddedWhenInvoiceIsOfTheYearSelectedAndResetErrorLabel() {
		Invoice invoiceToAdd=new Invoice(CLIENT_FIXTURE_1, DateTestsUtil.getDateFromYear(YEAR_FIXTURE),
										10);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearsModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearsModel.addElement(YEAR_FIXTURE);
			comboboxYearsModel.addElement(CURRENT_YEAR);
			comboboxYearsModel.setSelectedItem(YEAR_FIXTURE);
			balanceSwingView.invoiceAdded(invoiceToAdd);
			}
		);
		String[][] tableContents = window.table("invoicesTable").contents(); 
		assertThat(tableContents[0]).containsExactly(invoiceToAdd.getClient().getIdentifier(),
				invoiceToAdd.getDateInString(),invoiceToAdd.getRevenueInString());
		window.textBox("paneInvoiceErrorMessage").requireText("");
	}
	
	@Test @GUITest
	public void testInvoiceAddedInOrderWhenInvoiceIsOfTheYearSelectedAndResetErrorLabel() {
		Invoice invoiceExistingPrevious=new Invoice(
				CLIENT_FIXTURE_1, DateTestsUtil.getDate(2, 10, CURRENT_YEAR)
				,20);
		Invoice invoiceExistingNext=new Invoice(
				CLIENT_FIXTURE_1, DateTestsUtil.getDate(4, 10, CURRENT_YEAR),10);
		Invoice invoiceToAdd=new Invoice(CLIENT_FIXTURE_1, DateTestsUtil.getDate(3, 10, CURRENT_YEAR),
				10);
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
		Invoice invoiceToAdd=new Invoice(CLIENT_FIXTURE_1, DateTestsUtil.getDateFromYear(YEAR_FIXTURE),
										10);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearsModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearsModel.addElement(CURRENT_YEAR);
			comboboxYearsModel.addElement(YEAR_FIXTURE-1);
			comboboxYearsModel.addElement(YEAR_FIXTURE-2);
			comboboxYearsModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.invoiceAdded(invoiceToAdd);
			}
		);
		assertThat(window.table("invoicesTable").contents())
			.doesNotContain(new String[] {invoiceToAdd.getClient().getIdentifier(),
					invoiceToAdd.getDateInString(),invoiceToAdd.getRevenueInString()});
		assertThat(window.comboBox("yearsCombobox").contents())
			.containsExactly(""+CURRENT_YEAR,""+YEAR_FIXTURE,""+(YEAR_FIXTURE-1),""+(YEAR_FIXTURE-2));
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
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(CLIENT_FIXTURE_1);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText("2020");
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireEnabled();
	}
	
	@Test @GUITest
	public void testWhenNoClientSelectedOrTextFieldsIsEmptyThenAddInvoiceButtonShouldBeDisabled() {
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(CLIENT_FIXTURE_1);
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
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(CLIENT_FIXTURE_1);
			}
		);
		window.comboBox("clientsCombobox").selectItem(0);
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText(""+YEAR_FIXTURE);
		window.textBox("textField_revenueInvoice").enterText("10,20");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).click();
		verify(balanceController).newInvoice(new Invoice(
				CLIENT_FIXTURE_1, DateTestsUtil.getDate(1, 5, YEAR_FIXTURE), 10.20));
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
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(CLIENT_FIXTURE_1);
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
				"La data 1/5/"+(CURRENT_YEAR+1)+" non è corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
		
		window.textBox("textField_dayOfDateInvoice").enterText("1");
		window.textBox("textField_monthOfDateInvoice").enterText("5");
		window.textBox("textField_yearOfDateInvoice").enterText(""+(CURRENT_YEAR-101));
		window.button(JButtonMatcher.withText("Aggiungi fattura")).click();
		window.textBox("paneInvoiceErrorMessage").requireText(
				"La data 1/5/"+(CURRENT_YEAR-101)+" non è corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
	}
	
	@Test @GUITest
	public void testAddInvoiceBtnShouldDelegateToControllerNewInvoiceWhenMonthIsNotCorrect() {
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(CLIENT_FIXTURE_1);
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
		window.textBox("paneInvoiceErrorMessage").requireText("La data 1/13/2020 non è corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
	}
	
	@Test @GUITest
	public void testAddInvoiceBtnShouldDelegateToControllerNewInvoiceWhenDayIsNotCorrect() {
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Client> comboboxClientsModel =balanceSwingView.getComboboxClientsModel();
			comboboxClientsModel.addElement(CLIENT_FIXTURE_1);
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
		window.textBox("paneInvoiceErrorMessage").requireText("La data 31/2/2020 non è corretta");
		window.button(JButtonMatcher.withText("Aggiungi fattura")).requireDisabled();
		verifyNoMoreInteractions(balanceController);
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenShowAllInvoicesAndAnyClientIsSelected() {
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1,INVOICE_FIXTURE_2));
			}
		);
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_1.getRevenue()+
								INVOICE_FIXTURE_2.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenShowAllInvoicesAndAClientIsSelected() {
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(CLIENT_FIXTURE_1);
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1,INVOICE_FIXTURE_2));
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1,INVOICE_FIXTURE_2));
				}
			);
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ CLIENT_FIXTURE_1.getIdentifier()+" nel "
						+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_1.getRevenue()+
								INVOICE_FIXTURE_2.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceOfTheYearSelectedIsAddedAndAnyClientIsSelected() {
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1));
				balanceSwingView.invoiceAdded(INVOICE_FIXTURE_2);
			}
		);
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_1.getRevenue()+
								INVOICE_FIXTURE_2.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceNotOfTheYearSelectedIsAddedAndAnyClientIsSelected() {
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(CURRENT_YEAR);
				balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1));
				balanceSwingView.invoiceAdded(
						new Invoice(CLIENT_FIXTURE_1, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 10.20));
			}
		);
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_1.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceOfTheYearSelectedAndOfTheClientSelectedIsAdded() {
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(CLIENT_FIXTURE_1);
				listClientModel.addElement(CLIENT_FIXTURE_2);
			}
		);
		window.list("clientsList").selectItem(1);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.invoiceAdded(INVOICE_FIXTURE_2);
		}
	);
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ CLIENT_FIXTURE_2.getIdentifier()+" nel "
						+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_2.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceNotOfTheYearSelectedAndOfTheClientSelectedIsAdded() {
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(CLIENT_FIXTURE_1);
				listClientModel.addElement(CLIENT_FIXTURE_2);
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1));
			balanceSwingView.invoiceAdded(
					new Invoice(CLIENT_FIXTURE_1, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 10.20));
		}
	);
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ CLIENT_FIXTURE_1.getIdentifier()+" nel "
						+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_1.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testUpdateTotalRevenueWhenInvoiceNotOfTheYearSelectedAndNotOfTheClientSelectedIsAdded() {
		GuiActionRunner.execute(() -> {
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(CLIENT_FIXTURE_1);
				listClientModel.addElement(CLIENT_FIXTURE_2);
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			balanceSwingView.showInvoices(Arrays.asList(INVOICE_FIXTURE_1));
			balanceSwingView.invoiceAdded(
					new Invoice(CLIENT_FIXTURE_2, DateTestsUtil.getDateFromYear(YEAR_FIXTURE), 10.20));
		}
	);
		window.label("revenueLabel").requireText(
				"Il ricavo totale delle fatture del cliente "+ CLIENT_FIXTURE_1.getIdentifier()+" nel "
						+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_1.getRevenue())+"€");
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
	public void testNotResetShowInvoiceOfCurrentYearWhenShowInvoiceIsCalledWithEmptyArgumentAndAClientIsSelected() {
		GuiActionRunner.execute(() -> {
				DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
				comboboxYearModel.addElement(CURRENT_YEAR);
				comboboxYearModel.addElement(YEAR_FIXTURE);
				comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
				DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
				listClientModel.addElement(CLIENT_FIXTURE_1);				
			}
		);
		window.list("clientsList").selectItem(0);
		GuiActionRunner.execute(() -> balanceSwingView.showInvoices(Arrays.asList()) );
		window.label("revenueLabel").requireText("Non sono presenti fatture del "+YEAR_FIXTURE+
				" per il cliente "+CLIENT_FIXTURE_1.getIdentifier());
		verify(balanceController, never()).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testNotResetShowInvoiceOfCurrentYearWhenShowInvoiceIsCalledWithEmptyArgumentAndCurrentYearIsSelected() {
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
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(INVOICE_FIXTURE_1);
			listInvoiceModel.addElement(INVOICE_FIXTURE_2);
			balanceSwingView.removeInvoicesOfClient(new Client(CLIENT_FIXTURE_1.getIdentifier()));
			}
		);
		assertThat(window.table("invoicesTable").contents())
			.containsOnly(new String[] {INVOICE_FIXTURE_2.getClient().getIdentifier(),INVOICE_FIXTURE_2.getDateInString(),
					INVOICE_FIXTURE_2.getRevenueInString()});
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_2.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testRemoveInvoicesOfAClientWhenInvoicesRemanentInListIsEmptyAndAnyClientIsSelected() {
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(INVOICE_FIXTURE_1);
			listInvoiceModel.addElement(new Invoice(CLIENT_FIXTURE_1,
					DateTestsUtil.getDateFromYear(YEAR_FIXTURE),10.20));
			balanceSwingView.removeInvoicesOfClient(new Client(CLIENT_FIXTURE_1.getIdentifier()));
			}
		);
		assertThat(window.table("invoicesTable").contents()).isEmpty();
		verify(balanceController).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testRemoveInvoicesOfAClientWhenInvoicesRemanentInListIsEmptyAndAnOtherClientIsSelected() {
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel=balanceSwingView.getClientListModel();
			listClientModel.addElement(CLIENT_FIXTURE_1);
			listClientModel.addElement(CLIENT_FIXTURE_2);
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(INVOICE_FIXTURE_1);
			listInvoiceModel.addElement(new Invoice(CLIENT_FIXTURE_1,
					DateTestsUtil.getDateFromYear(YEAR_FIXTURE),10.20));
			}
		);
		window.list("clientsList").selectItem(1);
		GuiActionRunner.execute(() ->
			balanceSwingView.removeInvoicesOfClient(new Client(CLIENT_FIXTURE_1.getIdentifier()))
			);
		assertThat(window.table("invoicesTable").contents()).isEmpty();
		verify(balanceController,never()).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testDeleteInvoiceButtonShouldBeEnabledOnlyWhenAnInvoiceIsSelected() {
		GuiActionRunner.execute(() -> balanceSwingView.getInvoiceTableModel().addElement(INVOICE_FIXTURE_1)); 
		window.table("invoicesTable").selectRows(0); 
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText(Pattern.compile(".*Rimuovi.*fattura.*")));
		deleteButton.requireEnabled();
		window.table("invoicesTable").unselectRows(0); 
		deleteButton.requireDisabled(); 
	}
	
	@Test @GUITest
	public void testDeleteInvoiceButtonShouldDelegateToBalanceControllerDeleteInvoice() {
		GuiActionRunner.execute(() -> {
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(INVOICE_FIXTURE_1);
			listInvoiceModel.addElement(INVOICE_FIXTURE_2);
			}
		);
		window.table("invoicesTable").selectRows(0); 
		window.button(JButtonMatcher.withText(Pattern.compile(".*Rimuovi.*fattura.*"))).click();
		verify(balanceController).deleteInvoice(new Invoice(INVOICE_FIXTURE_1.getClient(),
				INVOICE_FIXTURE_1.getDate(), INVOICE_FIXTURE_1.getRevenue()));
	}
	
	@Test @GUITest
	public void testRemoveInvoiceWhenInvoicesRemanentInTabletIsNotEmptyAndUpdateTotalRevenue() {
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(CURRENT_YEAR);
			InvoiceTableModel listInvoiceModel=balanceSwingView.getInvoiceTableModel();
			listInvoiceModel.addElement(INVOICE_FIXTURE_1);
			listInvoiceModel.addElement(INVOICE_FIXTURE_2);
			balanceSwingView.invoiceRemoved(new Invoice(INVOICE_FIXTURE_1.getClient(),
					INVOICE_FIXTURE_1.getDate(), INVOICE_FIXTURE_1.getRevenue()));
			}
		);
		assertThat(window.table("invoicesTable").contents())
			.containsOnly(new String[] {INVOICE_FIXTURE_2.getClient().getIdentifier(),
					INVOICE_FIXTURE_2.getDateInString(),INVOICE_FIXTURE_2.getRevenueInString()});
		window.label("revenueLabel").requireText(
				"Il ricavo totale del "+CURRENT_YEAR+" è di "
						+String.format("%.2f", INVOICE_FIXTURE_2.getRevenue())+"€");
	}
	
	@Test @GUITest
	public void testRemoveInvoiceWhenInvoicesRemanentInListIsEmpty() {
		GuiActionRunner.execute(() -> {
			DefaultComboBoxModel<Integer> comboboxYearModel=balanceSwingView.getComboboxYearsModel();
			comboboxYearModel.addElement(YEAR_FIXTURE);
			comboboxYearModel.addElement(CURRENT_YEAR);
			comboboxYearModel.setSelectedItem(YEAR_FIXTURE);
			balanceSwingView.getInvoiceTableModel().addElement(INVOICE_FIXTURE_1);
			balanceSwingView.invoiceRemoved(new Invoice(INVOICE_FIXTURE_1.getClient(),
					INVOICE_FIXTURE_1.getDate(), INVOICE_FIXTURE_1.getRevenue()));
			}
		);
		assertThat(window.table("invoicesTable").contents()).isEmpty();
		verify(balanceController).yearsOfTheInvoices();
	}
	
	@Test @GUITest
	public void testShowErrorInvoiceShouldShowTheMessageInTheInvoiceErrorLabel() {
		GuiActionRunner.execute(
				() -> balanceSwingView.showInvoiceError("error message", INVOICE_FIXTURE_1) );
		window.textBox("paneInvoiceErrorMessage").requireText("error message: " + 
				INVOICE_FIXTURE_1.toString());
	}
	
}
