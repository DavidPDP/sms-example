package com.company.smspub;

import com.company.smspub.core.SmsPubManagment;

public class SmsPubApplication {

	public static void main(String[] args) throws Exception {
		// For simplicity the first argument is the properties path.
		new SmsPubManagment(args[0]);
	}
	
}
