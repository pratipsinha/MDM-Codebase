package com.ww.pim.common.utils;

import java.io.File;
import java.io.FilenameFilter;

public class WattsPriceFileNameFilter implements FilenameFilter{

	@Override
	public boolean accept(File file, String name) {
        if (name.startsWith("mdmpimpr")) {
            return true;
        } else {
            return false;
        }
    }


}
