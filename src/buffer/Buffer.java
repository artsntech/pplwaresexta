package buffer;

import java.io.BufferedReader;

/**
 * Classe responsável pela definição de um buffer
 * 
 * @author Marco Costa
 * @version 1.0
 * @date 24-04-2018
 *
 */
public class Buffer {
	String str;
	BufferedReader br;
	
	/**
	 * Construtor da classe
	 * 
	 * @param str
	 * @param br
	 */
	public Buffer(String str, BufferedReader br) {
		this.str=str;
		this.br=br;
	}
	
	/**
	 * Método que devolve o link de um vídeo
	 * 
	 * @return String
	 */
	public String getStr() {
		return this.str;
	}
	
	/**
	 * Método que devolve o buffer de um vídeo
	 * 
	 * @return BufferedReader
	 */
	public BufferedReader getBr() {
		return this.br;
	}
	
	/**
	 * Método para alterar o link de um vídeo
	 * 
	 * @param str
	 */
	public void setStr(String str) {
		this.str=str;
	}
	
	/**
	 * Método para alterar o BufferedReader de um vídeo
	 * 
	 * @param br
	 */
	public void setBr(BufferedReader br) {
		this.br=br;
	}
}