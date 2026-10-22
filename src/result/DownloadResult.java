package result;
import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.concurrent.Callable;

import rapidofas.PplwareSexta;
import video.Video;

/**
 * Classe responsável pela definição de um download
 * 
 * @author Marco Costa
 * @version 1.0
 * @date 23-04-2018
 * 
 */
public class DownloadResult implements Callable<Video> {
	BufferedReader br;
	Hashtable<String, Video> h;
	String strvideo;

	/**
	 * Construtor da classe
	 * 
	 * @param br
	 * @param h
	 */
	public DownloadResult(BufferedReader br, Hashtable<String, Video> h) {
		this.br = br;
		this.h = h;
	}

	/**
	 * Outro construtor da classe
	 * 
	 * @param br
	 * @param h
	 * @param strvideo
	 */
	public DownloadResult(BufferedReader br, Hashtable<String, Video> h, String strvideo) {
		this.br = br;
		this.h = h;
		this.strvideo = strvideo;
	}

	/**
	 * Método que devolve o vídeo presente num determinado buffer
	 * 
	 * @return um vídeo 
	 */
	@Override
	public Video call() throws Exception {
		Video v = null;
		String linha = "";
		boolean qualidade = false, extensao = false;
		while ((linha = this.br.readLine()) != null) {
			if (!qualidade)
				qualidade = PplwareSexta.qualidade(linha);
			if (qualidade)
				extensao = PplwareSexta.extensao(linha);
			String titulo = PplwareSexta.alteraTitulo(PplwareSexta.tituloVideo(linha, qualidade, extensao));
			String resultado = PplwareSexta.linkDownload(linha, qualidade, extensao);
			if (!resultado.equalsIgnoreCase("")) {
				v = new Video(this.strvideo, titulo, resultado);
				break;
			} else {
				v = new Video(this.strvideo, "", "");
			}
		}
		return v;
	}
}