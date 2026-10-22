package result;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import buffer.Buffer;

/**
 * Classe responsável pela definição de um buffer
 * 
 * @author Marco Costa
 * @version 1.0
 * @date 23-04-2018
 */
public class BufferResult implements Callable<Buffer> {
	String strurl;

	/**
	 * Construtor da classe
	 * 
	 * @param strurl
	 */
	public BufferResult(String strurl) {
		this.strurl = strurl;
	}

	/**
	 * Método que devolve um objecto da classe buffer, com o buffer associado a um 
	 * link de um determinado vídeo
	 * 
	 * @return um objecto da classe Buffer 
	 */
	@Override
	public Buffer call() throws Exception {
		Buffer b = null;
		String urlvideo = "";
		if(this.strurl.contains("youtube")) {
			urlvideo = this.strurl.substring(this.strurl.indexOf("www."), this.strurl.length());
		} else if(this.strurl.contains("pplware")) {
			urlvideo = this.strurl.substring(this.strurl.length()-1, this.strurl.length());
		}
		
		String encoding = "UTF-8";
		URL url = new URL(this.strurl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestProperty("Accept-Encoding", "gzip");

		String contentType = con.getContentType();
		int encodingStart = contentType.indexOf("charset=");
		if (encodingStart != -1) {
			encoding = contentType.substring(encodingStart + 8);
		}

		GZIPInputStream gis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		if ("gzip".equals(con.getContentEncoding())) {
			gis = new GZIPInputStream(con.getInputStream());
			isr = new InputStreamReader(gis, encoding);
			br = new BufferedReader(isr);
		} else {
			isr = new InputStreamReader(con.getInputStream(), encoding);
			br = new BufferedReader(isr);
		}
		
		if(!urlvideo.equalsIgnoreCase("")) {
			b = new Buffer(urlvideo, br);
		}
		
		return b;
	}
}