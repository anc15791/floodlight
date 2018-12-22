package net.floodlightcontroller.ipblacklist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlacklistMgr {


	public static ArrayList<InetAddress> GetIpList() throws FileNotFoundException, UnknownHostException{
		ArrayList<InetAddress> list = new ArrayList<InetAddress>();
		Logger logger= LoggerFactory.getLogger(IPBlacklist.class);
		String currentDir = System.getProperty("user.dir");
		logger.info("reading file: "+ currentDir+"/src/main/java/net/floodlightcontroller/ipblacklist/blacklist.txt");
		File file = new File(currentDir+"/src/main/java/net/floodlightcontroller/ipblacklist/blacklist.txt");
		if(!file.canRead()) {
				logger.info("cant read file"+ file.getAbsolutePath());

		}
		Scanner s = new Scanner(file);

		while (s.hasNextLine()){
			String line = s.nextLine();
			if(!line.equals(""))
				list.add(InetAddress.getByName(line));
		}
		s.close();
		return list;

	}

	public static void AddtoList(String ipaddr) throws IOException{

		Logger logger= LoggerFactory.getLogger(IPBlacklist.class);
		String currentDir = System.getProperty("user.dir");
		logger.info("reading file: "+ currentDir+"/src/main/java/net/floodlightcontroller/ipblacklist/blacklist.txt");
		File file = new File(currentDir+"/src/main/java/net/floodlightcontroller/ipblacklist/blacklist.txt");
		if(!file.canRead()) {
				logger.info("cant read file"+ file.getAbsolutePath());
		}

		//Here true is to append the content to file
		FileWriter fw = new FileWriter(file,true);
		//BufferedWriter writer give better performance
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);

		pw.println("");
		pw.print(ipaddr);

		pw.close();


	}

	public static void RemoveFromList(ArrayList<InetAddress> list) throws IOException{

		Logger logger= LoggerFactory.getLogger(IPBlacklist.class);
		String currentDir = System.getProperty("user.dir");
		logger.info("reading file: "+ currentDir+"/src/main/java/net/floodlightcontroller/ipblacklist/blacklist.txt");
		File file = new File(currentDir+"/src/main/java/net/floodlightcontroller/ipblacklist/blacklist.txt");
		if(!file.canRead()) {
				logger.info("cant read file"+ file.getAbsolutePath());
		}

		//Here true is to append the content to file
		FileWriter fw = new FileWriter(file);
		//BufferedWriter writer give better performance
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);

		for(InetAddress ip: list) {
			pw.println(ip.getHostAddress());
		}

		pw.close();


	}


}
