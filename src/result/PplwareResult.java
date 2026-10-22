package result;
import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.concurrent.Callable;

import rapidofas.PplwareSexta;
import video.Video;

/**
 * Classe responsável pela definição de um vídeo no Pplware
 * 
 * @author Marco Costa
 * @version 1.0
 * @date 23-04-2018
 *
 */
public class PplwareResult implements Callable<Hashtable<String, Video>> {
	BufferedReader br;

	/**
	 * Construtor da classe
	 * 
	 * @param br
	 */
	public PplwareResult(BufferedReader br) {
		this.br = br;
	}

	/**
	 * Método que devolve uma Hashtable com o vídeo associado a um determinado buffer
	 * 
	 * @return uma Hashtable com o vídeo presente num determinado buffer
	 */
	@Override
	public Hashtable<String, Video> call() throws Exception {
		Hashtable<String, Video> h = new Hashtable<String, Video>();
		String line = "";
		while ((line = this.br.readLine()) != null) {
			String resultado = PplwareSexta.videosYoutube(line);
			if (!resultado.equalsIgnoreCase("")) {
				Video v = new Video(resultado, "", "");
				if (h.get(resultado) == null) {
					h.put(resultado, v);
				}
			}
		}
		return h;
	}
}