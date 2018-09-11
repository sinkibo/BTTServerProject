package com.ibm.btt.application.op;
import com.ibm.btt.base.BTTServerOperation;
/** 
 * Class Generated by BTT Tool
 * Created since: 2017/11/27 15:22:07
 */
public class LoginOp extends BTTServerOperation {
  /** 
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 */
  public void execute() throws Exception {
	  String id = (String)getContext().tryGetValueAt("account_id");
	  String pw = (String)getContext().tryGetValueAt("password");
	  
	  if("123456".equals(pw) && id != null && id.length()>1 && !id.startsWith("X")){
		  fireExitEvent("success");
	  }else{
		  getContext().setValueAt("message", "Please check your ID and passwords!");
		  fireExitEvent("error");
	  }
	  
	  return;
  }
}
