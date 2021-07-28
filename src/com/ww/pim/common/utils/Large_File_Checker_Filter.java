package com.ww.pim.common.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Large_File_Checker_Filter implements FilenameFilter{
	@Override
	public boolean accept(File file, String name) {
		String pattern = "yyyyMdd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		System.out.println(date);
        if (name.contains("2020515")) {
		// if (name.contains("date")) {
            return true;
        } else {
            return false;
        }
    }
}
