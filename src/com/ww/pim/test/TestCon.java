package com.ww.pim.test;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class TestCon {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
		 String username = "wattswatertech_salsify";
		    String password = "re2GN9fs!!";
		    String host = "ftp.salsify.com";
		    JSch jsch = new JSch();
		    Session jschSession = jsch.getSession(username, host , 22);
		    java.util.Properties config = new java.util.Properties();
		    config.put("StrictHostKeyChecking", "no");
		    jschSession.setConfig(config);
		    jschSession.setPassword(password);
		    jschSession.connect();
		    ChannelSftp channelSftp =(ChannelSftp) jschSession.openChannel("sftp");
		    if (null !=channelSftp){
		    	System.out.println(channelSftp);
		    	
		    	String remoteFile = "testfile.txt";
				String localFile = "C:\\Users\\kumarsp\\Documents\\SalsifyExport\\WATTS_DATA_COMPARISON\\testfile.txt";
				channelSftp.get(remoteFile);
		        System.out.println("Download Complete");
		    }
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
