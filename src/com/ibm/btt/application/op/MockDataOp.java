package com.ibm.btt.application.op;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.ibm.btt.base.BTTServerOperation;
import com.ibm.btt.base.Context;
import com.ibm.btt.base.IndexedCollection;
import com.ibm.btt.base.KeyedCollection;
import com.ibm.btt.base.types.impl.Currency;
/** 
 * Class Generated by BTT Tool
 * Created since: 2017/11/27 16:48:15
 */
public class MockDataOp extends BTTServerOperation {
  /** 
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 */

	private static String[][] NAME = {{"John","Robert","David","Richard","Charles"},
									  {"Emily","Alice","Carol","Barbara","Linda"}};
	private static boolean MALE = true;
  public void execute() throws Exception {
	  KeyedCollection mockData = (KeyedCollection)this.getContext().tryGetElementAt("MockData");

	  Object mdh = this.getContext().tryGetValueAt("MockDataHash");
	  if (mdh == null)
	  {
		  mdh = new HashMap<String, KeyedCollection>();
		  this.getContext().setValueAt("MockDataHash", mdh);
	  }
	  HashMap<String, KeyedCollection> mockDataHash = (HashMap)mdh;
	  String account_id = (String)getContext().tryGetValueAt("account_id");
	  KeyedCollection md = mockDataHash.get(account_id);
	  if (md != null)
	  {
		  Context seCtx = this.getContextByType("session");
		  seCtx.removeAt("MockData");
		  seCtx.addElement(md);
		  fireExitEvent("next");
		  return;
	  }
	  
	  mockDataHash.put(account_id, mockData);
	  
	  mockData.setValueAt("name", NAME[(MALE=!MALE)? 0 : 1][(int)(new Date().getTime()%5)]);
	  mockData.setValueAt("gender", MALE ? "M" : "F");
	  IndexedCollection cardlistCollection =  (IndexedCollection)mockData.getElementAt("cardList");
	  IndexedCollection payeeCollection =  (IndexedCollection) mockData.getElementAt("payeeList");

	  cardlistCollection.removeAll();
	  double total = 0;
	  for(int i = 0; i < 5; i++){	
		  KeyedCollection card = (KeyedCollection) cardlistCollection.createElement(true);

		  BigDecimal cardid = new BigDecimal(1001000 + i) ;
		  card.setValueAt("card_id",  String.valueOf(cardid));
		  card.setValueAt("card_type", i%3 < 1 ? "credit":"debit");
		  Currency balance = new Currency("USD", new BigDecimal((float)Math.round(Math.random()*10000000)/100));
		  card.setValueAt("balance", balance);
		  //add history list
		  IndexedCollection historyList = (IndexedCollection) card.tryGetElementAt("historyList");
		  historyList.removeAll();
		  int eventNum = (int) Math.round(Math.random()*5) + 1;
		  for(int j = 0; j < eventNum; j++)
		  {	  
			  KeyedCollection event = (KeyedCollection) historyList.createElement(true);
			  Calendar date = Calendar.getInstance();
			  date.add(Calendar.DAY_OF_MONTH, -j);
			  event.setValueAt("date", date.getTime());
			  long type = Math.round(Math.random()*10) % 3; // 0: cost, 1: transfer out, 2: transfer in
			  event.setValueAt("name", type > 0 ? "Transfer" : "Cost");
			  Currency bala = new Currency("USD",new BigDecimal((float)Math.round(Math.random()*10000000 + 10)/100*(type<2?-1:1)));
			  event.setValueAt("amount", bala);
			  historyList.addElement(event);
		  }
		  
		  total += balance.getValue().doubleValue();
		  cardlistCollection.addElement(card);
	  }
	  payeeCollection.removeAll();

	  KeyedCollection payee = (KeyedCollection) payeeCollection.createElement(true);

	  BigDecimal cardid = new BigDecimal(1001018) ;
	  payee.setValueAt("card_id",  String.valueOf(cardid));
	  payee.setValueAt("name", "Bob");
	  payee.setValueAt("phone", "13522222222");
	  payeeCollection.addElement(payee);
	  
	  payee = (KeyedCollection) payeeCollection.createElement(true);
	  cardid =  new BigDecimal(1001019);
	  payee.setValueAt("card_id",  String.valueOf(cardid));
	  payee.setValueAt("name", "Roy");
	  payee.setValueAt("phone", "13533333333");
	  payeeCollection.addElement(payee);
	  
	  payee = (KeyedCollection) payeeCollection.createElement(true);
	  cardid =  new BigDecimal(1001020);
	  payee.setValueAt("card_id",  String.valueOf(cardid));
	  payee.setValueAt("name", "Eric");
	  payee.setValueAt("phone", "13544444444");
	  payeeCollection.addElement(payee);
	  
	  mockData.setValueAt("total_balance", new Currency("USD", new BigDecimal(total)));
	  mockData.setValueAt("card_amount",5);

	  fireExitEvent("next");
  }
}
