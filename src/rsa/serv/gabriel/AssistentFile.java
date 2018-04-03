package rsa.serv.gabriel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AssistentFile {

	private String folderFile;
	private String nameFile;
	private List<String> linesFiles = new ArrayList<>();
	private File file;
	/**
	 * 
	 * @param folderFile String Exemplo: D:/Folder1/Folder2 ou C:\\Folder1
	 * @param nameFile String Exemplo: nomeArquivo.txt ou nomeArquivo.key
	 */
	public AssistentFile(String folderFile, String nameFile) {
		this.folderFile = folderFile;
		this.nameFile = nameFile;
		this.file = new File(this.folderFile + "/" + this.nameFile);
	}


	/**
	 * 
	 * @param txt - Texto a ser salvo no arquivo
	 * @return  <b>true</b> caso consiga escrever no arquivo com sucesso, <b>false</b> quando não conseguir
	 */
	public boolean write(String txt) {
		FileWriter arq = null;
		try {
			if (!this.file.exists()) {
				this.file.createNewFile();
			}
			arq = new FileWriter(this.folderFile + "/" + this.nameFile, true);
			PrintWriter gravarArq = new PrintWriter(arq);
			gravarArq.println(txt);
			arq.close();
		} catch (IOException e) {
			System.out.println(e.getMessage() + ": " + e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * @return <b>String</b> - última linha do arquivo especificado no construtor;<br>
	 * 			<b>Null</b> - se não encontrar o arquivo;
	 */
	public String getLastLineAsString() {
		popFile();
		if (this.linesFiles.size() > 0) {
			return this.linesFiles.get(this.linesFiles.size()-1);
		}
		return null ;
	}

	private void popFile() {
		try {
			if (!this.file.exists()) {
				return;
			}
			BufferedReader buffer = new BufferedReader(new FileReader(this.folderFile + "/" + this.nameFile));
			while (buffer.ready()) {
				String line = buffer.readLine();
				this.linesFiles.add(line);
			}
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {// C:/keys/public.key
		AssistentFile f = new AssistentFile("C:/Arquivos", "testinho.txt");
		f.write("texto1");
		f.write("texto2");
		f.write("texto3");
		f.write("texto2");
		
		System.out.println(f.getLastLineAsString());
	}


	public String getFolderFile() {
		return folderFile;
	}

	public void setFolderFile(String folderFile) {
		this.folderFile = folderFile;
	}

	public String getNameFile() {
		return nameFile;
	}

	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}

	public List<String> getLinesFiles() {
		return linesFiles;
	}

	public void setLinesFiles(List<String> linesFiles) {
		this.linesFiles = linesFiles;
	}
}
