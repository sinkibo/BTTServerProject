package com.ibm.btt.application.op;
import java.util.Enumeration;

import com.ibm.btt.base.BTTServerOperation;
import com.ibm.btt.base.IndexedCollection;
import com.ibm.btt.base.KeyedCollection;
/** 
 * Class Generated by BTT Tool
 * Created since: 2017/12/01 15:34:33
 */
public class AccountDetailOp extends BTTServerOperation {
  /** 
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 */
  public void execute() throws Exception {
//	  account_id,cardList,card_amount,total_balance,selectCardId

	  String selectCardID = (String) this.getValueAt("selectCardId");
	  if(selectCardID == null) return;
	  
	  Enumeration<KeyedCollection> cards = ((IndexedCollection)this.getElementAt("MockData.cardList")).getElements().elements();
	  IndexedCollection historyList = null;
	  while(cards.hasMoreElements())
	  {
		  KeyedCollection card = cards.nextElement();
		  if(selectCardID.equals(card.getValueAt("card_id")))
		  {
			  historyList = (IndexedCollection) card.tryGetElementAt("historyList");
			  break;
		  }
	  }
	  
	  if(null == historyList) return;
	  
	  historyList = (IndexedCollection)historyList.clone();

	  this.addElement(historyList);
  }
}
