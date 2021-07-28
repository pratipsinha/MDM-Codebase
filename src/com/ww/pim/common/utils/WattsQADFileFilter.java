package com.ww.pim.common.utils;

import java.io.File;
import java.io.FileFilter;

public class WattsQADFileFilter implements FileFilter{

	@Override
	public boolean accept(File file) {
		 if (file.isFile() && file.length() > 0) {
	            return true;
	        } else {
	            return false;
	        }
	}

}
