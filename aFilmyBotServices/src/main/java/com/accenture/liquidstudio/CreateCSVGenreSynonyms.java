package com.accenture.liquidstudio;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreateCSVGenreSynonyms {
	public static void main(String[] params) {
		List<String> ls = method();
		printList(ls);
	}
	
	public static void printList(List<String> ls)
	{
		for(String s : ls)
		{
			System.out.print("\""+s+"\",");
		}
	}

	public static List<String> method() {
		List<String> list = new ArrayList<String>();
		
		Scanner scanner = new Scanner(System.in);
		while (true) {
			
			String input = scanner.nextLine();
			if (input.equals("quit")) {
				break;
			}
			list.add(input);
			
		}
		scanner.close();
		return list;
	}
}
