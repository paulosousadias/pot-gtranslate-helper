import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PotLoaderConverter {

    public static String replaceFileExtension(String path, String newExtension) {
        int lastDotPostion = path.lastIndexOf('.');
        String st = (lastDotPostion != -1) ? (path.substring(0, lastDotPostion)) : path;
        return st + "." + newExtension;
    }

    enum StateEnum {
    	NONE, MSG_ID, MSG_STRING;
    };

	public static void phase1(String[] args) {
		StateEnum state = StateEnum.NONE;
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		String inFileStr = args[0];
		
		File inFile = new File(inFileStr);
		
		String outFileStr = replaceFileExtension(inFile.getName(), "gen.po.txt");
		File outFile = new File(inFile.getParentFile() , outFileStr);
		
		try {
			reader = new BufferedReader(new FileReader(inFile));
			writer = new BufferedWriter(new FileWriter(outFile));
			
			Pattern msgidPat = Pattern.compile("msgid \"(.+?)\"");
			Pattern msgidPatTwo = Pattern.compile("%\\w+");
			String line;
			boolean headerEmptyMsgId = true;
			while ((line = reader.readLine()) != null) {
				StateEnum pevState = state;
				String lineToW = "";
				switch (state) {
					case NONE:
						if (line.startsWith("msgid \"")) {
							if (line.equals("msgid \"\"") && headerEmptyMsgId) {
								headerEmptyMsgId = false;
								break;
							}
							
							state = StateEnum.MSG_ID;
							if (!line.equals("msgid \"\"")) {
								Matcher matcher = msgidPat.matcher(line);
								matcher.matches();
								lineToW = matcher.group(1);
								lineToW = lineToW.replaceAll("%\\w+", "%!");
							}
							else {
								lineToW = "";
							}
							writer.write("##-##\n");
							writer.write(lineToW + "\n");
						}
						break;
					case MSG_ID:
						if (line.startsWith("\"")) {
							lineToW = line.substring(1, line.length() - 2);
							writer.write(lineToW + "\n");
						}
						else if (line.startsWith("msgstr \"")) {
							state = StateEnum.MSG_STRING;
						}
						else {
							state = StateEnum.NONE;
						}
						break;
					case MSG_STRING:
						if (line.startsWith("\"")) {
							lineToW = line.substring(1, line.length() - 2);
							writer.write(lineToW + "\n");
						}
						else {
							state = StateEnum.NONE;
						}
						break;
	
					default:
						break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void phase2(String[] args) {
		StateEnum state = StateEnum.NONE;
		
		BufferedReader reader = null;
		BufferedReader reader2 = null;
		BufferedWriter writer = null;
		
		String inFileStr = args[0];
		String inFile2Str = args[1];
		
		File inFile = new File(inFileStr);
		File inFile2 = new File(inFile2Str);
		
		String outFileStr = replaceFileExtension(inFile.getName(), "gen.mod.po");
		File outFile = new File(inFile.getParentFile() , outFileStr);
		
		try {
			reader = new BufferedReader(new FileReader(inFile));
			reader2 = new BufferedReader(new FileReader(inFile2));
			writer = new BufferedWriter(new FileWriter(outFile));
			
			Pattern msgidPat = Pattern.compile("msgid \"(.+?)\"");
			Pattern msgidPatTwo = Pattern.compile("%\\w+");
			Pattern msgidPatThree = Pattern.compile("%!");
			
			String line;
			String line2;
			boolean headerEmptyMsgId = true;
			ArrayList<String> replacementsList = new ArrayList<>();
			ArrayList<String> toReplaceList = new ArrayList<>();
			int index = 0; 
			int replacementsCount = 0; 
			int toReplaceCount = 0; 
			reader2.readLine();

			while ((line = reader.readLine()) != null) {
				StateEnum pevState = state;
				String lineToW = "";
				switch (state) {
					case NONE:
						if (line.startsWith("msgid \"")) {
							if (line.equals("msgid \"\"") && headerEmptyMsgId) {
								headerEmptyMsgId = false;
								writer.write(line + "\n");
								break;
							}
							
							state = StateEnum.MSG_ID;
							if (!line.equals("msgid \"\"")) {
								Matcher matcher = msgidPatTwo.matcher(line);
								while (matcher.find()) {
									String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
									System.out.println(m1);
									replacementsList.add(m1);
									replacementsCount++;
								}
							}
							else {
								lineToW = "";
							}
						}
						writer.write(line + "\n");
						break;
					case MSG_ID:
						if (line.startsWith("\"")) {
							Matcher matcher = msgidPatTwo.matcher(line);
							while (matcher.find()) {
								String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
								System.out.println(m1);
								replacementsList.add(m1);
								replacementsCount++;
							}
						}
						else if (line.startsWith("msgstr \"")) {
							state = StateEnum.MSG_STRING;
							// let us read from the translation
							boolean first = true;
							line = "";
							while ((line2 = reader2.readLine()) != null) {
								if (line2.replaceAll(" ", "").startsWith("##-##")) {
									break;
								}
								Matcher matcher = msgidPatThree.matcher(line2);
//								System.out.println(line2);
								while (matcher.find()) {
									String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
									toReplaceList.add(m1);
									toReplaceCount++;
									System.out.println(m1 + "  " + "  #" + toReplaceList.size() + ":" + replacementsList.size() + ":" + index);
									index++;
								}
								line += (first ? "msgstr ": "\n") + "\"" + line2 + "\"";
								first = false;
							}
						}
						else {
							state = StateEnum.NONE;
						}
						writer.write(line + "\n");
						break;
					case MSG_STRING:
						if (line.startsWith("\"")) {
							// let us read from the translation
							boolean first = true;
							line = "";
							while ((line2 = reader2.readLine()) != null) {
								if (line2.replaceAll(" ", "").startsWith("##-##")) {
									break;
								}
								Matcher matcher = msgidPatThree.matcher(line2);
//								System.out.println(line2);
								while (matcher.find()) {
									String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
									toReplaceList.add(m1);
									toReplaceCount++;
									System.out.println(m1 + "  " + "  #" + toReplaceList.size() + ":" + replacementsList.size() + ":" + index);
									index++;
								}
								line += (first ? "": "\n") + "\"" + line2 + "\"";
								first = false;
							}
						}
						else {
							state = StateEnum.NONE;
							replacementsList.clear();
							toReplaceList.clear();
							index = 0;
						}
						writer.write(line + "\n");
						break;
	
					default:
						break;
				}
			}
			System.out.println(replacementsCount + " : " + toReplaceCount);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader2 != null) {
				try {
					reader2.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	public static void main(String[] args) {
		
		String ag1 = args.length == 0 ? "" : args[0];

		System.out.println(args.length);
		
		String[] argsFl = args.length == 0 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
		
		switch (ag1) {
			case "to-google":
				phase1(argsFl);
				break;
			case "from-google":
				phase2(argsFl);
				break;
	
			default:
				
				Pattern msgidPatTwo = Pattern.compile("%\\w+");
				String line = "df %msg1 sd%msg2 s %msg3";
				Matcher matcher = msgidPatTwo.matcher(line);
				while (matcher.find()) {
					String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
					System.out.println(m1);
				}
				break;
		}
	}
}
