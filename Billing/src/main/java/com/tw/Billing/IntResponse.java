package com.tw.Billing;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IntResponse {
	private int res;

	public IntResponse(){
    }
    
	public int getRes(){
        return res;
    }
    
	public void setRes(int resp){
        this.res = resp;
    }
	
	@Override
	public String toString(){
		return "INFO [value="+res+"]";
	}
}
