package fr.feavy.wordgenerator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

	private static Random rand = new Random();
	private static String[] words;
	private static float[][] letterFreq = new float[26][26];
	private static float[] letterSum = new float[26];

	private static List<String> alreadyGeneratedWords = new ArrayList<>();

	public static void main(String[] args) throws Exception {

		Scanner sc = new Scanner(System.in);
		
		System.out.println("Langue des noms à générer (fr/en) : ");
		boolean fr = sc.nextLine().equalsIgnoreCase("fr");
		if (fr)
			words = new String[22740];
		else
			words = new String[370099];

		try {
			System.out.println("Chargement des mots...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/words" + (!fr ? "_en" : "") + ".txt")));
			String line = null;
			int c = 0;
			while ((line = reader.readLine()) != null) {
				words[c] = Normalizer.normalize(line.toLowerCase(), Form.NFD);
				words[c] = words[c++].replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			}
			reader.close();
			System.out.println("Mots chargés !");
			System.out.println("Analse des mots...");

			for (String word : words) {
				for (int i = 0; i < word.length() - 1; i++) {
					int code = (int) word.charAt(i) - 97;
					int code2 = (int) word.charAt(i + 1) - 97;
					if (code < 0 || code2 < 0 || code > 100 || code2 > 100)
						continue;
					letterFreq[code][code2] += 1;
					letterSum[code] += 1;
				}
			}

			for (int i = 0; i < 26; i++) {
				float s = 0;
				for (int j = 0; j < 26; j++) {
					s += letterFreq[i][j] / letterSum[i];
					letterFreq[i][j] = s;
				}
			}
			System.out.println("Mots analysés...");

			System.out.print("Entrez la base des mots à générer : ");
			String base = sc.nextLine();
			int start = base.length();

			System.out.print("Entrez la taille des mots à générer : ");
			int wordLength = sc.nextInt();

			System.out.print("Entrez le nombre de mots à générer : ");
			int wordAmount = sc.nextInt();

			long t = System.currentTimeMillis();
			System.out.println("Génération des mots en cours...");

			int availableAmount = 0;

			while (availableAmount < wordAmount) {
				String currentWord = base;
				for (int i = start; i < wordLength; i++)
					currentWord += getNextLetter(currentWord.charAt(i - 1));

				if (alreadyGeneratedWords.contains(currentWord))
					continue;

				if (isAvailable(currentWord)) {
					System.out.println((availableAmount + 1) + " : " + currentWord);
					alreadyGeneratedWords.add(currentWord);
					availableAmount++;
				}
			}

			System.out.println(
					"Génération terminée. (Temps passé : " + ((System.currentTimeMillis() - t) / 1000) + "s).");
			sc.nextLine();
		} catch (Exception e) {
			System.err.println("Le fichier n'existe pas.");
		}
		sc.nextLine();
	}

	private static char getNextLetter(char letter) {
		int index = letter - 97;
		float lclRand = rand.nextFloat();
		for (int i = 0; i < 26; i++) {
			if (lclRand < letterFreq[index][i]) {
				return (char) (i + 97);
			}
		}
		return (char) 0;
	}

	private static boolean isAvailable(String hostname) throws Exception {
		InetAddress server = InetAddress.getByName("whois.verisign-grs.com");
		Socket theSocket = new Socket(server, 43);
		Writer out = new OutputStreamWriter(theSocket.getOutputStream(), "8859_1");
		out.write("=" + hostname + ".com");
		out.write("\r\n");
		out.flush();
		BufferedReader reader = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));
		String rep = reader.readLine();
		return rep.contains("No match") ? true : false;
	}

}
